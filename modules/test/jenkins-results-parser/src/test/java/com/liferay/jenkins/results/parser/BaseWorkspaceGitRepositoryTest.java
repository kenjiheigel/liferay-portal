/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import java.io.File;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.json.JSONObject;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

/**
 * @author Kenji Heigel
 */
public class BaseWorkspaceGitRepositoryTest
	extends com.liferay.jenkins.results.parser.Test {

	@Test
	public void testDownloadGitArchives() throws Exception {
		_testDownloadGitArchives(
			false, "test-portal-stable(7.3.x)", "compile", false);
		_testDownloadGitArchives(
			false, "test-portal-stable(master)", null, false);
		_testDownloadGitArchives(true, "test-portal-release", "compile", true);
		_testDownloadGitArchives(
			true, "test-portal-stable(7.3.x)", "service-builder", false);
		_testDownloadGitArchives(
			true, "test-portal-stable(master)", "service-builder", false);
		_testDownloadGitArchives(
			true, "test-portal-upstream(7.4.x)", "rest-builder", false);
	}

	@Test
	public void testGetGitWorkingDirectory() throws Exception {
		_testGetGitWorkingDirectory(false, false, false, false);
		_testGetGitWorkingDirectory(false, false, false, true);
		_testGetGitWorkingDirectory(false, false, true, false);
		_testGetGitWorkingDirectory(false, true, true, true);
		_testGetGitWorkingDirectory(true, false, true, true);
	}

	@Test
	public void testPrepareGitWorkingDirectory() throws Exception {
		_testPrepareGitWorkingDirectory(false, false);
		_testPrepareGitWorkingDirectory(false, true);
		_testPrepareGitWorkingDirectory(true, false);
		_testPrepareGitWorkingDirectory(true, true);
	}

	private BaseWorkspaceGitRepository _newWorkspaceGitRepository(
			String directoryName)
		throws Exception {

		File workingDirectory = File.createTempFile("workspace-", null);

		workingDirectory.delete();

		workingDirectory.mkdir();

		String baseBranchSHA = RandomTestUtil.randomSHA();
		String baseBranchUsername = RandomTestUtil.randomString();
		String senderBranchSHA = RandomTestUtil.randomSHA();

		JSONObject jsonObject = new JSONObject();

		jsonObject.put(
			"base_branch_head_sha", baseBranchSHA
		).put(
			"base_branch_sha", baseBranchSHA
		).put(
			"base_branch_username", baseBranchUsername
		).put(
			"directory",
			JenkinsResultsParserUtil.getCanonicalPath(workingDirectory)
		).put(
			"directory_name", directoryName
		).put(
			"git_hub_url",
			JenkinsResultsParserUtil.combine(
				"https://github.com/", baseBranchUsername, "/", directoryName)
		).put(
			"name", directoryName
		).put(
			"sender_branch_head_sha", senderBranchSHA
		).put(
			"sender_branch_name", "master"
		).put(
			"sender_branch_sha", senderBranchSHA
		).put(
			"sender_branch_username", RandomTestUtil.randomString()
		).put(
			"upstream_branch_name", "master"
		);

		return Mockito.spy(
			new BaseWorkspaceGitRepository(jsonObject) {
			});
	}

	private void _setUpEnvironment(
		String jobName, String jobVariant, boolean topLevelJobName) {

		Map<String, String> environmentValues = new HashMap<>();

		if (jobVariant != null) {
			environmentValues.put("JOB_VARIANT", jobVariant);
		}

		environmentValues.put("JOB_NAME", jobName);
		environmentValues.put("MASTER_NETWORK_NAME", "aws-network");

		mockEnvironment(environmentValues);

		Set<String> topLevelJobNames = new HashSet<>();

		if (topLevelJobName) {
			topLevelJobNames.add(jobName);
		}

		JenkinsResultsParserUtil.setTopLevelJobNames(topLevelJobNames);
	}

	private void _testDownloadGitArchives(
			boolean dotGitDirArchiveRequired, String jobName, String jobVariant,
			boolean topLevelJobName)
		throws Exception {

		_setUpEnvironment(jobName, jobVariant, topLevelJobName);

		Properties buildProperties = new Properties();

		buildProperties.setProperty(
			"git.archive.dot.git.dir.required[liferay-portal*][*rest-builder*]",
			"true");
		buildProperties.setProperty(
			"git.archive.dot.git.dir.required[liferay-portal*]" +
				"[*service-builder*]",
			"true");

		JenkinsResultsParserUtil.setBuildProperties(buildProperties);

		BaseWorkspaceGitRepository baseWorkspaceGitRepository =
			_newWorkspaceGitRepository("liferay-portal");

		Mockito.doNothing(
		).when(
			baseWorkspaceGitRepository
		).downloadDotGitArchive();

		Mockito.doNothing(
		).when(
			baseWorkspaceGitRepository
		).downloadGitArchive();

		baseWorkspaceGitRepository.downloadGitArchives();

		VerificationMode verificationMode = Mockito.never();

		if (dotGitDirArchiveRequired) {
			verificationMode = Mockito.times(1);
		}

		Mockito.verify(
			baseWorkspaceGitRepository, verificationMode
		).downloadDotGitArchive();

		Mockito.verify(
			baseWorkspaceGitRepository, Mockito.times(1)
		).downloadGitArchive();
	}

	private void _testGetGitWorkingDirectory(
			boolean dotGitDirArchiveRequired, boolean exceptionThrown,
			boolean gitArchiveEnabled, boolean snapshot)
		throws Exception {

		_setUpEnvironment(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			false);

		Properties buildProperties = new Properties();

		buildProperties.setProperty(
			"git.archive.dot.git.dir.required",
			String.valueOf(dotGitDirArchiveRequired));
		buildProperties.setProperty(
			"git.archive.enabled", String.valueOf(gitArchiveEnabled));

		JenkinsResultsParserUtil.setBuildProperties(buildProperties);

		BaseWorkspaceGitRepository baseWorkspaceGitRepository =
			_newWorkspaceGitRepository(RandomTestUtil.randomString());

		GitWorkingDirectory gitWorkingDirectory = Mockito.mock(
			GitWorkingDirectory.class);

		ReflectionTestUtil.setFieldValue(
			baseWorkspaceGitRepository, "_gitWorkingDirectory",
			gitWorkingDirectory);

		baseWorkspaceGitRepository.setSnapshot(snapshot);

		if (exceptionThrown) {
			try {
				baseWorkspaceGitRepository.getGitWorkingDirectory();

				Assert.fail("Expected RuntimeException");
			}
			catch (RuntimeException runtimeException) {
				testEquals(
					"Using Git archive, unable to get Git working directory",
					runtimeException.getMessage());
			}

			return;
		}

		testSame(
			gitWorkingDirectory,
			baseWorkspaceGitRepository.getGitWorkingDirectory());
	}

	private void _testPrepareGitWorkingDirectory(
			boolean buildCachingEnabled, boolean gitArchiveEnabled)
		throws Exception {

		_setUpEnvironment(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			false);

		Properties buildProperties = new Properties();

		buildProperties.setProperty(
			"build.caching.enabled", String.valueOf(buildCachingEnabled));
		buildProperties.setProperty(
			"git.archive.enabled", String.valueOf(gitArchiveEnabled));

		JenkinsResultsParserUtil.setBuildProperties(buildProperties);

		BaseWorkspaceGitRepository baseWorkspaceGitRepository =
			_newWorkspaceGitRepository(RandomTestUtil.randomString());

		Mockito.doNothing(
		).when(
			baseWorkspaceGitRepository
		).downloadGitArchives();

		Mockito.doNothing(
		).when(
			baseWorkspaceGitRepository
		).initializeGitWorkingDirectory();

		baseWorkspaceGitRepository.setSnapshot(true);

		baseWorkspaceGitRepository.prepareGitWorkingDirectory();

		VerificationMode archiveVerificationMode = Mockito.never();
		VerificationMode cloneVerificationMode = Mockito.times(1);

		if (gitArchiveEnabled) {
			archiveVerificationMode = Mockito.times(1);
			cloneVerificationMode = Mockito.never();
		}

		Mockito.verify(
			baseWorkspaceGitRepository, archiveVerificationMode
		).downloadGitArchives();

		Mockito.verify(
			baseWorkspaceGitRepository, cloneVerificationMode
		).initializeGitWorkingDirectory();

		Mockito.verify(
			baseWorkspaceGitRepository, archiveVerificationMode
		).promoteGitArchive();
	}

}