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

import static com.liferay.poshi.runner.ReadableSyntaxKeys.THESE_PROPERTIES;

import org.dom4j.Element;

/**
 * @author Kenji Heigel
 */
public class PropertyElement extends VarElement {

	public PropertyElement(Element element) {
		this(element, null);
	}

	public PropertyElement(Element element, PoshiElement parentElement) {
		super(element, parentElement);
	}

	public PropertyElement(String readableSyntax, PoshiElement parentElement) {
		super(readableSyntax, parentElement);
	}

	@Override
	protected String getReadableVariableKey() {
		return THESE_PROPERTIES;
	}

	@Override
	protected void setTagName() {
		tagName = "property";
	}

}