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

package com.liferay.document.library.kernel.service;

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.kernel.bean.PortalBeanLocatorUtil;
import com.liferay.portal.kernel.util.ReferenceRegistry;

/**
 * Provides the local service utility for DLSyncEvent. This utility wraps
 * {@link com.liferay.portlet.documentlibrary.service.impl.DLSyncEventLocalServiceImpl} and is the
 * primary access point for service operations in application layer code running
 * on the local server. Methods of this service will not have security checks
 * based on the propagated JAAS credentials because this service can only be
 * accessed from within the same VM.
 *
 * @author Brian Wing Shun Chan
 * @see DLSyncEventLocalService
 * @see com.liferay.portlet.documentlibrary.service.base.DLSyncEventLocalServiceBaseImpl
 * @see com.liferay.portlet.documentlibrary.service.impl.DLSyncEventLocalServiceImpl
 * @generated
 */
@ProviderType
public class DLSyncEventLocalServiceUtil {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Add custom service methods to {@link com.liferay.portlet.documentlibrary.service.impl.DLSyncEventLocalServiceImpl} and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	* Adds the d l sync event to the database. Also notifies the appropriate model listeners.
	*
	* @param dlSyncEvent the d l sync event
	* @return the d l sync event that was added
	*/
	public static com.liferay.document.library.kernel.model.DLSyncEvent addDLSyncEvent(
		com.liferay.document.library.kernel.model.DLSyncEvent dlSyncEvent) {
		return getService().addDLSyncEvent(dlSyncEvent);
	}

	public static com.liferay.document.library.kernel.model.DLSyncEvent addDLSyncEvent(
		java.lang.String event, java.lang.String type, long typePK) {
		return getService().addDLSyncEvent(event, type, typePK);
	}

	/**
	* Creates a new d l sync event with the primary key. Does not add the d l sync event to the database.
	*
	* @param syncEventId the primary key for the new d l sync event
	* @return the new d l sync event
	*/
	public static com.liferay.document.library.kernel.model.DLSyncEvent createDLSyncEvent(
		long syncEventId) {
		return getService().createDLSyncEvent(syncEventId);
	}

	/**
	* Deletes the d l sync event from the database. Also notifies the appropriate model listeners.
	*
	* @param dlSyncEvent the d l sync event
	* @return the d l sync event that was removed
	*/
	public static com.liferay.document.library.kernel.model.DLSyncEvent deleteDLSyncEvent(
		com.liferay.document.library.kernel.model.DLSyncEvent dlSyncEvent) {
		return getService().deleteDLSyncEvent(dlSyncEvent);
	}

	/**
	* Deletes the d l sync event with the primary key from the database. Also notifies the appropriate model listeners.
	*
	* @param syncEventId the primary key of the d l sync event
	* @return the d l sync event that was removed
	* @throws PortalException if a d l sync event with the primary key could not be found
	*/
	public static com.liferay.document.library.kernel.model.DLSyncEvent deleteDLSyncEvent(
		long syncEventId)
		throws com.liferay.portal.kernel.exception.PortalException {
		return getService().deleteDLSyncEvent(syncEventId);
	}

	public static void deleteDLSyncEvents() {
		getService().deleteDLSyncEvents();
	}

	/**
	* @throws PortalException
	*/
	public static com.liferay.portal.kernel.model.PersistedModel deletePersistedModel(
		com.liferay.portal.kernel.model.PersistedModel persistedModel)
		throws com.liferay.portal.kernel.exception.PortalException {
		return getService().deletePersistedModel(persistedModel);
	}

	public static com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery() {
		return getService().dynamicQuery();
	}

	/**
	* Performs a dynamic query on the database and returns the matching rows.
	*
	* @param dynamicQuery the dynamic query
	* @return the matching rows
	*/
	public static <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {
		return getService().dynamicQuery(dynamicQuery);
	}

	/**
	* Performs a dynamic query on the database and returns a range of the matching rows.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link com.liferay.portlet.documentlibrary.model.impl.DLSyncEventModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	* </p>
	*
	* @param dynamicQuery the dynamic query
	* @param start the lower bound of the range of model instances
	* @param end the upper bound of the range of model instances (not inclusive)
	* @return the range of matching rows
	*/
	public static <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end) {
		return getService().dynamicQuery(dynamicQuery, start, end);
	}

	/**
	* Performs a dynamic query on the database and returns an ordered range of the matching rows.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link com.liferay.portlet.documentlibrary.model.impl.DLSyncEventModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	* </p>
	*
	* @param dynamicQuery the dynamic query
	* @param start the lower bound of the range of model instances
	* @param end the upper bound of the range of model instances (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of matching rows
	*/
	public static <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end,
		com.liferay.portal.kernel.util.OrderByComparator<T> orderByComparator) {
		return getService()
				   .dynamicQuery(dynamicQuery, start, end, orderByComparator);
	}

	/**
	* Returns the number of rows matching the dynamic query.
	*
	* @param dynamicQuery the dynamic query
	* @return the number of rows matching the dynamic query
	*/
	public static long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {
		return getService().dynamicQueryCount(dynamicQuery);
	}

	/**
	* Returns the number of rows matching the dynamic query.
	*
	* @param dynamicQuery the dynamic query
	* @param projection the projection to apply to the query
	* @return the number of rows matching the dynamic query
	*/
	public static long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery,
		com.liferay.portal.kernel.dao.orm.Projection projection) {
		return getService().dynamicQueryCount(dynamicQuery, projection);
	}

	public static com.liferay.document.library.kernel.model.DLSyncEvent fetchDLSyncEvent(
		long syncEventId) {
		return getService().fetchDLSyncEvent(syncEventId);
	}

	public static com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery getActionableDynamicQuery() {
		return getService().getActionableDynamicQuery();
	}

	/**
	* Returns the d l sync event with the primary key.
	*
	* @param syncEventId the primary key of the d l sync event
	* @return the d l sync event
	* @throws PortalException if a d l sync event with the primary key could not be found
	*/
	public static com.liferay.document.library.kernel.model.DLSyncEvent getDLSyncEvent(
		long syncEventId)
		throws com.liferay.portal.kernel.exception.PortalException {
		return getService().getDLSyncEvent(syncEventId);
	}

	public static java.util.List<com.liferay.document.library.kernel.model.DLSyncEvent> getDLSyncEvents(
		long modifiedTime) {
		return getService().getDLSyncEvents(modifiedTime);
	}

	/**
	* Returns a range of all the d l sync events.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link com.liferay.portlet.documentlibrary.model.impl.DLSyncEventModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	* </p>
	*
	* @param start the lower bound of the range of d l sync events
	* @param end the upper bound of the range of d l sync events (not inclusive)
	* @return the range of d l sync events
	*/
	public static java.util.List<com.liferay.document.library.kernel.model.DLSyncEvent> getDLSyncEvents(
		int start, int end) {
		return getService().getDLSyncEvents(start, end);
	}

	/**
	* Returns the number of d l sync events.
	*
	* @return the number of d l sync events
	*/
	public static int getDLSyncEventsCount() {
		return getService().getDLSyncEventsCount();
	}

	public static com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery getIndexableActionableDynamicQuery() {
		return getService().getIndexableActionableDynamicQuery();
	}

	public static java.util.List<com.liferay.document.library.kernel.model.DLSyncEvent> getLatestDLSyncEvents() {
		return getService().getLatestDLSyncEvents();
	}

	/**
	* Returns the OSGi service identifier.
	*
	* @return the OSGi service identifier
	*/
	public static java.lang.String getOSGiServiceIdentifier() {
		return getService().getOSGiServiceIdentifier();
	}

	public static com.liferay.portal.kernel.model.PersistedModel getPersistedModel(
		java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {
		return getService().getPersistedModel(primaryKeyObj);
	}

	/**
	* Updates the d l sync event in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	*
	* @param dlSyncEvent the d l sync event
	* @return the d l sync event that was updated
	*/
	public static com.liferay.document.library.kernel.model.DLSyncEvent updateDLSyncEvent(
		com.liferay.document.library.kernel.model.DLSyncEvent dlSyncEvent) {
		return getService().updateDLSyncEvent(dlSyncEvent);
	}

	public static DLSyncEventLocalService getService() {
		if (_service == null) {
			_service = (DLSyncEventLocalService)PortalBeanLocatorUtil.locate(DLSyncEventLocalService.class.getName());

			ReferenceRegistry.registerReference(DLSyncEventLocalServiceUtil.class,
				"_service");
		}

		return _service;
	}

	private static DLSyncEventLocalService _service;
}