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

import static com.liferay.poshi.runner.ReadableSyntaxKeys.BACKGROUND;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.FEATURE;

import org.dom4j.Element;

/**
 * @author Kenji Heigel
 */
public class TestFileElement extends BasePoshiElement {

	public TestFileElement(Element element) {
		this(element, null);
	}

	public TestFileElement(Element element, PoshiElement parentElement) {
		super(element, parentElement);
	}

	public TestFileElement(String readableSyntax, PoshiElement parentElement) {
		super(readableSyntax, parentElement);
	}

	@Override
	public void addAttributes(String readableSyntax) {
		attributes.put("component-name", "portal-acceptance");
	}

	@Override
	public void addChildElements(String readableSyntax) {
		List<String> readableBlocks = StringUtil.splitByKeys(
			readableSyntax, READABLE_COMMAND_BLOCK_KEYS);

		for (String readableBlock : readableBlocks) {
			if (readableBlock.startsWith(FEATURE)) {
				continue;
			}
			else if (readableBlock.startsWith(BACKGROUND)) {
				List<String> readableCommandBlocks = StringUtil.splitByKeys(
					readableBlock, READABLE_COMMAND_BLOCK_KEYS);

				for (String readableCommandBlock : readableCommandBlocks) {
					addChildVariableElements(readableCommandBlock);
				}

				continue;
			}

			PoshiElement poshiElement = PoshiElementFactory.newPoshiElement(
				readableBlock, this);

			addChildElement(poshiElement);
		}
	}

	@Override
	public String toReadableSyntax() {
		StringBuilder sb = new StringBuilder();

		sb.append("Feature:");
		sb.append("\n\n");
		sb.append("Background: This executes once per feature file");
		sb.append("\n\t");
		sb.append("Given these properties");

		for (PoshiElement childElement : getChildElements("property")) {
			sb.append(childElement.toReadableSyntax());
		}

		sb.append("\n\t");
		sb.append("And these variables");

		for (PoshiElement childElement : getChildElements("var")) {
			sb.append(childElement.toReadableSyntax());
		}

		sb.append("\n");

		for (PoshiElement childElement : getChildElements("set-up")) {
			sb.append(childElement.toReadableSyntax());
		}

		sb.append("\n");

		for (PoshiElement childElement : getChildElements("tear-down")) {
			sb.append(childElement.toReadableSyntax());
		}

		for (PoshiElement childElement : getChildElements("command")) {
			sb.append("\n");
			sb.append(childElement.toReadableSyntax());
		}

		return sb.toString();
	}

	@Override
	protected void setTagName() {
		tagName = "definition";
	}

}