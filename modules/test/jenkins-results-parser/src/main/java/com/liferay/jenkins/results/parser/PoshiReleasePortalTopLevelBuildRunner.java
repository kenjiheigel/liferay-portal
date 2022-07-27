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

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

/**
 * @author Kenji Heigel
 */
public class PoshiReleasePortalTopLevelBuildRunner
	extends PortalTopLevelBuildRunner<PortalTopLevelBuildData> {

	public void commitGradlePluginsPoshiRunnerCache(
			GitWorkingDirectory gitWorkingDirectory)
		throws IOException {

		File buildTestGradleFile = getBuildTestGradleFile(gitWorkingDirectory);

		gitWorkingDirectory.commitFileToCurrentBranch(
			buildTestGradleFile.getCanonicalPath(),
			"POSHI-0 CI TESTING ONLY: Use latest gradle-plugins-poshi-runner");

		gitWorkingDirectory.commitFileToCurrentBranch(
			".m2-tmp", "POSHI-0 CI TESTING ONLY: FAKE GRADLE CACHE");
	}

	public LocalGitBranch createLocalGitBranch(
			GitWorkingDirectory gitWorkingDirectory)
		throws IOException {

		String upstreamBranchName = gitWorkingDirectory.getUpstreamBranchName();

		String distPortalBundlesBuildURL =
			JenkinsResultsParserUtil.getDistPortalBundlesBuildURL(
				upstreamBranchName);

		String distPortalBundlesBuildSHA = JenkinsResultsParserUtil.toString(
			distPortalBundlesBuildURL + "/git-hash");

		distPortalBundlesBuildSHA = distPortalBundlesBuildSHA.trim();

		LocalGitBranch pullRequestLocalGitBranch =
			gitWorkingDirectory.createLocalGitBranch(
				upstreamBranchName + "-temp-pr-" + System.currentTimeMillis(),
				true, distPortalBundlesBuildSHA);

		gitWorkingDirectory.checkoutLocalGitBranch(pullRequestLocalGitBranch);

		gitWorkingDirectory.reset("--hard");

		updateGradlePluginsPoshiRunnerDependency(gitWorkingDirectory);

		commitGradlePluginsPoshiRunnerCache(gitWorkingDirectory);

		return pullRequestLocalGitBranch;
	}

	public File getBuildTestGradleFile(
		GitWorkingDirectory gitWorkingDirectory) {

		return new File(
			gitWorkingDirectory.getWorkingDirectory(),
			"portal-web/build-test.gradle");
	}

	public RemoteGitRepository getPullRequestRemoteGitRepository() {
		String portalGitHubUrl = getBuildParameter("PORTAL_GITHUB_URL");

		Matcher matcher = GitRemote.getRemoteURLMatcher(portalGitHubUrl);

		if (matcher.find()) {
			return GitRepositoryFactory.getRemoteGitRepository(
				matcher.group("hostname"), "liferay-portal-ee",
				matcher.group("username"));
		}

		throw new RuntimeException(
			"Invalid Portal GitHub URL: " + portalGitHubUrl);
	}

	@Override
	public void run() {
		validateBuildParameters();

		publishJenkinsReport();

		updateBuildDescription();

		setUpWorkspace();

		preparePoshiPortalPullRequests();

		invokeDownstreamBuilds();

		waitForDownstreamBuildsToComplete();

		publishJenkinsReport();
	}

	@Override
	public void tearDown() {
		for (Map.Entry<GitWorkingDirectory, RemoteGitBranch> entry :
				_remoteGitBranches.entrySet()) {

			RemoteGitBranch remoteGitBranch = entry.getValue();

			if (remoteGitBranch != null) {
				GitWorkingDirectory gitWorkingDirectory = entry.getKey();

				gitWorkingDirectory.deleteRemoteGitBranch(remoteGitBranch);
			}
		}

		super.tearDown();
	}

	public void updateGradlePluginsPoshiRunnerDependency(
			GitWorkingDirectory gitWorkingDirectory)
		throws IOException {

		Workspace workspace = getWorkspace();

		WorkspaceGitRepository workspaceGitRepository =
			workspace.getPrimaryWorkspaceGitRepository();

		File sourceCacheDir = new File(
			workspaceGitRepository.getDirectory(),
			".m2-tmp/com/liferay/com.liferay.gradle.plugins.poshi.runner");

		if (!sourceCacheDir.exists()) {
			throw new RuntimeException(sourceCacheDir + " does not exist.");
		}

		File targetCacheDir = new File(
			gitWorkingDirectory.getWorkingDirectory(),
			".m2-tmp/com/liferay/com.liferay.gradle.plugins.poshi.runner");

		FileUtils.copyDirectory(sourceCacheDir, targetCacheDir);

		File file = getBuildTestGradleFile(gitWorkingDirectory);

		String fileContent = JenkinsResultsParserUtil.read(file);

		String gradlePluginsPoshiRunnerVersion = getBuildParameter(
			"GRADLE_PLUGINS_POSHI_RUNNER_VERSION");

		JenkinsResultsParserUtil.write(
			file,
			fileContent.replaceAll(
				"([\\s]*)(.*\"com\\.liferay\\.gradle\\.plugins\\.defaults\".*)",
				"$1$2$1classpath group: \"com.liferay\", name: " +
					"\"com.liferay.gradle.plugins.poshi.runner\", version: \"" +
						gradlePluginsPoshiRunnerVersion + "\""));
	}

	protected PoshiReleasePortalTopLevelBuildRunner(
		PortalTopLevelBuildData portalTopLevelBuildData) {

		super(portalTopLevelBuildData);
	}

	protected PullRequest createPortalPullRequest(
		GitWorkingDirectory gitWorkingDirectory) {

		try {
			LocalGitBranch localGitBranch = createLocalGitBranch(
				gitWorkingDirectory);

			RemoteGitBranch remoteGitBranch =
				gitWorkingDirectory.pushToRemoteGitRepository(
					true, localGitBranch, localGitBranch.getName(),
					getPullRequestRemoteGitRepository());

			_remoteGitBranches.put(gitWorkingDirectory, remoteGitBranch);

			return PullRequestFactory.newPullRequest(
				gitWorkingDirectory.createPullRequest(
					"Testing Poshi Release", remoteGitBranch.getName(),
					remoteGitBranch.getUsername(),
					remoteGitBranch.getUsername(),
					"Poshi Release | " +
						gitWorkingDirectory.getUpstreamBranchName()));
		}
		catch (IOException ioException) {
			throw new RuntimeException(
				"Unable to create pull request", ioException);
		}
	}

	protected String getJobInvocationURL(String jobName) {
		BuildData buildData = getBuildData();

		String cohortName = buildData.getCohortName();

		String mostAvailableMasterURL =
			JenkinsResultsParserUtil.getMostAvailableMasterURL(
				"http://" + cohortName + ".liferay.com", 1);

		return JenkinsResultsParserUtil.combine(
			mostAvailableMasterURL, "/job/",
			jobName.replaceAll("-controller\\(.*\\)", ""));
	}

	protected void invokeDownstreamBuilds() {
		BuildData buildData = getBuildData();

		TopLevelBuild topLevelBuild = getTopLevelBuild();

		for (Map.Entry<String, PullRequest> entry : _pullRequests.entrySet()) {
			StringBuilder sb = new StringBuilder();

			String jobName =
				"test-portal-acceptance-pullrequest(" + entry.getKey() + ")";

			sb.append(getJobInvocationURL(jobName));

			sb.append("/buildWithParameters?");
			sb.append("token=");

			try {
				sb.append(
					JenkinsResultsParserUtil.getBuildProperty(
						"jenkins.authentication.token"));
			}
			catch (IOException ioException) {
				throw new RuntimeException(ioException);
			}

			Map<String, String> invocationParameters = new HashMap<>();

			String upstreamBranchName = entry.getKey();

			PullRequest pullRequest = entry.getValue();

			invocationParameters.put("CI_TEST_SUITE", _CI_TEST_SUITE);
			invocationParameters.put(
				"GITHUB_PULL_REQUEST_NUMBER", pullRequest.getNumber());
			invocationParameters.put(
				"GITHUB_RECEIVER_USERNAME", pullRequest.getReceiverUsername());
			invocationParameters.put(
				"JENKINS_GITHUB_BRANCH_NAME",
				_getGitHubBranchName("JENKINS_GITHUB_URL"));
			invocationParameters.put(
				"JENKINS_GITHUB_BRANCH_USERNAME",
				_getGitHubBranchUsername("JENKINS_GITHUB_URL"));
			invocationParameters.put("JENKINS_JOB_VARIANT", _CI_TEST_SUITE);
			invocationParameters.put(
				"JENKINS_TOP_LEVEL_BUILD_URL", buildData.getBuildURL());
			invocationParameters.put(
				"PORTAL_BUNDLES_DIST_URL",
				JenkinsResultsParserUtil.getDistPortalBundlesBuildURL(
					upstreamBranchName));

			for (Map.Entry<String, String> invocationParameter :
					invocationParameters.entrySet()) {

				String invocationParameterValue =
					invocationParameter.getValue();

				if (JenkinsResultsParserUtil.isNullOrEmpty(
						invocationParameterValue)) {

					continue;
				}

				sb.append("&");
				sb.append(invocationParameter.getKey());
				sb.append("=");
				sb.append(invocationParameterValue);
			}

			topLevelBuild.addDownstreamBuilds(sb.toString());
		}
	}

	@Override
	protected void prepareInvocationBuildDataList() {
	}

	protected void preparePoshiPortalPullRequests() {
		Workspace workspace = getWorkspace();

		for (WorkspaceGitRepository workspaceGitRepository :
				workspace.getWorkspaceGitRepositories()) {

			System.out.println("workspaceGitRepository: " + workspaceGitRepository);

			File workspaceGitRepositoryDir = workspaceGitRepository.getDirectory();

			WorkspaceGitRepository primaryWorkspaceGitRepository = workspace.getPrimaryWorkspaceGitRepository();

			if (workspaceGitRepositoryDir.equals(primaryWorkspaceGitRepository.getDirectory())) {
				continue;
			}

			if (workspaceGitRepository instanceof
					PortalWorkspaceGitRepository) {

				GitWorkingDirectory gitWorkingDirectory =
					workspaceGitRepository.getGitWorkingDirectory();

				_pullRequests.put(
					gitWorkingDirectory.getUpstreamBranchName(),
					createPortalPullRequest(gitWorkingDirectory));
			}
		}
	}

	@Override
	public Workspace getWorkspace() {
		Workspace workspace = super.getWorkspace();

		WorkspaceGitRepository primaryWorkspaceGitRepository =
			workspace.getPrimaryWorkspaceGitRepository();

		primaryWorkspaceGitRepository.setRebase(true);

		PortalTopLevelBuildData portalTopLevelBuildData = getBuildData();

		primaryWorkspaceGitRepository.setGitHubURL(portalTopLevelBuildData.getPortalGitHubURL());

		return workspace;
	}

	@Override
	protected void setUpWorkspace() {
		super.setUpWorkspace();
	}

	@Override
	protected void validateBuildParameters() {
	}

	private String _getGitHubBranchName(String parameterName) {
		BuildData buildData = getBuildData();

		String gitHubURL = buildData.getBuildParameter(parameterName);

		if (JenkinsResultsParserUtil.isNullOrEmpty(gitHubURL)) {
			return null;
		}

		Matcher matcher = _gitHubURLPattern.matcher(gitHubURL);

		if (!matcher.find()) {
			return null;
		}

		return matcher.group("branch");
	}

	private String _getGitHubBranchUsername(String parameterName) {
		BuildData buildData = getBuildData();

		String gitHubURL = buildData.getBuildParameter(parameterName);

		if (JenkinsResultsParserUtil.isNullOrEmpty(gitHubURL)) {
			return null;
		}

		Matcher matcher = _gitHubURLPattern.matcher(gitHubURL);

		if (!matcher.find()) {
			return null;
		}

		return matcher.group("username");
	}

	private static final String _CI_TEST_SUITE = "poshi-release";

	private static final Pattern _gitHubURLPattern = Pattern.compile(
		"https://github.com/(?<username>[^/]+)/(?<repository>[^/]+)/" +
			"(commits|tree)/(?<branch>[^/]+)");

	private final Map<String, PullRequest> _pullRequests = new HashMap<>();
	private final Map<GitWorkingDirectory, RemoteGitBranch> _remoteGitBranches =
		new HashMap<>();

}