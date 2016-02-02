'use strict';

import core from 'metal/src/core'
import dom from 'metal/src/dom/dom'
import Surface from 'senna/src/surface/Surface'

Surface.DEFAULT = 'defaultScreen';

class LiferaySurface extends Surface {
	addContent(screenId, content) {
		if (content) {
			if (core.isString(content)) {
				content = dom.buildFragment(content);
			}

			Liferay.Data.sharedResources.forEach(
				function(outputKey) {
					var resources = content.querySelectorAll('[data-outputkey="' + outputKey + '"]');

					for (var i = 0; i < resources.length; i++) {
						resources[i].remove();
					}
				}
			);

			var newResources = content.querySelectorAll('[data-outputkey]');

			var newResourceKeys = [];

			for (var i = 0; i < newResources.length; i++) {
				newResourceKeys.push(newResources[i].dataset.outputkey);

				console.log('new resources', newResources[i]);
			}

			Liferay.Data.sharedResources = Liferay.Data.sharedResources.concat([...new Set(newResourceKeys)]);

			Liferay.DOMTaskRunner.runTasks(content);
		}

		console.log('addContent');

		return super.addContent(screenId, content);
	}
}

export default LiferaySurface;