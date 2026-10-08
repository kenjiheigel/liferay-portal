/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.io.unsync.UnsyncBufferedReader;
import com.liferay.petra.io.unsync.UnsyncStringReader;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alejandro Tardín
 */
public class PropertiesLanguageTitleOrSentenceCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
			String fileName, String absolutePath, String content)
		throws IOException {

		if (!fileName.endsWith("/content/Language.properties")) {
			return content;
		}

		try (UnsyncBufferedReader unsyncBufferedReader =
				new UnsyncBufferedReader(new UnsyncStringReader(content))) {

			String line = null;
			int lineNumber = 0;

			while ((line = unsyncBufferedReader.readLine()) != null) {
				lineNumber++;

				if (line.startsWith("#")) {
					continue;
				}

				String[] array = line.split("=", 2);

				if (array.length != 2) {
					continue;
				}

				String key = StringUtil.trim(array[0]);

				Matcher matcher = _exemptKeyPattern.matcher(key);

				if (matcher.find()) {
					continue;
				}

				String value = array[1].replaceAll("<[^>]+>", "");

				value = StringUtil.trim(value);

				matcher = _prosePattern.matcher(value);

				if (!matcher.find() || _isSentence(value) || _isTitle(value)) {
					continue;
				}

				addMessage(
					fileName,
					StringBundler.concat(
						"The value of key \"", key,
						"\" should be a title in APA title case or a complete ",
						"sentence ending in a period"),
					lineNumber);
			}
		}

		return content;
	}

	private boolean _isSentence(String value) {
		String trimmedValue = value.replaceAll("[\"'\\s]+$", "");

		if (!trimmedValue.endsWith(".") && !trimmedValue.endsWith(":") &&
			!trimmedValue.endsWith("!") && !trimmedValue.endsWith("?")) {

			return false;
		}

		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);

			if (Character.isLetter(c)) {
				if ((i == 0) && Character.isLowerCase(c)) {
					return false;
				}

				return true;
			}
		}

		return true;
	}

	private boolean _isTitle(String value) {
		String title = value.replaceAll("\\([^)]*\\)", "");

		title = StringUtil.trim(title);

		if (title.endsWith(".") || title.endsWith("!") || title.endsWith("?")) {
			return false;
		}

		title = title.replaceAll("\\{\\d+\\}", "");

		Matcher matcher = _wordPattern.matcher(title);

		boolean firstWord = true;

		while (matcher.find()) {
			String word = matcher.group();

			if (Character.isLowerCase(word.charAt(0)) &&
				(firstWord || !ArrayUtil.contains(_MINOR_WORDS, word))) {

				return false;
			}

			firstWord = false;
		}

		return true;
	}

	private static final String[] _MINOR_WORDS = {
		"a", "an", "and", "as", "at", "but", "by", "for", "if", "in", "nor",
		"of", "off", "on", "or", "per", "so", "the", "to", "up", "via", "vs",
		"yet"
	};

	private static final Pattern _exemptKeyPattern = Pattern.compile(
		"^(lang\\.|oauth2\\.scope\\.|OAUTH2_)|" +
			"(-definition-term|-fragment|[.-]format)$|\\.units?\\.");
	private static final Pattern _prosePattern = Pattern.compile("[a-z]{3}");
	private static final Pattern _wordPattern = Pattern.compile(
		"[A-Za-z][A-Za-z'’-]*");

}