/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
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
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.importexport.validation;

import java.util.Collections;
import java.util.List;

import org.apache.jackrabbit.util.ISO9075;
import org.jahia.services.importexport.BaseDocumentViewHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SAX handler that performs validation of the JCR content, provided in a document format.
 * 
 * @author Benjamin Papez
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public class DocumentViewValidationHandler extends BaseDocumentViewHandler {

    private List<ImportValidator> validators = Collections.emptyList();

    public void endDocument() throws SAXException {
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (validators.isEmpty()) {
            return;
        }
        pathes.pop();
    }

    /**
     * Returns the overall validation results.
     * 
     * @return the overall validation results
     */
    public ValidationResults getResults() {
        ValidationResults results = new ValidationResults();
        for (ImportValidator validator : validators) {
            results.getResults().add(validator.getResult());
        }

        return results;
    }

    public void setValidators(List<ImportValidator> validators) {
        if (validators != null) {
            this.validators = validators;
        }
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        if (validators.isEmpty()) {
            return;
        }

        try {
            String decodedLocalName = ISO9075.decode(localName);

            String decodedQName = qName.replace(localName, decodedLocalName);

            pathes.push(pathes.peek() + "/" + decodedQName);

            if (noRoot && pathes.size() <= 3) {
                return;
            }

            for (ImportValidator validator : validators) {
                validator.validate(decodedLocalName, decodedQName, pathes.peek(), atts);
            }
        } catch (Exception re) {
            throw new SAXException(re);
        }
    }
}
