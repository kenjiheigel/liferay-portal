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
		this.setLinkSelector('a:not([target="_blank"]):not([data-resource-href]):not([data-senna-off])');

		this.on('beforeNavigate', this.onBeforeNavigate);
		this.on('endNavigate', this.onEndNavigate);
		this.on('startNavigate', this.onStartNavigate);

		this.addSurfaces(document.body.id);

		dom.append(document.body, '<div class="lfr-surface-loading-bar"></div>');
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

	onDocClickDelegate_(event) {
		var inBlacklist = false;

		Object.keys(this.blacklist).map(
			(portletId) => {
				var boundaryId = Utils.getPortletBoundaryId(portletId);
				var portlets = document.querySelectorAll('[id^="' + boundaryId +  '"]');

				Array.prototype.slice.call(portlets).forEach(
					(portlet) => {
						if (dom.contains(portlet, event.delegateTarget)) {
							inBlacklist = true;
							return;
						}
					}
				);
			}
		);

		if (inBlacklist) {
			return;
		}

		super.onDocClickDelegate_(event);
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
	}
}

export default LiferayApp;