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

package com.liferay.portal.service;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.ReservedUserEmailAddressException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.test.AggregateTestRule;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.ReflectionUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.OrganizationConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.test.LiferayIntegrationTestRule;
import com.liferay.portal.test.MainServletTestRule;
import com.liferay.portal.test.Sync;
import com.liferay.portal.test.SynchronousMailTestRule;
import com.liferay.portal.util.PrefsPropsUtil;
import com.liferay.portal.util.PropsValues;
import com.liferay.portal.util.comparator.UserScreenNameComparator;
import com.liferay.portal.util.test.GroupTestUtil;
import com.liferay.portal.util.test.MailServiceTestUtil;
import com.liferay.portal.util.test.OrganizationTestUtil;
import com.liferay.portal.util.test.RandomTestUtil;
import com.liferay.portal.util.test.TestPropsValues;
import com.liferay.portal.util.test.UserGroupTestUtil;
import com.liferay.portal.util.test.UserTestUtil;

import java.lang.reflect.Field;

import java.util.LinkedHashMap;
import java.util.List;

import javax.portlet.PortletPreferences;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * @author Brian Wing Shun Chan
 * @author José Manuel Navarro
 */
@RunWith(Enclosed.class)
public class UserServiceTest {

	public static class WhenCompanySecurityStrangersWithMXDisabled {

		@ClassRule
		@Rule
		public static final AggregateTestRule aggregateTestRule =
			new AggregateTestRule(
				new LiferayIntegrationTestRule(), MainServletTestRule.INSTANCE);

		@Test(expected = ReservedUserEmailAddressException.class)
		public void shouldNotAddUser() throws Exception {
			boolean companySecurityStrangersWithMx =
				PropsValues.COMPANY_SECURITY_STRANGERS_WITH_MX;

			String name = PrincipalThreadLocal.getName();

			try {
				ReflectionTestUtil.setFieldValue(
					PropsValues.class, "COMPANY_SECURITY_STRANGERS_WITH_MX",
					false);

				PrincipalThreadLocal.setName(0);

				UserTestUtil.addUser(true);
			}
			finally {
				ReflectionTestUtil.setFieldValue(
					PropsValues.class, "COMPANY_SECURITY_STRANGERS_WITH_MX",
					companySecurityStrangersWithMx);

				PrincipalThreadLocal.setName(name);
			}
		}

		@Test(expected = ReservedUserEmailAddressException.class)
		public void shouldNotUpdateEmailAddress() throws Exception {
			boolean companySecurityStrangersWithMx =
				PropsValues.COMPANY_SECURITY_STRANGERS_WITH_MX;

			String name = PrincipalThreadLocal.getName();

			try {
				ReflectionTestUtil.setFieldValue(
					PropsValues.class, "COMPANY_SECURITY_STRANGERS_WITH_MX",
					false);

				User user = UserTestUtil.addUser(false);

				PrincipalThreadLocal.setName(user.getUserId());

				String emailAddress =
					"UserServiceTest." + RandomTestUtil.nextLong() +
						"@liferay.com";

				UserServiceUtil.updateEmailAddress(
					user.getUserId(), user.getPassword(), emailAddress,
					emailAddress, new ServiceContext());
			}
			finally {
				ReflectionTestUtil.setFieldValue(
					PropsValues.class, "COMPANY_SECURITY_STRANGERS_WITH_MX",
					companySecurityStrangersWithMx);

				PrincipalThreadLocal.setName(name);
			}
		}

		@Test(expected = ReservedUserEmailAddressException.class)
		public void shouldNotUpdateUser() throws Exception {
			boolean companySecurityStrangersWithMx =
				PropsValues.COMPANY_SECURITY_STRANGERS_WITH_MX;

			String name = PrincipalThreadLocal.getName();

			User user = UserTestUtil.addUser(false);

			try {
				ReflectionTestUtil.setFieldValue(
					PropsValues.class, "COMPANY_SECURITY_STRANGERS_WITH_MX",
					false);

				PrincipalThreadLocal.setName(user.getUserId());

				UserTestUtil.updateUser(user);
			}
			finally {
				ReflectionTestUtil.setFieldValue(
					PropsValues.class, "COMPANY_SECURITY_STRANGERS_WITH_MX",
					companySecurityStrangersWithMx);

				PrincipalThreadLocal.setName(name);

				UserLocalServiceUtil.deleteUser(user);
			}
		}

	}

	public static class WhenGettingUserByEmailAddress {

		@ClassRule
		@Rule
		public static final AggregateTestRule aggregateTestRule =
			new AggregateTestRule(
				new LiferayIntegrationTestRule(), MainServletTestRule.INSTANCE);

		@Test(expected = NoSuchUserException.class)
		public void shouldFailIfUserDeleted() throws Exception {
			User user = UserTestUtil.addUser(true);

			UserServiceUtil.deleteUser(user.getUserId());

			UserServiceUtil.getUserByEmailAddress(
				TestPropsValues.getCompanyId(), user.getEmailAddress());
		}

		@Test
		public void shouldReturnUserIfPresent() throws Exception {
			User user = UserTestUtil.addUser(true);

			try {
				User retrievedUser = UserServiceUtil.getUserByEmailAddress(
					TestPropsValues.getCompanyId(), user.getEmailAddress());

				Assert.assertEquals(user, retrievedUser);
			}
			finally {
				UserLocalServiceUtil.deleteUser(user);
			}
		}

	}

	public static class WhenGroupAdminUnsetsGroupUsers {

		@ClassRule
		@Rule
		public static final AggregateTestRule aggregateTestRule =
			new AggregateTestRule(
				new LiferayIntegrationTestRule(), MainServletTestRule.INSTANCE);

		@Before
		public void setUp() throws Exception {
			_organization = OrganizationTestUtil.addOrganization(true);

			_group = GroupTestUtil.addGroup();

			_groupAdminUser = UserTestUtil.addGroupAdminUser(_group);
		}

		@Test
		public void shouldUnsetGroupAdmin() throws Exception {
			User groupAdminUser = UserTestUtil.addGroupAdminUser(_group);

			try {
				_unsetGroupUsers(
					_group.getGroupId(), _groupAdminUser, groupAdminUser);

				Assert.assertTrue(
					UserLocalServiceUtil.hasGroupUser(
						_group.getGroupId(), groupAdminUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(groupAdminUser);
			}
		}

		@Test
		public void shouldUnsetGroupOwner() throws Exception {
			User groupOwnerUser = UserTestUtil.addGroupOwnerUser(_group);

			try {
				_unsetGroupUsers(
					_group.getGroupId(), _groupAdminUser, groupOwnerUser);

				Assert.assertTrue(
					UserLocalServiceUtil.hasGroupUser(
						_group.getGroupId(), groupOwnerUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(groupOwnerUser);
			}
		}

		@Test
		public void shouldUnsetOrganizationAdmin() throws Exception {
			User organizationAdminUser = UserTestUtil.addOrganizationAdminUser(
				_organization);

			try {
				_unsetOrganizationUsers(
					_organization.getOrganizationId(), _groupAdminUser,
					organizationAdminUser);

				Assert.assertTrue(
					UserLocalServiceUtil.hasOrganizationUser(
						_organization.getOrganizationId(),
						organizationAdminUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(organizationAdminUser);
			}
		}

		@Test
		public void shouldUnsetOrganizationOwner() throws Exception {
			User organizationOwnerUser = UserTestUtil.addOrganizationOwnerUser(
				_organization);

			try {
				_unsetOrganizationUsers(
					_organization.getOrganizationId(), _groupAdminUser,
					organizationOwnerUser);

				Assert.assertTrue(
					UserLocalServiceUtil.hasOrganizationUser(
						_organization.getOrganizationId(),
						organizationOwnerUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(organizationOwnerUser);
			}
		}

		@After
		public void tearDown() throws PortalException {
			UserLocalServiceUtil.deleteUser(_groupAdminUser);

			GroupLocalServiceUtil.deleteGroup(_group);

			OrganizationLocalServiceUtil.deleteOrganization(_organization);
		}

		private Group _group;
		private User _groupAdminUser;
		private Organization _organization;

	}

	public static class WhenGroupOwnerUnsetsGroupUsers {

		@ClassRule
		@Rule
		public static final AggregateTestRule aggregateTestRule =
			new AggregateTestRule(
				new LiferayIntegrationTestRule(), MainServletTestRule.INSTANCE);

		@Before
		public void setUp() throws Exception {
			_organization = OrganizationTestUtil.addOrganization(true);

			_group = GroupTestUtil.addGroup();

			_groupOwnerUser = UserTestUtil.addGroupOwnerUser(_group);

			_organizationGroupUser = UserTestUtil.addGroupOwnerUser(
				_organization.getGroup());
		}

		@Test
		public void shouldUnsetGroupAdmin() throws Exception {
			User groupAdminUser = UserTestUtil.addGroupAdminUser(_group);

			try {
				_unsetGroupUsers(
					_group.getGroupId(), _groupOwnerUser, groupAdminUser);

				Assert.assertFalse(
					UserLocalServiceUtil.hasGroupUser(
						_group.getGroupId(), groupAdminUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(groupAdminUser);
			}
		}

		@Test
		public void shouldUnsetGroupOwner() throws Exception {
			User groupOwnerUser = UserTestUtil.addGroupOwnerUser(_group);

			try {
				_unsetGroupUsers(
					_group.getGroupId(), _groupOwnerUser, groupOwnerUser);

				Assert.assertFalse(
					UserLocalServiceUtil.hasGroupUser(
						_group.getGroupId(), groupOwnerUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(groupOwnerUser);
			}
		}

		@Test
		public void shouldUnsetOrganizationAdmin() throws Exception {
			User organizationAdminUser = UserTestUtil.addOrganizationAdminUser(
				_organization);

			try {
				_unsetOrganizationUsers(
					_organization.getOrganizationId(), _organizationGroupUser,
					organizationAdminUser);

				Assert.assertTrue(
					UserLocalServiceUtil.hasOrganizationUser(
						_organization.getOrganizationId(),
						organizationAdminUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(organizationAdminUser);
			}
		}

		@Test
		public void shouldUnsetOrganizationOwner() throws Exception {
			User organizationOwnerUser = UserTestUtil.addOrganizationOwnerUser(
				_organization);

			try {
				_unsetOrganizationUsers(
					_organization.getOrganizationId(), _organizationGroupUser,
					organizationOwnerUser);

				Assert.assertTrue(
					UserLocalServiceUtil.hasOrganizationUser(
						_organization.getOrganizationId(),
						organizationOwnerUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(organizationOwnerUser);
			}
		}

		@After
		public void tearDown() throws PortalException {
			UserLocalServiceUtil.deleteUser(_organizationGroupUser);

			UserLocalServiceUtil.deleteUser(_groupOwnerUser);

			GroupLocalServiceUtil.deleteGroup(_group);

			OrganizationLocalServiceUtil.deleteOrganization(_organization);
		}

		private Group _group;
		private User _groupOwnerUser;
		private Organization _organization;
		private User _organizationGroupUser;

	}

	public static class WhenOrganizationAdminUnsetsUsersForNonSiteOrganization {

		@ClassRule
		@Rule
		public static final AggregateTestRule aggregateTestRule =
			new AggregateTestRule(
				new LiferayIntegrationTestRule(), MainServletTestRule.INSTANCE);

		@Before
		public void setUp() throws Exception {
			_organization = OrganizationTestUtil.addOrganization();

			_organizationAdminUser = UserTestUtil.addOrganizationAdminUser(
				_organization);

			_organizationOwnerUser = UserTestUtil.addOrganizationOwnerUser(
				_organization);
		}

		@Test
		public void shouldUnsetOrganizationAdmin() throws Exception {
			User otherOrganizationAdminUser =
				UserTestUtil.addOrganizationAdminUser(_organization);

			try {
				_unsetOrganizationUsers(
					_organization.getOrganizationId(), _organizationAdminUser,
					otherOrganizationAdminUser);

				Assert.assertTrue(
					UserLocalServiceUtil.hasOrganizationUser(
						_organization.getOrganizationId(),
						otherOrganizationAdminUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(otherOrganizationAdminUser);
			}
		}

		@Test
		public void shouldUnsetOrganizationOwner() throws Exception {
			_unsetOrganizationUsers(
				_organization.getOrganizationId(), _organizationAdminUser,
				_organizationOwnerUser);

			Assert.assertTrue(
				UserLocalServiceUtil.hasOrganizationUser(
					_organization.getOrganizationId(),
					_organizationOwnerUser.getUserId()));
		}

		@After
		public void tearDown() throws PortalException {
			UserLocalServiceUtil.deleteUser(_organizationAdminUser);
			UserLocalServiceUtil.deleteUser(_organizationOwnerUser);

			OrganizationLocalServiceUtil.deleteOrganization(_organization);
		}

		private Organization _organization;
		private User _organizationAdminUser;
		private User _organizationOwnerUser;

	}

	public static class WhenOrganizationAdminUnsetsUsersForSiteOrganization {

		@ClassRule
		@Rule
		public static final AggregateTestRule aggregateTestRule =
			new AggregateTestRule(
				new LiferayIntegrationTestRule(), MainServletTestRule.INSTANCE);

		@Before
		public void setUp() throws Exception {
			_organization = OrganizationTestUtil.addOrganization(true);

			_group = _organization.getGroup();

			_organizationAdminUser = UserTestUtil.addOrganizationAdminUser(
				_organization);
		}

		@Test
		public void shouldUnsetSiteAdmin() throws Exception {
			User groupAdminUser = UserTestUtil.addGroupAdminUser(_group);

			try {
				_unsetGroupUsers(
					_group.getGroupId(), _organizationAdminUser,
					groupAdminUser);

				Assert.assertTrue(
					UserLocalServiceUtil.hasGroupUser(
						_group.getGroupId(), groupAdminUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(groupAdminUser);
			}
		}

		@Test
		public void shouldUnsetSiteOwner() throws Exception {
			User groupOwnerUser = UserTestUtil.addGroupOwnerUser(_group);

			try {
				_unsetGroupUsers(
					_group.getGroupId(), _organizationAdminUser,
					groupOwnerUser);

				Assert.assertTrue(
					UserLocalServiceUtil.hasGroupUser(
						_group.getGroupId(), groupOwnerUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(groupOwnerUser);
			}
		}

		@After
		public void tearDown() throws PortalException {
			UserLocalServiceUtil.deleteUser(_organizationAdminUser);

			OrganizationLocalServiceUtil.deleteOrganization(_organization);
		}

		private Group _group;
		private Organization _organization;
		private User _organizationAdminUser;

	}

	public static class WhenOrganizationOwnerUnsetsUsersForNonSiteOrganization {

		@ClassRule
		@Rule
		public static final AggregateTestRule aggregateTestRule =
			new AggregateTestRule(
				new LiferayIntegrationTestRule(), MainServletTestRule.INSTANCE);

		@Before
		public void setUp() throws Exception {
			_organization = OrganizationTestUtil.addOrganization();

			_organizationOwnerUser = UserTestUtil.addOrganizationOwnerUser(
				_organization);
		}

		@Test
		public void shouldUnsetOrganizationAdmin() throws Exception {
			User organizationAdminUser = UserTestUtil.addOrganizationAdminUser(
				_organization);

			try {
				_unsetOrganizationUsers(
					_organization.getOrganizationId(), _organizationOwnerUser,
					organizationAdminUser);

				Assert.assertFalse(
					UserLocalServiceUtil.hasOrganizationUser(
						_organization.getOrganizationId(),
						organizationAdminUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(organizationAdminUser);
			}
		}

		@Test
		public void shouldUnsetOrganizationOwner() throws Exception {
			User otherOrganizationOwnerUser =
				UserTestUtil.addOrganizationOwnerUser(_organization);

			try {
				_unsetOrganizationUsers(
					_organization.getOrganizationId(), _organizationOwnerUser,
					otherOrganizationOwnerUser);

				Assert.assertFalse(
					UserLocalServiceUtil.hasOrganizationUser(
						_organization.getOrganizationId(),
						otherOrganizationOwnerUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(otherOrganizationOwnerUser);
			}
		}

		@After
		public void tearDown() throws PortalException {
			UserLocalServiceUtil.deleteUser(_organizationOwnerUser);

			OrganizationLocalServiceUtil.deleteOrganization(_organization);
		}

		private Organization _organization;
		private User _organizationOwnerUser;

	}

	public static class WhenOrganizationOwnerUnsetsUsersForSiteOrganization {

		@ClassRule
		@Rule
		public static final AggregateTestRule aggregateTestRule =
			new AggregateTestRule(
				new LiferayIntegrationTestRule(), MainServletTestRule.INSTANCE);

		@Before
		public void setUp() throws Exception {
			_organization = OrganizationTestUtil.addOrganization(true);

			_group = _organization.getGroup();

			_organizationOwnerUser = UserTestUtil.addOrganizationOwnerUser(
				_organization);
		}

		@Test
		public void shouldUnsetSiteAdmin() throws Exception {
			User groupAdminUser = UserTestUtil.addGroupAdminUser(_group);

			try {
				_unsetGroupUsers(
					_group.getGroupId(), _organizationOwnerUser,
					groupAdminUser);

				Assert.assertFalse(
					UserLocalServiceUtil.hasGroupUser(
						_group.getGroupId(), groupAdminUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(groupAdminUser);
			}
		}

		@Test
		public void shouldUnsetSiteOwner() throws Exception {
			User groupOwnerUser = UserTestUtil.addGroupOwnerUser(_group);

			try {
				_unsetGroupUsers(
					_group.getGroupId(), _organizationOwnerUser,
					groupOwnerUser);

				Assert.assertFalse(
					UserLocalServiceUtil.hasGroupUser(
						_group.getGroupId(), groupOwnerUser.getUserId()));
			}
			finally {
				UserLocalServiceUtil.deleteUser(groupOwnerUser);
			}
		}

		@After
		public void tearDown() throws PortalException {
			UserLocalServiceUtil.deleteUser(_organizationOwnerUser);

			OrganizationLocalServiceUtil.deleteOrganization(_organization);
		}

		private Group _group;
		private Organization _organization;
		private User _organizationOwnerUser;

	}

	@Sync
	public static class WhenPortalSendsPasswordEmail {

		@ClassRule
		@Rule
		public static final AggregateTestRule aggregateTestRule =
			new AggregateTestRule(
				new LiferayIntegrationTestRule(), MainServletTestRule.INSTANCE,
				SynchronousMailTestRule.INSTANCE);

		@Before
		public void setUp() throws Exception {
			_user = UserTestUtil.addUser();
		}

		@Test
		public void shouldSendNewPasswordEmailByEmailAddress()
			throws Exception {

			PortletPreferences portletPreferences =
				givenThatCompanySendsNewPassword();

			try {
				int initialInboxSize = MailServiceTestUtil.getInboxSize();

				boolean sentPassword =
					UserServiceUtil.sendPasswordByEmailAddress(
						_user.getCompanyId(), _user.getEmailAddress());

				Assert.assertTrue(sentPassword);
				Assert.assertEquals(
					initialInboxSize + 1, MailServiceTestUtil.getInboxSize());
				Assert.assertTrue(
					MailServiceTestUtil.lastMailMessageContains(
						"email_password_sent_body.tmpl"));
			}
			finally {
				restorePortletPreferences(portletPreferences);
			}
		}

		@Test
		public void shouldSendNewPasswordEmailByScreenName() throws Exception {
			PortletPreferences portletPreferences =
				givenThatCompanySendsNewPassword();

			try {
				int initialInboxSize = MailServiceTestUtil.getInboxSize();

				boolean sentPassword = UserServiceUtil.sendPasswordByScreenName(
					_user.getCompanyId(), _user.getScreenName());

				Assert.assertTrue(sentPassword);
				Assert.assertEquals(
					initialInboxSize + 1, MailServiceTestUtil.getInboxSize());
				Assert.assertTrue(
					MailServiceTestUtil.lastMailMessageContains(
						"email_password_sent_body.tmpl"));
			}
			finally {
				restorePortletPreferences(portletPreferences);
			}
		}

		@Test
		public void shouldSendNewPasswordEmailByUserId() throws Exception {
			PortletPreferences portletPreferences =
				givenThatCompanySendsNewPassword();

			try {
				int initialInboxSize = MailServiceTestUtil.getInboxSize();

				boolean sentPassword = UserServiceUtil.sendPasswordByUserId(
					_user.getUserId());

				Assert.assertTrue(sentPassword);
				Assert.assertEquals(
					initialInboxSize + 1, MailServiceTestUtil.getInboxSize());
				Assert.assertTrue(
					MailServiceTestUtil.lastMailMessageContains(
						"email_password_sent_body.tmpl"));
			}
			finally {
				restorePortletPreferences(portletPreferences);
			}
		}

		@Test
		public void shouldSendResetLinkEmailByEmailAddress() throws Exception {
			PortletPreferences portletPreferences =
				givenThatCompanySendsResetPasswordLink();

			try {
				int initialInboxSize = MailServiceTestUtil.getInboxSize();

				boolean sentPassword =
					UserServiceUtil.sendPasswordByEmailAddress(
						_user.getCompanyId(), _user.getEmailAddress());

				Assert.assertFalse(sentPassword);
				Assert.assertEquals(
					initialInboxSize + 1, MailServiceTestUtil.getInboxSize());
				Assert.assertTrue(
					MailServiceTestUtil.lastMailMessageContains(
						"email_password_reset_body.tmpl"));
			}
			finally {
				restorePortletPreferences(portletPreferences);
			}
		}

		@Test
		public void shouldSendResetLinkEmailByScreenName() throws Exception {
			PortletPreferences portletPreferences =
				givenThatCompanySendsResetPasswordLink();

			try {
				int initialInboxSize = MailServiceTestUtil.getInboxSize();

				boolean sentPassword = UserServiceUtil.sendPasswordByScreenName(
					_user.getCompanyId(), _user.getScreenName());

				Assert.assertFalse(sentPassword);
				Assert.assertEquals(
					initialInboxSize + 1, MailServiceTestUtil.getInboxSize());
				Assert.assertTrue(
					MailServiceTestUtil.lastMailMessageContains(
						"email_password_reset_body.tmpl"));
			}
			finally {
				restorePortletPreferences(portletPreferences);
			}
		}

		@Test
		public void shouldSendResetLinkEmailByUserId() throws Exception {
			PortletPreferences portletPreferences =
				givenThatCompanySendsResetPasswordLink();

			try {
				int initialInboxSize = MailServiceTestUtil.getInboxSize();

				boolean sentPassword = UserServiceUtil.sendPasswordByUserId(
					_user.getUserId());

				Assert.assertFalse(sentPassword);
				Assert.assertEquals(
					initialInboxSize + 1, MailServiceTestUtil.getInboxSize());
				Assert.assertTrue(
					MailServiceTestUtil.lastMailMessageContains(
						"email_password_reset_body.tmpl"));
			}
			finally {
				restorePortletPreferences(portletPreferences);
			}
		}

		@After
		public void tearDown() throws PortalException {
			UserLocalServiceUtil.deleteUser(_user);
		}

		protected PortletPreferences givenThatCompanySendsNewPassword()
			throws Exception {

			PortletPreferences portletPreferences =
				PrefsPropsUtil.getPreferences(_user.getCompanyId(), false);

			portletPreferences.setValue(
				PropsKeys.COMPANY_SECURITY_SEND_PASSWORD,
				Boolean.TRUE.toString());

			portletPreferences.setValue(
				PropsKeys.COMPANY_SECURITY_SEND_PASSWORD_RESET_LINK,
				Boolean.FALSE.toString());

			portletPreferences.store();

			return portletPreferences;
		}

		protected PortletPreferences givenThatCompanySendsResetPasswordLink()
			throws Exception {

			PortletPreferences portletPreferences =
				PrefsPropsUtil.getPreferences(_user.getCompanyId(), false);

			portletPreferences.setValue(
				PropsKeys.COMPANY_SECURITY_SEND_PASSWORD,
				Boolean.FALSE.toString());
			portletPreferences.setValue(
				PropsKeys.COMPANY_SECURITY_SEND_PASSWORD_RESET_LINK,
				Boolean.TRUE.toString());

			portletPreferences.store();

			return portletPreferences;
		}

		protected void restorePortletPreferences(
				PortletPreferences portletPreferences)
			throws Exception {

			portletPreferences.reset(PropsKeys.COMPANY_SECURITY_SEND_PASSWORD);
			portletPreferences.reset(
				PropsKeys.COMPANY_SECURITY_SEND_PASSWORD_RESET_LINK);

			portletPreferences.store();
		}

		private User _user;

	}

	public static class WhenSearchingUsers {

		@ClassRule
		public static final AggregateTestRule aggregateTestRule =
			new AggregateTestRule(
				new LiferayIntegrationTestRule(), MainServletTestRule.INSTANCE);

		@BeforeClass
		public static void setUp() throws Exception {
			_group = GroupTestUtil.addGroup();

			for (int i = 0; i < (_PARENT_USERS_COUNT - 1); i++) {
				_user = UserTestUtil.addUser("parent" + i, _group.getGroupId());
			}

			User user = UserTestUtil.addUser("child1", false, null);

			GroupTestUtil.addGroup(
				TestPropsValues.getCompanyId(), user.getUserId(),
				_group.getGroupId());

			UserGroup userGroup = UserGroupTestUtil.addUserGroup(
				_group.getGroupId());

			user = UserTestUtil.addUser("UserGroup", false, null);

			UserGroupLocalServiceUtil.addUserUserGroup(
				user.getUserId(), userGroup);

			user = UserTestUtil.addUser("Organization", false, null);

			Organization organization =
				OrganizationLocalServiceUtil.addOrganization(
					user.getUserId(),
					OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID,
					RandomTestUtil.randomString(), false);

			GroupLocalServiceUtil.addOrganizationGroup(
				organization.getOrganizationId(), _group);

			OrganizationLocalServiceUtil.addUserOrganization(
				user.getUserId(), organization);

			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			_totalUsersCount = UserLocalServiceUtil.searchCount(
				TestPropsValues.getCompanyId(), null, null, null, null, null,
				WorkflowConstants.STATUS_APPROVED, params, true);
		}

		@Test
		public void shouldCountAllUsersByGroupAdvancedUsingIndex()
			throws Exception {

			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			params.put("usersGroups", _group.getGroupId());

			int usersCount = UserLocalServiceUtil.searchCount(
				TestPropsValues.getCompanyId(), null, null, null, null, null,
				WorkflowConstants.STATUS_APPROVED, params, true);

			Assert.assertEquals(_PARENT_USERS_COUNT, usersCount);
		}

		@Test
		public void shouldCountAllUsersByGroupKeywordUsingIndex()
			throws Exception {

			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			params.put("usersGroups", _group.getGroupId());

			int usersCount = UserLocalServiceUtil.searchCount(
				TestPropsValues.getCompanyId(), null,
				WorkflowConstants.STATUS_APPROVED, params);

			Assert.assertEquals(_PARENT_USERS_COUNT, usersCount);
		}

		@Test
		public void shouldCountAllUsersByGroupWithRelationsAdvancedUsingDB()
			throws Exception {

			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			params.put("usersGroups", _group.getGroupId());
			params.put("inherit", Boolean.TRUE);

			int usersCount = UserLocalServiceUtil.searchCount(
				TestPropsValues.getCompanyId(), null, null, null, null, null,
				WorkflowConstants.STATUS_APPROVED, params, true);

			Assert.assertEquals(_PARENT_USERS_COUNT + 1, usersCount);
		}

		@Test
		public void shouldCountAllUsersByGroupWithRelationsKeywordUsingDB()
			throws Exception {

			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			params.put("usersGroups", _group.getGroupId());
			params.put("inherit", Boolean.TRUE);

			int usersCount = UserLocalServiceUtil.searchCount(
				TestPropsValues.getCompanyId(), null,
				WorkflowConstants.STATUS_APPROVED, params);

			Assert.assertEquals(_PARENT_USERS_COUNT + 1, usersCount);
		}

		@Test
		public void shouldCountUsersUsingIndex() throws Exception {
			int count = UserLocalServiceUtil.searchCount(
				TestPropsValues.getCompanyId(), null, null, null, null, null,
				WorkflowConstants.STATUS_APPROVED, null, true);

			Assert.assertEquals(_totalUsersCount, count);
		}

		@Test
		public void shouldFindAllUsersAdvancedUsingDB() throws Exception {
			List<User> users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), null, null, null, null, null,
				WorkflowConstants.STATUS_APPROVED, null, true,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS,
				_NULL_ORDER_BY_COMPARATOR);

			Assert.assertNotNull(users);
			Assert.assertEquals(_totalUsersCount, users.size());
		}

		@Test
		public void shouldFindAllUsersByGroupAdvancedUsingDB()
			throws Exception {

			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			params.put("usersGroups", _group.getGroupId());

			List<User> users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), null, null, null, null, null,
				WorkflowConstants.STATUS_APPROVED, params, true,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS,
				_NULL_ORDER_BY_COMPARATOR);

			Assert.assertNotNull(users);
			Assert.assertEquals(_PARENT_USERS_COUNT, users.size());
		}

		@Test
		public void shouldFindAllUsersByGroupKeywordUsingDB() throws Exception {
			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			params.put("usersGroups", _group.getGroupId());

			List<User> users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), null,
				WorkflowConstants.STATUS_APPROVED, params, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, _NULL_ORDER_BY_COMPARATOR);

			Assert.assertNotNull(users);
			Assert.assertEquals(_PARENT_USERS_COUNT, users.size());
		}

		@Test
		public void
				shouldFindAllUsersByGroupWithRelationsAdvancedOrderedUsingDB()
			throws Exception {

			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			params.put("usersGroups", _group.getGroupId());
			params.put("inherit", Boolean.TRUE);

			OrderByComparator<User> orderByComparator =
				new UserScreenNameComparator(false);

			List<User> users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), null, null, null, null, null,
				WorkflowConstants.STATUS_APPROVED, params, true,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS, orderByComparator);

			Assert.assertNotNull(users);
			Assert.assertEquals(_PARENT_USERS_COUNT + 1, users.size());
			Assert.assertEquals(
				_user.getScreenName(), users.get(1).getScreenName());
		}

		@Test
		public void shouldFindAllUsersByGroupWithRelationsAdvancedUsingDB()
			throws Exception {

			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			params.put("usersGroups", _group.getGroupId());
			params.put("inherit", Boolean.TRUE);

			List<User> users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), null, null, null, null, null,
				WorkflowConstants.STATUS_APPROVED, params, true,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS,
				_NULL_ORDER_BY_COMPARATOR);

			Assert.assertNotNull(users);
			Assert.assertEquals(_PARENT_USERS_COUNT + 1, users.size());
		}

		@Test
		public void
				shouldFindAllUsersByGroupWithRelationsKeywordOrderedUsingDB()
			throws Exception {

			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			params.put("usersGroups", _group.getGroupId());
			params.put("inherit", Boolean.TRUE);

			OrderByComparator<User> orderByComparator =
				new UserScreenNameComparator(false);

			List<User> users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), null,
				WorkflowConstants.STATUS_APPROVED, params, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, orderByComparator);

			Assert.assertNotNull(users);
			Assert.assertEquals(_PARENT_USERS_COUNT + 1, users.size());
			Assert.assertEquals(
				_user.getScreenName(), users.get(1).getScreenName());
		}

		@Test
		public void shouldFindAllUsersByGroupWithRelationsKeywordUsingDB()
			throws Exception {

			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			params.put("usersGroups", _group.getGroupId());
			params.put("inherit", Boolean.TRUE);

			List<User> users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), null,
				WorkflowConstants.STATUS_APPROVED, params, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, _NULL_ORDER_BY_COMPARATOR);

			Assert.assertNotNull(users);
			Assert.assertEquals(_PARENT_USERS_COUNT + 1, users.size());
		}

		@Test
		public void shouldFindAllUsersKeywordUsingDB() throws Exception {
			List<User> users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), null,
				WorkflowConstants.STATUS_APPROVED, null, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, _NULL_ORDER_BY_COMPARATOR);

			Assert.assertNotNull(users);
			Assert.assertEquals(_totalUsersCount, users.size());
		}

		@Test
		public void shouldFindAllUsersUsingIndex() throws Exception {
			Hits users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), null, null, null, null, null,
				WorkflowConstants.STATUS_APPROVED, null, true,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS, _NULL_SORT);

			Assert.assertNotNull(users);
			Assert.assertEquals(_totalUsersCount, users.getLength());
		}

		@Test
		public void shouldFindUserByKeywordFromGroupUsingDB() throws Exception {
			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			params.put("usersGroups", _group.getGroupId());

			List<User> users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), _user.getEmailAddress(),
				WorkflowConstants.STATUS_APPROVED, params, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, _NULL_ORDER_BY_COMPARATOR);

			Assert.assertNotNull(users);
			Assert.assertEquals(1, users.size());
			Assert.assertEquals(
				_user.getScreenName(), users.get(0).getScreenName());
		}

		@Test
		public void shouldFindUserByParamsFromGroupUsingDB() throws Exception {
			LinkedHashMap<String, Object> params = new LinkedHashMap<>();

			params.put("usersGroups", _group.getGroupId());

			List<User> users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), null, null, null,
				_user.getScreenName(), _user.getEmailAddress(),
				WorkflowConstants.STATUS_APPROVED, params, true,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS,
				_NULL_ORDER_BY_COMPARATOR);

			Assert.assertNotNull(users);
			Assert.assertEquals(1, users.size());
			Assert.assertEquals(
				_user.getScreenName(), users.get(0).getScreenName());
		}

		@Test
		public void shouldFindUserUsingDB() throws Exception {
			List<User> users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), null, null, null,
				_user.getScreenName(), null, WorkflowConstants.STATUS_APPROVED,
				null, true, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
				_NULL_ORDER_BY_COMPARATOR);

			Assert.assertNotNull(users);
			Assert.assertFalse(users.isEmpty());
			Assert.assertEquals(_user.getUserId(), users.get(0).getUserId());
			Assert.assertEquals(
				_user.getScreenName(), users.get(0).getScreenName());
		}

		@Test
		public void shouldFindUserUsingIndex() throws Exception {
			Hits users = UserLocalServiceUtil.search(
				TestPropsValues.getCompanyId(), null, null, null,
				_user.getScreenName(), null, WorkflowConstants.STATUS_APPROVED,
				null, true, QueryUtil.ALL_POS, QueryUtil.ALL_POS, _NULL_SORT);

			Assert.assertNotNull(users);
			Assert.assertNotEquals(users.getLength(), 0);

			Document doc = users.doc(0);

			long userId = GetterUtil.getLong(
				doc.get(com.liferay.portal.kernel.search.Field.USER_ID));

			Assert.assertEquals(_user.getUserId(), userId);
		}

		private static final int _PARENT_USERS_COUNT = 7;

		private static final OrderByComparator<User> _NULL_ORDER_BY_COMPARATOR =
			null;

		private static final Sort _NULL_SORT = null;

		private static Group _group;
		private static int _totalUsersCount;
		private static User _user;

	}

	private static void _unsetGroupUsers(
			long groupId, User subjectUser, User objectUser)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(subjectUser);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		ServiceContext serviceContext = new ServiceContext();

		UserServiceUtil.unsetGroupUsers(
			groupId, new long[] {objectUser.getUserId()}, serviceContext);
	}

	private static void _unsetOrganizationUsers(
			long organizationId, User subjectUser, User objectUser)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(subjectUser);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		UserServiceUtil.unsetOrganizationUsers(
			organizationId, new long[] {objectUser.getUserId()});
	}

}