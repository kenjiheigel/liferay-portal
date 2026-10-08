/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alejandro Tardín
 */
public class SHFailFastCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
		String fileName, String absolutePath, String content) {

		if (!_isCloudScript(absolutePath)) {
			return content;
		}

		List<String> options = new ArrayList<>();

		for (String line : StringUtil.splitLines(content)) {
			line = StringUtil.trim(line);

			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			if (!line.startsWith("set -o ")) {
				break;
			}

			options.add(StringUtil.trim(line.substring(7)));
		}

		List<String> expectedOptions = new ArrayList<>();

		for (String option : options) {
			if (_options.contains(option)) {
				expectedOptions.add(option);
			}
		}

		if (!expectedOptions.equals(_options)) {
			addMessage(
				fileName,
				StringBundler.concat(
					"Start the script with \"set -o errexit\", \"set -o ",
					"nounset\", and \"set -o pipefail\", in that order, ",
					"before the first command"),
				1);
		}

		return content;
	}

	private boolean _isCloudScript(String absolutePath) {
		File portalDir = getPortalDir();

		if (portalDir != null) {
			String portalDirName = StringUtil.replace(
				portalDir.getAbsolutePath(), '\\', '/');

			if (absolutePath.startsWith(portalDirName + "/")) {
				return absolutePath.startsWith(portalDirName + "/cloud/");
			}
		}

		return absolutePath.contains("/cloud/");
	}

	private static final List<String> _options = Arrays.asList(
		"errexit", "nounset", "pipefail");

}