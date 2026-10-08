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
public class IndexVariableNameCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		List<DetailAST> variableDefinitionDetailASTs = getAllChildTokens(
			detailAST, true, TokenTypes.VARIABLE_DEF);

		List<DetailAST> indexVariableDefinitionDetailASTs = new ArrayList<>();

		for (DetailAST variableDefinitionDetailAST :
				variableDefinitionDetailASTs) {

			if (Objects.equals(getName(variableDefinitionDetailAST), "index")) {
				return;
			}

			if (_isIndexVariableDefinition(variableDefinitionDetailAST)) {
				indexVariableDefinitionDetailASTs.add(
					variableDefinitionDetailAST);
			}
		}

		if (indexVariableDefinitionDetailASTs.size() == 1) {
			DetailAST variableDefinitionDetailAST =
				indexVariableDefinitionDetailASTs.get(0);

			String name = getName(variableDefinitionDetailAST);

			if (name.endsWith("Index")) {
				log(variableDefinitionDetailAST, _MSG_RENAME_VARIABLE, name);
			}

			return;
		}

		for (int i = 1; i < indexVariableDefinitionDetailASTs.size(); i++) {
			DetailAST variableDefinitionDetailAST1 =
				indexVariableDefinitionDetailASTs.get(i - 1);
			DetailAST variableDefinitionDetailAST2 =
				indexVariableDefinitionDetailASTs.get(i);

			String name1 = getName(variableDefinitionDetailAST1);
			String name2 = getName(variableDefinitionDetailAST2);

			if (!name1.endsWith("Index") || !name2.endsWith("Index") ||
				!_references(variableDefinitionDetailAST2, name1) ||
				_isReferencedAfter(
					detailAST, getEndLineNumber(variableDefinitionDetailAST2),
					name1)) {

				continue;
			}

			log(
				variableDefinitionDetailAST2, _MSG_REUSE_VARIABLE, name1,
				name2);
		}
	}

	private boolean _isIndexVariableDefinition(
		DetailAST variableDefinitionDetailAST) {

		DetailAST typeDetailAST = variableDefinitionDetailAST.findFirstToken(
			TokenTypes.TYPE);

		DetailAST firstChildDetailAST = typeDetailAST.getFirstChild();

		if (firstChildDetailAST.getType() != TokenTypes.LITERAL_INT) {
			return false;
		}

		DetailAST assignDetailAST = variableDefinitionDetailAST.findFirstToken(
			TokenTypes.ASSIGN);

		if (assignDetailAST == null) {
			return false;
		}

		DetailAST exprDetailAST = assignDetailAST.getFirstChild();

		DetailAST methodCallDetailAST = exprDetailAST.getFirstChild();

		if (methodCallDetailAST.getType() != TokenTypes.METHOD_CALL) {
			return false;
		}

		String methodName = getMethodName(methodCallDetailAST);

		if (methodName.equals("indexOf") || methodName.equals("lastIndexOf")) {
			return true;
		}

		return false;
	}

	private boolean _isReferencedAfter(
		DetailAST detailAST, int lineNumber, String name) {

		for (DetailAST identDetailAST :
				getAllChildTokens(detailAST, true, TokenTypes.IDENT)) {

			if (name.equals(identDetailAST.getText()) &&
				(identDetailAST.getLineNo() > lineNumber)) {

				return true;
			}
		}

		return false;
	}

	private boolean _references(DetailAST detailAST, String name) {
		for (DetailAST identDetailAST :
				getAllChildTokens(detailAST, true, TokenTypes.IDENT)) {

			if (name.equals(identDetailAST.getText())) {
				return true;
			}
		}

		return false;
	}

	private static final String _MSG_RENAME_VARIABLE = "variable.rename";

	private static final String _MSG_REUSE_VARIABLE = "variable.reuse";

}