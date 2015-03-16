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
package org.jahia.bin;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.jahia.services.textextraction.TextExtractionService;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.files.FileUpload;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;

/**
 * Performs text and metadata extraction from the submitted document.
 * 
 * @author Sergiy Shyrkov
 */
public class TextExtractor extends JahiaController {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TextExtractor.class);

    private SettingsBean settingsBean;

    private TextExtractionService textExtractionService;

    private View view;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet
     * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (!textExtractionService.isEnabled()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Text extraction service is not enabled.");
            return null;
        }

        if (!ServletFileUpload.isMultipartContent(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file was submitted");
            return null;
        }
        
        FileUpload upload = new FileUpload(request, settingsBean.getTmpContentDiskPath(), Integer.MAX_VALUE);
        if (upload.getFileItems().size() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file was submitted");
            return null;
        }

        DiskFileItem inputFile = upload.getFileItems().values().iterator().next();
        InputStream stream = null;
        try {
            stream = inputFile.getInputStream();
            Metadata metadata = new Metadata();
            metadata.set(Metadata.CONTENT_TYPE, inputFile.getContentType());
            metadata.set(Metadata.RESOURCE_NAME_KEY, inputFile.getName());

            long startTime = System.currentTimeMillis();
            
            String content = textExtractionService.parse(stream, metadata);

            Map<String, Object> model = new HashMap<String, Object>();
            
            Map<String, Object> properties = new HashMap<String, Object>();
            for (String name : metadata.names()) {
                properties.put(name, metadata.isMultiValued(name) ? metadata.getValues(name) : metadata.get(name));
            }
            model.put("metadata", properties);
            model.put("content", content);
            model.put("file", inputFile);
            model.put("extracted", Boolean.TRUE);
            model.put("extractionTime", Long.valueOf(System.currentTimeMillis() - startTime));

            return new ModelAndView(view, model);

        } catch (Exception e) {
            logger.error("Error extracting text for uploaded file " + inputFile.getFieldName() + ". Cause: "
                    + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exception occurred: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(stream);
            for (DiskFileItem file : upload.getFileItems().values()) {
                file.delete();
            }
        }

        return null;
    }

    /**
     * @param settingsBean the settingsBean to set
     */
    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    /**
     * @param textExtractionService the textExtractionService to set
     */
    public void setTextExtractionService(TextExtractionService textExtractionService) {
        this.textExtractionService = textExtractionService;
    }

    /**
     * @param view the view to set
     */
    public void setView(String view) {
        this.view = new InternalResourceView(view);
    }

}
