/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.poshi.runner.report;

import com.liferay.poshi.core.PoshiContext;
import com.liferay.poshi.core.elements.PoshiElement;
import com.liferay.poshi.core.util.FileUtil;
import com.liferay.poshi.core.util.PoshiProperties;
import com.liferay.poshi.core.util.PropsUtil;
import com.liferay.poshi.runner.util.DateUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dom4j.Element;

import org.json.JSONArray;

/**
 * @author Calum Ragan
 */
public class PoshiReportGenerator {

	public static Map<String, Set<String>> executeFilePaths = new HashMap<>();
	public static Map<String, Set<String>> macroFilePaths = new HashMap<>();

	public static List<List<Element>> covertInnerSetToList(
		List<Set<Element>> elementSets) {

		List<List<Element>> elementsLists = new ArrayList<>();

		for (Set<Element> elementSet : elementSets) {
			List<Element> newElementList = new ArrayList<>(elementSet);

			elementsLists.add(newElementList);
		}

		return elementsLists;
	}

	public static void createPoshiReport() throws IOException {
		String currentDirName = FileUtil.getCanonicalPath(".");

		ClassLoader classLoader = PoshiReportGenerator.class.getClassLoader();

		URL url = classLoader.getResource("reports/usage/index.html");

		String indexHTMLContent = FileUtil.read(url);

		PoshiProperties poshiProperties = PoshiProperties.getPoshiProperties();

		String testBaseDirName = poshiProperties.testBaseDirName;

		if (poshiProperties.testRunLocally) {
			FileUtil.copyFileFromResource(
				"reports/usage/css/main.css",
				currentDirName + "/usage-report/css/main.css");
			FileUtil.copyFileFromResource(
				"reports/usage/js/main.js",
				currentDirName + "/usage-report/js/main.js");
			FileUtil.copyFileFromResource(
				"reports/usage/js/data.js",
				currentDirName + "/usage-report/js/data.js");
		}
		else {
			FileUtil.copyFileFromResource(
				"reports/usage/css/main.css",
				testBaseDirName + "/usage-report/css/main.css");
			FileUtil.copyFileFromResource(
				"reports/usage/js/main.js",
				testBaseDirName + "/usage-report/js/main.js");
			FileUtil.copyFileFromResource(
				"reports/usage/js/data.js",
				testBaseDirName + "/usage-report/js/data.js");
		}

		StringBuilder sb = new StringBuilder();

		if (poshiProperties.testRunLocally) {
			sb.append(currentDirName);
		}
		else {
			sb.append(testBaseDirName);
		}

		sb.append("/usage-report/index.html");

		FileUtil.write(sb.toString(), indexHTMLContent);
	}

	public static void generateData(
		Map<String, Set<Element>> elementsMap,
		Map<String, Set<String>> filePathsMap) {

		List<Set<Element>> elementSetList = new ArrayList<>(
			elementsMap.values());

		List<List<Element>> elementsList = covertInnerSetToList(elementSetList);

		Collections.sort(elementsList, new SortBySize());

		getFilePaths(elementsMap, filePathsMap);
	}

	public static void getFilePaths(
		Map<String, Set<Element>> elementsMap,
		Map<String, Set<String>> filePathsMap) {

		Set<String> keySet = elementsMap.keySet();

		List<String> keyList = new ArrayList<>(keySet);

		for (String key : keyList) {
			Set<Element> elementSet = elementsMap.get(key);

			List<Element> elementList = new ArrayList<>(elementSet);

			Set<String> filePathList = new HashSet<>();

			for (Element element : elementList) {
				PoshiElement poshiElement = (PoshiElement)element;

				StringBuilder sb = new StringBuilder();
				URL fileURL = poshiElement.getFilePathURL();

				sb.append(fileURL.toString());

				sb.append(":");
				sb.append(poshiElement.getPoshiScriptLineNumber());

				filePathList.add(sb.toString());
			}

			filePathsMap.put(key, filePathList);
		}
	}

	public static void main(String[] args) throws Exception {
		String testBaseDirName =
			"/opt/dev/projects/github/liferay-portal/portal-web";

		File poshiPropertiesFile = new File(
			testBaseDirName, "poshi.properties");

		Properties properties = new Properties();

		if (poshiPropertiesFile.exists()) {
			properties.load(new FileInputStream(poshiPropertiesFile));
		}

		PropsUtil.clear();

		PropsUtil.setProperties(properties);
		PoshiContext.readFiles();
		setJavaScriptDataFilePath();
		generateData(PoshiContext.getMacroElementsMap(), macroFilePaths);
		createPoshiReport();
		writeDataToDataJavaScriptFile(macroFilePaths, _javaScriptDataFilePath);
	}

	public static void setJavaScriptDataFilePath() {
		PoshiProperties poshiProperties = PoshiProperties.getPoshiProperties();
		StringBuilder sb = new StringBuilder();

		if (poshiProperties.testRunLocally) {
			sb.append(FileUtil.getCanonicalPath("./usage-report/js/data.js"));
		}
		else {
			sb.append(poshiProperties.testBaseDirName);
			sb.append("/usage-report/js/data.js");
		}

		_javaScriptDataFilePath = sb.toString();
	}

	public static void writeDataToDataJavaScriptFile(
			Map<String, Set<String>> filePathsMap, String filePath)
		throws IOException {

		JSONArray executeDataJSONArray = new JSONArray();

		executeDataJSONArray.put(
			new String[] {"Name", "File Paths", "Usage Count"});

		Set<String> keySet = filePathsMap.keySet();

		List<String> keyList = new ArrayList<>(keySet);

		for (String key : keyList) {
			JSONArray jsonArray = new JSONArray();

			jsonArray.put(key);

			Set<String> filePaths = filePathsMap.get(key);

			jsonArray.put(filePaths);

			executeDataJSONArray.put(jsonArray);
		}

		StringBuilder sb = new StringBuilder();

		sb.append("var executeUsageData = ");
		sb.append(executeDataJSONArray);
		sb.append(";\nvar executeUsageDataGeneratedDate = new Date(");
		sb.append(DateUtil.getTimeInMilliseconds());
		sb.append(")");

		FileUtil.write(filePath, sb.toString());
	}

	public static class SortBySize implements Comparator<List<?>> {

		@Override
		public int compare(List<?> o1, List<?> o2) {
			return o2.size() - o1.size();
		}

	}

	private static String _javaScriptDataFilePath;

}