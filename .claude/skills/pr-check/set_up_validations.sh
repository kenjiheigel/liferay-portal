#!/usr/bin/env bash

function main {
	local skill_dir

	skill_dir=$(cd "$(dirname "${0}")" && pwd)

	cd "$(git rev-parse --show-toplevel)" || exit 1

	local selection

	selection=$(bash "${skill_dir}/select_validations.sh" "${1}") || exit 1

	echo "${selection}"

	local validation_file

	for validation_file in "${skill_dir}/validations"/*/*.md
	do
		local sections_file

		sections_file=build/pr-check/sections/$(basename "$(dirname "${validation_file}")")/$(basename "${validation_file}")

		mkdir -p "$(dirname "${sections_file}")"

		awk '/^## / {print_section = ($0 == "## Command" || $0 == "## Autocommit")} print_section' "${validation_file}" > "${sections_file}"

		if ! grep --line-regexp "## Autocommit" "${validation_file}" > /dev/null
		then
			{
				echo "## Autocommit"
				echo ""
				echo "None. This validation makes no commit."
			} >> "${sections_file}"
		fi
	done

	local log_dir=build/pr-check/shared-preconditions

	rm -fr "${log_dir}"

	mkdir -p "${log_dir}"

	export ANT_OPTS="-Xmx2560m"

	local project

	if echo "${selection}" | grep --line-regexp -- "- Portal Snapshots" > /dev/null
	then
		_run "${log_dir}/portal-snapshots.log" ant compile install-portal-snapshots

		local missing_projects=""

		for project in portal-impl portal-kernel portal-test util-bridges util-java util-slf4j util-taglib
		do
			local artifact
			local version

			artifact=com.liferay.$(echo "${project}" | tr - .)
			version=$(sed -e "s/^Bundle-Version: //p" -n "${project}/bnd.bnd")

			if [ ! -f ${project}/${project}.jar ] || [ ! -f .m2/com/liferay/portal/${artifact}/${version}-SNAPSHOT/${artifact}-${version}-SNAPSHOT.jar ]
			then
				missing_projects="${missing_projects} ${project}"
			fi
		done

		if [[ ! -z ${missing_projects} ]]
		then
			_exit_with_error "Unable to find the jar or the snapshot of these projects at the version their \"bnd.bnd\" declares:${missing_projects}"
		fi
	fi

	if echo "${selection}" | grep --line-regexp -- "- Portal Classpath" > /dev/null
	then
		_run "${log_dir}/portal-classpath-additional-jars.log" ant deploy-additional-jars

		for project in portal-impl portal-kernel portal-test util-java util-taglib
		do
			_run "${log_dir}/portal-classpath-${project}.log" ant -buildfile "${project}/build.xml" deploy
		done

		_run "${log_dir}/portal-classpath-core.log" ./gradlew --parallel --project-dir modules/core deploy
	fi

	if echo "${selection}" | grep --line-regexp -- "- SDK" > /dev/null
	then
		_run "${log_dir}/sdk.log" ant setup-sdk
	fi
}

function _exit_with_error {
	echo "${*}" >&2

	exit 1
}

function _run {
	local changed_files

	changed_files=$(git diff --name-only)

	"${@:2}" > "${1}" 2>&1

	local exit_code=${?}

	local rewritten_files

	rewritten_files=$(git diff --name-only | grep --fixed-strings --invert-match --line-regexp --regexp="${changed_files:-/}")

	if [[ ! -z ${rewritten_files} ]]
	then
		echo "${rewritten_files}" | tr "\n" "\0" | xargs --null git checkout --

		echo "Restored the files \"${*:2}\" rewrote as a side effect: $(echo "${rewritten_files}" | paste -d " " -s -)"
	fi

	if [ ${exit_code} -ne 0 ]
	then
		echo "Unable to run \"${*:2}\", see ${1}" >&2

		grep --extended-regexp "BUILD FAILED|Could not|error:|FAILURE" "${1}" | head -n 20

		exit 1
	fi
}

main "${@}"