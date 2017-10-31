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

package com.liferay.poshi.runner.elements;

import java.io.File;

import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dom4j.Comment;

/**
 * @author Michael Hashimoto
 */
public class PoshiCommentFactory {

	public static PoshiComment newPoshiComment(Comment comment) {
		for (PoshiComment poshiComment : _poshiComments) {
			PoshiComment newPoshiComment = poshiComment.clone(comment);

			if (newPoshiComment != null) {
				return newPoshiComment;
			}
		}

		throw new RuntimeException("Unknown comment\n" + comment);
	}

	public static PoshiComment newPoshiComment(String readableSyntax) {
		for (PoshiComment poshiComment : _poshiComments) {
			PoshiComment newPoshiComment = poshiComment.clone(readableSyntax);

			if (newPoshiComment != null) {
				return newPoshiComment;
			}
		}

		throw new RuntimeException(
			"Unknown readable syntax\n" + readableSyntax);
	}

	private static final List<PoshiComment> _poshiComments = new ArrayList<>();

	static {
		try {
			URL url = BasePoshiComment.class.getResource(
				BasePoshiComment.class.getSimpleName() + ".class");

			File basePoshiCommentClassFile = new File(url.toURI());

			File dir = basePoshiCommentClassFile.getParentFile();

			List<File> dirFiles = Arrays.asList(dir.listFiles());

			Collections.sort(dirFiles);

			for (File file : dirFiles) {
				String fileName = file.getName();

				if (fileName.endsWith(
						BasePoshiComment.class.getSimpleName() + ".class")) {

					continue;
				}

				if (!fileName.endsWith("PoshiComment.class")) {
					continue;
				}

				Package pkg = BasePoshiComment.class.getPackage();

				int index = fileName.indexOf(".");

				String className = fileName.substring(0, index);

				Class<?> clazz = Class.forName(pkg.getName() + "." + className);

				if (BasePoshiComment.class.isAssignableFrom(clazz)) {
					PoshiComment poshiComment =
						(PoshiComment)clazz.newInstance();

					_poshiComments.add(poshiComment);
				}
			}
		}
		catch (ClassNotFoundException | IllegalAccessException |
			   InstantiationException | URISyntaxException e) {

			throw new RuntimeException(e);
		}
	}

}