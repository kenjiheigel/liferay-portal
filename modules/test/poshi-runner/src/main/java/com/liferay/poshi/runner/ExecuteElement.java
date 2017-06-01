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

import static com.liferay.poshi.runner.ReadableSyntaxKeys.AND;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.GIVEN;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.THEN;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.WHEN;

import com.liferay.poshi.runner.util.StringUtil;

import java.util.List;

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

	public ExecuteElement(String readableSyntax, PoshiElement parentElement) {
		super(readableSyntax, parentElement);
	}

	@Override
	public void addAttributes(String readableSyntax) {
		attributes.put("macro", _getClassCommandName(readableSyntax));
	}

	@Override
	public void addChildElements(String readableSyntax) {
		List<String> readableBlocks = StringUtil.splitByKeys(
			readableSyntax, READABLE_VARIABLE_BLOCK_KEYS);

		for (String readableBlock : readableBlocks) {
			if (readableBlock.contains(AND) || readableBlock.contains(GIVEN) ||
				readableBlock.contains(THEN) || readableBlock.contains(WHEN)) {

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

		sb.append("\n\t");
		sb.append(getReadableTitle());

		if (attributes.get("macro") != null) {
			sb.append(" ");

			String macro = attributes.get("macro");

			String macroClass = macro.split("#")[0];

			sb.append(macroClass);

			sb.append(" ");

			String macroCommand = macro.split("#")[1];

			String macroCommandSentence = StringUtil.toPhrase(
				macroCommand);

			sb.append(macroCommandSentence);
		}

		sb.append(super.toReadableSyntax());

		return sb.toString();
	}

	protected void setTagName() {
		tagName = "execute";
	}

	private String _getClassCommandName(String readableSyntax) {
		int index = readableSyntax.indexOf("\n");

		if (index < 0) {
			index = readableSyntax.length();
		}

		String line = readableSyntax.substring(0, index);

		for (String key : READABLE_EXECUTE_BLOCK_KEYS) {
			if (line.startsWith(key)) {
				Pattern pattern = Pattern.compile(
					".*?" + key + ".*?.([A-z]*)(.*)");

				Matcher matcher = pattern.matcher(line);

				if (matcher.find()) {
					StringBuilder sb = new StringBuilder();

					sb.append(matcher.group(1));

					String commandName = matcher.group(2);

					commandName = StringUtil.removeSpaces(commandName);

					if (commandName.length() > 0) {
						sb.append("#");
						sb.append(commandName);
					}

					return sb.toString();
				}
			}
		}

		return null;
	}

}