/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.io.File;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
		temporaryFolder.newFolder("modules");
		temporaryFolder.newFolder("workspaces");

		Assert.assertEquals(
			new HashSet<>(Arrays.asList("modules", "workspaces")),
			_getModuleBaseDirNames());
	}

	@Test
	public void testGetModuleBaseDirsWithoutWorkspaces() throws Exception {
		temporaryFolder.newFolder("modules");

		Assert.assertEquals(
			Collections.singleton("modules"), _getModuleBaseDirNames());
	}

	@Test
	public void testGetModuleDirsListDiscoversWorkspaceModules()
		throws Exception {

		File workspaceModuleDir = _newModuleDir(
			"workspaces", "liferay-test-workspace", "modules", "test-module");

		PortalGitWorkingDirectory portalGitWorkingDirectory =
			_mockPortalGitWorkingDirectory(temporaryFolder.getRoot());

		Mockito.doCallRealMethod(
		).when(
			portalGitWorkingDirectory
		).getModuleDirsList(
			Mockito.anyList(), Mockito.anyList(), Mockito.anyList()
		);

		List<File> moduleDirs = portalGitWorkingDirectory.getModuleDirsList(
			Collections.emptyList(), Collections.emptyList(),
			portalGitWorkingDirectory.getModuleBaseDirs());

		Assert.assertEquals(moduleDirs.toString(), 1, moduleDirs.size());
		Assert.assertEquals(
			JenkinsResultsParserUtil.getCanonicalPath(workspaceModuleDir),
			JenkinsResultsParserUtil.getCanonicalPath(moduleDirs.get(0)));
	}

	@Test
	public void testGetModuleDirsListExcludesWorkspaceModules()
		throws Exception {

		File moduleDir = _newModuleDir("modules", "test-module");

		_newModuleDir(
			"workspaces", "liferay-test-workspace", "modules", "test-module");

		PortalGitWorkingDirectory portalGitWorkingDirectory =
			_mockPortalGitWorkingDirectory(temporaryFolder.getRoot());

		Mockito.doCallRealMethod(
		).when(
			portalGitWorkingDirectory
		).getModuleDirsList(
			Mockito.anyList(), Mockito.anyList()
		);

		Mockito.doCallRealMethod(
		).when(
			portalGitWorkingDirectory
		).getModuleDirsList(
			Mockito.anyList(), Mockito.anyList(), Mockito.anyList()
		);

		List<File> moduleDirs = portalGitWorkingDirectory.getModuleDirsList(
			Collections.emptyList(), Collections.emptyList());

		Assert.assertEquals(moduleDirs.toString(), 1, moduleDirs.size());
		Assert.assertEquals(
			JenkinsResultsParserUtil.getCanonicalPath(moduleDir),
			JenkinsResultsParserUtil.getCanonicalPath(moduleDirs.get(0)));
	}

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

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

	private Set<String> _getModuleBaseDirNames() {
		Set<String> moduleBaseDirNames = new HashSet<>();

		PortalGitWorkingDirectory portalGitWorkingDirectory =
			_mockPortalGitWorkingDirectory(temporaryFolder.getRoot());

		for (File moduleBaseDir :
				portalGitWorkingDirectory.getModuleBaseDirs()) {

			moduleBaseDirNames.add(moduleBaseDir.getName());
		}

		return moduleBaseDirNames;
	}

	private PortalGitWorkingDirectory _mockPortalGitWorkingDirectory(
		File workingDirectory) {

		PortalGitWorkingDirectory portalGitWorkingDirectory = Mockito.mock(
			PortalGitWorkingDirectory.class);

		Mockito.doCallRealMethod(
		).when(
			portalGitWorkingDirectory
		).getModuleBaseDirs();

		Mockito.doReturn(
			workingDirectory
		).when(
			portalGitWorkingDirectory
		).getWorkingDirectory();

		return portalGitWorkingDirectory;
	}

	private File _newModuleDir(String... pathNames) throws Exception {
		File moduleDir = temporaryFolder.newFolder(pathNames);

		File bndBndFile = new File(moduleDir, "bnd.bnd");

		bndBndFile.createNewFile();

		return moduleDir;
	}

}