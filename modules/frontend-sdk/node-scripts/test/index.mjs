/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs/promises';
import path from 'path';

import getNamedArguments from '../util/getNamedArguments.mjs';
import getYarnWorkspaceProjects from '../util/getYarnWorkspaceProjects.mjs';
import runJest from '../util/jest/runJest.mjs';
import runJsUnitBatch from '../util/jest/runJsUnitBatch.mjs';
import {PORTAL_DIR} from '../util/locations.mjs';
import print from '../util/print.mjs';
import runConcurrentTasks from '../util/runConcurrentTasks.mjs';

const BATCH_TASKS_FILE_NAME = 'TEMP_js_unit_batch_tasks.txt';

export default async function () {
	const {sync} = getNamedArguments({
		sync: '--sync',
	});

	const originalNodeEnv = process.env.NODE_ENV;

	process.env.NODE_ENV = 'test';

	const args = process.argv.slice(3);

	/**
	 * When a batch tasks file is present, one Jest run covers every module of
	 * the test batch axis through a shared worker pool (see `runJsUnitBatch`),
	 * rather than one Jest per module. The CI js-unit target writes the file so
	 * a single Gradle-driven invocation, which already has Node set up, runs
	 * the whole axis.
	 */
	const batchTasksFile = path.join(
		PORTAL_DIR,
		'modules',
		BATCH_TASKS_FILE_NAME
	);

	let batchTasks = null;

	try {
		batchTasks = await fs.readFile(batchTasksFile, 'utf8');
	}
	catch (error) {
		batchTasks = null;
	}

	if (batchTasks) {
		const failed = await runJsUnitBatch({
			cliFlags: args.filter((arg) => !arg.startsWith(':')),
			moduleTasks: batchTasks.trim().split(/\s+/),
		});

		process.env.NODE_ENV = originalNodeEnv;

		if (failed) {
			throw new Error('One or more js-unit projects failed.');
		}

		return;
	}

	/**
	 * When using 'yarn run ...' it sets the cwd to the nearest package.json
	 */
	let cwd = process.env.INIT_CWD;

	if (!cwd) {
		cwd = process.cwd();
	}

	const projects = await getYarnWorkspaceProjects();

	/**
	 * Map containing the path to the project and the environment variables
	 * to be used when running the tests.
	 */
	const testableProjectsMap = new Map();

	/**
	 * Filter out projects that do not have `node-scripts test`
	 */
	for (const projectPath of projects) {

		// Check if deeply nested passed a project root or check if shallowly
		// nested before several project roots

		if (
			cwd.includes(projectPath) ||
			projectPath.includes(process.env.INIT_CWD)
		) {
			const packageJson = path.join(projectPath, 'package.json');
			const pkgJsonContents = await fs.readFile(packageJson, 'utf8');

			if (pkgJsonContents.includes('node-scripts test')) {
				const pkgJson = JSON.parse(pkgJsonContents);

				testableProjectsMap.set(
					projectPath,
					getEnvVars(pkgJson.scripts.test)
				);
			}
		}
	}

	const totalTestableProjects = testableProjectsMap.size;

	if (totalTestableProjects === 1) {
		const [[projectPath, envObj]] = testableProjectsMap.entries();

		await runJest({
			cliFlags: args,
			execaConfig: {
				env: envObj,
				stdio: 'inherit',
			},
			projectPath,
		});
	}
	else {
		print(
			0,
			print.title(
				`\n> Testing ${totalTestableProjects} projects in ${sync ? 'series' : 'parallel'}...\n`
			)
		);

		const asyncItems = [];

		for (const [projectPath, envObj] of testableProjectsMap) {
			asyncItems.push(async () => {
				const projectName = path.relative(PORTAL_DIR, projectPath);

				const {all, failed} = await runJest({
					cliFlags: args,
					execaConfig: {
						all: true,
						env: envObj,
						reject: false,
						stdio: 'pipe',
					},
					projectPath,
				});

				if (failed) {
					print(
						1,
						print.error('FAILED:'),
						print.underline(projectName),
						'\n'
					);
					print(2, `${all}\n`);
				}
				else {
					print(
						1,
						print.success('PASSED:'),
						print.underline(projectName),
						'\n'
					);
				}
			});
		}

		if (sync) {
			for (const task of asyncItems) {
				await task('inherit');
			}
		}
		else {
			await runConcurrentTasks(asyncItems);
		}
	}

	process.env.NODE_ENV = originalNodeEnv;
}

function getEnvVars(value) {
	return value
		.split(' ')
		.filter((part) => part.includes('='))
		.reduce((acc, part) => {
			const [key, value] = part.split('=');
			acc[key] = value;

			return acc;
		}, {});
}
