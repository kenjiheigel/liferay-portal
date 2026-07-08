/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const junitReporter = require('@liferay/jest-junit-reporter');
const fs = require('fs');
const path = require('path');

/**
 * A Jest `testResultsProcessor` for shared-pool runs (see `runJestSharedPool`).
 * The `@liferay/jest-junit-reporter` writes one `TEST-frontend-js.xml` in the
 * current working directory, which is correct when Jest runs one module at a
 * time but collapses every module into a single file when several modules run
 * in one process. This groups the results by their owning module and delegates
 * to that same reporter once per module, with the working directory set to the
 * module, so the per-module `TEST-frontend-js.xml` layout the CI collects is
 * preserved.
 */

const moduleRootCache = new Map();

function findModuleRoot(filePath) {
	let dir = path.dirname(filePath);

	while (true) {
		if (moduleRootCache.has(dir)) {
			return moduleRootCache.get(dir);
		}

		if (fs.existsSync(path.join(dir, 'package.json'))) {
			moduleRootCache.set(dir, dir);

			return dir;
		}

		const parent = path.dirname(dir);

		if (parent === dir) {
			return null;
		}

		dir = parent;
	}
}

module.exports = (report) => {
	const suitesByModuleRoot = new Map();

	for (const suite of report.testResults) {
		const moduleRoot = findModuleRoot(suite.testFilePath);

		if (!moduleRoot) {
			continue;
		}

		if (!suitesByModuleRoot.has(moduleRoot)) {
			suitesByModuleRoot.set(moduleRoot, []);
		}

		suitesByModuleRoot.get(moduleRoot).push(suite);
	}

	const originalCwd = process.cwd();

	try {
		for (const [moduleRoot, suites] of suitesByModuleRoot) {
			const numFailedTests = suites.reduce(
				(total, suite) => total + suite.numFailingTests,
				0
			);
			const numTotalTests = suites.reduce(
				(total, suite) =>
					total +
					suite.numFailingTests +
					suite.numPassingTests +
					suite.numPendingTests,
				0
			);

			process.chdir(moduleRoot);

			junitReporter({
				...report,
				numFailedTests,
				numTotalTests,
				testResults: suites,
			});
		}
	}
	finally {
		process.chdir(originalCwd);
	}

	return report;
};
