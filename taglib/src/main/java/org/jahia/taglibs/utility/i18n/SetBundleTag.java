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
package org.jahia.taglibs.utility.i18n;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.i18n.ResourceBundles;
import org.slf4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Used to set the resource bundle to be used in the page by &lt;fmt:message/&gt; tags.
 * User: rincevent
 * Date: 26 févr. 2009
 * Time: 16:34:03
 */
@SuppressWarnings("serial")
public class SetBundleTag extends AbstractJahiaTag {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(SetBundleTag.class);

    private String basename;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;
    private boolean useUiLocale = false;
    private String templateName;

    @Override
    protected void resetState() {
        basename = null;
        useUiLocale = false;
        scope = PageContext.PAGE_SCOPE;
        var = null;
        templateName = null;
        super.resetState();
    }

    @Override
    public int doStartTag() throws JspException {
        // Position localisationContext
        if (basename != null && !"".equals(basename)) {
            RenderContext context = getRenderContext();
            Locale locale = useUiLocale ? getUILocale() : null;
            if (locale == null) {
                try {
                    locale = context != null ? context.getMainResourceLocale() : (Locale) pageContext.getAttribute(Constants.SESSION_LOCALE, PageContext.SESSION_SCOPE);
                } catch (Exception e) {
                    logger.debug(e.getMessage(), e);
                    locale = pageContext.getRequest().getLocale();
                }
            }
            if (locale == null) {
                locale = pageContext.getRequest().getLocale();
            }
            ResourceBundle resourceBundle = null;
            try {
                if (templateName == null) {
                    resourceBundle = ResourceBundles.get(basename, locale);
                } else {
                    resourceBundle = ResourceBundles.get(basename, ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(templateName), locale);
                }
            } catch (MissingResourceException e) {
                logger.warn(e.getMessage(), e);
            }
            LocalizationContext locCtxt = new LocalizationContext(resourceBundle, locale);
            if (var != null) {
                pageContext.setAttribute(var, locCtxt, scope);
            } else {
                Config.set(pageContext, Config.FMT_LOCALIZATION_CONTEXT, locCtxt, scope);
            }
        } else {
            logger.warn("Provided 'basename' for the tag is either null or empty");
        }
        return SKIP_BODY;
    }

    public void setBasename(String basename) {
        this.basename = basename;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setUseUILocale(String useUILocale) {
        this.useUiLocale = Boolean.parseBoolean(useUILocale);
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
