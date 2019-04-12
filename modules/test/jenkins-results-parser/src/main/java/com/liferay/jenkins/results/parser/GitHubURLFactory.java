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

import com.google.common.reflect.ClassPath;

import java.io.IOException;

import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kenji Heigel
 */
public class GitHubURLFactory {

	public static GitHubURL newGitHubURL(
		String hostname, String username, String repositoryName) {

		String url =
			BaseGitHubURL.getRemoteSSHURL(hostname, username, repositoryName);

		return newGitHubURL(url);
	}

	public static GitHubURL newGitHubURL(String url) {
		for (GitHubURL gitHubURL : _gitHubURLs) {
			try {
				return gitHubURL.create(url);
			}
			catch (MalformedURLException murle) {
				continue;
			}
		}

		throw new RuntimeException("Invalid GitHub URL: " + url);
	}

	private static final List<GitHubURL> _gitHubURLs =
		new ArrayList<GitHubURL>() {
			{
				try {
					ClassPath classPath = ClassPath.from(
						GitHubURL.class.getClassLoader());

					for (ClassPath.ClassInfo classInfo :
							classPath.getTopLevelClasses(
								"com.liferay.jenkins.results.parser")) {

						Class<?> clazz = classInfo.load();

						if (BaseGitHubURL.class.isAssignableFrom(clazz)) {
							BaseGitHubURL gitHubURL =
								(BaseGitHubURL)clazz.newInstance();

							add(gitHubURL);
						}

						continue;
					}
				}
				catch (IllegalAccessException | InstantiationException |
					   IOException e) {

					throw new RuntimeException(e);
				}
			}
		};

}