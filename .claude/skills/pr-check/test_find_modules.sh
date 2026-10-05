#!/usr/bin/env bash

source "$(dirname "${0}")/_test_common.sh"

function main {
	set_up

	test_find_modules_deleted_module
	test_find_modules_portal
	test_find_modules_workspace

	tear_down
}

function set_up {
	common_set_up

	add_files \
		build.xml \
		modules/apps/blogs/blogs-api/bnd.bnd \
		modules/apps/blogs/blogs-web/bnd.bnd \
		modules/apps/frontend-theme/frontend-theme-classic/gulpfile.js \
		modules/apps/gone/gone-api/bnd.bnd \
		modules/apps/outer/bnd.bnd \
		modules/apps/outer/inner/bnd.bnd \
		modules/apps/spring/spring-app/src/main/resources/application.properties \
		modules/build.xml \
		modules/test/playwright/build.gradle \
		modules/test/playwright/package.json \
		modules/test/poshi/poshi-core/bnd.bnd \
		portal-impl/build.xml \
		portal-web/build.xml \
		workspaces/liferay-sample-workspace/client-extensions/liferay-sample-batch/client-extension.yaml \
		workspaces/liferay-sample-workspace/modules/liferay-sample-api/bnd.bnd \
		workspaces/liferay-sample-workspace/themes/liferay-sample-theme/gulpfile.js

	_MERGE_BASE=$(git rev-parse HEAD)

	git rm -r --quiet modules/apps/gone

	git commit --message "Delete gone-api" --quiet
}

function tear_down {
	common_tear_down
}

function test_find_modules_deleted_module {
	_test_find_modules modules/apps/gone/gone-api/src/main/java/Gone.java modules/apps/gone/gone-api
}

function test_find_modules_portal {
	_test_find_modules build.xml -
	_test_find_modules modules/.releng/apps/blogs/blogs-api.properties -
	_test_find_modules modules/apps/blogs/blogs-api/bnd.bnd modules/apps/blogs/blogs-api
	_test_find_modules modules/apps/blogs/blogs-api/src/jmh/java/BlogsBenchmark.java modules/apps/blogs/blogs-api
	_test_find_modules modules/apps/blogs/blogs-api/src/main/java/Blogs.java modules/apps/blogs/blogs-api
	_test_find_modules modules/apps/blogs/blogs-api/src/test/java/BlogsTest.java modules/apps/blogs/blogs-api
	_test_find_modules modules/apps/blogs/blogs-api/src/testIntegration/java/BlogsTest.java modules/apps/blogs/blogs-api
	_test_find_modules modules/apps/blogs/blogs-web/src/main/resources/META-INF/resources/view.jsp modules/apps/blogs/blogs-web
	_test_find_modules "modules/apps/blogs/blogs-web/src/main/resources/META-INF/resources/with space.jsp" modules/apps/blogs/blogs-web
	_test_find_modules modules/apps/blogs/blogs-web/src/main/resources/com/liferay/blogs/script/blogs.groovy modules/apps/blogs/blogs-web
	_test_find_modules modules/apps/frontend-theme/frontend-theme-classic/src/css/main.scss modules/apps/frontend-theme/frontend-theme-classic
	_test_find_modules modules/apps/outer/inner/src/main/java/Inner.java modules/apps/outer
	_test_find_modules modules/apps/spring/spring-app/src/main/java/Application.java modules/apps/spring/spring-app
	_test_find_modules modules/build.gradle -
	_test_find_modules modules/test/playwright/tests/blogs.spec.ts -
	_test_find_modules modules/test/poshi/poshi-core/src/main/java/Poshi.java modules/test/poshi/poshi-core
	_test_find_modules portal-impl/src/com/liferay/portal/Portal.java portal-impl
	_test_find_modules portal-web/test/functional/com/liferay/portalweb/tests/Blogs.testcase portal-web
}

function test_find_modules_workspace {
	_test_find_modules workspaces/liferay-sample-workspace/build.gradle -
	_test_find_modules workspaces/liferay-sample-workspace/client-extensions/liferay-sample-batch/batch/data.json workspaces/liferay-sample-workspace/client-extensions/liferay-sample-batch
	_test_find_modules workspaces/liferay-sample-workspace/modules/liferay-sample-api/src/main/java/Sample.java workspaces/liferay-sample-workspace/modules/liferay-sample-api
	_test_find_modules workspaces/liferay-sample-workspace/themes/liferay-sample-theme/src/css/main.scss -
}

function _test_find_modules {
	assert_equals \
		"$(echo "${1}" | bash "${_SKILL_DIR}/find_modules.sh" "${_MERGE_BASE}")" \
		"${2} ${1}"
}

main
