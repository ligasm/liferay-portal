/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.liferay.util.bridges.soy;

import com.liferay.portal.kernel.io.unsync.UnsyncStringWriter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.template.Template;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.template.TemplateException;
import com.liferay.portal.kernel.template.TemplateManager;
import com.liferay.portal.kernel.template.TemplateManagerUtil;
import com.liferay.portal.kernel.template.TemplateResource;
import com.liferay.portal.kernel.template.TemplateResourceLoaderUtil;
import com.liferay.portal.kernel.util.UnsyncPrintWriterPool;
import com.liferay.portal.util.PortalUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import javax.portlet.MimeResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Miroslav Ligas
 */
public class SoyPortlet extends MVCPortlet{

	@Override
	public void destroy() {
		super.destroy();

		Class<?> clazz = getClass();

		TemplateManagerUtil.destroy(clazz.getClassLoader());
	}

	@Override
	protected void include(
		String path, PortletRequest portletRequest,
		PortletResponse portletResponse, String lifecycle)
		throws IOException, PortletException {

		PortletContext portletContext = getPortletContext();

		String servletContextName = portletContext.getPortletContextName();

		String resourcePath = servletContextName.concat(
			TemplateConstants.SERVLET_SEPARATOR).concat(path);

		boolean resourceExists = false;

		try {
			resourceExists = TemplateResourceLoaderUtil.hasTemplateResource(
				TemplateConstants.LANG_TYPE_SOY, resourcePath);
		}
		catch (TemplateException te) {
			throw new IOException(te);
		}

		if (!resourceExists) {
			_log.error(path + " is not a valid include");
		}


		else {
			try {

				//TODO: accept custom path coming form outside
				List<TemplateResource> templateResources =
					_getTemplateResources(TEMPLATE_SET_FILE);

				TemplateManager templateManager =
					TemplateManagerUtil.getTemplateManager(
						TemplateConstants.LANG_TYPE_SOY);

				Template template = TemplateManagerUtil.getTemplates(
					TemplateConstants.LANG_TYPE_SOY, templateResources, false);

				templateManager.addTaglibApplication(
					template, "Application", getServletContext());

				templateManager.addTaglibRequest(
					template, "Request",
					PortalUtil.getHttpServletRequest(portletRequest),
					PortalUtil.getHttpServletResponse(portletResponse));

				template.put("portletContext", getPortletContext());
				template.put(
					"userInfo",
					portletRequest.getAttribute(PortletRequest.USER_INFO));

				Writer writer = null;

				if (portletResponse instanceof MimeResponse) {
					MimeResponse mimeResponse = (MimeResponse)portletResponse;

					writer = UnsyncPrintWriterPool.borrow(
						mimeResponse.getWriter());
				}
				else {
					writer = new UnsyncStringWriter();
				}

				template.processTemplate(writer);
			}
			catch (Exception e) {
				throw new PortletException(e);
			}
		}

		if (clearRequestParameters) {
			if (lifecycle.equals(PortletRequest.RENDER_PHASE)) {
				portletResponse.setProperty("clear-request-parameters", "true");
			}
		}
	}

	private List<TemplateResource> _getTemplateResources(String path) throws TemplateException {
		List<TemplateResource> result = new ArrayList<>();
		BundleContext context = FrameworkUtil.
			getBundle(this.getClass()).
			getBundleContext();

		Enumeration<URL> urls =
			context.getBundle().findEntries(path, "*", false);

		long bundleId = context.getBundle().getBundleId();

		while (urls.hasMoreElements()){
			URL url = urls.nextElement();

			String templatePath = bundleId +
				TemplateConstants.LANG_TYPE_SOY +
				url.getPath();

			TemplateResource templateResource =
				TemplateResourceLoaderUtil.getTemplateResource(
					TemplateConstants.LANG_TYPE_SOY, templatePath);

			result.add(templateResource);
		}
		return result;
	}

	private static final String TEMPLATE_SET_FILE = "soy";

	private static final Log _log = LogFactoryUtil.getLog(
		SoyPortlet.class);
}
