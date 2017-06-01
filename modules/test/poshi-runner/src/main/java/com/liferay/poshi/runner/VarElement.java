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
public class VarElement extends BasePoshiElement {

	public VarElement(Element element) {
		this(element, null);
	}

	public VarElement(Element element, PoshiElement parentElement) {
		super(element, parentElement);
	}

	public VarElement(String readableSyntax, PoshiElement parentElement) {
		super(readableSyntax, parentElement);
	}

	public void addAttributes(String readableSyntax) {
		String[] items = readableSyntax.split("\\|");

		attributes.put("name", items[1].trim());

		String value = items[2].trim();

		if (value.contains("Util#")) {
			attributes.put("method", value);

			return;
		}

		attributes.put("value", value);
	}

	@Override
	public String toReadableSyntax() {
		StringBuilder sb = new StringBuilder();

		String parentElementTagName = getParentElement().getTagName();

		if (parentElementTagName.equals("command") ||
			parentElementTagName.equals("set-up") ||
			parentElementTagName.equals("tear-down")) {

			sb.append("\n\t");
			sb.append(getReadableTitle());
			sb.append(" these variables");
		}

		sb.append("\n\t\t");
		sb.append("|");
		sb.append(attributes.get("name"));
		sb.append("|");

		if (attributes.get("method") != null) {
			sb.append(attributes.get("method"));
		}
		else if (attributes.get("value") != null) {
			sb.append(attributes.get("value"));
		}

		sb.append("|");

		return sb.toString();
	}

	@Override
	protected void setTagName() {
		tagName = "var";
	}

}