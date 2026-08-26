/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.io.File;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Brittney Nguyen
 */
public class PortalGitWorkingDirectoryTest
	extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testGetFilteredEnvironment() throws Exception {
		mockEnvironment(
			Collections.singletonMap("MASTER_NETWORK_NAME", "aws-network"));

		Properties buildProperties = new Properties();

		buildProperties.setProperty(
			"ant.opts.default[ee-7.4.x]", "-Xmx8g -Dee74");
		buildProperties.setProperty(
			"ant.opts.default[master]", "-Xmx4g -Dmaster");
		buildProperties.setProperty(
			"java.jdk.default.runtime[ee-7.4.x]", "/opt/java/jdk-zulu8-ee74");
		buildProperties.setProperty(
			"java.jdk.default.runtime[master]", "/opt/java/jdk-zulu11-master");

		JenkinsResultsParserUtil.setBuildProperties(buildProperties);

		Map<String, String> masterFilteredEnvironment = _getFilteredEnvironment(
			"master");

		testEquals(
			"-Xmx4g -Dmaster", masterFilteredEnvironment.get("ANT_OPTS"));
		testEquals(
			"-Xmx4g -Dmaster", masterFilteredEnvironment.get("JAVA_OPTS"));
		testEquals(
			"/opt/java/jdk-zulu11-master",
			masterFilteredEnvironment.get("JAVA_HOME"));

		Map<String, String> releaseFilteredEnvironment =
			_getFilteredEnvironment("ee-7.4.x");

		testEquals("-Xmx8g -Dee74", releaseFilteredEnvironment.get("ANT_OPTS"));
		testEquals(
			"-Xmx8g -Dee74", releaseFilteredEnvironment.get("JAVA_OPTS"));
		testEquals(
			"/opt/java/jdk-zulu8-ee74",
			releaseFilteredEnvironment.get("JAVA_HOME"));
	}

	@Test
	public void testGetModuleBaseDirs() throws Exception {
		File workingDirectory = JenkinsResultsParserUtil.getGitWorkingDir(
			new File("."));

		PortalGitWorkingDirectory portalGitWorkingDirectory = Mockito.mock(
			PortalGitWorkingDirectory.class);

		Mockito.doReturn(
			workingDirectory
		).when(
			portalGitWorkingDirectory
		).getWorkingDirectory();

		Mockito.doCallRealMethod(
		).when(
			portalGitWorkingDirectory
		).getModuleBaseDirs();

		Set<String> moduleBaseDirNames = new HashSet<>();

		for (File moduleBaseDir :
				portalGitWorkingDirectory.getModuleBaseDirs()) {

			moduleBaseDirNames.add(moduleBaseDir.getName());
		}

		Assert.assertTrue(
			"Missing modules base directory: " + moduleBaseDirNames,
			moduleBaseDirNames.contains("modules"));
		Assert.assertTrue(
			"Missing workspaces base directory: " + moduleBaseDirNames,
			moduleBaseDirNames.contains("workspaces"));
	}

	@Test
	public void testGetModuleDirsListDiscoversWorkspaceModules()
		throws Exception {

		File workingDirectory = JenkinsResultsParserUtil.getGitWorkingDir(
			new File("."));

		File osbFaroWorkspaceDir = new File(
			workingDirectory, "workspaces/liferay-osbfaro-workspace");

		PortalGitWorkingDirectory portalGitWorkingDirectory = Mockito.mock(
			PortalGitWorkingDirectory.class);

		Mockito.doReturn(
			Collections.singletonList(osbFaroWorkspaceDir)
		).when(
			portalGitWorkingDirectory
		).getModuleBaseDirs();

		Mockito.doCallRealMethod(
		).when(
			portalGitWorkingDirectory
		).getModuleDirsList(
			Mockito.anyList(), Mockito.anyList()
		);

		List<File> moduleDirsList = portalGitWorkingDirectory.getModuleDirsList(
			Collections.emptyList(), Collections.emptyList());

		boolean discoveredWorkspaceModule = false;

		for (File moduleDir : moduleDirsList) {
			String moduleDirPath = JenkinsResultsParserUtil.getCanonicalPath(
				moduleDir);

			if (moduleDirPath.contains("liferay-osbfaro-workspace")) {
				discoveredWorkspaceModule = true;

				break;
			}
		}

		Assert.assertTrue(
			"No osb-faro workspace module discovered: " + moduleDirsList,
			discoveredWorkspaceModule);
	}

	private Map<String, String> _getFilteredEnvironment(
			String upstreamBranchName)
		throws Exception {

		PortalGitWorkingDirectory portalGitWorkingDirectory = Mockito.mock(
			PortalGitWorkingDirectory.class);

		Mockito.doCallRealMethod(
		).when(
			portalGitWorkingDirectory
		).getFilteredEnvironment();

		Mockito.when(
			portalGitWorkingDirectory.getUpstreamBranchName()
		).thenReturn(
			upstreamBranchName
		);

		return portalGitWorkingDirectory.getFilteredEnvironment();
	}

}