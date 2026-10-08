/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.io.unsync.UnsyncBufferedReader;
import com.liferay.petra.io.unsync.UnsyncStringReader;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alejandro Tardín
 */
public class SHIncrementCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
			String fileName, String absolutePath, String content)
		throws IOException {

		if (content.startsWith("#!/bin/sh")) {
			return content;
		}

		Matcher matcher = _errexitPattern.matcher(content);

		if (!matcher.find()) {
			return content;
		}

		try (UnsyncBufferedReader unsyncBufferedReader =
				new UnsyncBufferedReader(new UnsyncStringReader(content))) {

			String line = null;
			int lineNumber = 0;

			while ((line = unsyncBufferedReader.readLine()) != null) {
				lineNumber++;

				String trimmedLine = StringUtil.trim(line);

				matcher = _postIncrementPattern.matcher(trimmedLine);

				if (matcher.matches()) {
					addMessage(
						fileName,
						StringBundler.concat(
							"Use \"((", matcher.group(2), matcher.group(1),
							"))\" instead of \"", trimmedLine, "\""),
						lineNumber);

					continue;
				}

				matcher = _assignIncrementPattern.matcher(trimmedLine);

				if (matcher.matches()) {
					addMessage(
						fileName,
						StringBundler.concat(
							"Use \"((++", matcher.group(1),
							"))\" instead of \"", trimmedLine, "\""),
						lineNumber);
				}
			}
		}

		return content;
	}

	private static final Pattern _assignIncrementPattern = Pattern.compile(
		"(?:local\\s+)?(\\w+)=" +
			"\\$\\(\\(\\s*\\$?\\{?\\1\\}?\\s*\\+\\s*1\\s*\\)\\)");
	private static final Pattern _errexitPattern = Pattern.compile(
		"(?m)^\\s*set\\s+(-o\\s+errexit|-\\w*e\\w*)\\b");
	private static final Pattern _postIncrementPattern = Pattern.compile(
		"\\(\\(\\s*(\\w+)(\\+\\+|--)\\s*\\)\\)");

}