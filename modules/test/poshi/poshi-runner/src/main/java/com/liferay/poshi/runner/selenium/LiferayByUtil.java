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

package com.liferay.poshi.runner.selenium;

import java.io.Serializable;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * @author Calum Ragan
 */
public class LiferayByUtil {

	public static By shadowDom(String selector) {
		return new ByShadowCss(selector);
	}

	public static class ByShadowCss extends By implements Serializable {

		public ByShadowCss(String cssSelector) {
			if (cssSelector == null) {
				throw new IllegalArgumentException(
					"Cannot find elements when the selector is null");
			}

			_cssSelector = cssSelector;
		}

		@Override
		public WebElement findElement(SearchContext context) {
			if (context instanceof ChromeDriver) {
				JavascriptExecutor jsExecutor;

				if (context instanceof JavascriptExecutor) {
					jsExecutor = (JavascriptExecutor)context;
				}
				else {
					WrapsDriver wrapsDriver = (WrapsDriver)context;

					WebDriver webDriver = wrapsDriver.getWrappedDriver();

					jsExecutor = (JavascriptExecutor)webDriver;
				}

				String[] subSelectors = _cssSelector.split(">>>");
				SearchContext currentContext = context;
				WebElement result = null;

				for (String subSelector : subSelectors) {
					By.ByCssSelector byCssSelector = new By.ByCssSelector(
						subSelector);

					result = byCssSelector.findElement(currentContext);

					currentContext = (WebElement)jsExecutor.executeScript(
						"return arguments[0].shadowRoot", result);
				}

				return result;
			}

			throw new WebDriverException(
				"Driver does not support finding an element by selector: " +
					_cssSelector);
		}

		@Override
		public List<WebElement> findElements(SearchContext context) {
			if (context instanceof ChromeDriver) {
				JavascriptExecutor jsExecutor;

				if (context instanceof JavascriptExecutor) {
					jsExecutor = (JavascriptExecutor)context;
				}
				else {
					WrapsDriver wrapsDriver = (WrapsDriver)context;

					WebDriver webDriver = wrapsDriver.getWrappedDriver();

					jsExecutor = (JavascriptExecutor)webDriver;
				}

				String[] subSelectors = _cssSelector.split(">>>");
				SearchContext currentContext = context;

				for (int i = 0; i < (subSelectors.length - 1); i++) {
					By.ByCssSelector byCssSelector = new By.ByCssSelector(
						subSelectors[i]);

					WebElement nextRoot = byCssSelector.findElement(
						currentContext);

					currentContext = (WebElement)jsExecutor.executeScript(
						"return arguments[0].shadowRoot", nextRoot);
				}

				By.ByCssSelector byCssSelector = new By.ByCssSelector(
					subSelectors[subSelectors.length - 1]);

				return byCssSelector.findElements(currentContext);
			}

			throw new WebDriverException(
				"Driver does not support finding elements by selector: " +
					_cssSelector);
		}

		@Override
		public String toString() {
			return "By.cssSelector: " + _cssSelector;
		}

		private static final long serialVersionUID = -1230258723099459239L;

		private final String _cssSelector;

	}

}