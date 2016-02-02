'use strict';

import EventScreen from './EventScreen.es'
import globals from 'senna/src/globals/globals'
import Utils from '../util/Utils.es';

class ActionURLScreen extends EventScreen {
	constructor() {
		super();

		this.httpMethod = 'POST';
	}

	beforeUpdateHistoryPath(path) {
		let form = globals.capturedFormElement;

		if (form) {
			Utils.removeFormRedirectIsolated(form);
		}

		let redirectPath = super.beforeUpdateHistoryPath(path);

		return Utils.removePortletURLIsolated(redirectPath);
	}

	flip(surfaces) {
		var instance = this;

		var currentPath = window.location.pathname + window.location.search;

		if (!Liferay.SPA.app.findRoute(currentPath)) {
			location.href = currentPath;
		}

		return super.flip(surfaces);
	}
}

export default ActionURLScreen;