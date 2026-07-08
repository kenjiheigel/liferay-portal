/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import merge from 'deepmerge';

import getJestConfig from './getJestConfig.js';
import getJestModuleNameMapper from './getJestModuleNameMapper.mjs';
import getUserConfig from './getUserConfig.mjs';

/**
 * Builds the fully merged Jest configuration for a single project, resolving
 * `<rootDir>` to the project path. This is the same configuration `runJest`
 * writes per project, factored out so a single Jest run can list several
 * projects at once and share one worker pool.
 */
export default async function getMergedJestConfig(projectPath) {
	let userConfig = await getUserConfig('jest', {cwd: projectPath});

	userConfig = JSON.parse(
		JSON.stringify(userConfig).replace('<rootDir>', projectPath)
	);

	return merge.all([
		getJestConfig({rootDir: projectPath}),
		{
			moduleNameMapper: await getJestModuleNameMapper({
				cwd: projectPath,
			}),
		},
		userConfig,
	]);
}
