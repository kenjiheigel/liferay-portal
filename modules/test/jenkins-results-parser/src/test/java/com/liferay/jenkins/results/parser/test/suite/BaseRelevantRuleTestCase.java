/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.test.suite;

import com.liferay.jenkins.results.parser.GitWorkingDirectoryFactory;
import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;
import com.liferay.jenkins.results.parser.Job;
import com.liferay.jenkins.results.parser.JobFactory;
import com.liferay.jenkins.results.parser.PortalAcceptancePullRequestJob;
import com.liferay.jenkins.results.parser.PortalGitWorkingDirectory;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;

import org.junit.After;

/**
 * @author Kenji Heigel
 */
public abstract class BaseRelevantRuleTestCase {

	@After
	public void tearDown() {
		RelevantRuleEngine.clear();
	}

	protected File getBaseDir() {
		if (_baseDir != null) {
			return _baseDir;
		}

		File baseDir = new File(
			"src/test/resources/dependencies/test/suite" +
				"/RelevantRuleEngineTest");

		_baseDir = JenkinsResultsParserUtil.getCanonicalFile(baseDir);

		return _baseDir;
	}

	protected PortalAcceptancePullRequestJob
		getPortalAcceptancePullRequestJob() {

		if (_portalAcceptancePullRequestJob != null) {
			return _portalAcceptancePullRequestJob;
		}

		String upstreamBranchName = "master";
		String repositoryName = "liferay-portal";

		_portalAcceptancePullRequestJob =
			(PortalAcceptancePullRequestJob)JobFactory.newJob(
				Job.BuildProfile.DXP,
				"test-portal-acceptance-pullrequest(master)",
				new JSONObject(
				).put(
					"build_profile", "DXP"
				).put(
					"git_repository_dir", "liferay-portal"
				).put(
					"job_name", "test-portal-acceptance-pullrequest("
				).put(
					"test_suite_name", "relevant"
				).put(
					"upstream_branch_name", "master"
				),
				(PortalGitWorkingDirectory)
					GitWorkingDirectoryFactory.newGitWorkingDirectory(
						upstreamBranchName, getPortalDir(), repositoryName),
				upstreamBranchName, null, repositoryName, "relevant",
				upstreamBranchName);

		List<File> jobPropertiesFiles =
			_portalAcceptancePullRequestJob.getJobPropertiesFiles();

		jobPropertiesFiles.clear();

		jobPropertiesFiles.add(new File(getBaseDir(), "test.properties"));

		return _portalAcceptancePullRequestJob;
	}

	protected File getPortalDir() {
		File workingDir = JenkinsResultsParserUtil.getCanonicalFile(
			new File("."));

		try {
			Process process = JenkinsResultsParserUtil.executeBashCommands(
				workingDir, true, false, 5000, "git rev-parse --show-toplevel");

			if (process.exitValue() != 0) {
				throw new RuntimeException(
					"Unable to find portal directory from: " + workingDir);
			}

			String output = JenkinsResultsParserUtil.readInputStream(
				process.getInputStream());

			return new File(output.split("\\R", 2)[0].trim());
		}
		catch (IOException | TimeoutException exception) {
			throw new RuntimeException(
				"Unable to find portal directory from: " + workingDir,
				exception);
		}
	}

	protected RelevantRuleEngine getRelevantRuleEngine() {
		RelevantRuleEngine relevantRuleEngine = RelevantRuleEngine.getInstance(
			getPortalAcceptancePullRequestJob());

		relevantRuleEngine.setBaseDir(getBaseDir());

		return relevantRuleEngine;
	}

	private File _baseDir;
	private PortalAcceptancePullRequestJob _portalAcceptancePullRequestJob;

}