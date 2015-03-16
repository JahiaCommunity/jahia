/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.content;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.*;
import javax.jcr.query.Query;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing roles and permissions for JCR nodes.
 * 
 * @author Sergiy Shyrkov
 * @deprecated
 */
public final class RBACUtils {

    private static final Logger logger = LoggerFactory.getLogger(RBACUtils.class);

    /**
     * Creates the specified permission if it does not exist yet. Also creates all intermediate permission nodes if not present yet. The
     * {@link Session#save()} is not called by this method; it is the responsibility of the caller.
     * 
     * @param path
     *            the path of the permission to get/create
     * @param session
     *            current JCR session
     * @return the permission node
     * @throws RepositoryException
     *             in case of an error
     */
    public static JCRNodeWrapper getOrCreatePermission(String path, JCRSessionWrapper session)
            throws RepositoryException {
        if (path == null || !path.startsWith("/permissions/")) {
            throw new IllegalArgumentException("Illegal value for the permission path: " + path);
        }
        String basePath = StringUtils.substringBeforeLast(path, "/");
        String name = StringUtils.substringAfterLast(path, "/");

        JCRNodeWrapper permission = null;

        JCRNodeWrapper base = null;
        try {
            base = session.getNode(basePath);
        } catch (PathNotFoundException e) {
            base = getOrCreatePermission(basePath, session);
        }
        if (!base.hasNode(name)) {
            session.checkout(base);
            permission = base.addNode(name, "jnt:permission");
            logger.info("Added permission node {}", permission.getPath());
        } else {
            permission = base.getNode(name);
        }

        return permission;
    }

    /**
     * Creates the specified role if it does not exist yet. The {@link Session#save()} is not called by this method; it is the
     * responsibility of the caller.
     * 
     * @param path
     *            the path of the role to get/create
     * @param session
     *            current JCR session
     * @return the role node
     * @throws RepositoryException
     *             in case of an error
     */
    public static JCRNodeWrapper getOrCreateRole(String path, JCRSessionWrapper session)
            throws RepositoryException {
        if (path == null || !path.startsWith("/roles/")) {
            throw new IllegalArgumentException("Illegal value for the role path: " + path);
        }
        String name = StringUtils.substringAfterLast(path, "/");
        JCRNodeWrapper role = null;
        JCRNodeWrapper base = session.getNode(StringUtils.substringBeforeLast(path, "/"));
        if (!base.hasNode(name)) {
            session.checkout(base);
            role = base.addNode(name, "jnt:role");
            logger.info("Added role node {}", role.getPath());
        } else {
            role = base.getNode(name);
        }

        return role;
    }

    /**
     * Grants the specified permission to the role. Both permission and role nodes have to exist. The {@link Session#save()} is not called
     * by this method; it is the responsibility of the caller.
     * 
     * @param permissionPath
     *            the path of the permission to be granted
     * @param rolePath
     *            the path of the role the permission should be granted to
     * @param session
     *            current JCR session
     * @return <code>true</code> if any modification was done; if the role already has that permission assigned, <code>false</code> is
     *         returned.
     * @throws RepositoryException
     *             in case of an error
     */
    public static boolean grantPermissionToRole(String permissionPath, String rolePath,
            JCRSessionWrapper session) throws RepositoryException {
        if (permissionPath == null || !permissionPath.startsWith("/permissions/")) {
            throw new IllegalArgumentException("Illegal value for the permission path: "
                    + permissionPath);
        }
        if (rolePath == null || rolePath.length() == 0) {
            throw new IllegalArgumentException("Illegal value for the role: " + rolePath);
        }

        boolean modified = true;

        JCRNodeWrapper permission = session.getNode(permissionPath);
        String permissionId = permission.getIdentifier();
        JCRNodeWrapper role = null;
        if (rolePath.contains("/")) {
            role = session.getNode(rolePath);
        } else {
            NodeIterator ni = session.getWorkspace().getQueryManager().createQuery(
                    "select * from [" + Constants.JAHIANT_ROLE + "] as r where localname()='" + JCRContentUtils.sqlEncode(rolePath) + "' and isdescendantnode(r,['/roles'])",
                    Query.JCR_SQL2).execute().getNodes();
            if (ni.hasNext()) {
                role = (JCRNodeWrapper) ni.nextNode();
            }
        }
        if (role == null) {
            throw new RepositoryException("Failed to get role: " + rolePath);
        }

        if (role.hasProperty("j:permissions")) {
            Value[] values = role.getProperty("j:permissions").getValues();
            boolean alreadyPresent = false;
            for (Value value : values) {
                if (permissionId.equals(value.getString())) {
                    alreadyPresent = true;
                    break;
                }
            }
            if (!alreadyPresent) {
                Value[] newValues = new Value[values.length + 1];
                System.arraycopy(values, 0, newValues, 0, values.length);
                newValues[values.length] = session.getValueFactory().createValue(permission, true);
                role.setProperty("j:permissions", newValues);
                logger.info("Granted permission {} to role {}", permission.getPath(),
                        role.getPath());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Role {} already has permission {} granted", role.getPath(),
                            permission.getPath());
                }
                modified = false;
            }
        } else {
            role.setProperty("j:permissions",
                    new Value[] { session.getValueFactory().createValue(permission, true) });
            logger.info("Granted permission {} to role {}", permission.getPath(), role.getPath());
        }

        return modified;
    }

    /**
     * Checks if the specified permissions is already grated to the role.
     * 
     * @param rolePath
     *            the path of the role the permission should be checked for
     * @param permissionPath
     *            the path of the permission to be checked
     * @param session
     *            current JCR session
     * @return <code>true</code> if the role already contains the permission, <code>false</code> otherwise
     * @throws RepositoryException
     *             in case of an error
     */
    public static boolean hasPermission(String rolePath, String permissionPath,
            JCRSessionWrapper session) throws RepositoryException {
        if (permissionPath == null || !permissionPath.startsWith("/permissions/")) {
            throw new IllegalArgumentException("Illegal value for the permission path: "
                    + permissionPath);
        }
        if (rolePath == null || rolePath.length() == 0) {
            throw new IllegalArgumentException("Illegal value for the role: " + rolePath);
        }

        JCRNodeWrapper permission = session.getNode(permissionPath);
        String permissionId = permission.getIdentifier();
        JCRNodeWrapper role = null;
        if (rolePath.contains("/")) {
            role = session.getNode(rolePath);
        } else {
            NodeIterator ni = session.getWorkspace().getQueryManager().createQuery(
                    "select * from [" + Constants.JAHIANT_ROLE + "] as r where localname()='" + JCRContentUtils.sqlEncode(rolePath) + "' and isdescendantnode(r,['/roles'])",
                    Query.JCR_SQL2).execute().getNodes();
            if (ni.hasNext()) {
                role = (JCRNodeWrapper) ni.nextNode();
            }
        }
        if (role == null) {
            throw new RepositoryException("Failed to get role: " + rolePath);
        }

        if (!role.hasProperty("j:permissions")) {
            return false;
        }
        Value[] values = role.getProperty("j:permissions").getValues();
        if (values == null || values.length == 0) {
            return false;
        }

        boolean found = false;

        for (Value value : values) {
            if (StringUtils.equals(permissionId, value.getString())) {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Grants the specified permission to the role. Both permission and role nodes have to exist. The {@link Session#save()} is not called
     * by this method; it is the responsibility of the caller.
     * 
     * @param permissionPath
     *            the path of the permission to be granted
     * @param rolePath
     *            the path of the role the permission should be granted to
     * @param session
     *            current JCR session
     * @return <code>true</code> if any modification was done; if the role already has that permission assigned, <code>false</code> is
     *         returned.
     * @throws RepositoryException
     *             in case of an error
     */
    public static boolean revokePermissionFromRole(String permissionPath, String rolePath,
            JCRSessionWrapper session) throws RepositoryException {
        if (permissionPath == null || !permissionPath.startsWith("/permissions/")) {
            throw new IllegalArgumentException("Illegal value for the permission path: "
                    + permissionPath);
        }
        if (rolePath == null || rolePath.length() == 0) {
            throw new IllegalArgumentException("Illegal value for the role: " + rolePath);
        }

        boolean modified = false;

        JCRNodeWrapper permission = session.getNode(permissionPath);
        String permissionId = permission.getIdentifier();
        JCRNodeWrapper role = null;
        if (rolePath.contains("/")) {
            role = session.getNode(rolePath);
        } else {
            NodeIterator ni = session.getWorkspace().getQueryManager().createQuery(
                    "select * from [" + Constants.JAHIANT_ROLE + "] as r where localname()='" + JCRContentUtils.sqlEncode(rolePath) + "' and isdescendantnode(r,['/roles'])",
                            Query.JCR_SQL2).execute().getNodes();
            if (ni.hasNext()) {
                role = (JCRNodeWrapper) ni.nextNode();
            }
        }
        if (role == null) {
            throw new RepositoryException("Failed to get role: " + rolePath);
        }

        if (!role.hasProperty("j:permissions")) {
            return false;
        }
        Value[] values = role.getProperty("j:permissions").getValues();
        if (values == null || values.length == 0) {
            return false;
        }
        List<Value> newValues = new LinkedList<Value>();
        Collections.addAll(newValues, values);
        boolean found = false;
        for (Iterator<Value> valueIterator = newValues.iterator(); valueIterator.hasNext();) {
            if (StringUtils.equals(permissionId, valueIterator.next().getString())) {
                found = true;
                valueIterator.remove();
            }
        }
        if (found) {
            modified = true;
            if (newValues.isEmpty()) {
                role.setProperty("j:permissions", (Value[]) null);
            } else {
                role.setProperty("j:permissions", newValues.toArray(new Value[] {}));
            }
            logger.info("Revoked permission {} from role {}", permission.getPath(), role.getPath());
        }

        return modified;
    }

    /**
     * Initializes an instance of this class.
     */
    private RBACUtils() {
        super();
    }

}
