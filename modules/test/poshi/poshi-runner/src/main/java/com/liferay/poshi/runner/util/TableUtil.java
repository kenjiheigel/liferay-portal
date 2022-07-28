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

package com.liferay.poshi.runner.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yi-Chen Tsai
 */
public class TableUtil {

	public static List<List<String>> getTableDataListFromString(
		String tableDataString) {

		Matcher rowMatcher = _rowPattern.matcher(tableDataString);

		List<List<String>> tableDataList = new ArrayList<>();

		while (rowMatcher.find()) {
			String row = rowMatcher.group("row");

			Matcher entryMatcher = _entryPattern.matcher(row);

			List<String> rowList = new ArrayList<>();

			while (entryMatcher.find()) {
				String entry = entryMatcher.group("entry");

				rowList.add(entry.trim());
			}

			tableDataList.add(rowList);
		}

		return tableDataList;
	}

	public static int getTableDataListWidth(List<List<String>> tableDataList) {
		if ((tableDataList == null) || tableDataList.isEmpty()) {
			return 0;
		}

		List<String> firstRow = tableDataList.get(0);

		return firstRow.size();
	}

	public static List<List<String>> getTransposedTableDataList(
		List<List<String>> tableDataList) {

		List<List<String>> transposedTableDataList = new ArrayList<>();

		for (int i = 0; i < getTableDataListWidth(tableDataList); i++) {
			List<String> column = new ArrayList<>();

			for (List<String> row : tableDataList) {
				column.add(row.get(i));
			}

			transposedTableDataList.add(column);
		}

		return transposedTableDataList;
	}

	private static final Pattern _entryPattern = Pattern.compile(
		"(?<entry>.*?)\\|");
	private static final Pattern _rowPattern = Pattern.compile(
		"\\|(?<row>.*\\|)(\\s*\\R)*");

}