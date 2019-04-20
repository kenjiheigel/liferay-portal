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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter Yoo
 */
public class GitRemote {

	public static final Pattern gitLsRemotePattern = Pattern.compile(
		"(?<sha>[^\\s]{40}+)[\\s]+refs/(?<type>[^/]+)+/(?<name>[^\\s]+)");

	public String getGitRepositoryName() {
		return _fetchRemoteURL.getRepositoryName();
	}

	public GitWorkingDirectory getGitWorkingDirectory() {
		return _gitWorkingDirectory;
	}

	public String getHostname() {
		return _fetchRemoteURL.getHostname();
	}

	public String getName() {
		return _name;
	}

	public GitHubURL getPushRemoteURL() {
		if (_pushRemoteURL != null) {
			return _pushRemoteURL;
		}

		return _fetchRemoteURL;
	}

	public GitHubURL getRemoteURL() {
		return _fetchRemoteURL;
	}

	public String getUsername() {
		return _fetchRemoteURL.getUsername();
	}

	@Override
	public String toString() {
		GitHubURL remoteURL = getRemoteURL();

		return JenkinsResultsParserUtil.combine(
			getName(), " (", remoteURL.getRemoteSSHURL(), ")");
	}

	protected GitRemote(
		GitWorkingDirectory gitWorkingDirectory, String name,
		GitHubURL remoteURL) {

		_gitWorkingDirectory = gitWorkingDirectory;
		_fetchRemoteURL = remoteURL;
		_name = name;
		_pushRemoteURL = remoteURL;
	}

	protected GitRemote(
		GitWorkingDirectory gitWorkingDirectory, String[] remoteInputLines) {

		_gitWorkingDirectory = gitWorkingDirectory;

		if (remoteInputLines.length != 2) {
			throw new IllegalArgumentException(
				"\"remoteInputLines\" must be an array of 2 strings");
		}

		if (remoteInputLines[0].equals(remoteInputLines[1])) {
			throw new IllegalArgumentException(
				JenkinsResultsParserUtil.combine(
					"\"remoteInputLines[0]\" and ",
					"\"remoteInputLines[1]\" are identical: ",
					remoteInputLines[0]));
		}

		if ((remoteInputLines[0] == null) || (remoteInputLines[1] == null)) {
			throw new IllegalArgumentException(
				JenkinsResultsParserUtil.combine(
					"Neither \"remoteInputLines[0]\" nor ",
					"\"remoteInputLines[1]\" may be NULL: ",
					Arrays.toString(remoteInputLines)));
		}

		String name = null;
		String fetchRemoteURL = null;
		String pushRemoteURL = null;

		for (String remoteInputLine : remoteInputLines) {
			Matcher matcher = _remotePattern.matcher(remoteInputLine);

			if (!matcher.matches()) {
				throw new IllegalArgumentException(
					"Invalid Git remote input line " + remoteInputLine);
			}

			if (name == null) {
				name = matcher.group("name");
			}

			String remoteURL = matcher.group("remoteURL");
			String type = matcher.group("type");

			if ((fetchRemoteURL == null) && type.equals("fetch")) {
				fetchRemoteURL = remoteURL;
			}

			if ((pushRemoteURL == null) && type.equals("push")) {
				pushRemoteURL = remoteURL;
			}
		}

		_fetchRemoteURL = GitHubURLFactory.newGitHubURL(fetchRemoteURL);
		_name = name;
		_pushRemoteURL = GitHubURLFactory.newGitHubURL(pushRemoteURL);
	}

	private static final Pattern _remotePattern = Pattern.compile(
		JenkinsResultsParserUtil.combine(
			"(?<name>[^\\s]+)[\\s]+(?<remoteURL>[^\\s]+)[\\s]+\\(",
			"(?<type>[^\\s]+)\\)"));

	private final GitHubURL _fetchRemoteURL;
	private final GitWorkingDirectory _gitWorkingDirectory;
	private final String _name;
	private final GitHubURL _pushRemoteURL;

}