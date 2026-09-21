/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.io.File;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * @author Charlotte Wong
 */
public class SubrepositoryMergePropertyTest
	extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testBuildCachingEnabled() {
		Properties buildProperties = _getBuildProperties();

		_assertProperty(
			buildProperties, "false", "merge-central-subrepository",
			"build.caching.enabled");
		_assertProperty(
			buildProperties, "false", "merge-portal-subrepository",
			"build.caching.enabled");
	}

	@Test
	public void testGitArchiveEnabled() {
		Properties buildProperties = _getBuildProperties();

		_assertProperty(
			buildProperties, "true", "merge-central-subrepository",
			"git.archive.enabled");
		_assertProperty(
			buildProperties, "false", "merge-portal-subrepository",
			"git.archive.enabled");
	}

	private void _assertProperty(
		Properties buildProperties, String expectedValue, String jobName,
		String propertyName) {

		Assert.assertEquals(
			expectedValue,
			JenkinsResultsParserUtil.getProperty(
				buildProperties, propertyName, jobName));
	}

	private Properties _getBuildProperties() {
		File jenkinsRepositoryDir =
			JenkinsResultsParserUtil.getJenkinsRepositoryDir();

		File commandsDir = new File(jenkinsRepositoryDir, "commands");

		Assume.assumeTrue(
			JenkinsResultsParserUtil.getCanonicalPath(commandsDir) +
				" does not exist",
			commandsDir.isDirectory());

		return JenkinsResultsParserUtil.getProperties(
			new File(jenkinsRepositoryDir, "build.properties"),
			new File(jenkinsRepositoryDir, "commands/build-aws.properties"),
			new File(jenkinsRepositoryDir, "commands/build-db.properties"),
			new File(jenkinsRepositoryDir, "commands/build-local.properties"),
			new File(jenkinsRepositoryDir, "commands/build-shared.properties"));
	}

}