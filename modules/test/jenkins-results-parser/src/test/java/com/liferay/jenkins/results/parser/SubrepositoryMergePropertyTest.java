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
		_testGetProperty(
			_getBuildAwsProperties(), "false", "merge-central-subrepository",
			"build.caching.enabled");
	}

	@Test
	public void testGitArchiveEnabled() {
		_testGetProperty(
			_getBuildAwsProperties(), "false", "merge-portal-subrepository",
			"git.archive.enabled");
	}

	private Properties _getBuildAwsProperties() {
		File jenkinsRepositoryDir =
			JenkinsResultsParserUtil.getJenkinsRepositoryDir();

		File buildAwsPropertiesFile = new File(
			jenkinsRepositoryDir, "commands/build-aws.properties");

		Assume.assumeTrue(
			JenkinsResultsParserUtil.getCanonicalPath(buildAwsPropertiesFile) +
				" does not exist",
			buildAwsPropertiesFile.exists());

		return JenkinsResultsParserUtil.getProperties(buildAwsPropertiesFile);
	}

	private void _testGetProperty(
		Properties buildProperties, String expectedValue, String jobName,
		String propertyName) {

		Assert.assertEquals(
			expectedValue,
			JenkinsResultsParserUtil.getProperty(
				buildProperties, propertyName, jobName));
	}

}