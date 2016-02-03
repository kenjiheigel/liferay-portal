'use strict';

import App from 'senna/src/app/App';
import dom from 'metal/src/dom/dom'
import globals from 'senna/src/globals/globals'
import Utils from '../util/Utils.es';

class LiferayApp extends App {
	constructor() {
		super();

		this.on('beforeNavigate', this.onBeforeNavigate);
		this.on('endNavigate', this.onEndNavigate);
		this.on('startNavigate', this.onStartNavigate);
	}

	onBeforeNavigate(event) {
		event.path = Utils.makePortletURLIsolated(event.path);

		let form = globals.capturedFormElement;

		if (form) {
			Utils.makeFormRedirectIsolated(form);
		}

		Liferay.fire(
			'surfaceBeforeNavigate',
			{
				app: this,
				path: event.path
			}
		);
	}

	onEndNavigate(event) {
		Liferay.DOMTaskRunner.reset();

		Liferay.fire(
			'surfaceEndNavigate',
			{
				app: this,
				error: event.error,
				path: event.path
			}
		);

		dom.removeClasses(document.body, 'lfr-surface-loading');
	}

	onStartNavigate(event) {
		Liferay.fire(
			'surfaceStartNavigate',
			{
				app: this,
				path: event.path
			}
		);

		dom.addClasses(document.body, 'lfr-surface-loading');
	}

	maybeNavigateToLinkElement_(link, event) {
		var path = link.pathname + link.search + link.hash;

		if (!this.isLinkSameOrigin_(link.hostname)) {
			console.log('Offsite link clicked');

			globals.capturedFormElement = null;
			return;
		}
		if (!this.isSameBasePath_(path)) {
			console.log('Link clicked outside app\'s base path');

			globals.capturedFormElement = null;
			return;
		}
		if (!this.findRoute(path)) {
			console.log('No route for ' + path);

			globals.capturedFormElement = null;
			return;
		}

		var navigateFailed = false;
		try {
			this.navigate(path);
		} catch (err) {
			// Do not prevent link navigation in case some synchronous error occurs
			navigateFailed = true;
		}

		if (!navigateFailed) {
			event.preventDefault();
		}
	}

	isFormAjaxable(form) {
		return dom.match(form, this.getFormSelector());
	}
}

export default LiferayApp;