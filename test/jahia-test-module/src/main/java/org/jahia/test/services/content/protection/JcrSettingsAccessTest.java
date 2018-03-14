/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.test.services.content.protection;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.jahia.services.usermanager.JahiaGroupManagerService.PRIVILEGED_GROUPNAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.JahiaTestCase;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for protecting access to JCR settings node.
 *
 * @author Sergiy Shyrkov
 */
public class JcrSettingsAccessTest extends JahiaTestCase {

    private static final String[] JCR_PATHS_TO_TEST = new String[] {
            "/settings/mail-server",
            "/settings/mail-server/j:activated",
            "/settings/search-settings",
            "/settings/search-settings/j:provider",
            "/settings/forgesSettings",
            "/passwordPolicy",
            "/passwordPolicy/j:policy"
    };

    private static final String PRIVILEGED_USER_NAME = "jcr-settings-test-privileged-user";

    private static final String SERVER_ADMIN_USER_NAME = "jcr-mount-point-test-serveradmin-user";

    private static final String USER_PASSWORD = "password";

    private static JahiaGroupManagerService groupManager;

    private static String mountPointPath;

    private static JahiaUserManagerService userManager;

    private static void checkExistence(JCRSessionWrapper session, boolean expectExists, String... paths)
            throws RepositoryException {
        String user = session.getUserID();
        String workspace = session.getWorkspace().getName();
        for (String path : paths) {
            if (expectExists) {
                assertTrue(path + " should be accessible with " + user + " session in " + workspace,
                        session.itemExists(path));
                assertNotNull(path + " should be accessible with " + user + " session in " + workspace,
                        session.getItem(path));
            } else {
                assertFalse(path + " should not be accessible with " + user + " session in " + workspace,
                        session.itemExists(path));
                try {
                    session.getItem(path);
                    fail(path + " should not be accessible with " + user + " session in " + workspace);
                } catch (PathNotFoundException e) {
                    // Expected.
                }
            }
        }
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        groupManager = JahiaGroupManagerService.getInstance();
        userManager = JahiaUserManagerService.getInstance();

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                // create privileged user
                JCRUserNode privilegedUser = userManager.createUser(PRIVILEGED_USER_NAME, USER_PASSWORD, new Properties(), session);
                session.save();

                // add her into privileged group
                groupManager.lookupGroup(null, PRIVILEGED_GROUPNAME, session).addMember(privilegedUser);
                session.save();
                
                // this call initializes the password policy node, if it does not yet present in JCR
                JahiaPasswordPolicyService.getInstance().getDefaultPolicy();

                // create server admin user
                userManager.createUser(SERVER_ADMIN_USER_NAME, USER_PASSWORD, new Properties(), session);
                session.save();

                // grant her the server-administrator role
                session.getRootNode().grantRoles("u:" + SERVER_ADMIN_USER_NAME, Collections.singleton("server-administrator"));
                session.save();

                // create VFS mount point
                JCRMountPointNode jcrMountPointNode = (JCRMountPointNode) session.getNode("/mounts").addNode(
                        "jcr-mount-point-test-" + System.currentTimeMillis() + JCRMountPointNode.MOUNT_SUFFIX,
                        "jnt:vfsMountPoint");
                jcrMountPointNode.setProperty("j:rootPath", FileUtils.getTempDirectoryPath());
                jcrMountPointNode.setMountStatus(JCRMountPointNode.MountStatus.mounted);
                session.save();
                JCRStoreProvider mountProvider = jcrMountPointNode.getMountProvider();
                assertTrue("Unable to create VFS mount point", mountProvider != null && mountProvider.isAvailable());
                mountPointPath = jcrMountPointNode.getPath();

                return null;
            }
        });
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRUserNode privilegedUser = userManager.lookupUser(PRIVILEGED_USER_NAME, session);
                if (privilegedUser != null) {
                    groupManager.lookupGroup(null, PRIVILEGED_GROUPNAME, session).removeMember(privilegedUser);
                    session.save();

                    userManager.deleteUser(privilegedUser.getPath(), session);
                    session.save();
                }
                JCRUserNode serverAdminUser = userManager.lookupUser(SERVER_ADMIN_USER_NAME, session);
                if (serverAdminUser != null) {
                    session.getRootNode().revokeRolesForPrincipal("u:" + SERVER_ADMIN_USER_NAME);
                    userManager.deleteUser(serverAdminUser.getPath(), session);
                    session.save();
                }
                
                if (mountPointPath != null && session.nodeExists(mountPointPath)) {
                    session.getNode(mountPointPath).remove();
                    session.save();
                }
                return null;
            }
        });

        groupManager = null;
        userManager = null;
    }

    private void checkNoAccessViaRest() throws IOException {
        checkNoAccessViaRest("/modules/api/jcr/v1/live/en/paths/settings/mail-server");
        checkNoAccessViaRest("/modules/api/jcr/v1/default/en/paths/settings/mail-server");
        checkNoAccessViaRest("/modules/api/jcr/v1/default/en/paths/settings/search-settings");
        checkNoAccessViaRest("/modules/api/jcr/v1/default/en/paths/settings/forgesSettings");

        checkNoAccessViaRest("/modules/api/jcr/v1/default/en/paths/passwordPolicy");

        checkNoAccessViaRest("/modules/api/jcr/v1/default/en/paths" + mountPointPath);
    }

    private void checkNoAccessViaRest(String url) throws IOException {
        String out = getAsText(url, SC_NOT_FOUND);
        assertTrue(StringUtils.contains(out, "\"exception\":\"javax.jcr.PathNotFoundException\""));
    }

    @Test
    public void shouldHaveAccessToMountPointNodeWithServerAdminUserViaJcr() throws RepositoryException {
        JCRTemplate.getInstance().doExecute(SERVER_ADMIN_USER_NAME, null, Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                new JCRCallback<Boolean>() {
                    @Override
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        // mount point
                        checkExistence(session, true, mountPointPath, mountPointPath + "/j:rootPath");
                        return null;
                    }
                });
    }

    @Test
    public void shouldHaveAccessToMountPointNodeWithServerAdminUserViaRest() throws RepositoryException, IOException {
        login(SERVER_ADMIN_USER_NAME, USER_PASSWORD);
        try {
            String out = getAsText("/modules/api/jcr/v1/default/en/paths" + mountPointPath);
            assertTrue(StringUtils.contains(out, "\"type\":\"jnt:vfsMountPoint\""));
            assertTrue(StringUtils.contains(out, "\"name\":\"j:rootPath\""));
            assertTrue(StringUtils.contains(out, "\"value\":" + JSONObject.quote(FileUtils.getTempDirectoryPath())));
        } finally {
            logout();
        }
    }

    @Test
    public void shouldHaveAccessToSettingsWithSystemUserViaJcr() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                checkExistence(session, true, JCR_PATHS_TO_TEST);

                // mount point
                checkExistence(session, true, mountPointPath, mountPointPath + "/j:rootPath");
                return null;
            }
        });
    }

    @Test
    public void shouldHaveAccessToSettingsWithSystemUserViaRest() throws RepositoryException, IOException {
        loginRoot();
        try {
            String out = getAsText("/modules/api/jcr/v1/default/en/paths/settings/mail-server");
            assertTrue(StringUtils.contains(out, "\"type\":\"jnt:mailServerSettings\""));
            assertTrue(StringUtils.contains(out, "\"path\":\"/settings/mail-server/j:activated\""));

            out = getAsText("/modules/api/jcr/v1/default/en/paths/settings/search-settings");
            assertTrue(StringUtils.contains(out, "\"type\":\"jnt:searchServerSettings\""));
            assertTrue(StringUtils.contains(out, "\"path\":\"/settings/search-settings/j:provider\""));

            out = getAsText("/modules/api/jcr/v1/default/en/paths/settings/forgesSettings");
            assertTrue(StringUtils.contains(out, "\"type\":\"jnt:forgesServerSettings\""));
            
            out = getAsText("/modules/api/jcr/v1/default/en/paths/passwordPolicy");
            assertTrue(StringUtils.contains(out, "\"type\":\"jnt:passwordPolicy\""));

            out = getAsText("/modules/api/jcr/v1/default/en/paths" + mountPointPath);
            assertTrue(StringUtils.contains(out, "\"type\":\"jnt:vfsMountPoint\""));
            assertTrue(StringUtils.contains(out, "\"name\":\"j:rootPath\""));
            assertTrue(StringUtils.contains(out, "\"value\":" + JSONObject.quote(FileUtils.getTempDirectoryPath())));
        } finally {
            logout();
        }
    }

    @Test
    public void shouldNotHaveAccessToSettingsWithGuestUserViaJcr() throws RepositoryException {
        JCRCallback<Boolean> callback = new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                checkExistence(session, false, JCR_PATHS_TO_TEST);

                // mount point
                checkExistence(session, false, mountPointPath + "/j:rootPath");
                return null;
            }

        };
        JCRTemplate.getInstance().doExecute(JahiaUserManagerService.GUEST_USERNAME, null, Constants.EDIT_WORKSPACE,
                Locale.ENGLISH, callback);
        JCRTemplate.getInstance().doExecute(JahiaUserManagerService.GUEST_USERNAME, null, Constants.LIVE_WORKSPACE,
                Locale.ENGLISH, callback);
    }

    @Test
    public void shouldNotHaveAccessToSettingsWithGuestUserViaRest() throws RepositoryException, IOException {
        checkNoAccessViaRest();
    }

    @Test
    public void shouldNotHaveAccessToSettingsWithPrivilegedUserViaJcr() throws RepositoryException {
        JCRTemplate.getInstance().doExecute(PRIVILEGED_USER_NAME, null, Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                new JCRCallback<Boolean>() {
                    @Override
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        checkExistence(session, false, JCR_PATHS_TO_TEST);

                        // mount point
                        checkExistence(session, false, mountPointPath + "/j:rootPath");
                        return null;
                    }
                });
    }

    @Test
    public void shouldNotHaveAccessToSettingsWithPrivilegedUserViaRest() throws RepositoryException, IOException {
        login(PRIVILEGED_GROUPNAME, USER_PASSWORD);
        try {
            checkNoAccessViaRest();
        } finally {
            logout();
        }
    }
}
