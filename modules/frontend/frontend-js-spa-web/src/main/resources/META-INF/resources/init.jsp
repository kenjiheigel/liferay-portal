<%--
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
--%>

<%@ page import="com.liferay.portal.kernel.json.JSONFactoryUtil" %><%@
page import="com.liferay.portal.kernel.json.JSONObject" %><%@
page import="com.liferay.portal.kernel.theme.ThemeDisplay" %><%@
page import="com.liferay.portal.kernel.util.WebKeys" %><%@
page import="com.liferay.portal.kernel.model.Portlet" %><%@
page import="com.liferay.portal.kernel.service.PortletLocalServiceUtil" %>

<%@ page import="java.util.List" %>

<script type="text/javascript">
	require(
		'frontend-js-spa-web@1.0.0/liferay/init.es',
		function(SPA) {
			SPA.default.app.setBlacklist(<%= getPortletsBlacklist(request) %>);
		}
	);
</script>

<%!
protected String getPortletsBlacklist(HttpServletRequest request) {
	JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

	ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(WebKeys.THEME_DISPLAY);

	List<Portlet> companyPortlets = PortletLocalServiceUtil.getPortlets(themeDisplay.getCompanyId());

	for (Portlet portlet : companyPortlets) {

		if (portlet.isActive() && portlet.isReady() && !portlet.isUndeployedPortlet() && !portlet.isSinglePageApplication()) {
			jsonObject.put(portlet.getPortletId(), true);
		}
	}

	return jsonObject.toString();
}
%>