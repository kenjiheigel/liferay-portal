#!/usr/bin/env bash

function main {
	set -o pipefail

	local skill_dir

	skill_dir=$(cd "$(dirname "${0}")" && pwd -P)

	cd "$(git rev-parse --show-toplevel)" || exit 1

	local base_branch=master
	local forward_target=brianchandotcom/liferay-portal
	local repository=liferay/liferay-portal

	if git remote --verbose | grep --extended-regexp "[:/]liferay/liferay-portal-ee(\.git)? " > /dev/null
	then
		base_branch=master-private
		forward_target=""
		repository=liferay/liferay-portal-ee
	fi

	local branch

	branch=$(git symbolic-ref --quiet --short HEAD)

	if [[ -z ${branch} ]] || [[ ${branch} == ${base_branch} ]]
	then
		_exit_with_error "pr-check runs on a feature branch, and \"HEAD\" is ${branch:-detached}"
	fi

	if [[ ! -z $(git status --porcelain) ]]
	then
		_exit_with_error "The working tree is not clean. Commit or remove the changes first."
	fi

	local remote

	remote=$(git remote | grep --line-regexp upstream)

	if [[ -z ${remote} ]]
	then
		remote=$( \
			git remote --verbose | \
				grep --extended-regexp "[:/]${repository}(\.git)? \(fetch\)" | \
				head -n 1 | \
				cut -f 1)
	fi

	local base_ref=${base_branch}
	local forward_tip=""

	if [[ -z ${remote} ]]
	then
		if [[ $(git merge-base HEAD "${base_branch}") != $(git rev-parse "${base_branch}") ]]
		then
			_exit_with_error "No remote points at ${repository}, and the branch is not rebased on local ${base_branch}. Rebase it first."
		fi

		echo "No remote points at ${repository}, so the branch was not checked against the latest ${base_branch}"
	else
		if ! git fetch --quiet "${remote}" "${base_branch}"
		then
			_exit_with_error "Unable to fetch ${base_branch} from ${remote}"
		fi

		local worktree

		worktree=$( \
			git worktree list --porcelain | \
				awk -v branch="refs/heads/${base_branch}" '/^worktree / {worktree = substr($0, 10)} $0 == "branch " branch {print worktree}')

		if [[ ! -z ${worktree} ]]
		then
			if ! git -C "${worktree}" merge --ff-only --quiet "${remote}/${base_branch}"
			then
				_exit_with_error "Unable to fast forward ${base_branch} in ${worktree}. Never continue against a stale base."
			fi
		elif ! git fetch --quiet "${remote}" "${base_branch}:${base_branch}"
		then
			_exit_with_error "Unable to fast forward local ${base_branch}. Never continue against a stale base."
		fi

		if [[ ! -z ${forward_target} ]]
		then
			if git fetch --no-tags --quiet "https://github.com/${forward_target}.git" "${base_branch}" 2> /dev/null
			then
				forward_tip=$(git rev-parse FETCH_HEAD)
			else
				echo "Unable to fetch ${forward_target}, so the branch was not checked against the forward target"
			fi
		fi

		if [[ ! -z ${forward_tip} ]] &&
		   ! git merge-base --is-ancestor "$(git merge-base HEAD "${forward_tip}")" "${remote}/${base_branch}"
		then
			base_ref=${forward_tip}

			echo "The branch sits on ${forward_target}, so the diff is taken against ${forward_tip} and the pull request goes to ${forward_target}"
		fi

		if ! git rebase --quiet "${base_ref}" > /dev/null 2>&1
		then
			local unmerged_files

			unmerged_files=$(git diff --diff-filter=U --name-only)

			if [[ -z ${unmerged_files} ]]
			then
				git rebase --abort > /dev/null 2>&1

				_exit_with_error "Unable to rebase onto ${base_ref}"
			fi

			echo "Rebasing onto ${base_ref} conflicts in these files, and the rebase is still in progress:"
			echo "${unmerged_files}"

			exit 3
		fi

		if [[ ! -z ${forward_tip} ]]
		then
			local conflicted_files

			conflicted_files=$(git merge-tree --name-only --write-tree "${forward_tip}" HEAD | sed -e "1d" -e "/^$/,\$d")

			local exit_code=${?}

			if [ ${exit_code} -eq 1 ]
			then
				echo "\"ci:forward\" would refuse this branch. These files conflict with ${forward_tip}:"
				echo "${conflicted_files}"
				echo ""
				echo "These commits of the forward target touched them:"

				echo "${conflicted_files}" | tr "\n" "\0" | xargs --null git log --format="%h %s" "HEAD..${forward_tip}" --

				exit 4
			elif [ ${exit_code} -ne 0 ]
			then
				echo "Unable to check whether \"ci:forward\" can merge the branch"
			fi
		fi

		if [[ ${repository} == liferay/liferay-portal-ee ]] &&
		   [[ ${skill_dir} == $(pwd -P)/* ]]
		then
			git fetch --quiet "${remote}" master

			if [[ $(git rev-parse master) != $(git rev-parse FETCH_HEAD) ]]
			then
				_exit_with_error "The skills were copied from a stale \"master\". Copy them again from the latest \"master\"."
			fi
		fi
	fi

	local merge_base

	merge_base=$(git merge-base HEAD "${base_ref}")

	if [[ -z $(git diff --name-only --no-renames "${merge_base}...HEAD") ]]
	then
		_exit_with_error "The branch changes no files, so there is nothing to check"
	fi

	local hidden_files

	hidden_files=$( \
		git diff --name-only --no-renames "${merge_base}...HEAD" | \
			bash "${skill_dir}/find_modules.sh" "${merge_base}" | \
			cut -d " " -f 1 | \
			grep --invert-match "^-$" | \
			sort --unique | \
			xargs --no-run-if-empty git ls-files --directory --exclude-standard --ignored --others -- | \
			git check-ignore --stdin --verbose | \
			grep --extended-regexp --invert-match "^([^/:][^:]*/)?\.gitignore:" | \
			grep --extended-regexp "\.(bnd|cjs|css|ftl|gradle|java|js|json|jsp|jspf|jsx|mjs|properties|sass|scss|ts|tsx|xml)$")

	if [[ ! -z ${hidden_files} ]]
	then
		echo "These source files are hidden from Git by an ignore rule outside the repository, so CI would never see them. Commit or delete each one:" >&2
		echo "${hidden_files}" >&2

		exit 1
	fi

	local build_cache_dir=.gradle/caches/build-cache-1
	local shared_gradle_cache=${SHARED_GRADLE_CACHE:-${HOME}/.liferay/gradle-build-cache}

	mkdir -p .gradle/caches "${shared_gradle_cache}"

	if [[ $(readlink "${build_cache_dir}") != ${shared_gradle_cache} ]]
	then
		if [[ -d ${build_cache_dir} ]] && [[ ! -L ${build_cache_dir} ]]
		then
			rsync --archive --exclude="*.lock" --ignore-existing "${build_cache_dir}/" "${shared_gradle_cache}/"

			rm -fr "${build_cache_dir}"
		fi

		ln -fns "${shared_gradle_cache}" "${build_cache_dir}"

		echo "Using ${shared_gradle_cache} as the shared Gradle build cache"
	fi

	echo "BASE_BRANCH=${base_branch}"
	echo "BASE_REF=${base_ref}"
	echo "FORWARD_TIP=${forward_tip}"
	echo "MERGE_BASE=${merge_base}"
	echo "REPO_ROOT=${PWD}"
}

function _exit_with_error {
	echo "${*}" >&2

	exit 1
}

main "${@}"