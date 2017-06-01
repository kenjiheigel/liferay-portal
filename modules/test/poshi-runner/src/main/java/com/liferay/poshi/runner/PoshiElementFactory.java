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
import static com.liferay.poshi.runner.ReadableSyntaxKeys.FEATURE;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.GIVEN;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.SCENARIO;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.SETUP;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.TEARDOWN;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.THEN;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.WHEN;
import static com.liferay.poshi.runner.util.StringPool.PIPE;

import com.liferay.poshi.runner.util.Dom4JUtil;
import com.liferay.poshi.runner.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * @author Kenji Heigel
 */
public class PoshiElementFactory {

	public static PoshiElement newPoshiElement(
		Element element, PoshiElement parentElement) {

		String elementName = element.getName();

		if (elementName.equals("command")) {
			return new CommandElement(element, parentElement);
		}

		if (elementName.equals("definition")) {
			return new TestFileElement(element, parentElement);
		}

		if (elementName.equals("execute")) {
			return new ExecuteElement(element, parentElement);
		}

		if (elementName.equals("property")) {
			return new PropertyElement(element, parentElement);
		}

		if (elementName.equals("set-up")) {
			return new SetUpElement(element, parentElement);
		}

		if (elementName.equals("tear-down")) {
			return new TearDownElement(element, parentElement);
		}

		if (elementName.equals("var")) {
			return new VarElement(element, parentElement);
		}

		return new UnsupportedElement(element, parentElement);
	}

	public static PoshiElement newPoshiElement(String filePath) {
		File file = new File(filePath);

		try {
			String fileContent = FileUtil.read(file);

			if (fileContent.contains("<definition")) {
				Document document = Dom4JUtil.parse(fileContent);

				Element rootElement = document.getRootElement();

				return newPoshiElement(rootElement, null);
			}

			return newPoshiElement(fileContent, null);
		}
		catch (Exception e) {
			System.out.println("The Poshi element could not be generated.");

			e.printStackTrace();
		}

		return null;
	}

	public static PoshiElement newPoshiElement(
		String readableSyntax, PoshiElement parentElement) {

		try (BufferedReader bufferedReader = new BufferedReader(
				new StringReader(readableSyntax))) {

			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();

				if (line.length() == 0) {
					continue;
				}

				if (line.startsWith(FEATURE)) {
					return new TestFileElement(readableSyntax, parentElement);
				}

				if (line.startsWith(SCENARIO)) {
					return new CommandElement(readableSyntax, parentElement);
				}

				if (line.startsWith(SETUP)) {
					return new SetUpElement(readableSyntax, parentElement);
				}

				if (line.startsWith(TEARDOWN)) {
					return new TearDownElement(readableSyntax, parentElement);
				}

				if (line.startsWith(AND) || line.startsWith(GIVEN) ||
					line.startsWith(THEN) || line.startsWith(WHEN)) {

					return new ExecuteElement(readableSyntax, parentElement);
				}

				if (line.startsWith(PIPE)) {
					return new VarElement(readableSyntax, parentElement);
				}
			}
		}
		catch (Exception e) {
			System.out.println("The Poshi element could not be generated.");

			e.printStackTrace();
		}

		return new UnsupportedElement(readableSyntax, parentElement);
	}

}