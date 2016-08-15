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

import com.liferay.poshi.runner.util.PropsValues;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.w3c.dom.Node;

/**
 * @author Kenji Heigel
 */
public abstract class BaseMobileDriverImpl extends MobileDriverWrapper {
	public BaseMobileDriverImpl(String browserURL, WebDriver webDriver) {
		super(browserURL, webDriver);
	}

	@Override
	public void assertAccessible() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void click(String locator) {
		try {
			tap(locator);
		}
		catch (Exception e) {
			if (!isInViewport(locator)) {
				swipeWebElementIntoView(locator);
			}

			tap(locator);
		}
	}

	public void clickAt(
		String locator, String coordString, boolean scrollIntoView) {

		click(locator);
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	public void copyText(String locator) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void copyValue(String locator) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doubleClick(String locator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doubleClickAt(String locator, String coordString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void dragAndDrop(String locator, String coordString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void dragAndDropToObject(
		String locatorOfObjectToBeDragged,
		String locatorOfDragDestinationObject) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void dragdrop(String locator, String movementsString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAlert() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getBodyText() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCurrentDay() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCurrentDayName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCurrentHour() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCurrentMonth() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCurrentYear() {
		throw new UnsupportedOperationException();
	}

	public String getElementValue(String locator, String timeout)
		throws Exception {

		WebElement webElement = getWebElement(locator, timeout);

		if (webElement == null) {
			throw new Exception(
				"Element is not present at \"" + locator + "\"");
		}

		if (!isInViewport(locator)) {
			swipeWebElementIntoView(locator);
		}

		return webElement.getAttribute("value");
	}

	@Override
	public String getFirstNumber(String locator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getFirstNumberIncrement(String locator) {
		throw new UnsupportedOperationException();
	}

	public Node getHtmlNode(String locator) {
		throw new UnsupportedOperationException();
	}

	public String getHtmlNodeHref(String locator) {
		throw new UnsupportedOperationException();
	}

	public String getHtmlNodeText(String locator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getHtmlSource() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getText(String locator) throws Exception {
		return getText(locator, null);
	}

	public String getText(String locator, String timeout) throws Exception {
		WebElement webElement = getWebElement(locator, timeout);

		if (webElement == null) {
			throw new Exception(
				"Element is not present at \"" + locator + "\"");
		}

		if (!isInViewport(locator)) {
			swipeWebElementIntoView(locator);
		}

		String text = webElement.getText();

		text = text.trim();

		return text.replace("\n", " ");
	}

	@Override
	public boolean isAlertPresent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isChecked(String locator) {
		WebElement webElement = getWebElement(locator, "1");

		if (!webElement.isDisplayed()) {
			return webElement.isDisplayed();
		}

		if (!isInViewport(locator)) {
			swipeWebElementIntoView(locator);
		}

		return webElement.isSelected();
	}

	@Override
	public boolean isEditable(String locator) {
		throw new UnsupportedOperationException();
	}

	public boolean isInViewport(String locator) {
		int elementPositionCenterY = WebDriverHelper.getElementPositionCenterY(
			this, locator);

		int viewportPositionBottom = WebDriverHelper.getViewportPositionBottom(
			this);

		int viewportPositionTop = WebDriverHelper.getScrollOffsetY(this);

		if ((elementPositionCenterY >= viewportPositionBottom) ||
			(elementPositionCenterY <= viewportPositionTop)) {

			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public boolean isNotEditable(String locator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isTextPresent(String pattern) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isVisible(String locator) {
		WebElement webElement = getWebElement(locator, "1");

		if (PropsValues.BROWSER_TYPE.equals("android")) {
			if (!isInViewport(locator)) {
				swipeWebElementIntoView(locator);
			}

			return isInViewport(locator);
		}
		else {
			WebDriverHelper.scrollWebElementIntoView(this, webElement);

			return webElement.isDisplayed();
		}
	}

	@Override
	public void javaScriptMouseDown(String locator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void javaScriptMouseUp(String locator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void keyDown(String locator, String keySequence) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void keyDownAndWait(String locator, String keySequence) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void keyPress(String locator, String keySequence) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void keyPressAndWait(String locator, String keySequence) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void keyUp(String locator, String keySequence) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void keyUpAndWait(String locator, String keySequence) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void mouseDown(String locator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void mouseDownAt(String locator, String coordString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void mouseMove(String locator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void mouseMoveAt(String locator, String coordString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void mouseOut(String locator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void mouseOver(String locator) {
	}

	@Override
	public void mouseRelease() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void mouseUp(String locator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void mouseUpAt(String locator, String coordString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void paste(String locator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void pauseLoggerCheck() throws Exception {
	}

	@Override
	public void refreshAndWait() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void scrollBy(String coordString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void scrollWebElementIntoView(String locator) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void selectFieldText() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void selectPopUp(String windowID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendKeysAceEditor(String locator, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefaultTimeout() {
	}

	@Override
	public void setTimeout(String timeout) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTimeoutImplicit(String timeout) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWindowSize(String coordString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void typeAceEditor(String locator, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void typeKeys(String locator, String value) {
		throw new UnsupportedOperationException();
	}

	public void typeKeys(String locator, String value, boolean typeAceEditor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void uploadCommonFile(String locator, String value)
		throws Exception {

		throw new UnsupportedOperationException();
	}

	@Override
	public void uploadFile(String locator, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void uploadTempFile(String locator, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void waitForPageToLoad(String timeout) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void waitForPopUp(String windowID, String timeout) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void windowMaximizeAndWait() {
		throw new UnsupportedOperationException();
	}

	protected void swipeWebElementIntoView(String locator) {
		WebElement webElement = getWebElement(locator, "1");

		WebDriverHelper.scrollWebElementIntoView(this, webElement);
	}

	protected void tap(String locator) {
	}

}