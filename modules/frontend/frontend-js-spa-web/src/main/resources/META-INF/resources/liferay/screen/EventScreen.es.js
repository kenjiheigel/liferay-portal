'use strict';

import dom from 'metal/src/dom/dom'
import HtmlScreen from 'senna/src/screen/HtmlScreen'
import Uri from '../util/Uri.es'
import Utils from '../util/Utils.es'

class EventScreen extends HtmlScreen {
	constructor() {
		super();

		this.cacheable = false;
		this.dataChannel = {};
		this.timeout = Liferay.PropsValues.JAVASCRIPT_SINGLE_PAGE_APPLICATION_TIMEOUT;
	}

	dispose() {
		super.dispose();

		Liferay.fire(
			'surfaceScreenDestructor',
			{
				app: Liferay.SPA.app,
				screen: this
			}
		);
	}

	activate() {
		super.activate();

		Liferay.fire(
			'surfaceScreenActivate',
			{
				app: Liferay.SPA.app,
				screen: this
			}
		);
	}

	deactivate() {
		super.deactivate();

		Liferay.fire(
			'surfaceScreenDeactivate',
			{
				app: Liferay.SPA.app,
				screen: this
			}
		);

		this.dataChannel = {};
	}

	flip(surfaces) {
		var instance = this;

		return super.flip(surfaces).then(
			function(data) {
				Liferay.fire(
					'surfaceScreenFlip',
					{
						app: Liferay.SPA.app,
						screen: instance
					}
				);

				return data;
			}
		);
	}

	getApp() {
		return this.app;
	}

	load(path) {
		var instance = this;

		return super.load(path).then(
			function(content) {
				var frag = dom.buildFragment(content);

				var dataChannel = frag.querySelector('script[type="text/surface-data-channel"]');

				if (dataChannel) {
					dataChannel.remove();

					instance.dataChannel = JSON.parse(dataChannel.textContent);
				}

				Liferay.fire(
					'surfaceScreenLoad',
					{
						app: Liferay.SPA.app,
						content: content,
						screen: instance
					}
				);

				return content;
			}
		);
	}
}

export default EventScreen;