/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs/promises';
import path from 'path';

import {PORTAL_DIR} from '../locations.mjs';
import runJestSharedPool from './runJestSharedPool.mjs';

/**
 * Runs the js-unit modules assigned to one test batch axis through a single
 * shared-pool Jest process instead of one Jest per module.
 *
 * `moduleTasks` are Gradle task paths as listed in `modules.test.class.group`
 * (for example ':apps:foo:foo-web:packageRunTest'), each mapped to its module
 * directory under `modules`. Modules are grouped by their React version,
 * because a module opts into React 16 through the `USE_REACT_16` environment
 * variable, which is read while the Jest config is built and while tests run,
 * so each group needs its own Jest process with that variable set accordingly.
 *
 * Returns `true` when any group failed.
 */
export default async function runJsUnitBatch({cliFlags = [], moduleTasks = []}) {
	const modulesDir = path.join(PORTAL_DIR, 'modules');

	const react16Paths = [];
	const react18Paths = [];

	for (const moduleTask of moduleTasks) {
		const projectPath = path.join(
			modulesDir,
			moduleTask
				.replace(/^:/, '')
				.replace(/:[^:]+$/, '')
				.split(':')
				.join(path.sep)
		);

		const packageJson = JSON.parse(
			await fs.readFile(path.join(projectPath, 'package.json'), 'utf8')
		);

		if (packageJson.scripts.test.includes('USE_REACT_16=true')) {
			react16Paths.push(projectPath);
		}
		else {
			react18Paths.push(projectPath);
		}
	}

	let failed = false;

	const groups = [
		{projectPaths: react18Paths, useReact16: false},
		{projectPaths: react16Paths, useReact16: true},
	];

	for (const {projectPaths, useReact16} of groups) {
		if (!projectPaths.length) {
			continue;
		}

		if (useReact16) {
			process.env.USE_REACT_16 = 'true';
		}
		else {
			delete process.env.USE_REACT_16;
		}

		const result = await runJestSharedPool({
			cliFlags,
			execaConfig: {
				env: useReact16 ? {USE_REACT_16: 'true'} : {},
				stdio: 'inherit',
			},
			projectPaths,
			rootDir: modulesDir,
		});

		if (result.failed) {
			failed = true;
		}
	}

	return failed;
}
