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

package com.liferay.jenkins.results.parser;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Kenji Heigel
 */
public class ExecutorServiceUtil {

	public static ThreadPoolExecutor getNewThreadPoolExecutor(
		int maximumPoolSize) {

		return getNewThreadPoolExecutor(5, maximumPoolSize);
	}

	public static ThreadPoolExecutor getNewThreadPoolExecutor(
		int keepAliveTime, int maximumPoolSize) {

		ThreadPoolExecutor threadPoolExecutor =
			(ThreadPoolExecutor)Executors.newFixedThreadPool(maximumPoolSize);

		threadPoolExecutor.setKeepAliveTime(keepAliveTime, TimeUnit.SECONDS);

		threadPoolExecutor.allowCoreThreadTimeOut(true);
		threadPoolExecutor.setCorePoolSize(maximumPoolSize);
		threadPoolExecutor.setMaximumPoolSize(maximumPoolSize);

		return threadPoolExecutor;
	}

}