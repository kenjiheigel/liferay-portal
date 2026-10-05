#!/usr/bin/env bash

source "$(dirname "${0}")/_test_common.sh"

function main {
	set_up

	test_select_paths_branch
	test_select_paths_errors
	test_select_paths_portal
	test_select_paths_workspaces

	tear_down
}

function set_up {
	common_set_up

	add_files \
		modules/apps/blogs/blogs-api/bnd.bnd \
		workspaces/liferay-osbfaro-workspace/modules/osb-faro-web/bnd.bnd \
		workspaces/liferay-sample-workspace/modules/liferay-sample-api/bnd.bnd

	printf '\t--exclude README.md \\\n\t--exclude modules \\\n' > workspaces/refresh_other_workspaces.sh

	git add --all

	git commit --message "Add the refresh script" --quiet

	_MERGE_BASE=$(git rev-parse HEAD)

	add_files \
		README.md \
		modules/apps/blogs/blogs-api/src/main/java/Blogs.java \
		modules/apps/blogs/blogs-api/src/test/java/BlogsTest.java \
		workspaces/liferay-osbfaro-workspace/.prettierrc.js \
		workspaces/liferay-osbfaro-workspace/README.md \
		workspaces/liferay-osbfaro-workspace/modules/osb-faro-web/src/main/java/Faro.java \
		workspaces/liferay-sample-workspace/.prettierrc.js

	_VALIDATIONS_DIR=$(mktemp -d)

	_write_validation branch/everything.md "."
	_write_validation portal/everything.md "."
	_write_validation portal/main-java.md "^modules/.+\\.java$ &! /src/test/"
	_write_validation workspaces/compile.md "."
	_write_validation workspaces/generated-file.md "."
}

function tear_down {
	rm -fr "${_VALIDATIONS_DIR}"

	common_tear_down
}

function test_select_paths_branch {
	assert_equals \
		"$(_select_paths branch/everything.md)" \
		"README.md
modules/apps/blogs/blogs-api/src/main/java/Blogs.java
modules/apps/blogs/blogs-api/src/test/java/BlogsTest.java
workspaces/liferay-osbfaro-workspace/.prettierrc.js
workspaces/liferay-osbfaro-workspace/README.md
workspaces/liferay-osbfaro-workspace/modules/osb-faro-web/src/main/java/Faro.java
workspaces/liferay-sample-workspace/.prettierrc.js"
}

function test_select_paths_errors {
	mkdir -p "${_VALIDATIONS_DIR}/other"

	printf '# Other\n' > "${_VALIDATIONS_DIR}/portal/no-match.md"
	printf '## Match\n\n`.`\n' > "${_VALIDATIONS_DIR}/other/unknown.md"

	_test_select_paths_exit_code portal/no-match.md 1
	_test_select_paths_exit_code other/unknown.md 1
	_test_select_paths_exit_code workspaces/compile.md 1
}

function test_select_paths_portal {
	assert_equals \
		"$(_select_paths portal/everything.md)" \
		"README.md
modules/apps/blogs/blogs-api/src/main/java/Blogs.java
modules/apps/blogs/blogs-api/src/test/java/BlogsTest.java"
	assert_equals \
		"$(_select_paths portal/main-java.md)" \
		modules/apps/blogs/blogs-api/src/main/java/Blogs.java
}

function test_select_paths_workspaces {
	assert_equals \
		"$(_select_paths workspaces/compile.md liferay-osbfaro-workspace)" \
		"README.md
modules/osb-faro-web/src/main/java/Faro.java"
	assert_equals \
		"$(_select_paths workspaces/compile.md liferay-sample-workspace)" \
		.prettierrc.js
	assert_equals \
		"$(_select_paths workspaces/generated-file.md liferay-osbfaro-workspace)" \
		.prettierrc.js

	_test_select_paths_exit_code workspaces/generated-file.md 1 liferay-sample-workspace
}

function _select_paths {
	bash "${_SKILL_DIR}/select_paths.sh" "${_MERGE_BASE}" "${_VALIDATIONS_DIR}/${1}" "${2}" 2> /dev/null
}

function _test_select_paths_exit_code {
	_select_paths "${1}" "${3}" > /dev/null

	assert_equals "${?}" "${2}"
}

function _write_validation {
	mkdir -p "$(dirname "${_VALIDATIONS_DIR}/${1}")"

	printf '## Match\n\n`%s`\n' "${2}" > "${_VALIDATIONS_DIR}/${1}"
}

main
