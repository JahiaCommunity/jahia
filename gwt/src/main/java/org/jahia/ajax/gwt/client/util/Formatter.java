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
package org.jahia.ajax.gwt.client.util;

import java.util.Date;

import org.jahia.ajax.gwt.client.messages.Messages;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * Helper to format strings, numbers, and other stuff.
 *
 * @author rfelden
 * @version 23 juin 2008 - 16:58:56
 */
public class Formatter {

    public static final float GB = 1073741824f;
    public static final float MB = 1048576f;
    public static final float KB = 1024f;
    public static final DateTimeFormat DEFAULT_DATETIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm");

    /**
     * Get a human readable size from a byte size.
     *
     * @param size the size to format
     * @return the formatted size
     */
    public static String getFormattedSize(long size) {
        NumberFormat nf = NumberFormat.getFormat("0.00");
        StringBuffer dispSize = new StringBuffer();
        if (size >= GB) {
            dispSize.append(String.valueOf(nf.format(size / GB))).append(" GB");
        } else if (size >= MB) {
            dispSize.append(String.valueOf(nf.format(size / MB))).append(" MB");
        } else if (size >= KB) {
            dispSize.append(String.valueOf(nf.format(size / KB))).append(" KB");
        } else {
            dispSize.append(String.valueOf(nf.format(size))).append(" B");
        }
        return dispSize.toString();
    }

    /**
     * Get a human readable date formatted according to a given pattern.
     *
     * @param date    the date to format
     * @param pattern the date pattern
     * @return the formatted date
     * @see com.google.gwt.i18n.client.DateTimeFormat
     */
    public static String getFormattedDate(Date date, String pattern) {
        return date != null ? (pattern != null ? DateTimeFormat.getFormat(pattern).format(date) : DEFAULT_DATETIME_FORMAT.format(date)) : "-";
    }

    /**
     * Get a human readable date formatted according to a default pattern.
     *
     * @param date    the date to format
     * @return the formatted date
     * @see com.google.gwt.i18n.client.DateTimeFormat
     */
    public static String getFormattedDate(Date date) {
        return getFormattedDate(date, null);
    }
    
    /**
     * Returns formatted lock info label based on the provided key.
     * 
     * @param lockInfo
     *            the lock information key
     * @return formatted lock info label based on the provided key
     */
    public static String getLockLabel(String lockInfo) {
        if (lockInfo == null || lockInfo.length() == 0) {
            return lockInfo;
        }
        if (lockInfo.startsWith("label.")) {
            return Messages.get(lockInfo);
        } else {
            return lockInfo.contains(":") ? (lockInfo.substring(0, lockInfo.indexOf(":")) + " ("
                    + lockInfo.substring(lockInfo.indexOf(":") + 1) + ")") : lockInfo;
        }
    }

    /**
     * Enable a Button changing the CSS style according to its state.
     *
     * @param ti the ToolItem
     */
    public static void enableButton(Component ti) {
        if (!ti.isEnabled()) {
            ti.setEnabled(true);
        }
    }

    /**
     * Disable a Button changing the CSS style according to its state.
     *
     * @param ti the ToolItem
     */
    public static void disableButton(Component ti) {
        if (ti.isEnabled()) {
            ti.disable();
        }
    }

    public static void setButtonEnabled(Component ti, boolean b) {
        if (b) {
            Formatter.enableButton(ti);
        } else {
            Formatter.disableButton(ti);
        }
    }


    /**
     * Enable a MenuItem changing the CSS style according to its state.
     *
     * @param mi the MenuItem
     */
    public static void enableMenuItem(Item mi) {
        if (!mi.isEnabled()) {
            mi.enable();
        }
    }

    /**
     * Disable a MenuItem changing the CSS style according to its state.
     *
     * @param mi the MenuItem
     */
    public static void disableMenuItem(Item mi) {
        if (mi.isEnabled()) {
            mi.disable();
        }
    }


    /**
     * Enable / disable item
     * @param ti
     * @param b
     */
    public static void setMenuItemEnabled(MenuItem ti, boolean b) {
        if (b) {
            Formatter.enableMenuItem(ti);
        } else {
            Formatter.disableMenuItem(ti);
        }
    }


}
