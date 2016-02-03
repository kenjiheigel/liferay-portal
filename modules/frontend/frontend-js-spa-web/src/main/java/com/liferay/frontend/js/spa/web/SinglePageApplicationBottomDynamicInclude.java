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

package com.liferay.frontend.js.spa.web;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.servlet.taglib.BaseDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PortletKeys;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;

/**
 * @author Bruno Basto
 */
@Component(immediate = true, service = DynamicInclude.class)
public class SinglePageApplicationBottomDynamicInclude
	extends BaseDynamicInclude {

	@Override
	public void include(
			HttpServletRequest request, HttpServletResponse response,
			String key)
		throws IOException {

		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);

		boolean singlePageApplicationEnabled = GetterUtil.getBoolean(
			PropsUtil.get(
				PropsKeys.JAVASCRIPT_SINGLE_PAGE_APPLICATION_ENABLED));

		if (singlePageApplicationEnabled) {
			PrintWriter printWriter = response.getWriter();

			printWriter.println(getOpenSurface());
			printWriter.println(getSPAInitializer(themeDisplay));
			printWriter.println(getSurfaceDataChannel(request));
			printWriter.println(getCurrentURL(request));
			printWriter.println(getCloseSurface());
		}
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register("/html/common/themes/bottom.jsp#post");
	}

	protected String getCloseSurface() {
		return "<div class=\"lfr-surface-loading-bar\"></div></div></div>";
	}

	protected String getCurrentURL(HttpServletRequest request) {
		StringBundler sb = new StringBundler(6);

		String currentURL = PortalUtil.getCurrentURL(request);

		sb.append("<script type=\"text/javascript\">");
		sb.append("Liferay.currentURL='");
		sb.append(HtmlUtil.escapeJS(currentURL));
		sb.append("';Liferay.currentURLEncoded='");
		sb.append(HttpUtil.encodeURL(currentURL));
		sb.append("';</script>");

		return sb.toString();
	}

	protected String getOpenSurface() {
		return "<div id=\"bottomJS\"><div id=\"bottomJS-defaultScreen\">";
	}

	protected String getSPAInitializer(ThemeDisplay themeDisplay) {
		List<Portlet> companyPortlets = PortletLocalServiceUtil.getPortlets(
			themeDisplay.getCompanyId());

		StringBundler sb = new StringBundler(companyPortlets.size() * 6 + 6);

		sb.append("<script type=\"text/javascript\">");
		sb.append("require('frontend-js-spa-web@1.0.0/liferay/init.es',");
		sb.append("function(){");

		for (Portlet portlet : companyPortlets) {
			if (portlet.isActive() && portlet.isReady() &&
				!portlet.isUndeployedPortlet() &&
				!portlet.isSinglePageApplication()) {

				sb.append("Liferay.SPA.blacklist.route['");
				sb.append(portlet.getPortletId());
				sb.append("'] = true;");
			}
		}

		sb.append("Liferay.SPA.blacklist.surface['");
		sb.append(PortletKeys.NESTED_PORTLETS);
		sb.append("'] = true;});</script>");

		return sb.toString();
	}

	protected String getSurfaceDataChannel(HttpServletRequest request) {
		StringBundler sb = new StringBundler(3);

		String scrollElementId = HtmlUtil.escape(
			ParamUtil.getString(request, "scroll"));

		sb.append("<script type=\"text/surface-data-channel\">");

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		jsonObject.put("scrollElementId", scrollElementId);

		sb.append(jsonObject.toJSONString());
		sb.append("</script>");

		return sb.toString();
	}

}