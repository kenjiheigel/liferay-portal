/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.checkstyle.check;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Alejandro Tardín
 */
public class OverrideAnnotationCheck extends BaseCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] {TokenTypes.CLASS_DEF, TokenTypes.LITERAL_NEW};
	}

	@Override
	protected void doVisitToken(DetailAST detailAST) {
		DetailAST objBlockDetailAST = detailAST.findFirstToken(
			TokenTypes.OBJBLOCK);

		if (objBlockDetailAST == null) {
			return;
		}

		List<Class<?>> superClasses = new ArrayList<>();

		if (detailAST.getType() == TokenTypes.CLASS_DEF) {
			superClasses.add(Object.class);

			_addSuperClasses(
				detailAST.findFirstToken(TokenTypes.EXTENDS_CLAUSE), detailAST,
				superClasses);
			_addSuperClasses(
				detailAST.findFirstToken(TokenTypes.IMPLEMENTS_CLAUSE),
				detailAST, superClasses);
		}
		else {
			Class<?> clazz = _getClass(
				detailAST,
				FullIdent.createFullIdent(detailAST.getFirstChild()));

			if (clazz == null) {
				return;
			}

			superClasses.add(clazz);
		}

		for (DetailAST methodDefinitionDetailAST :
				getAllChildTokens(
					objBlockDetailAST, false, TokenTypes.METHOD_DEF)) {

			if (AnnotationUtil.containsAnnotation(
					methodDefinitionDetailAST, "Override")) {

				continue;
			}

			DetailAST modifiersDetailAST =
				methodDefinitionDetailAST.findFirstToken(TokenTypes.MODIFIERS);

			DetailAST literalPrivateDetailAST =
				modifiersDetailAST.findFirstToken(TokenTypes.LITERAL_PRIVATE);
			DetailAST literalStaticDetailAST =
				modifiersDetailAST.findFirstToken(TokenTypes.LITERAL_STATIC);

			if ((literalPrivateDetailAST != null) ||
				(literalStaticDetailAST != null)) {

				continue;
			}

			String methodName = getName(methodDefinitionDetailAST);

			List<String> parameterTypeNames = new ArrayList<>();

			for (DetailAST parameterDefinitionDetailAST :
					getParameterDefs(methodDefinitionDetailAST)) {

				String parameterTypeName = getTypeName(
					parameterDefinitionDetailAST, false);

				DetailAST ellipsisDetailAST =
					parameterDefinitionDetailAST.findFirstToken(
						TokenTypes.ELLIPSIS);

				if (ellipsisDetailAST != null) {
					parameterTypeName += "[]";
				}

				parameterTypeNames.add(parameterTypeName);
			}

			if (_hasSuperMethod(methodName, parameterTypeNames, superClasses)) {
				log(
					methodDefinitionDetailAST, _MSG_ADD_OVERRIDE_ANNOTATION,
					methodName);
			}
		}
	}

	private void _addSuperClasses(
		DetailAST clauseDetailAST, DetailAST detailAST,
		List<Class<?>> superClasses) {

		if (clauseDetailAST == null) {
			return;
		}

		for (DetailAST childDetailAST = clauseDetailAST.getFirstChild();
			 childDetailAST != null;
			 childDetailAST = childDetailAST.getNextSibling()) {

			if ((childDetailAST.getType() != TokenTypes.DOT) &&
				(childDetailAST.getType() != TokenTypes.IDENT)) {

				continue;
			}

			Class<?> clazz = _getClass(
				detailAST, FullIdent.createFullIdent(childDetailAST));

			if (clazz != null) {
				superClasses.add(clazz);
			}
		}
	}

	private Class<?> _getClass(DetailAST detailAST, FullIdent fullIdent) {
		String name = fullIdent.getText();

		List<String> classNames = new ArrayList<>();

		if (name.contains(".")) {
			classNames.add(name);
		}
		else {
			for (String importName : getImportNames(detailAST)) {
				if (importName.endsWith("." + name)) {
					classNames.add(importName);
				}
			}

			classNames.add("java.lang." + name);
			classNames.add(getPackageName(detailAST) + "." + name);
		}

		ClassLoader classLoader =
			OverrideAnnotationCheck.class.getClassLoader();

		for (String className : classNames) {
			try {
				return Class.forName(className, false, classLoader);
			}
			catch (ClassNotFoundException | LinkageError exception) {
			}
		}

		return null;
	}

	private String _getSimpleName(Class<?> clazz) {
		if (clazz.isArray()) {
			return _getSimpleName(clazz.getComponentType()) + "[]";
		}

		return clazz.getSimpleName();
	}

	private boolean _hasSuperMethod(
		String methodName, List<String> parameterTypeNames,
		List<Class<?>> superClasses) {

		Set<Class<?>> visitedClasses = new HashSet<>();

		List<Class<?>> classes = new ArrayList<>(superClasses);

		while (!classes.isEmpty()) {
			Class<?> clazz = classes.remove(0);

			if (!visitedClasses.add(clazz)) {
				continue;
			}

			for (Method method : clazz.getDeclaredMethods()) {
				if (!Modifier.isPrivate(method.getModifiers()) &&
					!Modifier.isStatic(method.getModifiers()) &&
					!method.isSynthetic() &&
					methodName.equals(method.getName()) &&
					_matches(method, parameterTypeNames)) {

					return true;
				}
			}

			if (clazz.getSuperclass() != null) {
				classes.add(clazz.getSuperclass());
			}

			for (Class<?> interfaceClass : clazz.getInterfaces()) {
				classes.add(interfaceClass);
			}
		}

		return false;
	}

	private boolean _matches(Method method, List<String> parameterTypeNames) {
		Class<?>[] parameterTypes = method.getParameterTypes();

		if (parameterTypes.length != parameterTypeNames.size()) {
			return false;
		}

		Type[] genericParameterTypes = method.getGenericParameterTypes();

		for (int i = 0; i < parameterTypes.length; i++) {
			if ((genericParameterTypes.length == parameterTypes.length) &&
				(genericParameterTypes[i] instanceof TypeVariable)) {

				continue;
			}

			if (!Objects.equals(
					_getSimpleName(parameterTypes[i]),
					parameterTypeNames.get(i))) {

				return false;
			}
		}

		return true;
	}

	private static final String _MSG_ADD_OVERRIDE_ANNOTATION =
		"override.annotation.add";

}