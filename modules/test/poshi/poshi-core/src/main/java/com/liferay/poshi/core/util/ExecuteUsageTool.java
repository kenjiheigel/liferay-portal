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

package com.liferay.poshi.core.util;

import com.liferay.poshi.core.PoshiContext;
import com.liferay.poshi.core.elements.PoshiElement;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;

/**
 * @author Calum Ragan
 */
public class ExecuteUsageTool {

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
		PoshiContext.readFiles();
		generateData(PoshiContext.getExecuteElementsMap(), executeFilePaths);
		generateData(PoshiContext.getMacroElementsMap(), macroFilePaths);
	}

	public static class SortBySize implements Comparator<List<?>> {

		@Override
		public int compare(List<?> o1, List<?> o2) {
			return o2.size() - o1.size();
		}

	}

}