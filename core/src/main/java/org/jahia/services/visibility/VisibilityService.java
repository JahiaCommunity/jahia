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
package org.jahia.services.visibility;

import org.jahia.services.Conditional;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for evaluating visibility conditions on a content item.
 *
 * @author rincevent
 * @since JAHIA 6.6
 * Created : 8/29/11
 */
public class VisibilityService {
    
    public static final String NODE_NAME = "j:conditionalVisibility";

    private transient static Logger logger = LoggerFactory.getLogger(VisibilityService.class);

    private static volatile VisibilityService instance;

    private Map<String, VisibilityConditionRule> conditions = new HashMap<String, VisibilityConditionRule>(1);

    public static VisibilityService getInstance() {
        if (instance == null) {
            synchronized (VisibilityService.class) {
                if (instance == null) {
                    instance = new VisibilityService();
                }
            }
        }
        return instance;
    }

    public Map<String, VisibilityConditionRule> getConditions() {
        return conditions;
    }

    public void setConditions(Map<String, VisibilityConditionRule> conditions) {
        if (conditions != null) {
            for (Map.Entry<String, VisibilityConditionRule> cond : conditions.entrySet()) {
                addCondition(cond.getKey(), cond.getValue());
            }
        }
    }

    public void addCondition(String conditionType, VisibilityConditionRule instance) {
        if (instance instanceof Conditional) {
            if (!((Conditional) instance).evaluate()) {
                logger.info("Visibility condition of type {} is considered disabled. Skipping it.",
                        conditionType);
                return;
            }
        }
        this.conditions.put(conditionType, instance);
    }

    public void removeCondition(String conditionType) {
        if (instance instanceof Conditional) {
            if (!((Conditional) instance).evaluate()) {
                logger.info("Visibility condition of type {} is considered disabled. Skipping it.",
                        conditionType);
                return;
            }
        }
        this.conditions.remove(conditionType);
    }

    public boolean matchesConditions(JCRNodeWrapper node) {
        if (conditions.isEmpty()) {
            return true;
        }
        try {
            if (node.hasNode(NODE_NAME)) {
                node = node.getNode(NODE_NAME);
                boolean forceMatchAllConditions = node.getProperty("j:forceMatchAllConditions").getBoolean();
                List<JCRNodeWrapper> childrenOfType = JCRContentUtils.getChildrenOfType(node, "jnt:condition");
                if(childrenOfType.isEmpty()) {
                    return true;
                }
                if (forceMatchAllConditions) {
                    boolean matches = true;
                    for (JCRNodeWrapper nodeWrapper : childrenOfType) {
                        if (matches) {
                            VisibilityConditionRule rule = conditions.get(nodeWrapper.getPrimaryNodeTypeName());
                            if (rule != null) {
                                matches = rule.matches(nodeWrapper);
                            }
                        } else {
                            break;
                        }
                    }
                    return matches;
                } else {
                    for (JCRNodeWrapper nodeWrapper : childrenOfType) {
                        VisibilityConditionRule rule = conditions.get(nodeWrapper.getPrimaryNodeTypeName());
                        if (rule != null) {
                            if (rule.matches(nodeWrapper)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            } else {
                return true;
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public Map<JCRNodeWrapper,Boolean> getConditionMatchesDetails(JCRNodeWrapper node) {
        Map<JCRNodeWrapper, Boolean> conditions = new HashMap<JCRNodeWrapper, Boolean>();
        try {
            if (node.hasNode(NODE_NAME)) {
                node = node.getNode(NODE_NAME);
                List<JCRNodeWrapper> childrenOfType = JCRContentUtils.getChildrenOfType(node, "jnt:condition");
                for (JCRNodeWrapper nodeWrapper : childrenOfType) {
                    VisibilityConditionRule rule = this.conditions.get(nodeWrapper.getPrimaryNodeTypeName());
                    if (rule != null) {
                        conditions.put(nodeWrapper,rule.matches(nodeWrapper));
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return conditions;
    }
}
