'use strict';

import ActionURLScreen from './screen/ActionURLScreen.es';
import App from './app/App.es';
import async from 'metal/src/async/async'
import dom from 'metal/src/dom/dom'
import EventScreen from './screen/EventScreen.es';
import globals from 'senna/src/globals/globals'
import RenderURLScreen from './screen/RenderURLScreen.es';
import Surface from './surface/Surface.es';
import Utils from './util/Utils.es';

let app = new App();

app.setBasePath(Utils.getBasePath());
app.setFormSelector('form:not([data-senna-off])');
app.setLinkSelector('a:not(.portlet-icon-back):not([data-navigation]):not([data-resource-href]):not([target="_blank"])');

Utils.getSurfaceIds().forEach(
	(surfaceId) => {
		app.addSurfaces(new Surface(surfaceId));
	}
);

app.addRoutes(
	[
		{
			handler: ActionURLScreen,
			path: function(url) {
				return url.search(Utils.getPatternPortletURL('1')) > -1;
			}
		},
		{
			handler: RenderURLScreen,
			path: function(url) {
				return url.search(Utils.getPatternFriendlyURL(url)) > -1;
			}
		},
		{
			handler: RenderURLScreen,
			path: function(url) {
				return url.search(Utils.getPatternPortletURL('0')) > -1;
			}
		}
	]
);

Liferay.on(
	'closePortlet',
	function(event) {
		var portletId = event.portletId;

		var surfaceId = Utils.getPortletBoundaryId(portletId);

		delete app.surfaces[surfaceId];
	}
);

Liferay.on(
	'portletReady',
	function(event) {
		var portletId = event.portletId;

		var surfaceId = Utils.getPortletBoundaryId(portletId);

		var surface = app.surfaces[surfaceId];

		if (surface && surface.activeChild && !dom.contains(document, surface.activeChild)) {
			surface = null;
		}

		if (!surface && Utils.isPortletSurface(portletId)) {
			app.addSurfaces(surfaceId);
		}
	}
);

Liferay.on(
	'surfaceScreenLoad',
	function(event) {
		Utils.resetAllPortlets();
	}
);

Liferay.on(
	'surfaceEndNavigate',
	function(event) {
		if (!event.error) {
			var activeScreen = app.activeScreen;

			var dataChannel = activeScreen.dataChannel;

			if (dataChannel.scrollElementId) {
				var scrollElement = document.getElementById(dataChannel.scrollElementId);

				if (scrollElement) {
					scrollElement.scrollIntoView();
				}
			}
		}
		else {
			window.location.href = event.path;
		}
	}
);

Liferay.Util.submitForm = function(form) {
	async.nextTick(
		() => {
			let ajaxableForm = app.isFormAjaxable(form.getDOM());

			if (ajaxableForm && globals.capturedFormElement) {
				Liferay.Util._submitLocked = false;
			}
			else {
				form.submit();
			}
		}
	);
}

Liferay.SPA = {
	app: app,
	blacklist: Utils.blacklist
};

export default Liferay.SPA;