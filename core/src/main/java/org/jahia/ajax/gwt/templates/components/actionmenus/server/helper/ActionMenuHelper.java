/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.actionmenus.server.helper;

import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.actionmenu.actions.*;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.params.ProcessingContext;
import org.jahia.content.ObjectKey;
import org.jahia.content.ContentObject;
import org.jahia.data.beans.*;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.fields.ContentField;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.exceptions.JahiaException;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 27 f�vr. 2008 - 14:57:12
 */
public class ActionMenuHelper {

    private static Logger logger = Logger.getLogger(ActionMenuHelper.class) ;
    private static LockRegistry lockRegistry = LockRegistry.getInstance() ;


    public static String isActionMenuAvailable(ProcessingContext processingContext, GWTJahiaPageContext page, String objectKey, String bundleName, String labelKey) {
        logger.debug("Entered 'isActionMenuAvailable'") ;
        try {

            // if we are not in edit mode we don't display the GUI
            if (!ProcessingContext.EDIT.equals(page.getMode())) {
                logger.debug("live mode -> no action menu") ;
                return null ;
            }

            // get the content object type
            String contentType = objectKey.substring(0, objectKey.indexOf(ObjectKey.KEY_SEPARATOR)) ;

            // get the type of the current object
            ContentObject contentObject = JahiaObjectCreator.getContentObjectFromString(objectKey);

            String type = null ;

            // container list case
            if (contentType.equals(ContainerListBean.TYPE)) {
                final ContainerListBean listBean = (ContainerListBean) ContentBean.getInstance(contentObject, processingContext) ;
                final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
                if (listBean.getFullSize() == 0 &&
                        aclService.getSiteActionPermission("engines.languages." + processingContext.getLocale().toString(),
                        processingContext.getUser(),
                        JahiaBaseACL.READ_RIGHTS, processingContext.getSiteID()) <= 0) {
                    logger.debug("empty list / no rights for the current language") ;
                    return null ;
                }
                type = ActionMenuLabelProvider.CONTAINER_LIST ;
            } else if (contentType.equals(ContainerBean.TYPE)) {
                type = ActionMenuLabelProvider.CONTAINER ;
            } else if (contentType.equals(FieldBean.TYPE)) {
                type = ActionMenuLabelProvider.FIELD ;
            } else if (contentType.equals(PageBean.TYPE)) {
                type = ActionMenuLabelProvider.PAGE ;
            }

            try {
                if (contentObject.getID() > 0 && !contentObject.getACL().getPermission(processingContext.getUser(), JahiaBaseACL.WRITE_RIGHTS)) {
                    // if the user doesn't have Write access on the object, don't display the GUI
                    return null ;
                }
            } catch (Exception t) {
                logger.error(t.getMessage(), t);
                return null ;
            }

            ContentBean parentContentObject = (ContentBean) ContentBean.getInstance(contentObject.getParent(processingContext.getEntryLoadRequest()), processingContext) ;
            if (!(parentContentObject != null && parentContentObject.isPicker())) {
                if (labelKey != null) {
                    String label = ActionMenuLabelProvider.getIconLabel(bundleName, labelKey, type, processingContext) ;
                    if (label != null) {
                        return label ;
                    }
                }
                return "" ;
            }
            else {
                return null ;
            }

        } catch (ClassNotFoundException e) {
            logger.error("action menu availability check failed", e);
            return null ;
        } catch (JahiaException e) {
            logger.error("action menu availability check failed", e);
            return null ;
        }
    }


    public static List<GWTJahiaAction> getAvailableActions(HttpSession session, ProcessingContext jParams, final GWTJahiaPageContext page, final String objectKey, final String bundleName, final String namePostFix) {
        if (logger.isDebugEnabled()) {
            logger.debug("Page : " + page.getPid()) ;
            logger.debug("Object key : " + objectKey) ;
            logger.debug("Bundle name : " + bundleName) ;
            logger.debug("Name postfix " + namePostFix) ;
        }

        List<GWTJahiaAction> actions = new ArrayList<GWTJahiaAction>() ;

        try {
            final JahiaUser currentUser = jParams.getUser() ;
            final ContentObject contentObject = JahiaObjectCreator.getContentObjectFromString(objectKey) ;

            if (currentUser == null || contentObject == null || !contentObject.checkWriteAccess(currentUser)) {
                if (currentUser == null) logger.debug("currentUser is null");
                if (contentObject == null) logger.debug("object is null: " + objectKey);

                if (jParams.getPage() != null && jParams.getPage().checkWriteAccess(currentUser)) {
                    logger.debug("user has write access on currentPage: -> OK");
                } else {
                    throw new JahiaForbiddenAccessException();
                }
            }

            // get the content object type
            String objectType = objectKey.substring(0, objectKey.indexOf(ObjectKey.KEY_SEPARATOR)) ;
            if (logger.isDebugEnabled()) {
                logger.debug("Object type : " + objectType) ;
            }

            final EntryLoadRequest elr = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, jParams.getLocales(), true);
            EntryLoadRequest savedEntryLoadRequest = jParams.getSubstituteEntryLoadRequest();
            jParams.setSubstituteEntryLoadRequest(elr);

            // GWTJahiaAction Menu for a page
            if (PageBean.TYPE.equals(objectType)) {
                // Update
                String url = ActionMenuURIFormatter.drawPageUpdateUrl(jParams, contentObject.getID()) ;
                if (url != null) {
                    GWTJahiaAction updatePage = new GWTJahiaEngineAction(GWTJahiaAction.UPDATE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.UPDATE, namePostFix, ActionMenuLabelProvider.PAGE),url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        updatePage.setLocked(true);
                    }
                    actions.add(updatePage) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Update action : " + url);
                    }
                }

            // GWTJahiaAction Menu for a ContainerList
            } else if (ContainerListBean.TYPE.equals(objectType)) {
                ContentContainerList containerList = (ContentContainerList) contentObject ;
                if (logger.isDebugEnabled()) {
                    logger.debug("ContainerListID: " + containerList.getID() + ", def: " + containerList.getJahiaContainerList(jParams, jParams.getEntryLoadRequest()).getDefinition().getID());
                }
                String url ;
                // Add
                url = ActionMenuURIFormatter.drawContainerListAddUrl(jParams, containerList) ;
                if (url != null) {
                    GWTJahiaAction addContainer = new GWTJahiaEngineAction(GWTJahiaAction.ADD, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.ADD, namePostFix, ActionMenuLabelProvider.CONTAINER_LIST), url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.ADD_CONTAINER_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        addContainer.setLocked(true);
                    }
                    actions.add(addContainer) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Add action : " + url);
                    }
                }

                // Update
                url = ActionMenuURIFormatter.drawContainerListUpdateUrl(jParams, containerList) ;
                if (url != null) {
                    GWTJahiaAction updateContainerList = new GWTJahiaEngineAction(GWTJahiaAction.UPDATE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.UPDATE, namePostFix, ActionMenuLabelProvider.CONTAINER_LIST), url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINERLIST_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        updateContainerList.setLocked(true);
                    }
                    actions.add(updateContainerList) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Update action : " + url);
                    }
                }
                // Copy
                if (objectKey != null && !containerList.isMarkedForDelete() && containerList.getJahiaContainerList(jParams, elr).getFullSize() > 0) {
                    actions.add(new GWTJahiaClipboardAction(GWTJahiaAction.COPY, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.COPY, namePostFix, ActionMenuLabelProvider.CONTAINER_LIST), objectKey)) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Copy action : " + objectKey);
                    }
                }
                // Paste
                String pastedType ;
                if ((pastedType = ClipboardHelper.isPasteAllowed(session, jParams, objectKey)) != null) {
                    GWTJahiaAction pasteContainer = new GWTJahiaClipboardAction(GWTJahiaAction.PASTE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.PASTE, namePostFix, pastedType), objectKey) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.ADD_CONTAINER_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        pasteContainer.setLocked(true);
                    }
                    actions.add(pasteContainer) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Paste action : " + objectKey);
                    }
                }
                // Paste reference
                if (pastedType != null && ActionMenuLabelProvider.CONTAINER.equals(pastedType)) { // paste only container reference
                    GWTJahiaAction pasteContainerReference = new GWTJahiaClipboardAction(GWTJahiaAction.PASTE_REF, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.PASTE_REF, namePostFix, pastedType), objectKey) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.ADD_CONTAINER_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        pasteContainerReference.setLocked(true);
                    }
                    actions.add(pasteContainerReference) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Paste reference action : " + objectKey);
                    }
                }

            // GWTJahiaAction Menu for a Container
            } else if (ContainerBean.TYPE.equals(objectType)) {
                ContentContainer container = (ContentContainer) contentObject ;
                // Update
                String url = ActionMenuURIFormatter.drawContainerUpdateUrl(jParams, container, 0) ;
                if (url != null) {
                    GWTJahiaAction updateContainer = new GWTJahiaEngineAction(GWTJahiaAction.UPDATE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.UPDATE, namePostFix, ActionMenuLabelProvider.CONTAINER), url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINER_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        updateContainer.setLocked(true);
                    }
                    actions.add(updateContainer) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Update action : " + url);
                    }
                }
                // Restore
                // temporarily deactivated TODO
                /*url = ActionMenuURIFormatter.drawContainerRestoreUrl(jParams, container) ;
                if (url != null) {
                    GWTJahiaAction restoreContainer = new GWTJahiaEngineAction(GWTJahiaAction.RESTORE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, "restore", namePostFix), url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.RESTORE_LIVE_CONTAINER_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        restoreContainer.setLocked(true);
                    }
                    actions.add(restoreContainer) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Restore action : " + url);
                    }
                }*/
                // Copy
                if (objectKey != null && !container.isMarkedForDelete()) {
                    actions.add(new GWTJahiaClipboardAction(GWTJahiaAction.COPY, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.COPY, namePostFix, ActionMenuLabelProvider.CONTAINER), objectKey)) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Copy action : " + objectKey);
                    }
                }
                // Delete
                url = ActionMenuURIFormatter.drawContainerDeleteUrl(jParams, container) ;
                if (url != null) {
                    GWTJahiaAction deleteContainer = new GWTJahiaEngineAction(GWTJahiaAction.DELETE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.DELETE, namePostFix, ActionMenuLabelProvider.CONTAINER), url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.DELETE_CONTAINER_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        deleteContainer.setLocked(true);
                    }
                    actions.add(deleteContainer) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Delete action : " + url);
                    }
                }
                // Picked
                url = ActionMenuURIFormatter.drawContainerPickedUrl(jParams, container) ;
                if (url != null) {
                    actions.add(new GWTJahiaRedirectAction(GWTJahiaAction.PICKED, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.PICKED, namePostFix, ActionMenuLabelProvider.CONTAINER), url)) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Picked action : " + url);
                    }
                }
                // Picker List
                Map<String, String> pickers = ActionMenuURIFormatter.drawContainerPickerListUrl(jParams, container) ;
                if (pickers != null && pickers.size() > 0) {
                    List<GWTJahiaRedirectAction> pickersRedirect = new ArrayList<GWTJahiaRedirectAction>(pickers.size()) ;
                    for (String title: pickers.keySet()) {
                        pickersRedirect.add(new GWTJahiaRedirectAction(GWTJahiaAction.PICKER_LIST, title, pickers.get(title))) ;
                    }
                    actions.add(new GWTJahiaDisplayPickersAction(GWTJahiaAction.PICKER_LIST, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.PICKER_LIST, namePostFix, ActionMenuLabelProvider.CONTAINER), pickersRedirect)) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Picker list action : " + url);
                    }
                }
                // Source
                url = ActionMenuURIFormatter.drawContainerSourcePageReferenceUrl(jParams, container) ;
                if (url != null) {
                    actions.add(new GWTJahiaRedirectAction(GWTJahiaAction.SOURCE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.SOURCE, namePostFix, ActionMenuLabelProvider.CONTAINER), url)) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Source action : " + url);
                    }
                }

            // GWTJahiaAction Menu for a Field
            } else if (FieldBean.TYPE.equals(objectType)) {
                ContentField field = (ContentField)contentObject ;
                // Update
                String url = ActionMenuURIFormatter.drawFieldUpdateUrl(field, jParams) ;
                if (url != null) {
                    GWTJahiaAction updateField = new GWTJahiaEngineAction(GWTJahiaAction.UPDATE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.UPDATE, namePostFix, ActionMenuLabelProvider.FIELD), url) ;
                    LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_FIELD_TYPE, contentObject.getID());
                    if (!lockRegistry.isAcquireable(lockKey, currentUser, currentUser.getUserKey())) {
                        updateField.setLocked(true);
                    }
                    actions.add(updateField) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Update action : " + url);
                    }
                }
                // Source
                url = ActionMenuURIFormatter.drawFieldSourcePageReferenceUrl(field, jParams) ;
                if (url != null) {
                    actions.add(new GWTJahiaRedirectAction(GWTJahiaAction.SOURCE, ActionMenuLabelProvider.getLocalizedActionLabel(bundleName, jParams, GWTJahiaAction.SOURCE, namePostFix, ActionMenuLabelProvider.FIELD), url)) ;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Source action : " + url);
                    }
                }

            // unknown object type
            } else {
                final String msg = "Unknown 'ObjectType' value ! 'ObjectType' value should be '" +
                        PageBean.TYPE + "', '" + ContainerListBean.TYPE + "', '" +
                        ContainerBean.TYPE + "' or '" + FieldBean.TYPE + "'.";
                logger.error(msg);
                return null;
            }

            jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);

        } catch (JahiaSessionExpirationException ex) {
            logger.warn("Session already expired, unable to proceed.", ex);
            return null ;
        } catch (JahiaForbiddenAccessException ex) {
            logger.warn("Unauthorized attempt to use gwt action menu");
            return null ;
        } catch (Exception e) {
            logger.error("Unable to process the request !", e);
            return null ;
        }
        return actions ;
    }

}
