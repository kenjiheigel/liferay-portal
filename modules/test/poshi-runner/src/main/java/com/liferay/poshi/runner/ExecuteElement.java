/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.poshi.runner;

import com.liferay.poshi.runner.util.StringUtil;

import org.dom4j.Element;

/**
 * @author Kenji Heigel
 */
public class ExecuteElement extends BasePoshiElement {

	public ExecuteElement(Element element) {
		this(element, null);
	}

	public ExecuteElement(Element element, PoshiElement parentElement) {
		super(element, parentElement);
	}

	@Override
	public String toReadableSyntax() {
		StringBuilder sb = new StringBuilder();

		sb.append("\n\t");
		sb.append(getReadableTitle());

		if (attributes.get("macro") != null) {
			sb.append(" ");

			String macro = attributes.get("macro");

			String macroClass = macro.split("#")[0];

			sb.append(macroClass);

			sb.append(" ");

			String macroCommand = macro.split("#")[1];

			String macroCommandSentence = StringUtil.camelCaseToSentence(
				macroCommand);

			sb.append(macroCommandSentence);
		}

		sb.append(super.toReadableSyntax());

		return sb.toString();
	}

}