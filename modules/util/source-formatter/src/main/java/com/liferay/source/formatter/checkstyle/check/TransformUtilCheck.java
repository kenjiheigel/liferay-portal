/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Alejandro Tardín
 */
public class TransformUtilCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.VARIABLE_DEF};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		if (isExcludedPath(RUN_OUTSIDE_PORTAL_EXCLUDES)) {
			return;
		}

		DetailAST parentDetailAST = detailAST.getParent();

		if ((parentDetailAST.getType() != TokenTypes.SLIST) ||
			!Objects.equals(getTypeName(detailAST, false), "List") ||
			!isAssignNewArrayList(detailAST)) {

			return;
		}

		DetailAST nextSiblingDetailAST = detailAST.getNextSibling();

		while ((nextSiblingDetailAST != null) &&
			   (nextSiblingDetailAST.getType() == TokenTypes.SEMI)) {

			nextSiblingDetailAST = nextSiblingDetailAST.getNextSibling();
		}

		if ((nextSiblingDetailAST == null) ||
			(nextSiblingDetailAST.getType() != TokenTypes.LITERAL_FOR) ||
			(nextSiblingDetailAST.findFirstToken(TokenTypes.FOR_EACH_CLAUSE) ==
				null)) {

			return;
		}

		String name = getName(detailAST);

		DetailAST forEachClauseDetailAST = nextSiblingDetailAST.findFirstToken(
			TokenTypes.FOR_EACH_CLAUSE);

		DetailAST addMethodCallDetailAST = _getAddMethodCallDetailAST(
			name, _getStatementDetailASTs(nextSiblingDetailAST.getLastChild()));

		if (addMethodCallDetailAST == null) {
			return;
		}

		DetailAST elistDetailAST = addMethodCallDetailAST.findFirstToken(
			TokenTypes.ELIST);

		DetailAST exprDetailAST = elistDetailAST.getFirstChild();

		DetailAST firstChildDetailAST = exprDetailAST.getFirstChild();

		String loopVariableName = getName(
			forEachClauseDetailAST.findFirstToken(TokenTypes.VARIABLE_DEF));

		if ((firstChildDetailAST.getType() == TokenTypes.IDENT) &&
			loopVariableName.equals(firstChildDetailAST.getText())) {

			return;
		}

		log(detailAST, _MSG_USE_TRANSFORM_UTIL, name);
	}

	private DetailAST _getAddMethodCallDetailAST(
		String name, List<DetailAST> statementDetailASTs) {

		if (statementDetailASTs.size() == 2) {
			DetailAST literalIfDetailAST = statementDetailASTs.get(0);

			if ((literalIfDetailAST.getType() != TokenTypes.LITERAL_IF) ||
				(literalIfDetailAST.findFirstToken(TokenTypes.LITERAL_ELSE) !=
					null)) {

				return null;
			}

			List<DetailAST> ifStatementDetailASTs = _getStatementDetailASTs(
				literalIfDetailAST.getLastChild());

			if (ifStatementDetailASTs.size() != 1) {
				return null;
			}

			DetailAST ifStatementDetailAST = ifStatementDetailASTs.get(0);

			if (ifStatementDetailAST.getType() != TokenTypes.LITERAL_CONTINUE) {
				return null;
			}

			statementDetailASTs = statementDetailASTs.subList(1, 2);
		}

		if (statementDetailASTs.size() != 1) {
			return null;
		}

		DetailAST statementDetailAST = statementDetailASTs.get(0);

		if (statementDetailAST.getType() == TokenTypes.LITERAL_IF) {
			if (statementDetailAST.findFirstToken(TokenTypes.LITERAL_ELSE) !=
					null) {

				return null;
			}

			return _getAddMethodCallDetailAST(
				name,
				_getStatementDetailASTs(statementDetailAST.getLastChild()));
		}

		if (statementDetailAST.getType() != TokenTypes.EXPR) {
			return null;
		}

		DetailAST methodCallDetailAST = statementDetailAST.getFirstChild();

		if ((methodCallDetailAST.getType() != TokenTypes.METHOD_CALL) ||
			!Objects.equals(getMethodName(methodCallDetailAST), "add") ||
			!Objects.equals(getVariableName(methodCallDetailAST), name)) {

			return null;
		}

		DetailAST elistDetailAST = methodCallDetailAST.findFirstToken(
			TokenTypes.ELIST);

		if (elistDetailAST.getChildCount(TokenTypes.EXPR) != 1) {
			return null;
		}

		return methodCallDetailAST;
	}

	private List<DetailAST> _getStatementDetailASTs(DetailAST detailAST) {
		List<DetailAST> statementDetailASTs = new ArrayList<>();

		if (detailAST.getType() != TokenTypes.SLIST) {
			statementDetailASTs.add(detailAST);

			return statementDetailASTs;
		}

		for (DetailAST childDetailAST = detailAST.getFirstChild();
			 childDetailAST != null;
			 childDetailAST = childDetailAST.getNextSibling()) {

			if ((childDetailAST.getType() != TokenTypes.RCURLY) &&
				(childDetailAST.getType() != TokenTypes.SEMI)) {

				statementDetailASTs.add(childDetailAST);
			}
		}

		return statementDetailASTs;
	}

	private static final String _MSG_USE_TRANSFORM_UTIL = "transform.util.use";

}