/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.test.clazz.group;

import com.liferay.jenkins.results.parser.GitWorkingDirectoryFactory;
import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;
import com.liferay.jenkins.results.parser.Job;
import com.liferay.jenkins.results.parser.JobFactory;
import com.liferay.jenkins.results.parser.PortalGitWorkingDirectory;
import com.liferay.jenkins.results.parser.PortalTestClassJob;
import com.liferay.jenkins.results.parser.ReflectionTestUtil;
import com.liferay.jenkins.results.parser.job.property.JobPropertyFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.nio.file.Files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mockito.Mockito;

/**
 * @author Kenji Heigel
 */
public class BatchTestClassGroupTestUtil {

	public static String getGitRemotesShellCommandOutput() {
		return _GIT_REMOTES_SHELL_COMMAND_OUTPUT;
	}

	public static PortalTestClassJob getPortalTestClassJob() {
		return _setJobPropertiesFiles();
	}

	public static PortalTestClassJob getPortalTestClassJob(
		Properties jobProperties) {

		return _setJobPropertiesFiles(_writeJobPropertiesFile(jobProperties));
	}

	public static PortalTestClassJob getPortalTestClassJob(
		Properties jobProperties, List<File> modifiedFiles,
		File workingDirectory) {

		PortalTestClassJob portalTestClassJob = getPortalTestClassJob(
			jobProperties);

		PortalGitWorkingDirectory portalGitWorkingDirectory =
			portalTestClassJob.getPortalGitWorkingDirectory();

		Mockito.doReturn(
			modifiedFiles
		).when(
			portalGitWorkingDirectory
		).getModifiedFilesList();

		Mockito.doReturn(
			modifiedFiles
		).when(
			portalGitWorkingDirectory
		).getModifiedFilesList(
			Mockito.anyBoolean(), Mockito.any(), Mockito.anyList()
		);

		Mockito.doReturn(
			workingDirectory
		).when(
			portalGitWorkingDirectory
		).getWorkingDirectory();

		return portalTestClassJob;
	}

	public static CompileModulesBatchTestClassGroup
		newCompileModulesBatchTestClassGroup(
			Properties jobProperties, File... modifiedModuleDirs) {

		PortalTestClassJob portalTestClassJob = getPortalTestClassJob(
			jobProperties);

		PortalGitWorkingDirectory portalGitWorkingDirectory =
			portalTestClassJob.getPortalGitWorkingDirectory();

		try {
			Mockito.doReturn(
				Arrays.asList(modifiedModuleDirs)
			).when(
				portalGitWorkingDirectory
			).getModifiedModuleDirsList(
				Mockito.anyList(), Mockito.anyList()
			);
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}

		return new CompileModulesBatchTestClassGroup(
			"modules-compile", portalTestClassJob);
	}

	public static ServiceBuilderModulesBatchTestClassGroup
		newServiceBuilderModulesBatchTestClassGroup(
			String... modifiedFilePaths) {

		List<File> modifiedFiles = new ArrayList<>();

		PortalTestClassJob portalTestClassJob = getPortalTestClassJob();

		PortalGitWorkingDirectory portalGitWorkingDirectory =
			portalTestClassJob.getPortalGitWorkingDirectory();

		File workingDirectory = portalGitWorkingDirectory.getWorkingDirectory();

		for (String modifiedFilePath : modifiedFilePaths) {
			modifiedFiles.add(new File(workingDirectory, modifiedFilePath));
		}

		Mockito.doReturn(
			modifiedFiles
		).when(
			portalGitWorkingDirectory
		).getModifiedFilesList();

		return new ServiceBuilderModulesBatchTestClassGroup(
			"service-builder-modules", portalTestClassJob);
	}

	public static File newTestClassFile(String className, File parentDir)
		throws IOException {

		File testClassFile = new File(parentDir, className + ".java");

		String testClassFileContent = _getTestClassFileContent(className);

		Files.write(
			testClassFile.toPath(), testClassFileContent.getBytes("UTF-8"));

		return testClassFile;
	}

	public static void resetCaches() {
		Set<String> javaDirPathStrings = ReflectionTestUtil.getFieldValue(
			JUnitBatchTestClassGroup.class, "_javaDirPathStrings");

		javaDirPathStrings.clear();

		AtomicBoolean javaFilesLoaded = ReflectionTestUtil.getFieldValue(
			JUnitBatchTestClassGroup.class, "_javaFilesLoaded");

		javaFilesLoaded.set(false);

		Set<File> javaTestClassFiles = ReflectionTestUtil.getFieldValue(
			JUnitBatchTestClassGroup.class, "_javaTestClassFiles");

		javaTestClassFiles.clear();

		Map<String, ?> jobProperties = ReflectionTestUtil.getFieldValue(
			JobPropertyFactory.class, "_jobProperties");

		jobProperties.clear();

		if (_portalTestClassJob != null) {
			Mockito.doReturn(
				_workingDirectory
			).when(
				_portalTestClassJob.getPortalGitWorkingDirectory()
			).getWorkingDirectory();
		}
	}

	private static PortalTestClassJob _getPortalTestClassJob() {
		if (_portalTestClassJob != null) {
			return _portalTestClassJob;
		}

		String repositoryName = "liferay-portal";
		String upstreamBranchName = "master";

		PortalGitWorkingDirectory portalGitWorkingDirectory = Mockito.spy(
			(PortalGitWorkingDirectory)
				GitWorkingDirectoryFactory.newGitWorkingDirectory(
					upstreamBranchName,
					JenkinsResultsParserUtil.getGitWorkingDir(new File(".")),
					repositoryName));

		try {
			Mockito.doReturn(
				Collections.emptyList()
			).when(
				portalGitWorkingDirectory
			).getModifiedModuleDirsList(
				Mockito.anyList(), Mockito.anyList()
			);

			Mockito.doReturn(
				Collections.emptyList()
			).when(
				portalGitWorkingDirectory
			).getModifiedNonposhiModules();

			Mockito.doReturn(
				Collections.emptyList()
			).when(
				portalGitWorkingDirectory
			).getModifiedPoshiModules();
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}

		_workingDirectory = portalGitWorkingDirectory.getWorkingDirectory();

		_portalTestClassJob = (PortalTestClassJob)JobFactory.newJob(
			Job.BuildProfile.DXP, "test-portal-acceptance-pullrequest(master)",
			null, portalGitWorkingDirectory, upstreamBranchName, null,
			repositoryName, "relevant", upstreamBranchName);

		return _portalTestClassJob;
	}

	private static String _getTestClassFileContent(String className) {
		return JenkinsResultsParserUtil.combine(
			"public class ", className, " {\n\n\t@Test\n\tpublic void ",
			"testSample() {\n\t}\n\n}");
	}

	private static PortalTestClassJob _setJobPropertiesFiles(
		File... overrideJobPropertiesFiles) {

		PortalTestClassJob portalTestClassJob = _getPortalTestClassJob();

		List<File> jobPropertiesFiles =
			portalTestClassJob.getJobPropertiesFiles();

		jobPropertiesFiles.clear();

		Collections.addAll(jobPropertiesFiles, overrideJobPropertiesFiles);

		jobPropertiesFiles.add(new File(_JOB_PROPERTIES_FILE_PATH));

		return portalTestClassJob;
	}

	private static File _writeJobPropertiesFile(Properties jobProperties) {
		try {
			File jobPropertiesFile = File.createTempFile(
				"BatchTestClassGroupTestUtil", ".properties");

			jobPropertiesFile.deleteOnExit();

			try (OutputStream outputStream = new FileOutputStream(
					jobPropertiesFile)) {

				jobProperties.store(outputStream, null);
			}

			return jobPropertiesFile;
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	private static final String _GIT_REMOTES_SHELL_COMMAND_OUTPUT =
		"upstream\tgit@github.com:liferay/liferay-portal.git (fetch)\n" +
			"upstream\tgit@github.com:liferay/liferay-portal.git (push)\n";

	private static final String _JOB_PROPERTIES_FILE_PATH =
		"src/test/resources/dependencies/test/clazz/group" +
			"/BatchTestClassGroupTestUtil/test.properties";

	private static PortalTestClassJob _portalTestClassJob;
	private static File _workingDirectory;

}