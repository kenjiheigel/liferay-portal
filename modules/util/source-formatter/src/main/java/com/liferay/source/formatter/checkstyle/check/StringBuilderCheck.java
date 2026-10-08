/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * @author Alejandro Tardín
 */
public class StringBuilderCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.LITERAL_NEW};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		if (isExcludedPath(RUN_OUTSIDE_PORTAL_EXCLUDES)) {
			return;
		}

		FullIdent fullIdent = FullIdent.createFullIdent(
			detailAST.getFirstChild());

		String name = fullIdent.getText();

		if (name.equals("java.lang.StringBuilder") ||
			name.equals("StringBuilder")) {

			log(detailAST, _MSG_USE_STRING_BUNDLER);
		}
	}

	private static final String _MSG_USE_STRING_BUNDLER = "string.bundler.use";

}