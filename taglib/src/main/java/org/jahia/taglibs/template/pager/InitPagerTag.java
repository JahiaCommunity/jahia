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
package org.jahia.taglibs.template.pager;

import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * User: toto
 * Date: Dec 7, 2009
 * Time: 11:39:29 AM
 * 
 */
public class InitPagerTag extends TagSupport {
    private static final long serialVersionUID = 3487375821225747403L;

    private int pageSize;
    private long totalSize;
    private boolean sizeNotExact = false;

    private String id;

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            @SuppressWarnings("unchecked")
            Map<String,Object> moduleMap  = (HashMap<String,Object>)  pageContext.getRequest().getAttribute("moduleMap");
            if (moduleMap == null) {
                moduleMap = new HashMap<String,Object>();
            }
            Object value = moduleMap.get("begin");
            if (value != null) {
                moduleMap.put("old_begin"+id, value);
            }
            value = moduleMap.get("end");
            if (value != null) {
                moduleMap.put("old_end"+id, value);
            }
            value = moduleMap.get("pageSize");
            if (value != null) {
                moduleMap.put("old_pageSize"+id, value);
            }
            value = moduleMap.get("nbPages");
            if (value != null) {
                moduleMap.put("old_nbPages"+id, value);
            }
            value = moduleMap.get("currentPage");
            if (value != null) {
                moduleMap.put("old_currentPage"+id, value);
            }
            value = moduleMap.get("paginationActive");
            if (value != null) {
                moduleMap.put("old_paginationActive"+id, value);
            }
            value = moduleMap.get("totalSize");
            if (value != null) {
                moduleMap.put("old_totalSize"+id, value);
            }
            String beginStr = StringEscapeUtils.escapeXml(pageContext.getRequest().getParameter("begin"+id));
            String endStr = StringEscapeUtils.escapeXml(pageContext.getRequest().getParameter("end"+id));

            if (pageContext.getRequest().getParameter("pagesize"+id) != null) {
                pageSize = Integer.parseInt(StringEscapeUtils.escapeXml(pageContext.getRequest().getParameter("pagesize"+id)));
            }

            int begin = beginStr == null ? 0 : Integer.parseInt(beginStr);
            int end = endStr == null ? pageSize - 1 : Integer.parseInt(endStr);

            int currentPage = begin / pageSize + 1;

            long nbPages = totalSize / pageSize;
            if (nbPages * pageSize < totalSize) {
                nbPages++;
            }
            if (totalSize == Integer.MAX_VALUE) {
                nbPages = currentPage;// + 1;
            }

            if (totalSize < pageSize) {
                begin = 0;
            } else if (begin > totalSize) {
                begin = (int) ((nbPages-1) * pageSize);
                end = begin + pageSize - 1;
            }
            
            if (currentPage > nbPages) {
                currentPage = (int)nbPages;
            }

            moduleMap.put("begin", begin);
            moduleMap.put("end", end);
            moduleMap.put("pageSize", pageSize);
            moduleMap.put("nbPages", nbPages);
            moduleMap.put("currentPage", currentPage);
            moduleMap.put("paginationActive", true);
            moduleMap.put("totalSize", totalSize);
            moduleMap.put("sizeNotExact", sizeNotExact);            
            moduleMap.put("totalSizeUnknown", totalSize == Integer.MAX_VALUE);
            pageContext.setAttribute("moduleMap",moduleMap);
            pageContext.setAttribute("begin_"+id,begin,PageContext.REQUEST_SCOPE);
            pageContext.setAttribute("end_"+id,end,PageContext.REQUEST_SCOPE);
        } catch (Exception e) {
            throw new JspException(e);
        }        
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        return super.doEndTag();
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSizeNotExact() {
        return sizeNotExact;
    }

    public void setSizeNotExact(boolean sizeNotExact) {
        this.sizeNotExact = sizeNotExact;
    }

    @Override
    public void release() {
        super.release();
        id = null;
        pageSize = 0;
        sizeNotExact = false;
        totalSize = 0;
    }
}
