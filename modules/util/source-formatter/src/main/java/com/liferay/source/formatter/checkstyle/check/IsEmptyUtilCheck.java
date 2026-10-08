/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.liferay.portal.kernel.util.StringUtil;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.List;
import java.util.Objects;

/**
 * @author Alejandro Tardín
 */
public class IsEmptyUtilCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.LAND, TokenTypes.LOR};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		if (isExcludedPath(RUN_OUTSIDE_PORTAL_EXCLUDES)) {
			return;
		}

		boolean negated = false;

		if (detailAST.getType() == TokenTypes.LAND) {
			negated = true;
		}

		int nullCheckTokenType = TokenTypes.EQUAL;

		if (negated) {
			nullCheckTokenType = TokenTypes.NOT_EQUAL;
		}

		String variableName = _getNullCheckVariableName(
			_getFirstOperandDetailAST(detailAST), nullCheckTokenType);

		if (variableName == null) {
			return;
		}

		String variableTypeName = getVariableTypeName(
			detailAST, variableName, false, true, false);

		boolean array = variableTypeName.endsWith("[]");

		String utilName = _getUtilName(array, variableTypeName);

		if ((utilName == null) ||
			(!negated && (array || variableTypeName.equals("List")))) {

			return;
		}

		DetailAST lastChildDetailAST = _getLastOperandDetailAST(detailAST);

		if (array) {
			if (!_isArrayLengthNotZero(lastChildDetailAST, variableName)) {
				return;
			}
		}
		else if (negated) {
			if ((lastChildDetailAST.getType() != TokenTypes.LNOT) ||
				!_isIsEmptyCall(
					lastChildDetailAST.getFirstChild(), variableName)) {

				return;
			}
		}
		else if (!_isIsEmptyCall(lastChildDetailAST, variableName)) {
			return;
		}

		if (negated) {
			log(detailAST, _MSG_USE_IS_NOT_EMPTY, utilName, variableName);
		}
		else {
			log(detailAST, _MSG_USE_IS_EMPTY, utilName, variableName);
		}
	}

	private DetailAST _getFirstOperandDetailAST(DetailAST detailAST) {
		DetailAST childDetailAST = detailAST.getFirstChild();

		while (childDetailAST.getType() == TokenTypes.LPAREN) {
			childDetailAST = childDetailAST.getNextSibling();
		}

		return childDetailAST;
	}

	private DetailAST _getLastOperandDetailAST(DetailAST detailAST) {
		DetailAST childDetailAST = detailAST.getLastChild();

		while (childDetailAST.getType() == TokenTypes.RPAREN) {
			childDetailAST = childDetailAST.getPreviousSibling();
		}

		return childDetailAST;
	}

	private String _getNullCheckVariableName(
		DetailAST detailAST, int tokenType) {

		if (detailAST.getType() != tokenType) {
			return null;
		}

		DetailAST firstChildDetailAST = detailAST.getFirstChild();
		DetailAST lastChildDetailAST = detailAST.getLastChild();

		if ((firstChildDetailAST.getType() != TokenTypes.IDENT) ||
			(lastChildDetailAST.getType() != TokenTypes.LITERAL_NULL)) {

			return null;
		}

		return firstChildDetailAST.getText();
	}

	private String _getUtilName(boolean array, String variableTypeName) {
		if (array) {
			return "ArrayUtil";
		}

		if (variableTypeName.equals("List")) {
			return "ListUtil";
		}

		if (variableTypeName.equals("Map")) {
			return "MapUtil";
		}

		if (variableTypeName.equals("Set")) {
			return "SetUtil";
		}

		return null;
	}

	private boolean _isArrayLengthNotZero(
		DetailAST detailAST, String variableName) {

		if (detailAST.getType() != TokenTypes.NOT_EQUAL) {
			return false;
		}

		DetailAST firstChildDetailAST = detailAST.getFirstChild();

		if (firstChildDetailAST.getType() != TokenTypes.DOT) {
			return false;
		}

		List<String> names = getNames(firstChildDetailAST, false);

		if ((names.size() != 2) ||
			!Objects.equals(names.get(0), variableName) ||
			!Objects.equals(names.get(1), "length")) {

			return false;
		}

		DetailAST lastChildDetailAST = detailAST.getLastChild();

		if ((lastChildDetailAST.getType() != TokenTypes.NUM_INT) ||
			!StringUtil.equals(lastChildDetailAST.getText(), "0")) {

			return false;
		}

		return true;
	}

	private boolean _isIsEmptyCall(DetailAST detailAST, String variableName) {
		if ((detailAST == null) ||
			(detailAST.getType() != TokenTypes.METHOD_CALL)) {

			return false;
		}

		DetailAST dotDetailAST = detailAST.findFirstToken(TokenTypes.DOT);

		if (dotDetailAST == null) {
			return false;
		}

		List<String> names = getNames(dotDetailAST, false);

		if ((names.size() != 2) ||
			!Objects.equals(names.get(0), variableName) ||
			!Objects.equals(names.get(1), "isEmpty")) {

			return false;
		}

		DetailAST elistDetailAST = detailAST.findFirstToken(TokenTypes.ELIST);

		if (elistDetailAST.getChildCount() != 0) {
			return false;
		}

		return true;
	}

	private static final String _MSG_USE_IS_EMPTY = "is.empty.use";

	private static final String _MSG_USE_IS_NOT_EMPTY = "is.not.empty.use";

}