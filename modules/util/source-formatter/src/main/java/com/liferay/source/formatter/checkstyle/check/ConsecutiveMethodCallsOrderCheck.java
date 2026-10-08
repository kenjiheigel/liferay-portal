/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.NaturalOrderStringComparator;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.source.formatter.check.util.JavaSourceUtil;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Objects;

/**
 * @author Alejandro Tardín
 */
public class ConsecutiveMethodCallsOrderCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.SLIST};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		if (hasParentWithTokenType(detailAST, TokenTypes.INSTANCE_INIT)) {
			return;
		}

		String className = JavaSourceUtil.getClassName(getAbsolutePath());

		boolean testClass = className.endsWith("Test");

		DetailAST previousMethodCallDetailAST = null;

		for (DetailAST childDetailAST = detailAST.getFirstChild();
			 childDetailAST != null;
			 childDetailAST = childDetailAST.getNextSibling()) {

			if (childDetailAST.getType() == TokenTypes.SEMI) {
				continue;
			}

			DetailAST methodCallDetailAST = _getMethodCallDetailAST(
				childDetailAST);

			if ((methodCallDetailAST != null) &&
				(previousMethodCallDetailAST != null) &&
				((getStartLineNumber(childDetailAST) - 1) == getEndLineNumber(
					previousMethodCallDetailAST))) {

				if (testClass) {
					_checkHelperCalls(
						previousMethodCallDetailAST, methodCallDetailAST);
				}

				_checkSetterCalls(
					previousMethodCallDetailAST, methodCallDetailAST);
			}

			previousMethodCallDetailAST = methodCallDetailAST;
		}
	}

	private void _checkHelperCalls(
		DetailAST methodCallDetailAST1, DetailAST methodCallDetailAST2) {

		if ((methodCallDetailAST1.findFirstToken(TokenTypes.DOT) != null) ||
			(methodCallDetailAST2.findFirstToken(TokenTypes.DOT) != null)) {

			return;
		}

		String methodName = getMethodName(methodCallDetailAST1);

		if (!methodName.startsWith("_") ||
			!methodName.equals(getMethodName(methodCallDetailAST2))) {

			return;
		}

		String text1 = _getFlattenedText(methodCallDetailAST1);
		String text2 = _getFlattenedText(methodCallDetailAST2);

		if (text1.compareTo(text2) > 0) {
			log(
				methodCallDetailAST2, _MSG_HELPER_CALL_ORDER_INCORRECT,
				methodName, getStartLineNumber(methodCallDetailAST1));
		}
	}

	private void _checkSetterCalls(
		DetailAST methodCallDetailAST1, DetailAST methodCallDetailAST2) {

		String variableName = _getSetterVariableName(methodCallDetailAST1);

		if ((variableName == null) ||
			!Objects.equals(
				variableName, _getSetterVariableName(methodCallDetailAST2))) {

			return;
		}

		String variableTypeName = getVariableTypeName(
			methodCallDetailAST1, variableName, false, false, true);

		if (variableTypeName.contains(".dto.") ||
			variableTypeName.contains(".model.")) {

			return;
		}

		String methodName1 = getMethodName(methodCallDetailAST1);
		String methodName2 = getMethodName(methodCallDetailAST2);

		NaturalOrderStringComparator comparator =
			new NaturalOrderStringComparator();

		if (comparator.compare(methodName1, methodName2) > 0) {
			log(
				methodCallDetailAST2, _MSG_METHOD_CALL_ORDER_INCORRECT,
				methodName2, methodName1);
		}
	}

	private String _getFlattenedText(DetailAST methodCallDetailAST) {
		StringBundler sb = new StringBundler();

		for (int lineNumber = getStartLineNumber(methodCallDetailAST);
			 lineNumber <= getEndLineNumber(methodCallDetailAST);
			 lineNumber++) {

			if (sb.index() > 0) {
				sb.append(" ");
			}

			sb.append(StringUtil.trim(getLine(lineNumber - 1)));
		}

		String flattenedText = sb.toString();

		flattenedText = StringUtil.replace(flattenedText, "( ", "(");

		return StringUtil.replace(flattenedText, " )", ")");
	}

	private DetailAST _getMethodCallDetailAST(DetailAST detailAST) {
		if (detailAST.getType() != TokenTypes.EXPR) {
			return null;
		}

		DetailAST firstChildDetailAST = detailAST.getFirstChild();

		if (firstChildDetailAST.getType() != TokenTypes.METHOD_CALL) {
			return null;
		}

		return firstChildDetailAST;
	}

	private String _getSetterVariableName(DetailAST methodCallDetailAST) {
		DetailAST dotDetailAST = methodCallDetailAST.findFirstToken(
			TokenTypes.DOT);

		if (dotDetailAST == null) {
			return null;
		}

		DetailAST firstChildDetailAST = dotDetailAST.getFirstChild();

		if (firstChildDetailAST.getType() != TokenTypes.IDENT) {
			return null;
		}

		String methodName = getMethodName(methodCallDetailAST);

		if (!methodName.matches("set[A-Z].*")) {
			return null;
		}

		return getVariableName(methodCallDetailAST);
	}

	private static final String _MSG_HELPER_CALL_ORDER_INCORRECT =
		"helper.call.order.incorrect";

	private static final String _MSG_METHOD_CALL_ORDER_INCORRECT =
		"method.call.order.incorrect";

}