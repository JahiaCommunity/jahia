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
package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.ajax.gwt.client.widget.contentengine.ButtonItem;
import org.jahia.services.uicomponents.bean.contentmanager.ManagerConfiguration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Core part of engine configuration
 * see GWTEngineConfiguration for gwt part
 * see {@link org.jahia.ajax.gwt.helper.UIConfigHelper} for link
 */

public class EngineConfiguration implements Serializable , InitializingBean, DisposableBean {

    private static final long serialVersionUID = -5991528610464460659L;

    private String key;

    private List<EngineTab> engineTabs;

    private List<ButtonItem> creationButtons = new ArrayList<ButtonItem>();
    private List<ButtonItem> editionButtons = new ArrayList<ButtonItem>();
    private List<ButtonItem> commonButtons = new ArrayList<ButtonItem>();

    private Object parent;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /***
     * @return list of engine tabs
     */
    public List<EngineTab> getEngineTabs() {
        return engineTabs;
    }

    public void setEngineTabs(List<EngineTab> engineTabs) {
        this.engineTabs = engineTabs;
    }

    /**
     * @return List of button to display for create engine
     */
    public List<ButtonItem> getCreationButtons() {
        return creationButtons;
    }

    public void setCreationButtons(List<ButtonItem> creationButtons) {
        this.creationButtons = creationButtons;
    }

    /**
     * @return List of ButtonItem for edit engine
     */
    public List<ButtonItem> getEditionButtons() {
        return editionButtons;
    }

    public void setEditionButtons(List<ButtonItem> editionButtons) {
        this.editionButtons = editionButtons;
    }

    /**
     * @return List of button for both create and edit (like cancel)
     */
    public List<ButtonItem> getCommonButtons() {
        return commonButtons;
    }

    public void setCommonButtons(List<ButtonItem> commonButtons) {
        this.commonButtons = commonButtons;
    }

    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public void afterPropertiesSet() throws Exception {
        if (parent instanceof List) {
            for (Object o : (List) parent) {
                addToParent(o);
            }
        } else {
            addToParent(parent);
        }
    }

    public void destroy() throws Exception {
        if (parent instanceof List) {
            for (Object o : (List) parent) {
                removeFromParent(o);
            }
        } else {
            removeFromParent(parent);
        }
    }

    private void removeFromParent(Object o) {
        Map<String, EngineConfiguration> configs = getParentConfigurationMap(o);

        if (configs != null) {
            configs.remove(getKey());
        }
    }

    private void addToParent(Object o) {
        Map<String, EngineConfiguration> configs = getParentConfigurationMap(o);

        if (configs != null) {
            configs.put(getKey(), this);
        } else if (o != null) {
            throw new IllegalArgumentException("Unknown parent type '"
                    + o.getClass().getName()
                    + "'. Can accept EditConfiguration, ManagerConfiguration, Engine or"
                    + " a String value with a beanId of the those beans");
        }
    }

    private Map<String, EngineConfiguration> getParentConfigurationMap(Object parent) {
        if (parent instanceof EditConfiguration) {
            return ((EditConfiguration)parent).getEngineConfigurations();
        } else if (parent instanceof ManagerConfiguration) {
            return ((ManagerConfiguration)parent).getEngineConfigurations();
        }
        return null;
    }
}
