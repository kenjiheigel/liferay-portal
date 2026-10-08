/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.List;

/**
 * @author Hugo Huijser
 */
public abstract class BaseMessageCheck extends BaseCheck {

	protected void checkMessage(int lineNo, String literalStringValue) {
		if (Validator.isNull(literalStringValue) ||
			literalStringValue.endsWith(StringPool.TRIPLE_PERIOD)) {

			return;
		}

		String[] parts = literalStringValue.split("\\S\\. [A-Z0-9]");

		if ((parts.length == 1) ^
			!literalStringValue.endsWith(StringPool.PERIOD)) {

			log(lineNo, _MSG_INCORRECT_MESSAGE);
		}
	}

	protected String getLiteralStringValue(DetailAST exprDetailAST) {
		DetailAST firstChildDetailAST = exprDetailAST.getFirstChild();

		if (firstChildDetailAST.getType() == TokenTypes.STRING_LITERAL) {
			String s = firstChildDetailAST.getText();

			return s.substring(1, s.length() - 1);
		}

		StringBundler sb = new StringBundler();

		if (firstChildDetailAST.getType() == TokenTypes.PLUS) {
			if (!_appendOperand(firstChildDetailAST, sb)) {
				return null;
			}

			return _getLiteralStringValue(sb);
		}

		if (firstChildDetailAST.getType() != TokenTypes.METHOD_CALL) {
			return null;
		}

		String methodName = getMethodName(firstChildDetailAST);

		if (!methodName.equals("concat")) {
			return null;
		}

		DetailAST elistDetailAST = firstChildDetailAST.findFirstToken(
			TokenTypes.ELIST);

		List<DetailAST> exprDetailASTs = getAllChildTokens(
			elistDetailAST, false, TokenTypes.EXPR);

		for (DetailAST curExprDetailAST : exprDetailASTs) {
			if (!_appendOperand(curExprDetailAST.getFirstChild(), sb)) {
				return null;
			}
		}

		return _getLiteralStringValue(sb);
	}

	private boolean _appendOperand(DetailAST detailAST, StringBundler sb) {
		if ((detailAST.getType() == TokenTypes.LPAREN) ||
			(detailAST.getType() == TokenTypes.RPAREN)) {

			return true;
		}

		if (detailAST.getType() == TokenTypes.STRING_LITERAL) {
			String s = detailAST.getText();

			sb.append(s.substring(1, s.length() - 1));

			return true;
		}

		if (detailAST.getType() == TokenTypes.PLUS) {
			for (DetailAST childDetailAST = detailAST.getFirstChild();
				 childDetailAST != null;
				 childDetailAST = childDetailAST.getNextSibling()) {

				if (!_appendOperand(childDetailAST, sb)) {
					return false;
				}
			}

			return true;
		}

		if (!isAttributeValue(_CHECK_CONCATENATED_MESSAGES_KEY)) {
			return false;
		}

		sb.append(_PLACEHOLDER);

		return true;
	}

	private String _getLiteralStringValue(StringBundler sb) {
		String s = sb.toString();

		if (Validator.isNull(StringUtil.removeSubstring(s, _PLACEHOLDER))) {
			return null;
		}

		return s;
	}

	private static final String _CHECK_CONCATENATED_MESSAGES_KEY =
		"checkConcatenatedMessages";

	private static final String _MSG_INCORRECT_MESSAGE = "message.incorrect";

	private static final String _PLACEHOLDER = "X";

}