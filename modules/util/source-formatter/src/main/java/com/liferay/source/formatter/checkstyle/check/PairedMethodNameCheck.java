/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.liferay.petra.string.StringBundler;
import com.liferay.source.formatter.check.util.JavaSourceUtil;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alejandro Tardín
 */
public class PairedMethodNameCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.OBJBLOCK};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		String className = JavaSourceUtil.getClassName(getAbsolutePath());

		if (className.endsWith("ServiceUtil") ||
			className.endsWith("ServiceWrapper")) {

			return;
		}

		Map<String, List<String>> parameterTypeNamesMap = new LinkedHashMap<>();

		for (DetailAST methodDefinitionDetailAST :
				getAllChildTokens(detailAST, false, TokenTypes.METHOD_DEF)) {

			parameterTypeNamesMap.put(
				getName(methodDefinitionDetailAST),
				_getParameterTypeNames(methodDefinitionDetailAST));
		}

		for (DetailAST methodDefinitionDetailAST :
				getAllChildTokens(detailAST, false, TokenTypes.METHOD_DEF)) {

			if (AnnotationUtil.containsAnnotation(
					methodDefinitionDetailAST, "Override")) {

				continue;
			}

			String methodName = getName(methodDefinitionDetailAST);

			Matcher matcher = _countMethodNamePattern.matcher(methodName);

			if (!matcher.matches()) {
				continue;
			}

			String stem = matcher.group(1);
			String qualifier = matcher.group(2);

			List<String> parameterTypeNames = parameterTypeNamesMap.get(
				methodName);

			if (parameterTypeNames.equals(
					parameterTypeNamesMap.get("get" + stem + qualifier))) {

				continue;
			}

			for (Map.Entry<String, List<String>> entry :
					parameterTypeNamesMap.entrySet()) {

				String siblingMethodName = entry.getKey();

				if (!siblingMethodName.startsWith("get" + stem + "By") ||
					!parameterTypeNames.equals(entry.getValue())) {

					continue;
				}

				String expectedMethodName = StringBundler.concat(
					"get", stem, "Count",
					siblingMethodName.substring(stem.length() + 3));

				if (!expectedMethodName.equals(methodName)) {
					log(
						methodDefinitionDetailAST, _MSG_RENAME_METHOD,
						methodName, expectedMethodName, siblingMethodName);
				}

				break;
			}
		}
	}

	private List<String> _getParameterTypeNames(
		DetailAST methodDefinitionDetailAST) {

		List<String> parameterTypeNames = new ArrayList<>();

		for (DetailAST parameterDefinitionDetailAST :
				getParameterDefs(methodDefinitionDetailAST)) {

			String parameterName = getName(parameterDefinitionDetailAST);

			if (parameterName.equals("end") || parameterName.equals("start") ||
				parameterName.equals("orderByComparator")) {

				continue;
			}

			parameterTypeNames.add(
				getTypeName(parameterDefinitionDetailAST, true));
		}

		return parameterTypeNames;
	}

	private static final String _MSG_RENAME_METHOD = "method.rename";

	private static final Pattern _countMethodNamePattern = Pattern.compile(
		"get(\\w+?)Count(\\w*)");

}