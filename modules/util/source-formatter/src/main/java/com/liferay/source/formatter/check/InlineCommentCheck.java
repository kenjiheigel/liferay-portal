/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alejandro Tardín
 */
public class InlineCommentCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
		String fileName, String absolutePath, String content) {

		String commentPrefix = "//";

		if (fileName.endsWith(".sh")) {
			commentPrefix = "#";
		}

		String[] lines = StringUtil.splitLines(content);

		int blockStart = -1;

		for (int i = 0; i <= lines.length; i++) {
			String trimmedLine = null;

			if (i < lines.length) {
				trimmedLine = StringUtil.trim(lines[i]);
			}

			if ((trimmedLine != null) &&
				trimmedLine.startsWith(commentPrefix) &&
				!trimmedLine.startsWith("#!")) {

				if (blockStart == -1) {
					blockStart = i;
				}

				continue;
			}

			if (blockStart != -1) {
				_checkCommentBlock(
					commentPrefix, i - 1, fileName, lines, blockStart);

				blockStart = -1;
			}
		}

		return content;
	}

	private void _checkCommentBlock(
		String commentPrefix, int end, String fileName, String[] lines,
		int start) {

		for (int i = start; i <= end; i++) {
			String text = StringUtil.trim(lines[i]);

			while (text.startsWith(commentPrefix)) {
				text = text.substring(commentPrefix.length());
			}

			text = StringUtil.trim(text);

			if (text.isEmpty()) {
				continue;
			}

			if (_isLowerCaseProse(text)) {
				addMessage(
					fileName, "Begin the comment with a capital letter", i + 1);
			}

			break;
		}

		if (!commentPrefix.equals("#")) {
			return;
		}

		if ((start > 0) && !_isBlockBoundary(true, lines[start - 1])) {
			addMessage(
				fileName, "There should be an empty line before the comment",
				start + 1);
		}

		if (((end + 1) < lines.length) &&
			!_isBlockBoundary(false, lines[end + 1])) {

			addMessage(
				fileName, "There should be an empty line after the comment",
				end + 1);
		}
	}

	private boolean _isBlockBoundary(boolean before, String line) {
		String trimmedLine = StringUtil.trim(line);

		if (trimmedLine.isEmpty()) {
			return true;
		}

		if (before) {
			if (trimmedLine.startsWith("#!") || trimmedLine.endsWith("{") ||
				trimmedLine.endsWith("(") || trimmedLine.endsWith("do") ||
				trimmedLine.endsWith("else") || trimmedLine.endsWith("in") ||
				trimmedLine.endsWith("then")) {

				return true;
			}

			return false;
		}

		if (trimmedLine.startsWith(")") || trimmedLine.startsWith(";;") ||
			trimmedLine.startsWith("}") ||
			ArrayUtil.contains(_BLOCK_END_KEYWORDS, trimmedLine)) {

			return true;
		}

		return false;
	}

	private boolean _isLowerCaseProse(String text) {
		Matcher matcher = _firstWordPattern.matcher(text);

		if (!matcher.find()) {
			return false;
		}

		String firstWord = matcher.group(1);

		if (ArrayUtil.contains(_CODE_WORDS, firstWord)) {
			return false;
		}

		String rest = text.substring(firstWord.length());

		if (!rest.isEmpty() && !rest.startsWith(" ")) {
			return false;
		}

		matcher = _codePattern.matcher(text);

		return !matcher.find();
	}

	private static final String[] _BLOCK_END_KEYWORDS = {
		"done", "elif", "else", "esac", "fi"
	};

	private static final String[] _CODE_WORDS = {
		"break", "case", "catch", "continue", "do", "echo", "else", "export",
		"for", "if", "import", "local", "new", "noinspection", "private",
		"protected", "public", "return", "set", "shellcheck", "static", "super",
		"this", "throw", "try", "while"
	};

	private static final Pattern _codePattern = Pattern.compile(
		"[;{}=$|`]|\\w\\(|->|\\w\\.\\w|^\\S+:");
	private static final Pattern _firstWordPattern = Pattern.compile(
		"^([a-z][a-z]*)");

}