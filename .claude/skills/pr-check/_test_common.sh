#!/usr/bin/env bash

function add_files {
	local file

	for file in "${@}"
	do
		mkdir -p "$(dirname "${file}")"

		touch "${file}"
	done

	git add --all

	git commit --message "${FUNCNAME[1]}" --quiet
}

function assert_equals {
	_TEST_COUNT=$((_TEST_COUNT + 1))

	if [ "${1}" == "${2}" ]
	then
		return
	fi

	echo "${FUNCNAME[1]} FAILED"
	echo "Actual: ${1}"
	echo "Expected: ${2}"

	_TEST_FAILURE_COUNT=$((_TEST_FAILURE_COUNT + 1))
}

function common_set_up {
	_SKILL_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
	_TEST_COUNT=0
	_TEST_DIR=$(mktemp -d)
	_TEST_FAILURE_COUNT=0

	cd "${_TEST_DIR}" || exit 1

	git init --quiet

	git config commit.gpgsign false
	git config user.email test@liferay.com
	git config user.name Test
}

function common_tear_down {
	cd / || exit 1

	rm -fr "${_TEST_DIR}"

	echo "$(basename "${0}"): ${_TEST_COUNT} assertions, ${_TEST_FAILURE_COUNT} failed"

	if [[ "${_TEST_FAILURE_COUNT}" -ne 0 ]]
	then
		exit 1
	fi
}
