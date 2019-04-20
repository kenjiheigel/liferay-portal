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

import java.lang.reflect.Proxy;

/**
 * @author Michael Hashimoto
 */
public abstract class WorkspaceFactory {

	public static Workspace newBatchWorkspace(
		GitHubURL gitHubURL, String upstreamBranchName, String batchName,
		String branchSHA) {

		if (gitHubURL == null) {
			throw new RuntimeException("GitHub URL is null");
		}

		String repositoryName = gitHubURL.getRepositoryName();

		if (!repositoryName.contains("liferay-portal")) {
			throw new RuntimeException(
				"Unsupported GitHub URL " + gitHubURL.toString());
		}

		if (batchName == null) {
			batchName = "default";
		}

		Workspace workspace = null;

		if (batchName.contains("functional")) {
			workspace = new FunctionalBatchPortalWorkspace(
				gitHubURL, upstreamBranchName, branchSHA);
		}
		else if (batchName.contains("integration") ||
				 batchName.contains("unit")) {

			workspace = new JunitBatchPortalWorkspace(
				gitHubURL, upstreamBranchName, branchSHA);
		}
		else {
			workspace = new BatchPortalWorkspace(
				gitHubURL, upstreamBranchName, branchSHA);
		}

		if (workspace == null) {
			throw new RuntimeException("Invalid workspace");
		}

		if (workspace instanceof PortalWorkspace) {
			return (PortalWorkspace)Proxy.newProxyInstance(
				PortalWorkspace.class.getClassLoader(),
				new Class<?>[] {PortalWorkspace.class},
				new MethodLogger(workspace));
		}

		return (Workspace)Proxy.newProxyInstance(
			Workspace.class.getClassLoader(), new Class<?>[] {Workspace.class},
			new MethodLogger(workspace));
	}

	public static Workspace newSimpleWorkspace() {
		Workspace workspace = new SimpleWorkspace();

		return (Workspace)Proxy.newProxyInstance(
			Workspace.class.getClassLoader(), new Class<?>[] {Workspace.class},
			new MethodLogger(workspace));
	}

	public static Workspace newTopLevelWorkspace(
		GitHubURL gitHubURL, String upstreamBranchName) {

		if (gitHubURL == null) {
			throw new RuntimeException("GitHub URL is null");
		}

		String repositoryName = gitHubURL.getRepositoryName();

		if (!repositoryName.contains("liferay-portal")) {
			throw new RuntimeException(
				"Unsupported GitHub URL " + gitHubURL.toString());
		}

		Workspace workspace = new TopLevelPortalWorkspace(
			gitHubURL, upstreamBranchName);

		if (workspace instanceof PortalWorkspace) {
			return (PortalWorkspace)Proxy.newProxyInstance(
				PortalWorkspace.class.getClassLoader(),
				new Class<?>[] {PortalWorkspace.class},
				new MethodLogger(workspace));
		}

		return (Workspace)Proxy.newProxyInstance(
			Workspace.class.getClassLoader(), new Class<?>[] {Workspace.class},
			new MethodLogger(workspace));
	}

}