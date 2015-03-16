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
package org.jahia.services.importexport;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 
 * User: toto
 * Date: 5 juin 2006
 * Time: 17:57:36
 * 
 */
public class UsersImportHandler extends DefaultHandler {
    private JahiaUserManagerService u;
    private JahiaGroupManagerService g;
    private JahiaSite site;
    private List<String[]> uuidProps = new ArrayList<String[]>();

    private JCRGroupNode currentGroup = null;
    private boolean member = false;

    private JCRSessionWrapper session;

    public UsersImportHandler(JahiaSite site, JCRSessionWrapper session) {
        this.site = site;
        this.session = session;
        u = ServicesRegistry.getInstance().getJahiaUserManagerService();
        g = ServicesRegistry.getInstance().getJahiaGroupManagerService();
    }

    public UsersImportHandler(JCRSessionWrapper session) {
        this.session = session;
        u = ServicesRegistry.getInstance().getJahiaUserManagerService();
        g = ServicesRegistry.getInstance().getJahiaGroupManagerService();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (currentGroup == null) {
            if (localName.equals("user")) {
                String name = attributes.getValue(ImportExportBaseService.JAHIA_URI, "name");
                String pass = null;
                Properties p = new Properties();

                for (int i = 0; i < attributes.getLength(); i++) {
                    String k = attributes.getLocalName(i);
                    String v = attributes.getValue(i);
                    if (k.equals("name")) {
                        //
                    } else if (k.equals("password")) {
                        pass = v;
                    } else if (k.equals("user_homepage")) {
                        uuidProps.add(new String[]{name, k, v});
                    } else {
                        p.put(k, v);
                    }
                }
                if (name != null && pass != null) {
                    if (!u.userExists(name)) {
                        u.createUser(name, pass, p, session);
                    }
                }
            } else if (localName.equals("group")) {
                String name = attributes.getValue(ImportExportBaseService.JAHIA_URI, "name");
                Properties p = new Properties();
                for (int i = 0; i < attributes.getLength(); i++) {
                    String k = attributes.getLocalName(i);
                    String v = attributes.getValue(i);
                    if (k.equals("name")) {
                        //
                    } else if (k.equals("group_homepage")) {
                        uuidProps.add(new String[]{name, k, v});
                    } else {
                        p.put(k, v);
                    }
                }
                if (name != null) {
                    currentGroup = g.lookupGroup(site.getSiteKey(), name, session);
                }
            }
        } else {
            member = true;
            JCRNodeWrapper p = null;
            String name = attributes.getValue(ImportExportBaseService.JAHIA_URI, "name");
            if (localName.equals("user")) {
                p = u.lookupUser(name);
            } else if (localName.equals("group")) {
                p = g.lookupGroup(site.getSiteKey(), name, session);
            }
            if (p != null && !currentGroup.getMembers().contains(p)) {
                currentGroup.addMember(p);
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (member) {
            member = false;
        } else if (localName.equals("group")) {
            currentGroup = null;
        }
    }


    public List<String[]> getUuidProps() {
        return uuidProps;
    }

    public void setUuidProps(List<String[]> p) {
        if (p == null) {
            return;
        }
    }

}
