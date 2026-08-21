/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.jenkins.results.parser.test.clazz.group;

import com.liferay.jenkins.results.parser.JenkinsResultsParserUtil;
import com.liferay.jenkins.results.parser.RandomTestUtil;
import com.liferay.jenkins.results.parser.Shell;
import com.liferay.jenkins.results.parser.test.clazz.TestClass;
import com.liferay.jenkins.results.parser.test.clazz.TestClassFactory;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mockito.Mockito;

/**
 * @author Kenji Heigel
 */
public class BatchTestClassGroupTest
	extends com.liferay.jenkins.results.parser.Test {

	@Before
	public void setUpGitRemotes() throws Exception {
		Shell shell = mockShell();

		setShellCommandOutput("git branch | grep", shell, "* master\n");
		setShellCommandOutput(
			"git remote -v", shell,
			BatchTestClassGroupTestUtil.getGitRemotesShellCommandOutput());
	}

	@Test
	public void testGetAxisCount() {
		_testGetAxisCount("-1", null, 3, 12);
		_testGetAxisCount("0", null, 0, 12);
		_testGetAxisCount("7", null, 7, 12);
		_testGetAxisCount("abc", null, 3, 12);
		_testGetAxisCount(null, "", 1, 12);
		_testGetAxisCount(null, "-3", 1, 12);
		_testGetAxisCount(null, "abc", 1, 12);
		_testGetAxisCount(null, null, 0, 0);
		_testGetAxisCount(null, null, 1, 1);
		_testGetAxisCount(null, null, 3, 12);
	}

	@Test
	public void testGetAxisCountAutoBalanceTests() throws Exception {
		BatchTestClassGroupTestUtil.resetCaches();

		String className = "SampleAutoBalanceTest";

		File workingDirectory = _newAutoBalanceWorkingDirectory(className);

		Properties jobProperties = new Properties();

		jobProperties.setProperty(
			"test.class.names.auto.balance",
			"com/liferay/" + className + ".java");

		JUnitBatchTestClassGroup jUnitBatchTestClassGroup =
			new JUnitBatchTestClassGroup(
				"unit",
				BatchTestClassGroupTestUtil.getPortalTestClassJob(
					jobProperties,
					Collections.singletonList(
						new File(workingDirectory, "Modified.java")),
					workingDirectory)) {

				@Override
				protected void setTestClasses() {
				}

			};

		testEquals(1, jUnitBatchTestClassGroup.getAxisCount());

		List<AxisTestClassGroup> axisTestClassGroups =
			jUnitBatchTestClassGroup.getAxisTestClassGroups();

		testEquals(1, axisTestClassGroups.size());

		AxisTestClassGroup axisTestClassGroup = axisTestClassGroups.get(0);

		List<TestClass> testClasses = axisTestClassGroup.getTestClasses();

		testEquals(1, testClasses.size());
	}

	@Test
	public void testGetAxisCountAxisMaxSizeZero() {
		BatchTestClassGroup batchTestClassGroup = _newBatchTestClassGroup(
			null, "0", null, 12);

		try {
			batchTestClassGroup.getAxisCount();

			Assert.fail();
		}
		catch (RuntimeException runtimeException) {
			testEquals(
				"'test.batch.axis.max.size' cannot be 0 or less",
				runtimeException.getMessage());
		}
	}

	@Test
	public void testGetAxisMaxSize() {
		_testGetAxisMaxSize("", 5000);
		_testGetAxisMaxSize("-3", 5000);
		_testGetAxisMaxSize("0", 0);
		_testGetAxisMaxSize("5", 5);
		_testGetAxisMaxSize("abc", 5000);
	}

	@Test
	public void testGetAxisTestClassGroups() {
		BatchTestClassGroupTestUtil.resetCaches();

		BatchTestClassGroup batchTestClassGroup = new BatchTestClassGroup(
			"default", BatchTestClassGroupTestUtil.getPortalTestClassJob()) {
		};

		int axisMaxSize = batchTestClassGroup.getAxisMaxSize();

		for (int i = 0; i < (axisMaxSize + 2); i++) {
			batchTestClassGroup.addTestClass(
				TestClassFactory.newTestClass(
					batchTestClassGroup,
					new File(RandomTestUtil.randomString())));
		}

		batchTestClassGroup.setAxisTestClassGroups();

		List<TestClass> testClasses = batchTestClassGroup.getTestClasses();

		List<AxisTestClassGroup> axisTestClassGroups =
			batchTestClassGroup.getAxisTestClassGroups();

		Assert.assertEquals(
			axisTestClassGroups.toString(),
			(int)Math.ceil((double)testClasses.size() / axisMaxSize),
			axisTestClassGroups.size());

		List<TestClass> axisTestClasses = _getTestClasses(axisTestClassGroups);

		Collections.sort(axisTestClasses);

		Assert.assertEquals(testClasses, axisTestClasses);
	}

	@Test
	public void testGetSegmentMaxChildren() {
		_testGetSegmentMaxChildren(0, "0");
		_testGetSegmentMaxChildren(3, "3");
		_testGetSegmentMaxChildren(25, "");
		_testGetSegmentMaxChildren(25, "-2");
		_testGetSegmentMaxChildren(25, "abc");
	}

	@Test
	public void testSetAxisTestClassGroups() {
		_testSetAxisTestClassGroups("10", null, Arrays.asList(1, 1, 1), 3);
		_testSetAxisTestClassGroups("4", null, Arrays.asList(2, 2, 2, 1), 7);
		_testSetAxisTestClassGroups(null, null, Collections.emptyList(), 0);
		_testSetAxisTestClassGroups(null, null, Arrays.asList(4, 4, 4), 12);
		_testSetAxisTestClassGroups(null, null, Arrays.asList(5, 5, 3), 13);
	}

	@Test
	public void testSetAxisTestClassGroupsBalancesByWeight() throws Exception {
		BatchTestClassGroupTestUtil.resetCaches();

		Properties jobProperties = new Properties();

		jobProperties.setProperty("test.batch.axis.count", "2");

		CompileModulesBatchTestClassGroup compileModulesBatchTestClassGroup =
			BatchTestClassGroupTestUtil.newCompileModulesBatchTestClassGroup(
				jobProperties, _newModuleDir("aaa-module", 2),
				_newModuleDir("aab-module", 2), _newModuleDir("zzy-module", 3),
				_newModuleDir("zzz-module", 3));

		List<AxisTestClassGroup> axisTestClassGroups =
			compileModulesBatchTestClassGroup.getAxisTestClassGroups();

		testEquals(2, axisTestClassGroups.size());

		for (AxisTestClassGroup axisTestClassGroup : axisTestClassGroups) {
			testEquals(5L, _getWeight(axisTestClassGroup));
		}
	}

	@Test
	public void testSetAxisTestClassGroupsTargetAxisDuration()
		throws Exception {

		_testSetAxisTestClassGroupsTargetAxisDuration(Arrays.asList(4, 3), "");
		_testSetAxisTestClassGroupsTargetAxisDuration(
			Arrays.asList(3, 3, 1), "3000");
	}

	@Test
	public void testSetSegmentTestClassGroups() {
		File testBaseDir = new File(RandomTestUtil.randomString());

		Integer minimumSlaveRAM = RandomTestUtil.randomInt();
		String baseSlaveLabel = RandomTestUtil.randomString();

		AxisTestClassGroup axisTestClassGroup = _mockAxisTestClassGroup(
			baseSlaveLabel, minimumSlaveRAM, testBaseDir);

		_testSetSegmentTestClassGroups(
			1, axisTestClassGroup,
			_mockAxisTestClassGroup(
				baseSlaveLabel, minimumSlaveRAM, testBaseDir));

		_testSetSegmentTestClassGroups(
			2, axisTestClassGroup,
			_mockAxisTestClassGroup(
				baseSlaveLabel, RandomTestUtil.randomInt(), testBaseDir));

		_testSetSegmentTestClassGroups(
			2, axisTestClassGroup,
			_mockAxisTestClassGroup(
				RandomTestUtil.randomString(), minimumSlaveRAM, testBaseDir));

		_testSetSegmentTestClassGroups(
			2, axisTestClassGroup,
			_mockAxisTestClassGroup(
				baseSlaveLabel, minimumSlaveRAM,
				new File(RandomTestUtil.randomString())));

		_testSetSegmentTestClassGroups(
			2, axisTestClassGroup,
			_mockAxisTestClassGroup(baseSlaveLabel, minimumSlaveRAM, null));
	}

	@Test
	public void testSetSegmentTestClassGroupsEmpty() {
		BatchTestClassGroup batchTestClassGroup = _newBatchTestClassGroup(
			null, null, null, 0);

		batchTestClassGroup.setSegmentTestClassGroups();

		testEquals(
			Collections.emptyList(),
			batchTestClassGroup.getSegmentTestClassGroups());
	}

	@Test
	public void testSetSegmentTestClassGroupsMaxChildren() {
		BatchTestClassGroup batchTestClassGroup = _newBatchTestClassGroup(
			null, null, "3", 0);

		Integer minimumSlaveRAM = RandomTestUtil.randomInt();
		String baseSlaveLabel = RandomTestUtil.randomString();

		for (int i = 0; i < 7; i++) {
			batchTestClassGroup.addAxisTestClassGroup(
				_mockAxisTestClassGroup(baseSlaveLabel, minimumSlaveRAM, null));
		}

		batchTestClassGroup.setSegmentTestClassGroups();

		testEquals(
			Arrays.asList(3, 3, 1),
			_getAxisCounts(batchTestClassGroup.getSegmentTestClassGroups()));
	}

	@Test
	public void testSetSegmentTestClassGroupsMaxChildrenPerGroup() {
		BatchTestClassGroup batchTestClassGroup = _newBatchTestClassGroup(
			null, null, "3", 0);

		String baseSlaveLabel = RandomTestUtil.randomString();

		Integer minimumSlaveRAM = RandomTestUtil.randomInt();

		Integer otherMinimumSlaveRAM = minimumSlaveRAM + 1;

		for (int i = 0; i < 2; i++) {
			batchTestClassGroup.addAxisTestClassGroup(
				_mockAxisTestClassGroup(baseSlaveLabel, minimumSlaveRAM, null));
			batchTestClassGroup.addAxisTestClassGroup(
				_mockAxisTestClassGroup(
					baseSlaveLabel, otherMinimumSlaveRAM, null));
		}

		batchTestClassGroup.setSegmentTestClassGroups();

		testEquals(
			Arrays.asList(2, 2),
			_getAxisCounts(batchTestClassGroup.getSegmentTestClassGroups()));
	}

	@Test
	public void testSetSegmentTestClassGroupsMaxChildrenZero() {
		BatchTestClassGroup batchTestClassGroup = _newBatchTestClassGroup(
			null, null, "0", 0);

		batchTestClassGroup.addAxisTestClassGroup(
			_mockAxisTestClassGroup(
				RandomTestUtil.randomString(), RandomTestUtil.randomInt(),
				null));

		try {
			batchTestClassGroup.setSegmentTestClassGroups();

			Assert.fail();
		}
		catch (IllegalArgumentException illegalArgumentException) {
			testEquals(null, illegalArgumentException.getMessage());
		}
	}

	@Test
	public void testSetSegmentTestClassGroupsRunsOnce() {
		BatchTestClassGroup batchTestClassGroup = _newBatchTestClassGroup(
			null, null, null, 0);

		batchTestClassGroup.addAxisTestClassGroup(
			_mockAxisTestClassGroup(
				RandomTestUtil.randomString(), RandomTestUtil.randomInt(),
				null));

		batchTestClassGroup.setSegmentTestClassGroups();
		batchTestClassGroup.setSegmentTestClassGroups();

		testEquals(1, batchTestClassGroup.getSegmentCount());
	}

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private List<Integer> _getAxisCounts(
		List<SegmentTestClassGroup> segmentTestClassGroups) {

		List<Integer> axisCounts = new ArrayList<>();

		for (SegmentTestClassGroup segmentTestClassGroup :
				segmentTestClassGroups) {

			axisCounts.add(segmentTestClassGroup.getAxisCount());
		}

		return axisCounts;
	}

	private List<Integer> _getAxisSizes(
		List<AxisTestClassGroup> axisTestClassGroups) {

		List<Integer> axisSizes = new ArrayList<>();

		for (AxisTestClassGroup axisTestClassGroup : axisTestClassGroups) {
			List<TestClass> testClasses = axisTestClassGroup.getTestClasses();

			axisSizes.add(testClasses.size());
		}

		return axisSizes;
	}

	private List<TestClass> _getTestClasses(
		List<AxisTestClassGroup> axisTestClassGroups) {

		List<TestClass> testClasses = new ArrayList<>();

		for (AxisTestClassGroup axisTestClassGroup : axisTestClassGroups) {
			testClasses.addAll(axisTestClassGroup.getTestClasses());
		}

		return testClasses;
	}

	private long _getWeight(AxisTestClassGroup axisTestClassGroup) {
		long weight = 0;

		for (TestClass testClass : axisTestClassGroup.getTestClasses()) {
			weight += testClass.getWeight();
		}

		return weight;
	}

	private AxisTestClassGroup _mockAxisTestClassGroup(
		String baseSlaveLabel, Integer minimumSlaveRAM, File testBaseDir) {

		AxisTestClassGroup axisTestClassGroup = Mockito.mock(
			AxisTestClassGroup.class);

		Mockito.doReturn(
			baseSlaveLabel
		).when(
			axisTestClassGroup
		).getBaseSlaveLabel();

		Mockito.doReturn(
			minimumSlaveRAM
		).when(
			axisTestClassGroup
		).getMinimumSlaveRAM();

		Mockito.doReturn(
			testBaseDir
		).when(
			axisTestClassGroup
		).getTestBaseDir();

		return axisTestClassGroup;
	}

	private File _newAutoBalanceWorkingDirectory(String className)
		throws Exception {

		File workingDirectory = new File(
			JenkinsResultsParserUtil.getCanonicalPath(
				temporaryFolder.newFolder()));

		File packageDir = new File(workingDirectory, "com/liferay");

		packageDir.mkdirs();

		BatchTestClassGroupTestUtil.newTestClassFile(className, packageDir);

		return workingDirectory;
	}

	private BatchTestClassGroup _newBatchTestClassGroup(
		String axisCount, String axisMaxSize, String segmentMaxChildren,
		int testClassCount) {

		BatchTestClassGroupTestUtil.resetCaches();

		Properties jobProperties = new Properties();

		if (axisCount != null) {
			jobProperties.setProperty("test.batch.axis.count", axisCount);
		}

		if (axisMaxSize != null) {
			jobProperties.setProperty("test.batch.axis.max.size", axisMaxSize);
		}

		if (segmentMaxChildren != null) {
			jobProperties.setProperty(
				"test.batch.segment.max.children", segmentMaxChildren);
		}

		BatchTestClassGroup batchTestClassGroup = new BatchTestClassGroup(
			"default",
			BatchTestClassGroupTestUtil.getPortalTestClassJob(jobProperties)) {
		};

		for (int i = 0; i < testClassCount; i++) {
			batchTestClassGroup.addTestClass(
				TestClassFactory.newTestClass(
					batchTestClassGroup,
					new File(RandomTestUtil.randomString())));
		}

		return batchTestClassGroup;
	}

	private File _newModuleDir(String moduleDirName, int modulesProjectDirCount)
		throws Exception {

		File moduleDir = temporaryFolder.newFolder(moduleDirName);

		File lfrBuildPortalFile = new File(moduleDir, ".lfrbuild-portal");

		lfrBuildPortalFile.createNewFile();

		for (int i = 0; i < modulesProjectDirCount; i++) {
			File modulesProjectDir = new File(moduleDir, "project-" + i);

			modulesProjectDir.mkdirs();

			File bndBndFile = new File(modulesProjectDir, "bnd.bnd");

			bndBndFile.createNewFile();

			File buildGradleFile = new File(modulesProjectDir, "build.gradle");

			buildGradleFile.createNewFile();
		}

		return moduleDir;
	}

	private void _testGetAxisCount(
		String axisCount, String axisMaxSize, int expectedAxisCount,
		int testClassCount) {

		BatchTestClassGroup batchTestClassGroup = _newBatchTestClassGroup(
			axisCount, axisMaxSize, null, testClassCount);

		testEquals(expectedAxisCount, batchTestClassGroup.getAxisCount());
	}

	private void _testGetAxisMaxSize(
		String axisMaxSize, int expectedAxisMaxSize) {

		BatchTestClassGroup batchTestClassGroup = _newBatchTestClassGroup(
			null, axisMaxSize, null, 0);

		testEquals(expectedAxisMaxSize, batchTestClassGroup.getAxisMaxSize());
	}

	private void _testGetSegmentMaxChildren(
		int expectedSegmentMaxChildren, String segmentMaxChildren) {

		BatchTestClassGroup batchTestClassGroup = _newBatchTestClassGroup(
			null, null, segmentMaxChildren, 0);

		testEquals(
			expectedSegmentMaxChildren,
			batchTestClassGroup.getSegmentMaxChildren());
	}

	private void _testSetAxisTestClassGroups(
		String axisCount, String axisMaxSize, List<Integer> expectedAxisSizes,
		int testClassCount) {

		BatchTestClassGroup batchTestClassGroup = _newBatchTestClassGroup(
			axisCount, axisMaxSize, null, testClassCount);

		batchTestClassGroup.setAxisTestClassGroups();

		List<AxisTestClassGroup> axisTestClassGroups =
			batchTestClassGroup.getAxisTestClassGroups();

		testEquals(expectedAxisSizes, _getAxisSizes(axisTestClassGroups));

		List<TestClass> axisTestClasses = _getTestClasses(axisTestClassGroups);

		Collections.sort(axisTestClasses);

		testEquals(batchTestClassGroup.getTestClasses(), axisTestClasses);
	}

	private void _testSetAxisTestClassGroupsTargetAxisDuration(
			List<Integer> expectedAxisSizes, String targetAxisDuration)
		throws Exception {

		BatchTestClassGroupTestUtil.resetCaches();

		Properties jobProperties = new Properties();

		jobProperties.setProperty("test.batch.default.test.duration", "1000");
		jobProperties.setProperty(
			"test.batch.default.test.overhead.duration", "0");
		jobProperties.setProperty(
			"test.batch.target.axis.duration", targetAxisDuration);

		File workingDirectory = temporaryFolder.newFolder();

		final List<File> testClassFiles = new ArrayList<>();

		for (int i = 0; i < 7; i++) {
			testClassFiles.add(
				BatchTestClassGroupTestUtil.newTestClassFile(
					"Sample" + i + "Test", workingDirectory));
		}

		JUnitBatchTestClassGroup jUnitBatchTestClassGroup =
			new JUnitBatchTestClassGroup(
				"unit",
				BatchTestClassGroupTestUtil.getPortalTestClassJob(
					jobProperties, new ArrayList<>(), workingDirectory)) {

				@Override
				protected void setTestClasses() {
					for (File testClassFile : testClassFiles) {
						addTestClass(
							TestClassFactory.newTestClass(this, testClassFile));
					}
				}

			};

		List<Integer> axisSizes = _getAxisSizes(
			jUnitBatchTestClassGroup.getAxisTestClassGroups());

		Collections.sort(axisSizes, Collections.reverseOrder());

		testEquals(expectedAxisSizes, axisSizes);
	}

	private void _testSetSegmentTestClassGroups(
		int expectedSegmentCount, AxisTestClassGroup... axisTestClassGroups) {

		BatchTestClassGroup batchTestClassGroup = _newBatchTestClassGroup(
			null, null, null, 0);

		for (AxisTestClassGroup axisTestClassGroup : axisTestClassGroups) {
			batchTestClassGroup.addAxisTestClassGroup(axisTestClassGroup);
		}

		batchTestClassGroup.setSegmentTestClassGroups();

		List<SegmentTestClassGroup> segmentTestClassGroups =
			batchTestClassGroup.getSegmentTestClassGroups();

		testEquals(expectedSegmentCount, segmentTestClassGroups.size());

		List<AxisTestClassGroup> segmentAxisTestClassGroups = new ArrayList<>();

		for (SegmentTestClassGroup segmentTestClassGroup :
				segmentTestClassGroups) {

			List<AxisTestClassGroup> childAxisTestClassGroups =
				segmentTestClassGroup.getAxisTestClassGroups();

			segmentAxisTestClassGroups.addAll(childAxisTestClassGroups);

			AxisTestClassGroup firstAxisTestClassGroup =
				childAxisTestClassGroups.get(0);

			for (AxisTestClassGroup childAxisTestClassGroup :
					childAxisTestClassGroups) {

				testEquals(
					firstAxisTestClassGroup.getBaseSlaveLabel(),
					childAxisTestClassGroup.getBaseSlaveLabel());
				testEquals(
					firstAxisTestClassGroup.getMinimumSlaveRAM(),
					childAxisTestClassGroup.getMinimumSlaveRAM());
				testEquals(
					firstAxisTestClassGroup.getTestBaseDir(),
					childAxisTestClassGroup.getTestBaseDir());
			}
		}

		List<AxisTestClassGroup> batchAxisTestClassGroups =
			batchTestClassGroup.getAxisTestClassGroups();

		testEquals(
			batchAxisTestClassGroups.size(), segmentAxisTestClassGroups.size());

		Assert.assertTrue(
			segmentAxisTestClassGroups.containsAll(batchAxisTestClassGroups));
	}

}