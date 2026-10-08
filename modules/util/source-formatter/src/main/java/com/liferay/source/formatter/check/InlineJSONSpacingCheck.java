/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.portal.kernel.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alejandro Tardín
 */
public class InlineJSONSpacingCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
		String fileName, String absolutePath, String content) {

		String[] lines = StringUtil.splitLines(content);

		for (int i = 0; i < lines.length; i++) {
			String line = StringUtil.replace(lines[i], "\\\"", "\"");

			Matcher matcher = _keyPattern.matcher(line);

			if (!matcher.find()) {
				continue;
			}

			matcher = _missingSpacePattern.matcher(line);

			if (matcher.find()) {
				addMessage(
					fileName,
					"Put a space after each \":\" and \",\" in inline JSON",
					i + 1);

				continue;
			}

			matcher = _paddedBracePattern.matcher(line);

			if (matcher.find()) {
				addMessage(
					fileName,
					"Do not put a space just inside \"{\" or \"}\" in inline " +
						"JSON",
					i + 1);
			}
		}

		return content;
	}

	private static final Pattern _keyPattern = Pattern.compile(
		"[{,]\\s*\"[\\w$.-]+\"\\s*:");
	private static final Pattern _missingSpacePattern = Pattern.compile(
		"[{,]\\s*\"[\\w$.-]+\":[^\\s]|" +
			"(?:\"|\\d|true|false|null|[}\\]]),\"[\\w$.-]+\"\\s*:");
	private static final Pattern _paddedBracePattern = Pattern.compile(
		"\\{ +\"[\\w$.-]+\"\\s*:|\" +\\}");

}