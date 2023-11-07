/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.poshi.runner.selenium;

import org.openqa.selenium.WebDriver;

/**
 * @author Kenji Heigel
 */
public class AndroidAppiumDriverImpl extends BaseWebDriverImpl {
	public AndroidAppiumDriverImpl(String browserURL, WebDriver webDriver) {
		super(browserURL, webDriver);
	}
}
