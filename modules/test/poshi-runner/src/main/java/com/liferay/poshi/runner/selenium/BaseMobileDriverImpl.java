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

import io.appium.java_client.MobileDriver;
import io.appium.java_client.MultiTouchAction;
import io.appium.java_client.TouchAction;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.remote.Response;

/**
 * @author Kenji Heigel
 */
public abstract class BaseMobileDriverImpl
	extends BaseWebDriverImpl implements MobileDriver {

	public BaseMobileDriverImpl(String browserURL, WebDriver webDriver) {
		super(browserURL, webDriver);

		_mobileDriver = (MobileDriver)webDriver;

		WebDriverHelper.setDefaultWindowHandle(webDriver.getWindowHandle());

		System.setProperty("java.awt.headless", "false");
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

	@Override
	public void closeApp() {
		_mobileDriver.closeApp();
	}

	@Override
	public WebDriver context(String name) {
		return _mobileDriver.context(name);
	}

	@Override
	public Response execute(String driverCommand, Map<String, ?> parameters) {
		return _mobileDriver.execute(driverCommand, parameters);
	}

	@Override
	public WebElement findElementByAccessibilityId(String using) {
		return _mobileDriver.findElementByAccessibilityId(using);
	}

	@Override
	public List<WebElement> findElementsByAccessibilityId(String using) {
		return _mobileDriver.findElementsByAccessibilityId(using);
	}

	@Override
	public String getAppStrings() {
		return _mobileDriver.getAppStrings();
	}

	@Override
	public String getAppStrings(String language) {
		return _mobileDriver.getAppStrings(language);
	}

	@Override
	public String getContext() {
		return _mobileDriver.getContext();
	}

	@Override
	public Set<String> getContextHandles() {
		return _mobileDriver.getContextHandles();
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
	public ScreenOrientation getOrientation() {
		return _mobileDriver.getOrientation();
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
	public void hideKeyboard() {
		_mobileDriver.hideKeyboard();
	}

	@Override
	public void installApp(String appPath) {
		_mobileDriver.installApp(appPath);
	}

	@Override
	public boolean isAppInstalled(String bundleId) {
		return _mobileDriver.isAppInstalled(bundleId);
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
	public boolean isVisible(String locator) {
		WebElement webElement = getWebElement(locator, "1");

		if (PropsValues.BROWSER_TYPE.equals("android")) {
			if (!isInViewport(locator)) {
				swipeWebElementIntoView(locator);
			}

			return isInViewport(locator);
		}
		else {
			if (!webElement.isDisplayed()) {
				WebDriverHelper.scrollWebElementIntoView(this, webElement);
			}

			return webElement.isDisplayed();
		}
	}

	@Override
	public void launchApp() {
		_mobileDriver.launchApp();
	}

	@Override
	public Location location() {
		return _mobileDriver.location();
	}

	@Override
	public void mouseOver(String locator) {
	}

	@Override
	public void performMultiTouchAction(MultiTouchAction multiAction) {
		_mobileDriver.performMultiTouchAction(multiAction);
	}

	@Override
	public TouchAction performTouchAction(TouchAction touchAction) {
		return _mobileDriver.performTouchAction(touchAction);
	}

	@Override
	public void pinch(int x, int y) {
		_mobileDriver.pinch(x, y);
	}

	@Override
	public void pinch(WebElement el) {
		_mobileDriver.pinch(el);
	}

	@Override
	public byte[] pullFile(String remotePath) {
		return _mobileDriver.pullFile(remotePath);
	}

	@Override
	public byte[] pullFolder(String remotePath) {
		return _mobileDriver.pullFolder(remotePath);
	}

	@Override
	public void removeApp(String bundleId) {
		_mobileDriver.removeApp(bundleId);
	}

	@Override
	public void resetApp() {
		_mobileDriver.resetApp();
	}

	@Override
	public void rotate(ScreenOrientation orientation) {
		_mobileDriver.rotate(orientation);
	}

	@Override
	public void runAppInBackground(int seconds) {
		_mobileDriver.runAppInBackground(seconds);
	}

	@Override
	public WebElement scrollTo(String text) {
		return _mobileDriver.scrollTo(text);
	}

	@Override
	public WebElement scrollToExact(String text) {
		return _mobileDriver.scrollToExact(text);
	}

	@Override
	public void scrollWebElementIntoView(String locator) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLocation(Location location) {
		_mobileDriver.setLocation(location);
	}

	@Override
	public void swipe(
		int startx, int starty, int endx, int endy, int duration) {

		_mobileDriver.swipe(startx, starty, endx, endy, duration);
	}

	@Override
	public void tap(int fingers, int x, int y, int duration) {
		_mobileDriver.tap(fingers, x, y, duration);
	}

	@Override
	public void tap(int fingers, WebElement element, int duration) {
		_mobileDriver.tap(fingers, element, duration);
	}

	@Override
	public void zoom(int x, int y) {
		_mobileDriver.zoom(x, y);
	}

	@Override
	public void zoom(WebElement el) {
		_mobileDriver.zoom(el);
	}

	protected void swipeWebElementIntoView(String locator) {
		WebElement webElement = getWebElement(locator, "1");

		WebDriverHelper.scrollWebElementIntoView(this, webElement);
	}

	protected void tap(String locator) {
	}

	private static final String _OUTPUT_DIR_NAME = PropsValues.OUTPUT_DIR_NAME;

	private static final String _SIKULI_IMAGES_DIR_NAME =
		PropsValues.TEST_DEPENDENCIES_DIR_NAME + "//sikuli//linux//";

	private static final String _TEST_DEPENDENCIES_DIR_NAME =
		PropsValues.TEST_DEPENDENCIES_DIR_NAME;

	private final MobileDriver _mobileDriver;
	private final String _primaryTestSuiteName;

}