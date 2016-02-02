'use strict';

import Uri from './Uri.es'

class Utils {
	static getBasePath() {
		var layoutRelativeURL = themeDisplay.getLayoutRelativeURL();

		var pos = layoutRelativeURL.lastIndexOf('?');

		if (pos > -1) {
			layoutRelativeURL = layoutRelativeURL.substr(0, pos);
		}

		return layoutRelativeURL;
	}

	static getNamespace(portletURL) {
		var uri = new Uri(portletURL);

		return Liferay.Util.getPortletNamespace(uri.getParameterValue('p_p_id'));
	}

	static getPatternFriendlyURL(url) {
		if (!themeDisplay.isControlPanel()) {
			var friendlyURLMaximized = url.indexOf('/maximized') > -1;

			if (themeDisplay.isStateMaximized() && !friendlyURLMaximized) {
				return null;
			}

			if (!themeDisplay.isStateMaximized() && friendlyURLMaximized) {
				return null;
			}
		}

		return /\/-\//;
	}

	static getPatternPortletURL(lifecycle) {
		var routeablePortletIds = Utils.getRouteablePortletIds();

		var windowState = 'NORMAL';

		if (themeDisplay.isStateExclusive()) {
			windowState = 'EXCLUSIVE';
		}
		else if (themeDisplay.isStatePopUp()) {
			windowState = 'POP_UP';
		}
		else if (themeDisplay.isStateMaximized()) {
			windowState = 'MAXIMIZED';
		}

		return new RegExp('p_p_id=(' + routeablePortletIds.join('|') + ')&p_p_lifecycle=' + lifecycle + '&p_p_state=' + windowState.toLowerCase());
	}

	static getPortletBoundaryId(portletId) {
		return 'p_p_id_' + portletId + '_';
	}

	static getPortletBoundaryIds(portletIds) {
		return portletIds.map(
			function(portletId) {
				return Utils.getPortletBoundaryId(portletId);
			}
		);
	}

	static addIsolatedParams(uri) {
		uri.setParameterValue('p_p_ajax', false);
		uri.setParameterValue('p_p_isolated', true);
	}

	static removeIsolatedParams(uri) {
		uri.removeParameter('p_p_ajax');
		uri.removeParameter('p_p_isolated');
	}

	static makePortletURLIsolated(portletURL) {
		let namespace = Utils.getNamespace(portletURL);

		let portletURI = new Uri(portletURL);

		Utils.addIsolatedParams(portletURI);

		let redirect = portletURI.getParameterValue(namespace + 'redirect');

		if (redirect) {
			let redirectURI = new Uri(decodeURIComponent(redirect));

			Utils.addIsolatedParams(redirectURI);

			portletURI.setParameterValue(namespace + 'redirect', redirectURI.toString());
		}

		return portletURI.toString();
	}

	static removePortletURLIsolated(portletURL) {
		let namespace = Utils.getNamespace(portletURL);

		let portletURI = new Uri(portletURL);

		Utils.removeIsolatedParams(portletURI);

		let redirect = portletURI.getParameterValue(namespace + 'redirect');

		if (redirect) {
			let redirectURI = new Uri(decodeURIComponent(redirect));

			Utils.removeIsolatedParams(redirectURI);

			portletURI.setParameterValue(namespace + 'redirect', redirectURI.toString());
		}

		return portletURI.toString();
	}

	static makeFormRedirectIsolated(form) {
		let namespace = Utils.getNamespace(form.action);

		let redirectInput = form[namespace + 'redirect'];

		if (redirectInput && redirectInput.value) {
			redirectInput.value = Utils.makePortletURLIsolated(redirectInput.value);
		}
	}

	static removeFormRedirectIsolated(form) {
		let namespace = Utils.getNamespace(form.action);

		let redirectInput = form[namespace + 'redirect'];

		if (redirectInput && redirectInput.value) {
			redirectInput.value = Utils.removePortletURLIsolated(redirectInput.value);
		}
	}

	static getRouteablePortletIds() {
		return Liferay.Portlet.list.filter(
			function(portletId) {
				return Utils.isPortletRouteable(portletId);
			}
		);
	}

	static getSurfaceIds() {
		var surfaces = Utils.getPortletBoundaryIds(Utils.getSurfacePortletIds());

		surfaces.push('bottomJS');

		return surfaces;
	}

	static getSurfacePortletIds() {
		return Liferay.Portlet.list.filter(
			function(portletId) {
				return Utils.isPortletSurface(portletId);
			}
		);
	}

	static isPortletRouteable(portletId) {
		return !Utils.blacklist.route[Utils.maybeExtractPortletId(portletId)];
	}

	static isPortletSurface(portletId) {
		return !Utils.blacklist.surface[Utils.maybeExtractPortletId(portletId)];
	}

	static maybeExtractPortletId(portletId) {
		var lastIndexOf = String(portletId).lastIndexOf('_INSTANCE_');

		if (lastIndexOf > 0) {
			portletId = portletId.substr(0, lastIndexOf);
		}

		return portletId;
	}

	static resetAllPortlets() {
		Utils.getPortletBoundaryIds(Liferay.Portlet.list).forEach(
			function(value, index, collection) {
				var portlet = document.querySelector('#' + value);

				if (portlet) {
					Liferay.Portlet.destroy(portlet);

					portlet.portletProcessed = false;
				}
			}
		);

		Liferay.Portlet.readyCounter = 0;
	}
}

Utils.blacklist = {
	route: {},
	surface: {}
};

Liferay.SPAUtils = Utils;

export default Utils;