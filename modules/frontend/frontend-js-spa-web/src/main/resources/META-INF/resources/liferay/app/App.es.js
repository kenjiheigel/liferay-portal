'use strict';

import App from 'senna/src/app/App';
import dom from 'metal-dom/src/dom'
import Uri from 'metal-uri/src/Uri'
import globals from 'senna/src/globals/globals'
import Utils from '../util/Utils.es';

class LiferayApp extends App {
	constructor() {
		super();

		this.blacklist = {};

		this.setFormSelector('form:not([target="_blank"])');
		this.setLinkSelector('a:not([data-resource-href]):not([target="_blank"])');

		this.on('beforeNavigate', this.onBeforeNavigate);
		this.on('endNavigate', this.onEndNavigate);
		this.on('startNavigate', this.onStartNavigate);

		this.addSurfaces(document.body.id);

		dom.append(document.body, '<div class="lfr-surface-loading-bar"></div>');
	}

	getSelectorBlacklist() {
		return Object.keys(this.blacklist).map(
			(portletId) => ':not([id^="' + Utils.getPortletBoundaryId(portletId) + '"] *)'
		).join('');
	}

	onBeforeNavigate(event) {
		Liferay.fire(
			'surfaceBeforeNavigate',
			{
				app: this,
				path: event.path
			}
		);
	}

	onEndNavigate(event) {
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

	setBlacklist(blacklist) {
		this.blacklist = blacklist;

		// let selectorBlacklist = this.getSelectorBlacklist();

		// this.setLinkSelector(this.getLinkSelector() + selectorBlacklist);
	}
}

export default LiferayApp;