/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.NaturalOrderStringComparator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alejandro Tardín
 */
public class ParameterOrderCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		if (AnnotationUtil.containsAnnotation(detailAST, "Override")) {
			return;
		}

		List<DetailAST> parameterDefinitionDetailASTs = new ArrayList<>();

		for (DetailAST parameterDefinitionDetailAST :
				getParameterDefs(detailAST)) {

			DetailAST ellipsisDetailAST =
				parameterDefinitionDetailAST.findFirstToken(
					TokenTypes.ELLIPSIS);

			if (ellipsisDetailAST != null) {
				break;
			}

			if (!ArrayUtil.contains(
					_PAGINATION_PARAMETER_NAMES,
					getName(parameterDefinitionDetailAST))) {

				parameterDefinitionDetailASTs.add(parameterDefinitionDetailAST);
			}
		}

		NaturalOrderStringComparator comparator =
			new NaturalOrderStringComparator();

		for (int i = 1; i < parameterDefinitionDetailASTs.size(); i++) {
			DetailAST parameterDefinitionDetailAST =
				parameterDefinitionDetailASTs.get(i);

			String name1 = getName(parameterDefinitionDetailASTs.get(i - 1));
			String name2 = getName(parameterDefinitionDetailAST);

			if (comparator.compare(name1, name2) > 0) {
				log(
					parameterDefinitionDetailAST,
					_MSG_PARAMETER_ORDER_INCORRECT, name2, name1);

				return;
			}
		}
	}

	private static final String _MSG_PARAMETER_ORDER_INCORRECT =
		"parameter.order.incorrect";

	private static final String[] _PAGINATION_PARAMETER_NAMES = {
		"end", "orderByComparator", "start"
	};

}