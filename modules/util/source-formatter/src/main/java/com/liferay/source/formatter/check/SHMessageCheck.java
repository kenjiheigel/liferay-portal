/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alejandro Tardín
 */
public class SHMessageCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
		String fileName, String absolutePath, String content) {

		List<String> messageFunctionNames = getAttributeValues(
			_MESSAGE_FUNCTION_NAMES_KEY, absolutePath);

		String[] lines = StringUtil.splitLines(content);

		for (int i = 0; i < lines.length; i++) {
			Matcher matcher = _messagePattern.matcher(
				StringUtil.trim(lines[i]));

			if (!matcher.matches() ||
				!messageFunctionNames.contains(matcher.group(1))) {

				continue;
			}

			String message = matcher.group(2);

			if (message.endsWith(StringPool.TRIPLE_PERIOD)) {
				continue;
			}

			String[] parts = message.split("\\S\\. [A-Z0-9]");

			if ((parts.length == 1) ^ !message.endsWith(StringPool.PERIOD)) {
				addMessage(
					fileName,
					StringBundler.concat(
						"Write the message as a phrase without a final ",
						"period, or as two or more sentences that each end ",
						"with a period"),
					i + 1);
			}
		}

		return content;
	}

	private static final String _MESSAGE_FUNCTION_NAMES_KEY =
		"messageFunctionNames";

	private static final Pattern _messagePattern = Pattern.compile(
		"([\\w-]+)\\s+\"((?:[^\"\\\\]|\\\\.)*)\".*");

}