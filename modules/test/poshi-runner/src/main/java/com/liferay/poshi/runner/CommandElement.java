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
public class CommandElement extends BasePoshiElement {

	public CommandElement(Element element) {
		this(element, null);
	}

	public CommandElement(Element element, PoshiElement parentElement) {
		super(element, parentElement);
	}

	@Override
	public String toReadableSyntax() {
		return toReadableSyntax(readableTitle);
	}

	protected String toReadableSyntax(String readableTitle) {
		StringBuilder sb = new StringBuilder();

		sb.append("\n");
		sb.append(readableTitle);

		if (attributes.get("name") != null) {
			String name = attributes.get("name");

			sb.append(StringUtil.camelCaseToSentence(name));
		}

		sb.append(super.toReadableSyntax());

		return sb.toString();
	}

	protected String readableTitle = "Scenario: ";

}