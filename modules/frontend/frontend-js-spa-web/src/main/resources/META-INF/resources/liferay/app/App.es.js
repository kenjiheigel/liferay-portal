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

	isFormAjaxable(form) {
		return dom.match(form, this.getFormSelector());
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
}

export default LiferayApp;