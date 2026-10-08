/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Objects;

/**
 * @author Alejandro Tardín
 */
public class ListNameCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.PARAMETER_DEF, TokenTypes.VARIABLE_DEF};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		String name = getName(detailAST);

		String prefix = name;

		if (prefix.startsWith("_")) {
			prefix = prefix.substring(1);
		}

		if (!prefix.endsWith("List")) {
			return;
		}

		prefix = prefix.substring(0, prefix.length() - 4);

		if ((prefix.length() < 3) || !prefix.endsWith("s")) {
			return;
		}

		DetailAST typeDetailAST = detailAST.findFirstToken(TokenTypes.TYPE);

		DetailAST firstChildDetailAST = typeDetailAST.getFirstChild();

		if ((firstChildDetailAST.getType() != TokenTypes.IDENT) ||
			!Objects.equals(firstChildDetailAST.getText(), "List")) {

			return;
		}

		DetailAST typeArgumentsDetailAST = typeDetailAST.findFirstToken(
			TokenTypes.TYPE_ARGUMENTS);

		if ((typeArgumentsDetailAST == null) ||
			(typeArgumentsDetailAST.getChildCount(TokenTypes.TYPE_ARGUMENT) !=
				1)) {

			return;
		}

		String typeArgumentName = getName(
			typeArgumentsDetailAST.findFirstToken(TokenTypes.TYPE_ARGUMENT));

		if ((typeArgumentName == null) || typeArgumentName.endsWith("s")) {
			return;
		}

		String expectedName = prefix;

		if (name.startsWith("_")) {
			expectedName = "_" + prefix;
		}

		log(detailAST, _MSG_RENAME_VARIABLE, name, expectedName);
	}

	private static final String _MSG_RENAME_VARIABLE = "variable.rename";

}