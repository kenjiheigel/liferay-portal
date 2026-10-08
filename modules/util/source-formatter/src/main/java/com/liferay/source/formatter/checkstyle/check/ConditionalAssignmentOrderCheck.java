/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.liferay.portal.kernel.util.NaturalOrderStringComparator;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Alejandro Tardín
 */
public class ConditionalAssignmentOrderCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		DetailAST slistDetailAST = detailAST.findFirstToken(TokenTypes.SLIST);

		if (slistDetailAST == null) {
			return;
		}

		List<String> parameterNames = getParameterNames(detailAST);

		if (parameterNames.isEmpty()) {
			return;
		}

		List<Assignment> assignments = new ArrayList<>();

		for (DetailAST childDetailAST = slistDetailAST.getFirstChild();
			 childDetailAST != null;
			 childDetailAST = childDetailAST.getNextSibling()) {

			if (childDetailAST.getType() == TokenTypes.SEMI) {
				continue;
			}

			Assignment assignment = _getAssignment(
				childDetailAST, parameterNames);

			if (assignment != null) {
				assignments.add(assignment);

				continue;
			}

			_checkOrder(assignments);

			assignments.clear();
		}

		_checkOrder(assignments);
	}

	private void _checkOrder(List<Assignment> assignments) {
		NaturalOrderStringComparator comparator =
			new NaturalOrderStringComparator();
		Set<String> fieldNames = new HashSet<>();

		for (int i = 0; i < assignments.size(); i++) {
			Assignment assignment = assignments.get(i);

			if ((i > 0) && !assignment.reads(fieldNames)) {
				Assignment previousAssignment = assignments.get(i - 1);

				int compare = comparator.compare(
					previousAssignment.getFieldName(),
					assignment.getFieldName());

				if ((assignment.isConditional() ||
					 previousAssignment.isConditional()) &&
					(compare > 0)) {

					log(
						assignment.getDetailAST(),
						_MSG_ASSIGNMENT_ORDER_INCORRECT,
						assignment.getFieldName(),
						previousAssignment.getFieldName());
				}
			}

			fieldNames.add(assignment.getFieldName());
		}
	}

	private Assignment _getAssignment(
		DetailAST detailAST, List<String> parameterNames) {

		if (detailAST.getType() == TokenTypes.EXPR) {
			String fieldName = _getFieldName(detailAST, parameterNames);

			if (fieldName == null) {
				return null;
			}

			return new Assignment(false, detailAST, fieldName);
		}

		if ((detailAST.getType() != TokenTypes.LITERAL_IF) ||
			!_referencesParameter(
				detailAST.findFirstToken(TokenTypes.EXPR), parameterNames)) {

			return null;
		}

		String fieldName = null;

		for (DetailAST branchDetailAST : _getBranchDetailASTs(detailAST)) {
			List<DetailAST> statementDetailASTs = new ArrayList<>();

			if (branchDetailAST.getType() == TokenTypes.SLIST) {
				for (DetailAST childDetailAST = branchDetailAST.getFirstChild();
					 childDetailAST != null;
					 childDetailAST = childDetailAST.getNextSibling()) {

					if ((childDetailAST.getType() != TokenTypes.SEMI) &&
						(childDetailAST.getType() != TokenTypes.RCURLY)) {

						statementDetailASTs.add(childDetailAST);
					}
				}
			}
			else {
				statementDetailASTs.add(branchDetailAST);
			}

			for (DetailAST statementDetailAST : statementDetailASTs) {
				String branchFieldName = _getFieldName(
					statementDetailAST, null);

				if ((branchFieldName == null) ||
					((fieldName != null) &&
					 !fieldName.equals(branchFieldName))) {

					return null;
				}

				fieldName = branchFieldName;
			}
		}

		if (fieldName == null) {
			return null;
		}

		return new Assignment(true, detailAST, fieldName);
	}

	private List<DetailAST> _getBranchDetailASTs(DetailAST detailAST) {
		List<DetailAST> branchDetailASTs = new ArrayList<>();

		DetailAST literalIfDetailAST = detailAST;

		while (true) {
			DetailAST rparenDetailAST = literalIfDetailAST.findFirstToken(
				TokenTypes.RPAREN);

			branchDetailASTs.add(rparenDetailAST.getNextSibling());

			DetailAST literalElseDetailAST = literalIfDetailAST.findFirstToken(
				TokenTypes.LITERAL_ELSE);

			if (literalElseDetailAST == null) {
				return branchDetailASTs;
			}

			DetailAST firstChildDetailAST =
				literalElseDetailAST.getFirstChild();

			if (firstChildDetailAST.getType() != TokenTypes.LITERAL_IF) {
				branchDetailASTs.add(firstChildDetailAST);

				return branchDetailASTs;
			}

			literalIfDetailAST = firstChildDetailAST;
		}
	}

	private String _getFieldName(
		DetailAST detailAST, List<String> parameterNames) {

		if (detailAST.getType() != TokenTypes.EXPR) {
			return null;
		}

		DetailAST assignDetailAST = detailAST.getFirstChild();

		if (assignDetailAST.getType() != TokenTypes.ASSIGN) {
			return null;
		}

		DetailAST targetDetailAST = assignDetailAST.getFirstChild();

		String fieldName = null;

		if (targetDetailAST.getType() == TokenTypes.IDENT) {
			String name = targetDetailAST.getText();

			if (name.startsWith("_")) {
				fieldName = name;
			}
		}
		else if (targetDetailAST.getType() == TokenTypes.DOT) {
			DetailAST firstChildDetailAST = targetDetailAST.getFirstChild();

			if (firstChildDetailAST.getType() == TokenTypes.LITERAL_THIS) {
				DetailAST lastChildDetailAST = targetDetailAST.getLastChild();

				fieldName = lastChildDetailAST.getText();
			}
		}

		if ((fieldName == null) || (parameterNames == null)) {
			return fieldName;
		}

		DetailAST valueDetailAST = targetDetailAST.getNextSibling();

		if ((valueDetailAST == null) ||
			(valueDetailAST.getType() != TokenTypes.DOT)) {

			return null;
		}

		DetailAST firstChildDetailAST = valueDetailAST.getFirstChild();

		if ((firstChildDetailAST.getType() == TokenTypes.IDENT) &&
			parameterNames.contains(firstChildDetailAST.getText())) {

			return fieldName;
		}

		return null;
	}

	private boolean _referencesParameter(
		DetailAST detailAST, List<String> parameterNames) {

		for (DetailAST identDetailAST :
				getAllChildTokens(detailAST, true, TokenTypes.IDENT)) {

			if (parameterNames.contains(identDetailAST.getText())) {
				return true;
			}
		}

		return false;
	}

	private static final String _MSG_ASSIGNMENT_ORDER_INCORRECT =
		"assignment.order.incorrect";

	private class Assignment {

		public Assignment(
			boolean conditional, DetailAST detailAST, String fieldName) {

			_conditional = conditional;
			_detailAST = detailAST;
			_fieldName = fieldName;
		}

		public DetailAST getDetailAST() {
			return _detailAST;
		}

		public String getFieldName() {
			return _fieldName;
		}

		public boolean isConditional() {
			return _conditional;
		}

		public boolean reads(Set<String> fieldNames) {
			if (!_conditional) {
				return false;
			}

			for (DetailAST identDetailAST :
					getAllChildTokens(_detailAST, true, TokenTypes.IDENT)) {

				if (fieldNames.contains(identDetailAST.getText()) &&
					!_isAssignmentTarget(identDetailAST) &&
					!_isMemberOfOtherObject(identDetailAST)) {

					return true;
				}
			}

			return false;
		}

		private boolean _isAssignmentTarget(DetailAST identDetailAST) {
			DetailAST parentDetailAST = identDetailAST.getParent();

			if ((parentDetailAST.getType() == TokenTypes.ASSIGN) &&
				(parentDetailAST.getFirstChild() == identDetailAST)) {

				return true;
			}

			return false;
		}

		private boolean _isMemberOfOtherObject(DetailAST identDetailAST) {
			DetailAST parentDetailAST = identDetailAST.getParent();

			if (parentDetailAST.getType() != TokenTypes.DOT) {
				return false;
			}

			DetailAST firstChildDetailAST = parentDetailAST.getFirstChild();

			if ((firstChildDetailAST == identDetailAST) ||
				(firstChildDetailAST.getType() == TokenTypes.LITERAL_THIS)) {

				return false;
			}

			return true;
		}

		private final boolean _conditional;
		private final DetailAST _detailAST;
		private final String _fieldName;

	}

}