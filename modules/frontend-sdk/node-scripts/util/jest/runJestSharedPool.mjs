/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {$} from 'execa';
import fs from 'fs/promises';
import path from 'path';
import {fileURLToPath} from 'url';

import fileExists from '../fileExists.mjs';
import onExit from '../onExit.mjs';
import getMergedJestConfig from './getMergedJestConfig.mjs';

const ROOT_CONFIG_NAME = 'TEMP_jest.projects.config.json';

const TEST_RESULTS_PROCESSOR = fileURLToPath(
	new URL('./writePerModuleJUnitResults.js', import.meta.url)
);

/**
 * Runs a single Jest process over several projects at once through Jest's
 * `projects` option, so every project's test files share one worker pool
 * instead of each project spinning up its own Jest with its own pool. Startup
 * is paid once and the pool stays fully utilized across the whole set.
 *
 * `getMergedJestConfig` reads `process.env.USE_REACT_16` when it builds a
 * project's `moduleNameMapper`, so the caller must set that variable to match
 * this group before invoking this function.
 */
export default async function runJestSharedPool({
	cliFlags = [],
	execaConfig = {},
	projectPaths = [],
	rootDir,
}) {
	const CONFIG_PATH = path.join(rootDir, ROOT_CONFIG_NAME);

	let result = false;

	try {
		const projects = [];

		for (const projectPath of projectPaths) {
			const projectConfig = await getMergedJestConfig(projectPath);

			// A single-module run gets its root directory from the Jest
			// '--projects <path>' argument. Here each project is an inline
			// config object, so its root directory must be set explicitly.

			projectConfig.rootDir = projectPath;

			// The per-project result processor is a global Jest option that is
			// ignored inside a project config and would only write one combined
			// file anyway. It is replaced by the root-level processor below,
			// which writes per-module output.

			delete projectConfig.testResultsProcessor;

			projects.push(projectConfig);
		}

		await fs.writeFile(
			CONFIG_PATH,
			JSON.stringify(
				{projects, testResultsProcessor: TEST_RESULTS_PROCESSOR},
				null,
				4
			)
		);

		onExit(() => fs.unlink(CONFIG_PATH));

		const config = {
			cwd: rootDir,
			env: {
				...process.env,
				...execaConfig.env,
				NODE_ENV: 'test',
			},
			...execaConfig,
		};

		result = await $(
			config
		)`jest --passWithNoTests --config ${CONFIG_PATH} ${cliFlags}`;
	}
	catch (error) {
		result = {
			all: error.toString(),
			failed: true,
		};
	}
	finally {
		if (await fileExists(CONFIG_PATH)) {
			await fs.unlink(CONFIG_PATH);
		}
	}

	return result;
}
