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
public class AssertMessageCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.METHOD_CALL};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		String className = getClassOrVariableName(detailAST);
		String methodName = getMethodName(detailAST);

		if (!Objects.equals(className, "Assert") ||
			(!methodName.equals("assertFalse") &&
			 !methodName.equals("assertTrue"))) {

			return;
		}

		DetailAST elistDetailAST = detailAST.findFirstToken(TokenTypes.ELIST);

		if (elistDetailAST.getChildCount(TokenTypes.EXPR) == 2) {
			log(detailAST, _MSG_MESSAGE_REMOVE, methodName);
		}
	}

	private static final String _MSG_MESSAGE_REMOVE = "message.remove";

}