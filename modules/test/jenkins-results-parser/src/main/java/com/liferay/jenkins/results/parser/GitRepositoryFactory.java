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

import org.json.JSONObject;

/**
 * @author Peter Yoo
 */
public class GitRepositoryFactory {

	public static WorkspaceGitRepository getDependencyWorkspaceGitRepository(
		String repositoryType, WorkspaceGitRepository workspaceGitRepository,
		PullRequest pullRequest, String upstreamBranchName) {

		if (repositoryType.equals(CompanionPortalWorkspaceGitRepository.TYPE)) {
			return new CompanionPortalWorkspaceGitRepository(
				pullRequest, upstreamBranchName, workspaceGitRepository);
		}

		if (repositoryType.equals(OtherPortalWorkspaceGitRepository.TYPE)) {
			return new OtherPortalWorkspaceGitRepository(
				pullRequest, upstreamBranchName);
		}

		if (repositoryType.equals(PortalPluginsWorkspaceGitRepository.TYPE)) {
			return new PortalPluginsWorkspaceGitRepository(
				pullRequest, upstreamBranchName);
		}

		throw new RuntimeException(
			"Unsupported dependency workspace Git repository");
	}

	public static WorkspaceGitRepository getDependencyWorkspaceGitRepository(
		String repositoryType, WorkspaceGitRepository workspaceGitRepository,
		RemoteGitRef remoteGitRef, String upstreamBranchName) {

		if (repositoryType.equals(CompanionPortalWorkspaceGitRepository.TYPE)) {
			return new CompanionPortalWorkspaceGitRepository(
				remoteGitRef, upstreamBranchName, workspaceGitRepository);
		}

		if (repositoryType.equals(OtherPortalWorkspaceGitRepository.TYPE)) {
			return new OtherPortalWorkspaceGitRepository(
				remoteGitRef, upstreamBranchName);
		}

		if (repositoryType.equals(PortalPluginsWorkspaceGitRepository.TYPE)) {
			return new PortalPluginsWorkspaceGitRepository(
				remoteGitRef, upstreamBranchName);
		}

		throw new RuntimeException(
			"Unsupported dependency workspace Git repository");
	}

	public static LocalGitRepository getLocalGitRepository(
		String repositoryName, String upstreamBranchName) {

		return new DefaultLocalGitRepository(
			repositoryName, upstreamBranchName);
	}

	public static RemoteGitRepository getRemoteGitRepository(
		GitHubURL gitHubURL) {

		String hostname = gitHubURL.getHostname();

		if (hostname.equalsIgnoreCase("github.com")) {
			return new GitHubRemoteGitRepository(gitHubURL);
		}

		return new DefaultRemoteGitRepository(gitHubURL);
	}

	public static RemoteGitRepository getRemoteGitRepository(
		GitRemote gitRemote) {

		String hostname = gitRemote.getHostname();

		if (hostname.equalsIgnoreCase("github.com")) {
			return new GitHubRemoteGitRepository(gitRemote);
		}

		return new DefaultRemoteGitRepository(gitRemote);
	}

	public static WorkspaceGitRepository getWorkspaceGitRepository(
		GitHubURL gitHubURL, RemoteGitRef remoteGitRef,
		String upstreamBranchName) {

		String repositoryName = gitHubURL.getRepositoryName();

		if (repositoryName.equals("liferay-jenkins-ee")) {
			return new JenkinsWorkspaceGitRepository(
				remoteGitRef, upstreamBranchName);
		}

		if (repositoryName.equals("liferay-plugins")) {
			return new PluginsWorkspaceGitRepository(
				remoteGitRef, upstreamBranchName);
		}

		if (repositoryName.equals("liferay-portal")) {
			return new PrimaryPortalWorkspaceGitRepository(
				remoteGitRef, upstreamBranchName);
		}

		if (repositoryName.equals("liferay-qa-portal-legacy-ee")) {
			return new LegacyWorkspaceGitRepository(
				remoteGitRef, upstreamBranchName);
		}

		throw new RuntimeException("Unsupported workspace Git repository");
	}

	public static WorkspaceGitRepository getWorkspaceGitRepository(
		JSONObject jsonObject) {

		String jsonObjectType = _getType(jsonObject);

		if (jsonObjectType.equals(CompanionPortalWorkspaceGitRepository.TYPE)) {
			return new CompanionPortalWorkspaceGitRepository(jsonObject);
		}

		if (jsonObjectType.equals(PrimaryPortalWorkspaceGitRepository.TYPE)) {
			return new PrimaryPortalWorkspaceGitRepository(jsonObject);
		}

		if (jsonObjectType.equals(JenkinsWorkspaceGitRepository.TYPE)) {
			return new JenkinsWorkspaceGitRepository(jsonObject);
		}

		if (jsonObjectType.equals(LegacyWorkspaceGitRepository.TYPE)) {
			return new LegacyWorkspaceGitRepository(jsonObject);
		}

		if (jsonObjectType.equals(OtherPortalWorkspaceGitRepository.TYPE)) {
			return new OtherPortalWorkspaceGitRepository(jsonObject);
		}

		if (jsonObjectType.equals(PluginsWorkspaceGitRepository.TYPE)) {
			return new PluginsWorkspaceGitRepository(jsonObject);
		}

		if (jsonObjectType.equals(PortalPluginsWorkspaceGitRepository.TYPE)) {
			return new PortalPluginsWorkspaceGitRepository(jsonObject);
		}

		throw new RuntimeException("Invalid JSONObject " + jsonObject);
	}

	public static WorkspaceGitRepository getWorkspaceGitRepository(
		PullRequest pullRequest, String upstreamBranchName) {

		PullRequestGitHubURL gitHubURL = pullRequest.getGitHubURL();

		String repositoryName = gitHubURL.getRepositoryName();

		if (repositoryName.equals("liferay-jenkins-ee")) {
			return new JenkinsWorkspaceGitRepository(
				pullRequest, upstreamBranchName);
		}

		if (repositoryName.equals("liferay-plugins")) {
			return new PluginsWorkspaceGitRepository(
				pullRequest, upstreamBranchName);
		}

		if (repositoryName.equals("liferay-portal")) {
			return new PrimaryPortalWorkspaceGitRepository(
				pullRequest, upstreamBranchName);
		}

		if (repositoryName.equals("liferay-qa-portal-legacy-ee")) {
			return new LegacyWorkspaceGitRepository(
				pullRequest, upstreamBranchName);
		}

		throw new RuntimeException("Unsupported workspace Git repository");
	}

	private static String _getType(JSONObject jsonObject) {
		String type = jsonObject.optString("type");

		if (type == null) {
			return "";
		}

		return type;
	}

}