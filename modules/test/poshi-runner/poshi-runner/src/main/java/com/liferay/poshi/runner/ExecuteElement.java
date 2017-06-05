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
import static com.liferay.poshi.runner.ReadableSyntaxKeys.AT_LOCATOR;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.GIVEN;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.THEN;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.THE_VALUE;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.WHEN;

import com.liferay.poshi.runner.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		if (readableSyntax.contains(AT_LOCATOR) ||
			readableSyntax.contains(THE_VALUE)) {

			_addFunctionAttributes(readableSyntax);

			return;
		}

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
		sb.append(getReadableExecuteKey());

		if (attributes.get("function") != null) {
			sb.append(" ");

			String function = attributes.get("function");

			sb.append(_getReadableSyntaxCommandPhrase(function));

			List<String> functionAttributes = Arrays.asList(
				"value1", "locator1", "value2", "locator2");

			for (String functionAttribute : functionAttributes) {
				if (attributes.get(functionAttribute) != null) {
					if (functionAttribute.startsWith("locator")) {
						sb.append(" ");
						sb.append(AT_LOCATOR);
					}
					else {
						sb.append(" ");
						sb.append(THE_VALUE);
					}

					sb.append(" '");

					sb.append(attributes.get(functionAttribute));

					sb.append("'");
				}
			}
		}
		else if (attributes.get("macro") != null) {
			sb.append(" ");

			String macro = attributes.get("macro");

			sb.append(_getReadableSyntaxCommandPhrase(macro));
		}

		sb.append(super.toReadableSyntax());

		return sb.toString();
	}

	protected void setTagName() {
		tagName = "execute";
	}

	private void _addFunctionAttribute(
		String readableSyntax, String attributeType) {

		int start = readableSyntax.indexOf("'");

		int end = readableSyntax.indexOf("'", start + 1);

		if (attributes.get(attributeType + "1") == null) {
			attributes.put(
				attributeType + "1", readableSyntax.substring(start + 1, end));

			return;
		}

		attributes.put(
			attributeType + "2", readableSyntax.substring(start + 1, end));
	}

	private void _addFunctionAttributes(String readableSyntax) {
		String[] keys = {AT_LOCATOR, THE_VALUE};

		List<String> functionItems = StringUtil.splitByKeys(
			readableSyntax, keys);

		for (String functionItem : functionItems) {
			if (functionItem.contains(AT_LOCATOR)) {
				_addFunctionAttribute(functionItem, "locator");

				continue;
			}

			if (functionItem.contains(THE_VALUE)) {
				_addFunctionAttribute(functionItem, "value");

				continue;
			}

			attributes.put("function", _getClassCommandName(functionItem));
		}
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

	private String _getReadableSyntaxCommandPhrase(String classCommandName) {
		StringBuilder sb = new StringBuilder();

		if (classCommandName.contains("#")) {
			String className = classCommandName.split("#")[0];

			sb.append(className);

			sb.append(" ");

			String commandName = classCommandName.split("#")[1];

			String commandSentence = StringUtil.toPhrase(commandName);

			sb.append(commandSentence);

			return sb.toString();
		}

		return classCommandName;
	}

}