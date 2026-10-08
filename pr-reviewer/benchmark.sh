#!/bin/bash

#
# Measure how well the source formatter and review.sh catch planted violations.
#
# A fixture plants known violations and records each one in a manifest, a JSON
# array of {file, line, rule, note} or {file, line, check, note} objects. An
# entry with a rule is owned by review.sh and names the pr-reviewer rule that
# should catch it; an entry with a check is owned by the source formatter and
# names the check that should report it, or several separated by "|" when any
# of them may fix it first. Moving a convention from a rule to a
# check moves its entries from rule to check, so the fixture keeps measuring it.
#
# The source formatter runs once, since it is deterministic, from the source of
# this branch: the script deploys modules/util/source-formatter first, so a
# check that is not released yet is measured too. Every finding is matched to
# the check entries by file, check, and line. A finding that lands on a planted
# rule entry instead is listed as a candidate to move to the source formatter.
#
# review.sh then reviews the range, possibly several times, since the model is
# not deterministic. Every reported violation is matched to the rule entries by
# file, rule, and line, and the script prints the recall per planted violation,
# the false positives, and the time per run.
#
# Usage:
# benchmark.sh [--group <label>] [--manifest <file>]... [--runs <count>] [--skip-review] [--skip-source-formatter] [--staged] [--tolerance <lines>] [<range>]
#
# With --group only that rule group of review.sh runs, the same call pr-check
# makes for it, only the planted violations of its rules are scored, and the
# source formatter is skipped, so tuning one group does not pay for the rest.
# With --skip-review only the source formatter runs, which costs no tokens.
#
# The range defaults to the commit that added the first manifest, which is the
# commit that adds the fixture, and --staged reviews the index
# instead, for a fixture that is not committed yet. The
# manifests default to every expected*.json under modules/apps/pr-reviewer-fixture.
#

set -o errexit
set -o nounset
set -o pipefail

function main {
	local diff_range=""
	local group_args=()
	local manifests=()
	local runs=1
	local skip_review=false
	local skip_source_formatter=false
	local source_formatter_seconds=0
	local tolerance=5

	while [ ${#} -gt 0 ]
	do
		if [ ${1} == --group ]
		then
			group_args=(--group ${2})
			skip_source_formatter=true

			shift
		elif [ ${1} == --manifest ]
		then
			manifests+=(${2})

			shift
		elif [ ${1} == --runs ]
		then
			runs=${2}

			shift
		elif [ ${1} == --skip-review ]
		then
			skip_review=true
		elif [ ${1} == --skip-source-formatter ]
		then
			skip_source_formatter=true
		elif [ ${1} == --staged ]
		then
			diff_range=--staged
		elif [ ${1} == --tolerance ]
		then
			tolerance=${2}

			shift
		elif [[ ${1} == --* ]]
		then
			echo "Unknown option ${1}." >&2

			exit 2
		else
			diff_range=${1}
		fi

		shift
	done

	if ${skip_review} && [ ${#group_args[@]} -gt 0 ]
	then
		echo "Pass either --group or --skip-review, not both." >&2

		exit 2
	fi

	local repo_root

	repo_root=$(git rev-parse --show-toplevel)

	cd ${repo_root}

	if [ ${#manifests[@]} -eq 0 ]
	then
		manifests=($(ls modules/apps/pr-reviewer-fixture/expected*.json 2> /dev/null || true))
	fi

	if [ ${#manifests[@]} -eq 0 ]
	then
		echo "No manifest found. Pass --manifest <file>." >&2

		exit 2
	fi

	if [[ -z ${diff_range} ]]
	then
		local fixture_commit

		fixture_commit=$(git log --diff-filter=A --format=%H --max-count=1 -- ${manifests[0]})

		diff_range="${fixture_commit}~1..${fixture_commit}"
	fi

	local work_dir

	work_dir=$(mktemp --directory)

	trap "rm --force --recursive ${work_dir}" EXIT

	jq --slurp '[.[][] | select(.check)] | sort_by([.check, .file, .line])' "${manifests[@]}" > ${work_dir}/expected-check.json
	jq --slurp '[.[][] | select(.rule)] | sort_by([.rule, .file, .line])' "${manifests[@]}" > ${work_dir}/expected-rule.json

	#
	# The manifests are the answer key, so they stay out of the reviewed diff, and
	# so does this script, which the commit that adds the fixture may also add.
	#

	local exclude_args=(--exclude pr-reviewer/benchmark.sh)
	local manifest

	for manifest in "${manifests[@]}"
	do
		exclude_args+=(--exclude ${manifest})
	done

	if ! ${skip_source_formatter}
	then
		_run_source_formatter
		_print_source_formatter_summary

		echo ""
	fi

	if ! ${skip_review}
	then
		_run_review
		_print_review_summary
	fi
}

function _get_reviewed_file_names {
	local diff_args=(${diff_range})

	if [ ${diff_range} == --staged ]
	then
		diff_args=(--cached)
	fi

	local exclude_pathspecs=(":(exclude)pr-reviewer/benchmark.sh")
	local manifest

	for manifest in "${manifests[@]}"
	do
		exclude_pathspecs+=(":(exclude)${manifest}")
	done

	git diff --diff-filter=d --name-only "${diff_args[@]}" -- . "${exclude_pathspecs[@]}"
}

function _print_review_summary {
	local summary_json

	summary_json=$(jq --slurp --argjson tolerance ${tolerance} --slurpfile expected ${work_dir}/expected-rule.json '
		def matches($e; $r):
			($e.file == $r.file) and ($e.rule == $r.rule) and (($e.line - $r.line) | fabs) <= $tolerance;

		def rule_matches($e; $r):
			($e.file == $r.file) and ($e.rule == $r.rule);

		. as $runs
		| ($expected[0]) as $expected
		| ($runs | length) as $runs_count
		| {
			runs: [
				$runs[]
				| . as $run
				| {
					run: .run,
					seconds: .seconds,
					found: [$expected[] | . as $e | select(any($run.violations[]; matches($e; .)))] | length,
					false_positives: [$run.violations[] | . as $r | select(any($expected[]; rule_matches(.; $r)) | not)] | length
				}
			],
			expected: [
				$expected[]
				| . as $e
				| {
					rule: .rule,
					file: (.file | sub("^modules/apps/pr-reviewer-fixture/pr-reviewer-fixture-impl/"; "")),
					line: .line,
					found_runs: [$runs[] | select(any(.violations[]; matches($e; .)))] | length,
					rule_runs: [$runs[] | select(any(.violations[]; rule_matches($e; .)))] | length
				}
			],
			false_positives: (
				[$runs[] | .violations[] | . as $r | select(any($expected[]; rule_matches(.; $r)) | not)]
				| group_by(.rule)
				| map({rule: .[0].rule, count: length, sample: ((.[0].file | sub("^modules/apps/pr-reviewer-fixture/pr-reviewer-fixture-impl/"; "")) + ":" + (.[0].line | tostring) + " " + .[0].message)})
				| sort_by(-.count)
			),
			runs_count: $runs_count
		}
	' ${work_dir}/scored-*.json)

	echo "${summary_json}" | jq --raw-output '
		.runs_count as $runs_count
		| (.expected | length) as $expected_count
		| (.runs | map(.found) | add / length) as $mean_found
		| (.runs | map(.false_positives) | add / length) as $mean_false_positives
		| (.runs | map(.seconds) | add / length) as $mean_seconds
		| (.expected | map(.rule_runs) | add / $runs_count) as $mean_rule_found
		| (.expected | map(select(.found_runs == $runs_count)) | length) as $always
		| (.expected | map(select(.found_runs == 0)) | length) as $never
		| "Recall: \($mean_found * 100 / $expected_count | floor)% (\($mean_found) of \($expected_count) per run, \($always) always found, \($never) never found), \($mean_rule_found * 100 / $expected_count | floor)% ignoring the line, \($mean_false_positives * 10 | round / 10) false positive(s) per run, \($mean_seconds | floor)s per run",
		"",
		"Rule  Found  Line  File",
		(.expected[] | "\(.rule)   \(.found_runs)/\($runs_count)\(if .rule_runs > .found_runs then "*" else " " end)   \(.line | tostring | .[0:5] + " " * (5 - length))\(.file)"),
		"",
		"An asterisk marks a rule the reviewer reported on the right file and rule but outside the line tolerance in at least one run.",
		"",
		(if (.false_positives | length) > 0 then
			"False positives by rule (over all runs):",
			(.false_positives[] | "\(.rule)   \(.count)   \(.sample)")
		else
			"No false positives."
		end)
	'
}

function _print_source_formatter_summary {
	jq --raw-output --argjson seconds ${source_formatter_seconds} --argjson tolerance ${tolerance} --slurpfile expected_check ${work_dir}/expected-check.json --slurpfile expected_rule ${work_dir}/expected-rule.json '
		def near($e; $f):
			($e.file == $f.file) and ($f.line != null) and (($e.line - $f.line) | fabs) <= $tolerance;

		def reports($e; $f):
			($e.check | split("|") | index([$f.check])) and ($e.file == $f.file) and (($f.line == null) or near($e; $f));

		def near_rule($e; $f):
			($e.file == $f.file) and ($f.line != null) and (($e.line - $f.line) | fabs) <= 1;

		def short_file:
			sub("^modules/apps/pr-reviewer-fixture/pr-reviewer-fixture-impl/"; "");

		. as $findings
		| $expected_check[0] as $expected_check
		| $expected_rule[0] as $expected_rule
		| [
			$expected_check[]
			| . as $e
			| . + {found: any($findings[]; reports($e; .))}
		] as $scored
		| [
			$findings[]
			| . as $f
			| select(any($expected_check[]; reports(.; $f)) | not)
		] as $unplanned
		| [
			$unplanned[]
			| . as $f
			| [$expected_rule[] | select(near_rule(.; $f))]
			| select(length > 0)
			| min_by((.line - $f.line) | fabs) as $e
			| $f + {rule: $e.rule, rule_line: $e.line}
		] as $candidates
		| [
			$unplanned[]
			| . as $f
			| select(any($candidates[]; . == ($f + {rule: .rule, rule_line: .rule_line})) | not)
		] as $other
		| ($scored | map(select(.found)) | length) as $found
		| "Source formatter recall: \(if ($scored | length) > 0 then $found * 100 / ($scored | length) | floor else 100 end)% (\($found) of \($scored | length)), \($other | length) other finding(s), \($seconds)s",
		"",
		"Found  Line  Check  File",
		($scored[] | "\(if .found then "yes " else "no " end)  \(.line | tostring | .[0:5] + " " * (5 - length))\(.check)  \(.file | short_file)"),
		"",
		(if ($candidates | length) > 0 then
			"Planted rule violations the source formatter also reports, candidates to move to a check:",
			($candidates[] | "\(.rule)  \(.file | short_file):\(.rule_line)  \(.check): \(.message)")
		else
			"No planted rule violation is also reported by the source formatter."
		end),
		"",
		(if ($other | length) > 0 then
			"Other source formatter findings:",
			($other[] | "\(.check)  \(.file | short_file)\(if .line then ":\(.line)" else "" end)  \(.message)")
		else
			"No other source formatter findings."
		end)
	' ${work_dir}/source-formatter.json
}

function _run_review {
	if [ ${#group_args[@]} -eq 0 ]
	then
		echo "Benchmarking ${runs} run(s) of review.sh over ${diff_range} against $(jq "length" ${work_dir}/expected-rule.json) planted rule violation(s) from ${manifests[*]}."
	else
		echo "Benchmarking ${runs} run(s) of the ${group_args[1]} group of review.sh over ${diff_range} against the violations planted for its rules in ${manifests[*]}."
	fi
	echo ""

	local run

	for ((run = 1; run <= runs; run++))
	do
		local seconds=$(date +%s)

		local status=0

		pr-reviewer/review.sh --json "${exclude_args[@]}" "${group_args[@]}" ${diff_range} > ${work_dir}/run-${run}.json 2> ${work_dir}/run-${run}.err || status=${?}

		if [ ${status} -eq 2 ] || ! jq --exit-status ".violations" ${work_dir}/run-${run}.json > /dev/null 2>&1
		then
			echo "Run ${run} produced no verdict: $(head --bytes=300 ${work_dir}/run-${run}.err)" >&2

			exit 1
		fi

		jq --argjson run ${run} --argjson seconds $(($(date +%s) - seconds)) '{cost: .cost, run: $run, seconds: $seconds, tokens: .tokens, violations: [.violations[] | .file |= sub("^[ab]/"; "")]}' ${work_dir}/run-${run}.json > ${work_dir}/scored-${run}.json

		echo "Run ${run}: $(jq --raw-output '"\(.violations | length) violation(s) in \(.seconds)s, \(.tokens.input + .tokens.cache_write + .tokens.cache_read) input tokens (\(.tokens.cache_read) cached, \(.tokens.cache_write) cache writes), \(.tokens.output) output tokens, $\(.cost * 100 | round / 100)"' ${work_dir}/scored-${run}.json)"
	done

	echo ""

	#
	# A run reports the rules it reviewed, so a run limited to one group is scored
	# only against the violations planted for that group.
	#

	jq --slurpfile run ${work_dir}/run-1.json '[.[] | select(.rule as $rule | $run[0].rules | index($rule))]' ${work_dir}/expected-rule.json > ${work_dir}/expected-reviewed.json

	mv ${work_dir}/expected-reviewed.json ${work_dir}/expected-rule.json
}

function _run_source_formatter {
	echo "Running the source formatter of this branch over ${diff_range} against $(jq "length" ${work_dir}/expected-check.json) planted check violation(s) from ${manifests[*]}."
	echo ""

	local seconds=$(date +%s)

	if ! (cd modules/util/source-formatter && ${repo_root}/gradlew deploy --quiet) > ${work_dir}/deploy.log 2>&1
	then
		echo "Unable to deploy the source formatter: $(tail --lines=20 ${work_dir}/deploy.log)" >&2

		exit 1
	fi

	local source_file_names

	source_file_names=$(_get_reviewed_file_names | sed "s#^#${repo_root}/#" | paste --delimiters=, --serial)

	if [[ -z ${source_file_names} ]]
	then
		echo "The range ${diff_range} changes no file for the source formatter." >&2

		exit 1
	fi

	#
	# Passing the files explicitly bypasses source.formatter.excludes, which keeps
	# the fixture out of every other source formatter run.
	#

	if ! (cd portal-impl && ANT_OPTS=${ANT_OPTS:--Xmx2560m} ant format-source-files -Dsource.auto.fix=false -Dsource.fail.on.auto.fix=false -Dsource.fail.on.has.warning=false -Dsource.files=${source_file_names} -Dsource.print.errors=true) > ${work_dir}/source-formatter.log 2>&1
	then
		echo "Unable to run the source formatter: $(tail --lines=20 ${work_dir}/source-formatter.log)" >&2

		exit 1
	fi

	source_formatter_seconds=$(($(date +%s) - seconds))

	jq --arg repo_root ${repo_root} --null-input --raw-input '
		[
			inputs
			| capture("^\\s*\\[java\\] ((?<message>.*): )?(?<file>/[^ ]+)( (?<line>[0-9]+))? \\((?<check>[A-Za-z]+(:[A-Za-z]+)?)\\)$")
			| {
				check: (.check | sub("^[A-Za-z]+:"; "")),
				file: (.file | ltrimstr($repo_root + "/")),
				line: (.line | if . then tonumber else null end),
				message: (.message // "The file needs formatting" | sub(", see https?://[^ ]+$"; ""))
			}
		]
	' ${work_dir}/source-formatter.log > ${work_dir}/source-formatter.json
}

main "${@}"