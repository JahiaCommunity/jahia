/*
 * JAHIA DUAL LICENSING IMPORTANT INFORMATION
 * ----------------------------------------------------------
 *
 * This program was developed exclusively for Jahia, the next-generation Open Source CMS.
 *
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS PROGRAM IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ----------------------------------------------------------
 *
 * IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * "This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * The GPL license is available in the LICENSE folder of this program.
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ----------------------------------------------------------
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * "Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained
 * in a separate written agreement between you and Jahia Solutions Group SA."
 *
 * The JSEL license is available in the LICENSE folder of this program.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.bundles.jaas;

import org.apache.karaf.jaas.config.JaasRealm;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Enable JAAS realm for Jahia
 */
public class Activator implements BundleActivator {


    @Override
    public void start(BundleContext bundleContext) throws Exception {
        JahiaJaasRealmService jahiaRealm = new JahiaJaasRealmService(bundleContext);

        bundleContext.registerService(JaasRealm.class, jahiaRealm, null);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
