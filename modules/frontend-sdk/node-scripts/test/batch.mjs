/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import runJsUnitBatch from '../util/jest/runJsUnitBatch.mjs';

/**
 * Runs the js-unit modules of one test batch axis through a single shared-pool
 * Jest process. Arguments starting with ':' are Gradle task paths (as listed
 * in 'modules.test.class.group'); the rest are forwarded to Jest.
 */
export default async function () {
	const args = process.argv.slice(3);

	const failed = await runJsUnitBatch({
		cliFlags: args.filter((arg) => !arg.startsWith(':')),
		moduleTasks: args.filter((arg) => arg.startsWith(':')),
	});

	if (failed) {
		throw new Error('One or more js-unit projects failed.');
	}
}
