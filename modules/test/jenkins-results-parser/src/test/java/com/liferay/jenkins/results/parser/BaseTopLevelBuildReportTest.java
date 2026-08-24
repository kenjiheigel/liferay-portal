/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser;

import com.liferay.jenkins.results.parser.testray.TestrayCloudBucket;

import java.net.URL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Calum Ragan
 */
public class BaseTopLevelBuildReportTest
	extends com.liferay.jenkins.results.parser.Test {

	@Before
	public void setUpCaches() {
		Map<String, ?> topLevelBuildReports = ReflectionTestUtil.getFieldValue(
			BuildReportFactory.class, "_topLevelBuildReports");

		topLevelBuildReports.clear();

		ReflectionTestUtil.setFieldValue(
			TestrayCloudBucket.class, "_hasGoogleApplicationCredentials", null);
	}

	@Test
	public void testAccessorsWithBuildReport() {
		String testSuiteName = RandomTestUtil.randomString();

		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(
				new JSONObject(
				).put(
					"testSuiteName", testSuiteName
				).put(
					"totalActualDuration", 1000L
				).put(
					"totalCachedDuration", 2000L
				).put(
					"totalDuration", 3000L
				));

		Assert.assertEquals(
			testSuiteName, baseTopLevelBuildReport.getTestSuiteName());

		Assert.assertEquals(
			1000L, baseTopLevelBuildReport.getTotalActualDuration());
		Assert.assertEquals(
			2000L, baseTopLevelBuildReport.getTotalCachedDuration());
		Assert.assertEquals(3000L, baseTopLevelBuildReport.getTotalDuration());
	}

	@Test
	public void testAccessorsWithoutBuildReport() throws Exception {
		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(null);

		Assert.assertNull(baseTopLevelBuildReport.getControllerBuildReport());
		Assert.assertNull(baseTopLevelBuildReport.getTestSuiteName());

		Assert.assertEquals(
			0L, baseTopLevelBuildReport.getTotalActualDuration());
		Assert.assertEquals(
			0L, baseTopLevelBuildReport.getTotalCachedDuration());
		Assert.assertEquals(0L, baseTopLevelBuildReport.getTotalDuration());

		baseTopLevelBuildReport.addTestrayAttachmentURL(
			new URL("https://test-1-1/attachment"));
	}

	@Test
	public void testAddDownstreamBuildReport() {
		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(new JSONObject());

		baseTopLevelBuildReport.addDownstreamBuildReport(null);

		List<DownstreamBuildReport> downstreamBuildReports =
			baseTopLevelBuildReport.getDownstreamBuildReports();

		Assert.assertEquals(
			downstreamBuildReports.toString(), 0,
			downstreamBuildReports.size());

		DownstreamBuildReport cachedDownstreamBuildReport =
			_newDownstreamBuildReport(RandomTestUtil.randomString(), true);
		DownstreamBuildReport downstreamBuildReport = _newDownstreamBuildReport(
			RandomTestUtil.randomString(), false);

		baseTopLevelBuildReport.addDownstreamBuildReport(
			cachedDownstreamBuildReport);
		baseTopLevelBuildReport.addDownstreamBuildReport(downstreamBuildReport);

		baseTopLevelBuildReport.addDownstreamBuildReport(
			cachedDownstreamBuildReport);
		baseTopLevelBuildReport.addDownstreamBuildReport(downstreamBuildReport);

		downstreamBuildReports =
			baseTopLevelBuildReport.getDownstreamBuildReports();

		Assert.assertEquals(
			downstreamBuildReports.toString(), 2,
			downstreamBuildReports.size());
	}

	@Test
	public void testAddDownstreamBuildReports() {
		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(new JSONObject());

		baseTopLevelBuildReport.addDownstreamBuildReports(null);

		List<DownstreamBuildReport> downstreamBuildReports =
			baseTopLevelBuildReport.getDownstreamBuildReports();

		Assert.assertEquals(
			downstreamBuildReports.toString(), 0,
			downstreamBuildReports.size());

		DownstreamBuildReport cachedDownstreamBuildReport =
			_newDownstreamBuildReport(RandomTestUtil.randomString(), true);
		DownstreamBuildReport downstreamBuildReport = _newDownstreamBuildReport(
			RandomTestUtil.randomString(), false);

		baseTopLevelBuildReport.addDownstreamBuildReports(
			Arrays.asList(cachedDownstreamBuildReport, downstreamBuildReport));

		downstreamBuildReports =
			baseTopLevelBuildReport.getDownstreamBuildReports();

		Assert.assertEquals(
			downstreamBuildReports.toString(), 2,
			downstreamBuildReports.size());
		Assert.assertTrue(
			downstreamBuildReports.contains(cachedDownstreamBuildReport));
		Assert.assertTrue(
			downstreamBuildReports.contains(downstreamBuildReport));
	}

	@Test
	public void testAddTestrayAttachmentURL() throws Exception {
		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(new JSONObject());

		baseTopLevelBuildReport.addTestrayAttachmentURL(
			new URL("https://test-1-1/first-attachment"));

		List<URL> testrayAttachmentURLs =
			baseTopLevelBuildReport.getTestrayAttachmentURLs();

		Assert.assertEquals(
			testrayAttachmentURLs.toString(), 1, testrayAttachmentURLs.size());

		baseTopLevelBuildReport.addTestrayAttachmentURL(
			new URL("https://test-1-1/second-attachment"));

		testrayAttachmentURLs =
			baseTopLevelBuildReport.getTestrayAttachmentURLs();

		Assert.assertEquals(
			testrayAttachmentURLs.toString(), 2, testrayAttachmentURLs.size());
		Assert.assertEquals(
			"https://test-1-1/second-attachment",
			String.valueOf(testrayAttachmentURLs.get(1)));
	}

	@Test
	public void testGetBuildProfile() {
		_testGetBuildProfile("portal", Job.BuildProfile.PORTAL);
		_testGetBuildProfile(
			RandomTestUtil.randomString(), Job.BuildProfile.DXP);
		_testGetBuildProfile(null, Job.BuildProfile.DXP);
	}

	@Test
	public void testGetBuildReportTestrayCloudObject() {
		mockEnvironment(Collections.<String, String>emptyMap());

		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(new JSONObject());

		Assert.assertNull(
			baseTopLevelBuildReport.getBuildReportTestrayCloudObject());
	}

	@Test
	public void testGetControllerBuildReport() {
		JSONObject buildReportJSONObject = new JSONObject();

		buildReportJSONObject.put("controller", new JSONObject());

		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(buildReportJSONObject);

		Assert.assertNull(baseTopLevelBuildReport.getControllerBuildReport());

		JSONObject controllerJSONObject = new JSONObject();

		controllerJSONObject.put(
			"buildURL", "https://test-1-1/job/controller-job/7"
		).put(
			"duration", 1000L
		).put(
			"result", "SUCCESS"
		);

		buildReportJSONObject.put("controller", controllerJSONObject);

		baseTopLevelBuildReport = _newBaseTopLevelBuildReport(
			buildReportJSONObject);

		ControllerBuildReport controllerBuildReport =
			baseTopLevelBuildReport.getControllerBuildReport();

		Assert.assertNotNull(controllerBuildReport);
		Assert.assertSame(
			controllerBuildReport,
			baseTopLevelBuildReport.getControllerBuildReport());
	}

	@Test
	public void testGetDistinctFailureReports() {
		FailureReport failureReport = Mockito.mock(FailureReport.class);
		FailureReport similarFailureReport = Mockito.mock(FailureReport.class);
		FailureReport uniqueFailureReport = Mockito.mock(FailureReport.class);

		Mockito.doReturn(
			true
		).when(
			similarFailureReport
		).isSimilar(
			failureReport
		);

		BaseTopLevelBuildReport baseTopLevelBuildReport = Mockito.mock(
			BaseTopLevelBuildReport.class);

		Mockito.doCallRealMethod(
		).when(
			baseTopLevelBuildReport
		).getDistinctFailureReports();

		Mockito.doReturn(
			Arrays.asList(
				failureReport, similarFailureReport, uniqueFailureReport)
		).when(
			baseTopLevelBuildReport
		).getFailureReports();

		List<FailureReport> distinctFailureReports =
			baseTopLevelBuildReport.getDistinctFailureReports();

		Assert.assertEquals(
			distinctFailureReports.toString(), 2,
			distinctFailureReports.size());
		Assert.assertTrue(distinctFailureReports.contains(failureReport));
		Assert.assertTrue(distinctFailureReports.contains(uniqueFailureReport));

		Assert.assertSame(
			distinctFailureReports,
			baseTopLevelBuildReport.getDistinctFailureReports());
	}

	@Test
	public void testGetDownstreamBuildReport() {
		String axisName = RandomTestUtil.randomString();

		DownstreamBuildReport cachedDownstreamBuildReport =
			_newDownstreamBuildReport(axisName, true);
		DownstreamBuildReport downstreamBuildReport = _newDownstreamBuildReport(
			axisName, false);

		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(new JSONObject());

		baseTopLevelBuildReport.addDownstreamBuildReport(
			cachedDownstreamBuildReport);
		baseTopLevelBuildReport.addDownstreamBuildReport(downstreamBuildReport);

		Assert.assertSame(
			downstreamBuildReport,
			baseTopLevelBuildReport.getDownstreamBuildReport(axisName));

		Assert.assertNull(
			baseTopLevelBuildReport.getDownstreamBuildReport(
				RandomTestUtil.randomString()));

		baseTopLevelBuildReport = _newBaseTopLevelBuildReport(new JSONObject());

		baseTopLevelBuildReport.addDownstreamBuildReport(
			cachedDownstreamBuildReport);

		Assert.assertSame(
			cachedDownstreamBuildReport,
			baseTopLevelBuildReport.getDownstreamBuildReport(axisName));
	}

	@Test
	public void testGetDownstreamBuildReports() {
		String axisName = RandomTestUtil.randomString();

		DownstreamBuildReport cachedDownstreamBuildReport =
			_newDownstreamBuildReport(axisName, true);
		DownstreamBuildReport downstreamBuildReport = _newDownstreamBuildReport(
			axisName, false);

		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(new JSONObject());

		baseTopLevelBuildReport.addDownstreamBuildReport(
			cachedDownstreamBuildReport);
		baseTopLevelBuildReport.addDownstreamBuildReport(downstreamBuildReport);

		List<DownstreamBuildReport> downstreamBuildReports =
			baseTopLevelBuildReport.getDownstreamBuildReports();

		Assert.assertEquals(
			downstreamBuildReports.toString(), 2,
			downstreamBuildReports.size());
		Assert.assertTrue(
			downstreamBuildReports.contains(cachedDownstreamBuildReport));
		Assert.assertTrue(
			downstreamBuildReports.contains(downstreamBuildReport));
	}

	@Test
	public void testGetFailureReports() {
		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(
				new JSONObject(
				).put(
					"failureReports",
					new JSONArray(
					).put(
						new JSONObject(
						).put(
							"message", RandomTestUtil.randomString()
						)
					)
				).put(
					"result", "FAILURE"
				));

		FailureReport cachedFailureReport = Mockito.mock(FailureReport.class);
		FailureReport downstreamFailureReport = Mockito.mock(
			FailureReport.class);

		DownstreamBuildReport cachedDownstreamBuildReport =
			_newDownstreamBuildReport(RandomTestUtil.randomString(), true);
		DownstreamBuildReport downstreamBuildReport = _newDownstreamBuildReport(
			RandomTestUtil.randomString(), false);

		Mockito.doReturn(
			Collections.singletonList(cachedFailureReport)
		).when(
			cachedDownstreamBuildReport
		).getFailureReports();

		Mockito.doReturn(
			Collections.singletonList(downstreamFailureReport)
		).when(
			downstreamBuildReport
		).getFailureReports();

		baseTopLevelBuildReport.addDownstreamBuildReport(
			cachedDownstreamBuildReport);
		baseTopLevelBuildReport.addDownstreamBuildReport(downstreamBuildReport);

		List<FailureReport> failureReports =
			baseTopLevelBuildReport.getFailureReports();

		Assert.assertEquals(
			failureReports.toString(), 3, failureReports.size());
		Assert.assertTrue(failureReports.contains(cachedFailureReport));
		Assert.assertTrue(failureReports.contains(downstreamFailureReport));

		Assert.assertSame(
			failureReports, baseTopLevelBuildReport.getFailureReports());
	}

	@Test
	public void testGetJobReport() {
		BaseTopLevelBuildReport baseTopLevelBuildReport = Mockito.mock(
			BaseTopLevelBuildReport.class);

		Mockito.doCallRealMethod(
		).when(
			baseTopLevelBuildReport
		).getJobReport();

		Mockito.doReturn(
			null
		).when(
			baseTopLevelBuildReport
		).getBuildURL();

		try {
			baseTopLevelBuildReport.getJobReport();

			Assert.fail("Expected a RuntimeException for an invalid build URL");
		}
		catch (RuntimeException runtimeException) {
			String message = runtimeException.getMessage();

			Assert.assertTrue(message.startsWith("Invalid Build URL"));
		}

		try {
			_newBaseTopLevelBuildReport(new JSONObject(), "not-a-build-url");

			Assert.fail("Expected a RuntimeException for an invalid build URL");
		}
		catch (RuntimeException runtimeException) {
			String message = runtimeException.getMessage();

			Assert.assertTrue(message.startsWith("Invalid Build URL"));
		}
	}

	@Test
	public void testGetPreviousTopLevelBuildReport() throws Exception {
		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(new JSONObject());

		Assert.assertNull(
			baseTopLevelBuildReport.getPreviousTopLevelBuildReport());

		baseTopLevelBuildReport.setControllerBuildReport(
			_newControllerBuildReport(1));

		Assert.assertNull(
			baseTopLevelBuildReport.getPreviousTopLevelBuildReport());

		UrlReader urlReader = mockUrlReader();

		JSONArray buildsJSONArray = new JSONArray();

		buildsJSONArray.put(_newControllerBuildJSONObject(4, "SUCCESS"));
		buildsJSONArray.put(_newControllerBuildJSONObject(3, "SUCCESS"));
		buildsJSONArray.put(_newControllerBuildJSONObject(2, "ABORTED"));
		buildsJSONArray.put(_newControllerBuildJSONObject(1, "SUCCESS"));

		JSONObject controllerJobJSONObject = new JSONObject();

		controllerJobJSONObject.put("builds", buildsJSONArray);

		setUrlReaderOutput(
			String.valueOf(controllerJobJSONObject), "controller-job",
			urlReader);

		setUrlReaderOutput("{}", "previous-job/1/", urlReader);

		baseTopLevelBuildReport = _newBaseTopLevelBuildReport(new JSONObject());

		baseTopLevelBuildReport.setControllerBuildReport(
			_newControllerBuildReport(3));

		TopLevelBuildReport previousTopLevelBuildReport =
			baseTopLevelBuildReport.getPreviousTopLevelBuildReport();

		Assert.assertNotNull(previousTopLevelBuildReport);

		Assert.assertEquals(
			"https://test-1-1.liferay.com/job/previous-job/1",
			String.valueOf(previousTopLevelBuildReport.getBuildURL()));

		Assert.assertSame(
			previousTopLevelBuildReport,
			baseTopLevelBuildReport.getPreviousTopLevelBuildReport());

		JSONObject currentBuildJSONObject = new JSONObject();

		currentBuildJSONObject.put("number", 3);

		JSONObject unparsableBuildJSONObject = new JSONObject();

		unparsableBuildJSONObject.put(
			"description", RandomTestUtil.randomString()
		).put(
			"number", 2
		);

		buildsJSONArray = new JSONArray();

		buildsJSONArray.put(currentBuildJSONObject);
		buildsJSONArray.put(unparsableBuildJSONObject);

		controllerJobJSONObject = new JSONObject();

		controllerJobJSONObject.put("builds", buildsJSONArray);

		setUrlReaderOutput(
			String.valueOf(controllerJobJSONObject), "controller-job",
			urlReader);

		baseTopLevelBuildReport = _newBaseTopLevelBuildReport(new JSONObject());

		baseTopLevelBuildReport.setControllerBuildReport(
			_newControllerBuildReport(3));

		Assert.assertNull(
			baseTopLevelBuildReport.getPreviousTopLevelBuildReport());
	}

	@Test
	public void testGetTestResultsJSONUserContentURL() {
		_testGetTestResultsJSONUserContentURL(
			"https://test-1-0.liferay.com/userContent/testResults/test-job" +
				"/builds/123/test.results.json",
			null);
		_testGetTestResultsJSONUserContentURL(
			"https://test-9-9.liferay.com/userContent/testResults/test-job" +
				"/builds/123/test.results.json",
			"https://test-9-9.liferay.com");
		_testGetTestResultsJSONUserContentURL(
			"https://test-9-9.liferay.com/userContent/testResults/test-job" +
				"/builds/123/test.results.json",
			"https://test-9-9.liferay.com/");
	}

	@Test
	public void testGetTopLevelActiveDuration() {
		JSONObject buildReportJSONObject = new JSONObject();

		buildReportJSONObject.put("duration", 5000L);

		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(buildReportJSONObject);

		Assert.assertEquals(
			0L, baseTopLevelBuildReport.getTopLevelActiveDuration());

		buildReportJSONObject = _newStopWatchBuildReportJSONObject(
			5000L, 2000L, "wait.for.invoked.jobs");

		baseTopLevelBuildReport = _newBaseTopLevelBuildReport(
			buildReportJSONObject);

		Assert.assertEquals(
			3000L, baseTopLevelBuildReport.getTopLevelActiveDuration());
	}

	@Test
	public void testGetTopLevelPassiveDuration() {
		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(new JSONObject());

		Assert.assertEquals(
			0L, baseTopLevelBuildReport.getTopLevelPassiveDuration());

		baseTopLevelBuildReport = _newBaseTopLevelBuildReport(
			_newStopWatchBuildReportJSONObject(
				9000L, 4000L, "invoke.downstream.builds"));

		Assert.assertEquals(
			4000L, baseTopLevelBuildReport.getTopLevelPassiveDuration());

		JSONObject buildReportJSONObject = _newStopWatchBuildReportJSONObject(
			9000L, 2000L, "wait.for.invoked.jobs");

		JSONArray stopWatchRecordsJSONArray =
			buildReportJSONObject.getJSONArray("stopWatchRecords");

		stopWatchRecordsJSONArray.put(
			_newStopWatchRecordJSONObject(
				3000L, "wait.for.invoked.smoke.jobs"));
		stopWatchRecordsJSONArray.put(
			_newStopWatchRecordJSONObject(4000L, "invoke.downstream.builds"));

		baseTopLevelBuildReport = _newBaseTopLevelBuildReport(
			buildReportJSONObject);

		Assert.assertEquals(
			5000L, baseTopLevelBuildReport.getTopLevelPassiveDuration());
	}

	@Test
	public void testGetUniqueFailureReports() {
		FailureReport failureReport = Mockito.mock(FailureReport.class);
		FailureReport previousFailureReport = Mockito.mock(FailureReport.class);
		FailureReport unaffectedFailureReport = Mockito.mock(
			FailureReport.class);

		Mockito.doReturn(
			true
		).when(
			failureReport
		).isSimilar(
			previousFailureReport
		);

		BaseTopLevelBuildReport baseTopLevelBuildReport = Mockito.mock(
			BaseTopLevelBuildReport.class);

		Mockito.doCallRealMethod(
		).when(
			baseTopLevelBuildReport
		).getUniqueFailureReports();

		Mockito.doReturn(
			Collections.singletonList(failureReport)
		).when(
			baseTopLevelBuildReport
		).getDistinctFailureReports();

		Mockito.doReturn(
			null
		).when(
			baseTopLevelBuildReport
		).getPreviousTopLevelBuildReport();

		List<FailureReport> uniqueFailureReports =
			baseTopLevelBuildReport.getUniqueFailureReports();

		Assert.assertEquals(
			uniqueFailureReports.toString(), 1, uniqueFailureReports.size());
		Assert.assertTrue(uniqueFailureReports.contains(failureReport));

		Assert.assertSame(
			uniqueFailureReports,
			baseTopLevelBuildReport.getUniqueFailureReports());

		TopLevelBuildReport previousTopLevelBuildReport = Mockito.mock(
			TopLevelBuildReport.class);

		Mockito.doReturn(
			Collections.singletonList(previousFailureReport)
		).when(
			previousTopLevelBuildReport
		).getDistinctFailureReports();

		baseTopLevelBuildReport = Mockito.mock(BaseTopLevelBuildReport.class);

		Mockito.doCallRealMethod(
		).when(
			baseTopLevelBuildReport
		).getUniqueFailureReports();

		Mockito.doReturn(
			Arrays.asList(failureReport, unaffectedFailureReport)
		).when(
			baseTopLevelBuildReport
		).getDistinctFailureReports();

		Mockito.doReturn(
			previousTopLevelBuildReport
		).when(
			baseTopLevelBuildReport
		).getPreviousTopLevelBuildReport();

		uniqueFailureReports =
			baseTopLevelBuildReport.getUniqueFailureReports();

		Assert.assertEquals(
			uniqueFailureReports.toString(), 1, uniqueFailureReports.size());
		Assert.assertTrue(
			uniqueFailureReports.contains(unaffectedFailureReport));
	}

	@Test
	public void testInitialize() {
		JSONArray buildsJSONArray = new JSONArray();

		JSONObject buildJSONObject = new JSONObject();

		buildJSONObject.put(
			"buildURL", "https://test-1-1/job/test-job/AXIS_VARIABLE=0/1"
		).put(
			"duration", 1000L
		).put(
			"result", "SUCCESS"
		);

		buildsJSONArray.put(buildJSONObject);

		buildsJSONArray.put(new JSONObject());

		JSONObject batchJSONObject = new JSONObject();

		batchJSONObject.put(
			"batchName", "functional-tomcat90-mysql80-jdk8"
		).put(
			"builds", buildsJSONArray
		);

		JSONArray batchesJSONArray = new JSONArray();

		batchesJSONArray.put(batchJSONObject);
		batchesJSONArray.put(new JSONObject());

		JSONObject controllerJSONObject = new JSONObject();

		controllerJSONObject.put(
			"buildURL", "https://test-1-1/job/controller-job/7"
		).put(
			"duration", 1000L
		).put(
			"result", "SUCCESS"
		);

		JSONObject buildReportJSONObject = new JSONObject();

		buildReportJSONObject.put(
			"batches", batchesJSONArray
		).put(
			"controller", controllerJSONObject
		);

		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(buildReportJSONObject);

		baseTopLevelBuildReport.initialize(buildReportJSONObject);

		List<DownstreamBuildReport> downstreamBuildReports =
			baseTopLevelBuildReport.getDownstreamBuildReports();

		Assert.assertEquals(
			downstreamBuildReports.toString(), 1,
			downstreamBuildReports.size());

		Assert.assertNotNull(
			baseTopLevelBuildReport.getControllerBuildReport());
	}

	private BaseTopLevelBuildReport _newBaseTopLevelBuildReport(
		JSONObject buildReportJSONObject) {

		return _newBaseTopLevelBuildReport(
			buildReportJSONObject, _BUILD_URL_STRING);
	}

	private BaseTopLevelBuildReport _newBaseTopLevelBuildReport(
		JSONObject buildReportJSONObject, String buildURLString) {

		return new BaseTopLevelBuildReport(buildURLString) {

			@Override
			public JSONObject getBuildReportJSONObject() {
				return buildReportJSONObject;
			}

		};
	}

	private JSONObject _newControllerBuildJSONObject(
		int buildNumber, String status) {

		return new JSONObject(
		).put(
			"description",
			JenkinsResultsParserUtil.combine(
				"<strong>", status, "</strong> - <a href=\"https://test-1-1",
				"/job/previous-job/", String.valueOf(buildNumber),
				"/\">Build URL</a>")
		).put(
			"number", buildNumber
		);
	}

	private ControllerBuildReport _newControllerBuildReport(int buildNumber) {
		ControllerBuildReport controllerBuildReport = Mockito.mock(
			ControllerBuildReport.class);

		JenkinsMaster jenkinsMaster = Mockito.mock(JenkinsMaster.class);

		Mockito.doReturn(
			"https://test-1-1.liferay.com/"
		).when(
			jenkinsMaster
		).getRemoteURL();

		Mockito.doReturn(
			buildNumber
		).when(
			controllerBuildReport
		).getBuildNumber();

		Mockito.doReturn(
			jenkinsMaster
		).when(
			controllerBuildReport
		).getJenkinsMaster();

		Mockito.doReturn(
			"controller-job"
		).when(
			controllerBuildReport
		).getJobName();

		return controllerBuildReport;
	}

	private DownstreamBuildReport _newDownstreamBuildReport(
		String axisName, boolean buildCached) {

		DownstreamBuildReport downstreamBuildReport = Mockito.mock(
			DownstreamBuildReport.class);

		Mockito.doReturn(
			axisName
		).when(
			downstreamBuildReport
		).getAxisName();

		Mockito.doReturn(
			buildCached
		).when(
			downstreamBuildReport
		).isBuildCached();

		return downstreamBuildReport;
	}

	private JSONObject _newStopWatchBuildReportJSONObject(
		long duration, long stopWatchRecordDuration,
		String stopWatchRecordName) {

		return new JSONObject(
		).put(
			"duration", duration
		).put(
			"stopWatchRecords",
			new JSONArray(
			).put(
				_newStopWatchRecordJSONObject(
					stopWatchRecordDuration, stopWatchRecordName)
			)
		);
	}

	private JSONObject _newStopWatchRecordJSONObject(
		long duration, String name) {

		return new JSONObject(
		).put(
			"duration", duration
		).put(
			"name", name
		);
	}

	private void _testGetBuildProfile(
		String buildProfileString, Job.BuildProfile expectedBuildProfile) {

		JSONObject buildReportJSONObject = new JSONObject();

		if (buildProfileString != null) {
			JSONObject buildParametersJSONObject = new JSONObject();

			buildParametersJSONObject.put(
				"TEST_PORTAL_BUILD_PROFILE", buildProfileString);

			buildReportJSONObject.put(
				"buildParameters", buildParametersJSONObject);
		}

		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(buildReportJSONObject);

		Assert.assertEquals(
			expectedBuildProfile, baseTopLevelBuildReport.getBuildProfile());
	}

	private void _testGetTestResultsJSONUserContentURL(
		String expectedURLString, String jenkinsRemoteURL) {

		Properties properties = new Properties();

		if (jenkinsRemoteURL != null) {
			properties.setProperty(
				"jenkins.remote.url[test-1-0]", jenkinsRemoteURL);
		}

		JenkinsResultsParserUtil.setBuildProperties(properties);

		BaseTopLevelBuildReport baseTopLevelBuildReport =
			_newBaseTopLevelBuildReport(new JSONObject());

		Assert.assertEquals(
			expectedURLString,
			String.valueOf(
				baseTopLevelBuildReport.getTestResultsJSONUserContentURL()));
	}

	private static final String _BUILD_URL_STRING =
		"https://test-1-1/job/test-job/123";

}