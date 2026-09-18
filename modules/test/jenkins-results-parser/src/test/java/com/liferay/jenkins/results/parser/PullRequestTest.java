/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import org.json.JSONObject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kenji Heigel
 */
public class PullRequestTest extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testGetCIMergeSHA() throws Exception {
		PullRequest pullRequest = _newPullRequest();

		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(
			JenkinsResultsParserUtil.combine(
				"[{\"filename\": \"test/ci-merge\", ",
				"\"patch\": \"+abcdef0123456789abcdef0123456789abcdef01\"}]"),
			"/files", urlReader);

		Assert.assertEquals(
			"abcdef0123456789abcdef0123456789abcdef01",
			pullRequest.getCIMergeSHA());
	}

	@Test
	public void testGetFileNames() throws Exception {
		PullRequest pullRequest = _newPullRequest();

		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(
			JenkinsResultsParserUtil.combine(
				"[{\"filename\": \"modules/apps/foo/Foo.java\"}, ",
				"{\"filename\": \"portal-impl/Bar.java\"}]"),
			"/files", urlReader);

		Assert.assertEquals(
			Arrays.asList("modules/apps/foo/Foo.java", "portal-impl/Bar.java"),
			pullRequest.getFileNames());
	}

	@Test
	public void testHasRequiredCompletedTestSuites() throws Exception {
		PullRequest pullRequest = _newPullRequestWithRequiredSuites(
			"ci.forward.required.completed.suites");

		Assert.assertTrue(pullRequest.hasRequiredCompletedTestSuites());
	}

	@Test
	public void testHasRequiredCompletedTestSuitesWhenForceIsEnabled()
		throws Exception {

		PullRequest pullRequest = _newPullRequestWithRequiredSuites(
			"ci.forward.force.required.completed.suites");

		Assert.assertTrue(pullRequest.hasRequiredCompletedTestSuites(true));
	}

	@Test
	public void testHasRequiredPassingTestSuites() throws Exception {
		PullRequest pullRequest = _newPullRequestWithRequiredSuites(
			"ci.forward.required.passing.suites");

		Assert.assertTrue(pullRequest.hasRequiredPassingTestSuites());
	}

	@Test
	public void testHasRequiredPassingTestSuitesWhenForceIsEnabled()
		throws Exception {

		PullRequest pullRequest = _newPullRequestWithRequiredSuites(
			"ci.forward.force.required.passing.suites");

		Assert.assertTrue(pullRequest.hasRequiredPassingTestSuites(true));
	}

	@Test
	public void testHasRequiredPassingTestSuitesWhenRefNameIsMissing()
		throws Exception {

		PullRequest pullRequest = PullRequestFactory.newPullRequest(
			new JSONObject(
			).put(
				"base",
				new JSONObject(
				).put(
					"repo",
					new JSONObject(
					).put(
						"name", "test-repository-no-ref"
					).put(
						"owner",
						new JSONObject(
						).put(
							"login", "test-owner-no-ref"
						)
					)
				)
			).put(
				"html_url",
				"https://github.com/test-owner-no-ref/test-repository-no-ref" +
					"/pull/1"
			).put(
				"number", 1
			));

		Properties buildProperties = new Properties();

		buildProperties.setProperty("ci.forward.required.passing.suites", "");

		JenkinsResultsParserUtil.setBuildProperties(buildProperties);

		Assert.assertTrue(pullRequest.hasRequiredPassingTestSuites());
	}

	private PullRequest _newPullRequest() {
		BuildDatabase buildDatabase =
			BuildDatabaseTestUtil.newBuildDatabaseWithPullRequest();

		BuildDatabaseUtil.setBuildDatabase(buildDatabase);

		JSONObject jsonObject = buildDatabase.getJSONObject();

		JSONObject pullRequestsJSONObject = jsonObject.getJSONObject(
			"pull_requests");

		Iterator<String> iterator = pullRequestsJSONObject.keys();

		return PullRequestFactory.newPullRequest(iterator.next(), null);
	}

	private PullRequest _newPullRequestWithRequiredSuites(
			String basePropertyName)
		throws Exception {

		PullRequest pullRequest = _newPullRequest();

		UrlReader urlReader = mockUrlReader();

		setUrlReaderOutput(
			"[{\"context\": \"liferay/ci:test:sf\", \"state\": \"success\"}]",
			"/statuses", urlReader);

		Properties buildProperties = new Properties();

		buildProperties.setProperty(basePropertyName, "relevant");

		buildProperties.setProperty(
			JenkinsResultsParserUtil.combine(
				basePropertyName, "[", pullRequest.getGitRepositoryName(), "][",
				pullRequest.getRefName(), "]"),
			"sf");

		JenkinsResultsParserUtil.setBuildProperties(buildProperties);

		return pullRequest;
	}

}