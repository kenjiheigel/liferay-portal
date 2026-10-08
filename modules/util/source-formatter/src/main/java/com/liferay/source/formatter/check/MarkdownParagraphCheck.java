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
public class MarkdownParagraphCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
		String fileName, String absolutePath, String content) {

		String[] lines = StringUtil.splitLines(content);

		boolean insideFence = false;
		boolean insideFrontMatter = false;
		boolean insideHTMLComment = false;
		boolean reported = false;
		boolean wrappable = false;

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];

			String trimmedLine = StringUtil.trim(line);

			if ((i == 0) && trimmedLine.equals("---")) {
				insideFrontMatter = true;

				continue;
			}

			if (insideFrontMatter) {
				if (trimmedLine.equals("---")) {
					insideFrontMatter = false;
				}

				continue;
			}

			if (trimmedLine.startsWith("```") ||
				trimmedLine.startsWith("~~~")) {

				insideFence = !insideFence;
				wrappable = false;

				continue;
			}

			if (insideFence) {
				continue;
			}

			if (trimmedLine.startsWith("<!--")) {
				insideHTMLComment = true;
			}

			if (insideHTMLComment) {
				if (trimmedLine.contains("-->")) {
					insideHTMLComment = false;
				}

				wrappable = false;

				continue;
			}

			if (trimmedLine.isEmpty()) {
				reported = false;
				wrappable = false;

				continue;
			}

			Matcher matcher = _structuralLinePattern.matcher(trimmedLine);

			boolean structuralLine = matcher.find();

			matcher = _listItemPattern.matcher(trimmedLine);

			boolean listItem = matcher.find();

			if (wrappable && !structuralLine && !listItem && !reported) {
				addMessage(
					fileName,
					"Write each paragraph and list item on a single line",
					i + 1);

				reported = true;
			}

			if (listItem) {
				reported = false;
			}

			wrappable =
				(!structuralLine || listItem) && !line.endsWith("  ") &&
				!line.endsWith("\\");
		}

		return content;
	}

	private static final Pattern _listItemPattern = Pattern.compile(
		"^([-*+]|\\d+[.)])\\s");
	private static final Pattern _structuralLinePattern = Pattern.compile(
		"^(#|>|\\||<|\\[[^\\]]+\\]:|([-*+]|\\d+[.)])\\s|[-*_=]{3,}$)");

}