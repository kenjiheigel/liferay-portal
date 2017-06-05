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
import static com.liferay.poshi.runner.ReadableSyntaxKeys.SCENARIO;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.SETUP;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.TEARDOWN;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.THESE_PROPERTIES;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.THESE_VARIABLES;
import static com.liferay.poshi.runner.util.StringPool.COLON;

import com.liferay.poshi.runner.util.StringUtil;

import java.util.List;

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

	public CommandElement(String readableSyntax, PoshiElement parentElement) {
		super(readableSyntax, parentElement);
	}

	@Override
	public void addAttributes(String readableSyntax) {
		attributes.put("name", _getCommandName(readableSyntax));

		_addPriorityAttribute(readableSyntax);
	}

	@Override
	public void addChildElements(String readableSyntax) {
		List<String> readableBlocks = StringUtil.splitByKeys(
			readableSyntax, READABLE_EXECUTE_BLOCK_KEYS);

		for (String readableBlock : readableBlocks) {
			if (readableBlock.contains(BACKGROUND) ||
				readableBlock.contains(FEATURE) ||
				readableBlock.contains(SCENARIO) ||
				readableBlock.contains(SETUP) ||
				readableBlock.contains(TEARDOWN)) {

				continue;
			}

			if (readableBlock.contains(THESE_PROPERTIES) ||
				readableBlock.contains(THESE_VARIABLES)) {

				addChildVariableElements(readableBlock);

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

		sb.append("\n");
		sb.append(getReadableCommandTitle());
		sb.append(" ");

		if (attributes.get("name") != null) {
			String name = attributes.get("name");

			sb.append(StringUtil.toPhrase(name));
		}

		if (attributes.get("priority") != null) {
			String priority = attributes.get("priority");

			sb.append("\n");
			sb.append("Priority: ");
			sb.append(priority);
		}

		sb.append(super.toReadableSyntax());

		return sb.toString();
	}

	protected String getReadableCommandTitle() {
		return SCENARIO + COLON;
	}

	@Override
	protected void setTagName() {
		tagName = "command";
	}

	private void _addPriorityAttribute(String readableSyntax) {
		int start = readableSyntax.indexOf("Priority: ");

		int end = readableSyntax.indexOf("\n", start);

		String priority = readableSyntax.substring(start + 9, end);

		attributes.put("priority", priority.trim());
	}

	private String _getCommandName(String readableSyntax) {
		int start = readableSyntax.indexOf(SCENARIO) + SCENARIO.length() + 1;
		int end = readableSyntax.indexOf("\n");

		String line = readableSyntax.substring(start, end);

		return StringUtil.removeSpaces(line);
	}

}