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

package com.liferay.portal.portlet.bridge.soy;

import com.liferay.portal.kernel.io.unsync.UnsyncStringWriter;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.template.Template;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.template.TemplateException;
import com.liferay.portal.kernel.template.TemplateManagerUtil;
import com.liferay.portal.kernel.template.TemplateResource;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.UnsyncPrintWriterPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.portlet.bridge.soy.internal.SoyPortletHelper;
import com.liferay.portal.portlet.bridge.soy.internal.SoyTemplateResourcesCollector;

import java.io.IOException;
import java.io.Writer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.MimeResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Miroslav Ligas
 * @author Bruno Basto
 */
public class SoyPortlet extends MVCPortlet {

	@Override
	public void init() throws PortletException {
		super.init();

		propagateRequestParameters = GetterUtil.getBoolean(
			getInitParameter("propagate-request-parameters"), true);

		Bundle bundle = FrameworkUtil.getBundle(this.getClass());

		try {
			_soyPortletHelper = new SoyPortletHelper(bundle);

			_templateResources = _getTemplateResources(bundle);
		}
		catch (Exception e) {
			throw new PortletException(e);
		}
	}

	public void doRender(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		renderRequest.setAttribute(WebKeys.TEMPLATE, _createTemplate());

		doRender(renderRequest, renderResponse);

		super.render(renderRequest, renderResponse);
	}

	protected void addRenderAttribute(RenderRequest request, String key,
			Object value) {

		Template template = getTemplate(request);

		template.put(key, value);
	}

	protected Set<String> getJavaScriptRequiredModules(String path) {
		return Collections.emptySet();
	}

	protected Template getTemplate(PortletRequest request) {
		Template template = (Template) request.getAttribute(WebKeys.TEMPLATE);

		if(template == null){
			throw new IllegalStateException(
				"Template was not set for request. " +
				"Don't overrider render method");
		}

		return template;
	}

	@Override
	protected void include(
			String namespace, PortletRequest portletRequest,
			PortletResponse portletResponse, String lifecycle)
		throws IOException, PortletException {

		try {
			String path = getPath(portletRequest, portletResponse);

			if (Validator.isNull(path)) {
				path = namespace;
			}

			Template template = getTemplate(portletRequest);

			template.put(
				TemplateConstants.NAMESPACE,
				_soyPortletHelper.getTemplateNamespace(path));

			if (propagateRequestParameters) {
				propagateRequestParameters(portletRequest, template);
			}

			Writer writer = null;

			if (portletResponse instanceof MimeResponse) {
				MimeResponse mimeResponse = (MimeResponse)portletResponse;

				writer = UnsyncPrintWriterPool.borrow(mimeResponse.getWriter());
			}
			else {
				writer = new UnsyncStringWriter();
			}

			populateJavaScriptTemplateContext(
				template, portletResponse.getNamespace());

			template.processTemplate(writer);

			String portletJavaScript = _soyPortletHelper.getPortletJavaScript(
				template, path, portletResponse.getNamespace(),
				getJavaScriptRequiredModules(path));

			writer.write(portletJavaScript);
		}
		catch (Exception e) {
			throw new PortletException(e);
		}

		if (clearRequestParameters) {
			if (lifecycle.equals(PortletRequest.RENDER_PHASE)) {
				portletResponse.setProperty("clear-request-parameters", "true");
			}
		}
	}

	protected void populateJavaScriptTemplateContext(
		Template template, String portletNamespace) {

		String portletComponentId = portletNamespace.concat("PortletComponent");

		template.put("element", StringPool.POUND.concat(portletComponentId));
		template.put("id", portletComponentId);
	}

	protected void propagateRequestParameters(PortletRequest portletRequest,
		Template template) {

		Map<String, String[]> parametersMap = portletRequest.getParameterMap();

		for (Map.Entry<String, String[]> entry : parametersMap.entrySet()) {
			String parameterName = entry.getKey();
			String[] parameterValues = entry.getValue();

			if (parameterValues.length == 1) {
				template.put(parameterName, parameterValues[0]);
			}
			else if (parameterValues.length > 1) {
				template.put(parameterName, parameterValues);
			}
		}
	}

	protected boolean propagateRequestParameters;

	private Template _createTemplate() throws PortletException {

		try {
			return TemplateManagerUtil.getTemplate(
				TemplateConstants.LANG_TYPE_SOY, _templateResources, false);
		}
		catch (TemplateException te) {
			throw new PortletException(te);
		}
	}

	private List<TemplateResource> _getTemplateResources(Bundle bundle)
		throws TemplateException {

		SoyTemplateResourcesCollector soyTemplateResourcesCollector =
			new SoyTemplateResourcesCollector(bundle, templatePath);

		return soyTemplateResourcesCollector.getTemplateResources();
	}

	private List<TemplateResource> _templateResources;
	private SoyPortletHelper _soyPortletHelper;

}