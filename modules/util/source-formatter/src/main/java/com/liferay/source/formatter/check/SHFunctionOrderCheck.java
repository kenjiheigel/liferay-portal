/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alejandro Tardín
 */
public class SHFunctionOrderCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
		String fileName, String absolutePath, String content) {

		List<String> functionNames = new ArrayList<>();
		List<Integer> lineNumbers = new ArrayList<>();

		String[] lines = StringUtil.splitLines(content);

		for (int i = 0; i < lines.length; i++) {
			Matcher matcher = _functionPattern.matcher(lines[i]);

			if (matcher.find()) {
				String functionName = matcher.group(1);

				if (functionName == null) {
					functionName = matcher.group(2);
				}

				functionNames.add(functionName);
				lineNumbers.add(i + 1);
			}
		}

		Comparator<String> comparator = Comparator.comparing(
			functionName -> functionName.startsWith("_"));

		comparator = comparator.thenComparing(Comparator.naturalOrder());

		for (int i = 1; i < functionNames.size(); i++) {
			String functionName1 = functionNames.get(i - 1);
			String functionName2 = functionNames.get(i);

			if (comparator.compare(functionName1, functionName2) > 0) {
				addMessage(
					fileName,
					StringBundler.concat(
						"Function \"", functionName2,
						"\" should come before function \"", functionName1,
						"\", public functions come first, then private ones, ",
						"each sorted alphabetically"),
					lineNumbers.get(i - 1));

				return content;
			}
		}

		return content;
	}

	private static final Pattern _functionPattern = Pattern.compile(
		"^(?:function\\s+([\\w-]+)|([\\w-]+)\\s*\\(\\s*\\))\\s*\\{?\\s*$");

}