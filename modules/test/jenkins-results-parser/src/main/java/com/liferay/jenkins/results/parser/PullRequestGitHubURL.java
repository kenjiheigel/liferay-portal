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

package com.liferay.jenkins.results.parser;

import java.net.MalformedURLException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kenji Heigel
 */
public class PullRequestGitHubURL extends BaseGitHubURL {

	public BaseGitHubURL create(String url) throws MalformedURLException {
		return new PullRequestGitHubURL(url);
	}

	public Integer getNumber() {
		Matcher matcher = getMatcher();

		return Integer.parseInt(matcher.group(NUMBER_KEY));
	}

	protected PullRequestGitHubURL() {
	}

	protected PullRequestGitHubURL(String url) throws MalformedURLException {
		super(url);
	}

	protected Pattern getURLPattern() {
		return _urlPattern;
	}

	protected static final String NUMBER_KEY = "number";

	protected static final String NUMBER_REGEX =
		"/pull/(?<" + NUMBER_KEY + ">\\d+)";

	private static final Pattern _urlPattern = Pattern.compile(
		USER_NAME_REGEX + REPOSITORY_NAME_REGEX + NUMBER_REGEX);

}