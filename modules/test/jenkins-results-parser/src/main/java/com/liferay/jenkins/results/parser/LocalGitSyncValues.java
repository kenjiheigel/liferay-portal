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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kenji Heigel
 */
public class LocalGitSyncValues {

	public LocalGitSyncValues(
		GitWorkingDirectory gitWorkingDirectory, String receiverUsername,
		String senderBranchName, String senderUsername, String senderBranchSHA,
		String upstreamBranchSHA) {

		setLocalGitSyncValues(
			gitWorkingDirectory, receiverUsername, senderBranchName,
			senderUsername, senderBranchSHA, upstreamBranchSHA);
	}

	public LocalGitSyncValues copy(LocalGitSyncValues localGitSyncValues) {
		return new LocalGitSyncValues(
			localGitSyncValues.getGitWorkingDirectory(),
			localGitSyncValues.getReceiverUsername(),
			localGitSyncValues.getSenderBranchName(),
			localGitSyncValues.getSenderUserName(),
			localGitSyncValues.getSenderBranchSHA(),
			localGitSyncValues.getUpstreamBranchSHA());
	}

	public GitWorkingDirectory getGitWorkingDirectory() {
		return _gitWorkingDirectory;
	}

	public String getReceiverUsername() {
		return _receiverUsername;
	}

	public String getSenderBranchName() {
		return _senderBranchName;
	}

	public String getSenderBranchSHA() {
		return _senderBranchSHA;
	}

	public String getSenderUserName() {
		return _senderUserName;
	}

	public String getUpstreamBranchSHA() {
		return _upstreamBranchSHA;
	}

	protected List<LocalGitSyncValues> getStoredLocalGitSyncValues() {
		return _storedLocalGitSyncValues;
	}

	protected void setLocalGitSyncValues(
		GitWorkingDirectory gitWorkingDirectory, String receiverUsername,
		String senderBranchName, String senderUsername, String senderBranchSHA,
		String upstreamBranchSHA) {

		_gitWorkingDirectory = gitWorkingDirectory;
		_receiverUsername = receiverUsername;
		_senderBranchName = senderBranchName;
		_senderBranchSHA = senderBranchSHA;
		_senderUserName = senderUsername;
		_upstreamBranchSHA = upstreamBranchSHA;
	}

	protected void storeLocalGitSyncValues() {
		_storedLocalGitSyncValues.add(copy(this));
	}

	private GitWorkingDirectory _gitWorkingDirectory;
	private String _receiverUsername;
	private String _senderBranchName;
	private String _senderBranchSHA;
	private String _senderUserName;
	private List<LocalGitSyncValues> _storedLocalGitSyncValues =
		new ArrayList<>();
	private String _upstreamBranchSHA;

}