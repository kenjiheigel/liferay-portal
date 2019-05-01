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
import java.net.URL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kenji Heigel
 */
public class BaseGitHubURL implements GitHubURL<BaseGitHubURL> {

	public static String getRemoteHTTPSURL(
		String hostName, String userName, String repositoryName) {

		return JenkinsResultsParserUtil.combine(
			"https://" + hostName + "/" + userName + "/" + repositoryName);
	}

	public static String getRemoteSSHURL(
		String hostName, String userName, String repositoryName) {

		return JenkinsResultsParserUtil.combine(
			"git@", hostName, ":", userName, "/", repositoryName, ".git");
	}

	public BaseGitHubURL create(String url) throws MalformedURLException {
		return new BaseGitHubURL(url);
	}

	public String getHostname() {
		return _url.getHost();
	}

	public String getRemoteHTTPSURL() {
		return getRemoteHTTPSURL(
			getHostname(), getUsername(), getRepositoryName());
	}

	public String getRemoteSSHURL() {
		return getRemoteSSHURL(
			getHostname(), getUsername(), getRepositoryName());
	}

	public String getRepositoryName() {
		Matcher matcher = getMatcher();

		return matcher.group(REPOSITORY_NAME_KEY);
	}

	public String getUsername() {
		Matcher matcher = getMatcher();

		return matcher.group(USER_NAME_KEY);
	}

	public void setHostname(String hostname) {
		try {
			_url = new URL(_url.getProtocol(), hostname, _url.getFile());
		}
		catch (MalformedURLException murle) {
			throw new RuntimeException(murle);
		}
	}

	@Override
	public String toString() {
		URL url = getURL();

		return url.toString();
	}

	protected BaseGitHubURL() {
	}

	protected BaseGitHubURL(String url) throws MalformedURLException {
		if (url.startsWith("git@")) {
			url = url.replaceFirst(":", "/");

			url = url.replaceFirst("git@", "https://");
		}

		setURL(new URL(url));

		if (!isValidURL()) {
			throw new MalformedURLException("Unexpected GitHub URL syntax");
		}
	}

	protected Matcher getMatcher() {
		if (_matcher == null) {
			URL url = getURL();

			String fileName = url.getFile();

			Pattern urlPattern = getURLPattern();

			_matcher = urlPattern.matcher(fileName);
		}

		_matcher.reset();

		_matcher.find();

		return _matcher;
	}

	protected URL getURL() {
		return _url;
	}

	protected Pattern getURLPattern() {
		return _urlPattern;
	}

	protected boolean isValidURL() {
		Matcher matcher = getMatcher();

		matcher.reset();

		return matcher.matches();
	}

	protected void setURL(URL url) {
		_url = url;
	}

	protected static final String REPOSITORY_END_REGEX = "(|\\.git|/|$)";

	protected static final String REPOSITORY_NAME_KEY = "repositoryName";

	protected static final String REPOSITORY_NAME_REGEX =
		"/(?<" + REPOSITORY_NAME_KEY + ">[^/\\.]+)";

	protected static final String USER_NAME_KEY = "userName";

	protected static final String USER_NAME_REGEX =
		"/(?<" + USER_NAME_KEY + ">[^/]+)";

	private static final Pattern _urlPattern = Pattern.compile(
		USER_NAME_REGEX + REPOSITORY_NAME_REGEX + REPOSITORY_END_REGEX);

	private Matcher _matcher;
	private URL _url;

}