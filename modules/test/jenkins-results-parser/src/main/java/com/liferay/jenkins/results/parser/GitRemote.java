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

	public GitHubURL getGitHubURL() {
		return _fetchGitHubURL;
	}

	public String getGitRepositoryName() {
		return _fetchGitHubURL.getRepositoryName();
	}

	public GitWorkingDirectory getGitWorkingDirectory() {
		return _gitWorkingDirectory;
	}

	public String getHostname() {
		return _fetchGitHubURL.getHostname();
	}

	public String getName() {
		return _name;
	}

	public GitHubURL getPushGitHubURL() {
		if (_pushGitHubURL != null) {
			return _pushGitHubURL;
		}

		return _fetchGitHubURL;
	}

	public String getUsername() {
		return _fetchGitHubURL.getUsername();
	}

	@Override
	public String toString() {
		GitHubURL gitHubURL = getGitHubURL();

		return JenkinsResultsParserUtil.combine(
			getName(), " (", gitHubURL.getRemoteSSHURL(), ")");
	}

	protected GitRemote(
		GitWorkingDirectory gitWorkingDirectory, String name,
		GitHubURL gitHubURL) {

		_gitWorkingDirectory = gitWorkingDirectory;
		_fetchGitHubURL = gitHubURL;
		_name = name;
		_pushGitHubURL = gitHubURL;
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

		_fetchGitHubURL = GitHubURLFactory.newGitHubURL(fetchRemoteURL);
		_name = name;
		_pushGitHubURL = GitHubURLFactory.newGitHubURL(pushRemoteURL);
	}

	private static final Pattern _remotePattern = Pattern.compile(
		JenkinsResultsParserUtil.combine(
			"(?<name>[^\\s]+)[\\s]+(?<remoteURL>[^\\s]+)[\\s]+\\(",
			"(?<type>[^\\s]+)\\)"));

	private final GitHubURL _fetchGitHubURL;
	private final GitWorkingDirectory _gitWorkingDirectory;
	private final String _name;
	private final GitHubURL _pushGitHubURL;

}