/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.liferay.portal.kernel.util.StringUtil;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Objects;

/**
 * @author Alejandro Tardín
 */
public class LongLiteralSuffixCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.NUM_LONG};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		if (!_isIntValue(detailAST.getText()) ||
			!_isPrimitiveLongSlot(detailAST)) {

			return;
		}

		log(detailAST, _MSG_REMOVE_SUFFIX, detailAST.getText());
	}

	private boolean _isIntValue(String text) {
		text = StringUtil.toLowerCase(text);

		text = StringUtil.removeChar(text, '_');

		text = text.substring(0, text.length() - 1);

		try {
			long value;

			if (text.startsWith("0x")) {
				value = Long.parseLong(text.substring(2), 16);
			}
			else if (text.startsWith("0b")) {
				value = Long.parseLong(text.substring(2), 2);
			}
			else if ((text.length() > 1) && text.startsWith("0")) {
				value = Long.parseLong(text.substring(1), 8);
			}
			else {
				value = Long.parseLong(text);
			}

			if (value <= Integer.MAX_VALUE) {
				return true;
			}
		}
		catch (NumberFormatException numberFormatException) {
		}

		return false;
	}

	private boolean _isPrimitiveLongSlot(DetailAST detailAST) {
		DetailAST exprDetailAST = detailAST.getParent();

		if (exprDetailAST.getType() == TokenTypes.ASSIGN) {
			DetailAST identDetailAST = exprDetailAST.getFirstChild();

			if (identDetailAST.getType() != TokenTypes.IDENT) {
				return false;
			}

			return Objects.equals(
				getVariableTypeName(detailAST, identDetailAST.getText(), false),
				"long");
		}

		if (exprDetailAST.getType() != TokenTypes.EXPR) {
			return false;
		}

		DetailAST parentDetailAST = exprDetailAST.getParent();

		if (parentDetailAST.getType() == TokenTypes.ASSIGN) {
			DetailAST variableDefinitionDetailAST = parentDetailAST.getParent();

			if (variableDefinitionDetailAST.getType() !=
					TokenTypes.VARIABLE_DEF) {

				return false;
			}

			return _isPrimitiveLongType(
				variableDefinitionDetailAST.findFirstToken(TokenTypes.TYPE));
		}

		if (parentDetailAST.getType() == TokenTypes.LITERAL_RETURN) {
			DetailAST methodDefinitionDetailAST = getParentWithTokenType(
				parentDetailAST, TokenTypes.LAMBDA, TokenTypes.METHOD_DEF);

			if ((methodDefinitionDetailAST == null) ||
				(methodDefinitionDetailAST.getType() !=
					TokenTypes.METHOD_DEF)) {

				return false;
			}

			return _isPrimitiveLongType(
				methodDefinitionDetailAST.findFirstToken(TokenTypes.TYPE));
		}

		return false;
	}

	private boolean _isPrimitiveLongType(DetailAST typeDetailAST) {
		DetailAST firstChildDetailAST = typeDetailAST.getFirstChild();

		if ((firstChildDetailAST.getType() == TokenTypes.LITERAL_LONG) &&
			(firstChildDetailAST.getNextSibling() == null)) {

			return true;
		}

		return false;
	}

	private static final String _MSG_REMOVE_SUFFIX = "suffix.remove";

}