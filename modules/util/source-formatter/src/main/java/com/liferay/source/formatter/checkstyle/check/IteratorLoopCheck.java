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
public class IteratorLoopCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.VARIABLE_DEF};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		DetailAST parentDetailAST = detailAST.getParent();

		if ((parentDetailAST.getType() != TokenTypes.SLIST) ||
			!Objects.equals(getTypeName(detailAST, false), "Iterator")) {

			return;
		}

		DetailAST assignDetailAST = detailAST.findFirstToken(TokenTypes.ASSIGN);

		if (assignDetailAST == null) {
			return;
		}

		DetailAST exprDetailAST = assignDetailAST.getFirstChild();

		DetailAST methodCallDetailAST = exprDetailAST.getFirstChild();

		if ((methodCallDetailAST.getType() != TokenTypes.METHOD_CALL) ||
			!Objects.equals(getMethodName(methodCallDetailAST), "iterator")) {

			return;
		}

		String name = getName(detailAST);

		DetailAST loopDetailAST = null;

		for (DetailAST siblingDetailAST = detailAST.getNextSibling();
			 siblingDetailAST != null;
			 siblingDetailAST = siblingDetailAST.getNextSibling()) {

			List<DetailAST> identDetailASTs = _getIdentDetailASTs(
				siblingDetailAST, name);

			if (loopDetailAST != null) {
				if (!identDetailASTs.isEmpty()) {
					return;
				}

				continue;
			}

			if (identDetailASTs.isEmpty()) {
				continue;
			}

			if ((siblingDetailAST.getType() != TokenTypes.LITERAL_WHILE) ||
				!_isLoop(identDetailASTs, siblingDetailAST)) {

				return;
			}

			loopDetailAST = siblingDetailAST;
		}

		if (loopDetailAST != null) {
			log(loopDetailAST, _MSG_USE_ENHANCED_FOR_LOOP, name);
		}
	}

	private List<DetailAST> _getIdentDetailASTs(
		DetailAST detailAST, String name) {

		List<DetailAST> identDetailASTs = new ArrayList<>();

		if ((detailAST.getType() == TokenTypes.IDENT) &&
			name.equals(detailAST.getText())) {

			identDetailASTs.add(detailAST);
		}

		for (DetailAST identDetailAST :
				getAllChildTokens(detailAST, true, TokenTypes.IDENT)) {

			if (name.equals(identDetailAST.getText())) {
				identDetailASTs.add(identDetailAST);
			}
		}

		return identDetailASTs;
	}

	private boolean _isCall(DetailAST identDetailAST, String methodName) {
		DetailAST parentDetailAST = identDetailAST.getParent();

		if ((parentDetailAST.getType() != TokenTypes.DOT) ||
			(parentDetailAST.getFirstChild() != identDetailAST)) {

			return false;
		}

		DetailAST lastChildDetailAST = parentDetailAST.getLastChild();

		if (!methodName.equals(lastChildDetailAST.getText())) {
			return false;
		}

		DetailAST methodCallDetailAST = parentDetailAST.getParent();

		if (methodCallDetailAST.getType() != TokenTypes.METHOD_CALL) {
			return false;
		}

		DetailAST elistDetailAST = methodCallDetailAST.findFirstToken(
			TokenTypes.ELIST);

		if (elistDetailAST.getChildCount() != 0) {
			return false;
		}

		return true;
	}

	private boolean _isLoop(
		List<DetailAST> identDetailASTs, DetailAST literalWhileDetailAST) {

		if (identDetailASTs.size() != 2) {
			return false;
		}

		DetailAST conditionIdentDetailAST = identDetailASTs.get(0);
		DetailAST nextIdentDetailAST = identDetailASTs.get(1);

		DetailAST exprDetailAST = literalWhileDetailAST.findFirstToken(
			TokenTypes.EXPR);

		DetailAST methodCallDetailAST = exprDetailAST.getFirstChild();

		if ((methodCallDetailAST.getType() != TokenTypes.METHOD_CALL) ||
			!_isCall(conditionIdentDetailAST, "hasNext") ||
			!_isCall(nextIdentDetailAST, "next")) {

			return false;
		}

		DetailAST dotDetailAST = conditionIdentDetailAST.getParent();

		if (dotDetailAST.getParent() != methodCallDetailAST) {
			return false;
		}

		return true;
	}

	private static final String _MSG_USE_ENHANCED_FOR_LOOP =
		"enhanced.for.loop.use";

}