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

import org.dom4j.Element;

/**
 * @author Kenji Heigel
 */
public class UnsupportedElement extends BasePoshiElement {

	public UnsupportedElement(Element element) {
		this(element, null);
	}

	public UnsupportedElement(Element element, PoshiElement parentElement) {
		super(element, parentElement);
	}

	public UnsupportedElement(
		String readableSyntax, PoshiElement parentElement) {

		super(readableSyntax, parentElement);
	}

	@Override
	public String toReadableSyntax() {
		StringBuilder sb = new StringBuilder();

		sb.append("\n");
		sb.append("##########################################################");
		sb.append("\n");
		sb.append("The Poshi ");
		sb.append(getTagName());
		sb.append(" element is not supported in the readable syntax. ");
		sb.append("Please update this test.");
		sb.append("\n");
		sb.append("##########################################################");

		return sb.toString();
	}

}