/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alejandro Tardín
 */
public class SentenceSpacingCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
		String fileName, String absolutePath, String content) {

		String[] lines = content.split("(?<=\n)");

		boolean insideFence = false;

		StringBundler sb = new StringBundler(lines.length);

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];

			String trimmedLine = StringUtil.trim(line);

			if (fileName.endsWith(".md") &&
				(trimmedLine.startsWith("```") ||
				 trimmedLine.startsWith("~~~"))) {

				insideFence = !insideFence;
			}

			if (!insideFence) {
				String prose = _getProse(fileName, trimmedLine);

				if (prose != null) {
					Matcher matcher = _doubleSpacePattern.matcher(prose);

					if (matcher.find()) {
						if (prose.equals(trimmedLine)) {
							line = StringUtil.replace(
								line, prose, matcher.replaceAll("$1 "));
						}
						else {
							addMessage(
								fileName, "Use a single space after a period",
								i + 1);
						}
					}
				}
			}

			sb.append(line);
		}

		return sb.toString();
	}

	private String _getProse(String fileName, String trimmedLine) {
		if (fileName.endsWith(".java")) {
			if (trimmedLine.startsWith("*") || trimmedLine.startsWith("//") ||
				trimmedLine.startsWith("/*")) {

				return trimmedLine;
			}

			Matcher matcher = _stringLiteralPattern.matcher(trimmedLine);

			StringBundler sb = new StringBundler();

			while (matcher.find()) {
				sb.append(matcher.group());
			}

			if (sb.index() > 0) {
				return sb.toString();
			}

			return null;
		}

		if (fileName.endsWith(".md")) {
			Matcher matcher = _inlineCodePattern.matcher(trimmedLine);

			if (matcher.find()) {
				return matcher.replaceAll("``");
			}

			return trimmedLine;
		}

		if (fileName.endsWith("/content/Language.properties")) {
			if (trimmedLine.startsWith("#") || !trimmedLine.contains("=")) {
				return null;
			}

			return trimmedLine;
		}

		if (fileName.endsWith(".sh") && trimmedLine.startsWith("#")) {
			return trimmedLine;
		}

		return null;
	}

	private static final Pattern _doubleSpacePattern = Pattern.compile(
		"([a-z)\"'][.!?]) {2,}(?=[A-Z0-9])");
	private static final Pattern _inlineCodePattern = Pattern.compile(
		"`[^`]*`");
	private static final Pattern _stringLiteralPattern = Pattern.compile(
		"\"(?:[^\"\\\\]|\\\\.)*\"");

}