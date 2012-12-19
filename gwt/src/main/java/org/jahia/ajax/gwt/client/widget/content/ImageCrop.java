/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 * User: rfelden
 * Date: 21 oct. 2008 - 14:38:21
 */
public class ImageCrop extends Window {

    private Linker linker;
    
    private boolean autoName = true;

    public ImageCrop(final Linker linker, final GWTJahiaNode n) {
        super();
        setLayout(new FitLayout());
        setSize(712, 550);

        this.linker = linker;
        setHeading(Messages.get("label.crop"));
        setId("JahiaGxtImageCrop");

        FormData formData = new FormData("100%");  
        FormPanel form = new FormPanel();  
        form.setFrame(false);
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setLabelWidth(70);
      
        LayoutContainer main = new LayoutContainer();  
        main.setLayout(new ColumnLayout());  
      
        LayoutContainer lcName = new LayoutContainer();  
        lcName.setStyleAttribute("paddingRight", "10px");  
        lcName.setLayout(new FormLayout(LabelAlign.LEFT));
      
        final TextField<String> newname = new TextField<String>();
        newname.setName("newname");
        newname.setId("newname");
        newname.setFieldLabel(Messages.get("label.rename", "Rename"));
        int extIndex = n.getName().lastIndexOf(".");
        if (extIndex > 0) {
            String dotExt = n.getName().substring(extIndex);
            newname.setValue(n.getName().replaceAll(dotExt, "-crop" + dotExt));
        } else {
            newname.setValue(n.getName() + "-crop");
        }
        
        newname.addListener(Events.Change, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent be) {
                autoName = false;
            }
        });

        lcName.add(newname, formData);

        LayoutContainer lcWidth = new LayoutContainer();  
        lcWidth.setStyleAttribute("paddingRight", "10px");  
        FormLayout formLayout = new FormLayout(LabelAlign.RIGHT);
        formLayout.setLabelWidth(40);
        lcWidth.setLayout(formLayout);
      
        final NumberField width = new NumberField();  
        width.setFieldLabel(Messages.get("label.width", "Width"));  
        width.setName("width");
        width.setId("width");
        lcWidth.add(width, formData);
      
        LayoutContainer lcHeight = new LayoutContainer();  
        lcHeight.setStyleAttribute("paddingLeft", "10px");  
        formLayout = new FormLayout(LabelAlign.RIGHT);
        formLayout.setLabelWidth(40);
        lcHeight.setLayout(formLayout);  
      
        final NumberField height = new NumberField();  
        height.setFieldLabel(Messages.get("label.height", "Height"));  
        height.setName("height");
        height.setId("height");
        lcHeight.add(height, formData);
      
        main.add(lcName, new ColumnData(.6));
        main.add(lcWidth, new ColumnData(.2));
        main.add(lcHeight, new ColumnData(.2));  
      
        form.add(main, new FormData("100%"));
        
        final Image image = new Image();
        image.addLoadHandler(new LoadHandler() {
            public void onLoad(LoadEvent event) {
                initJcrop();
            }
        });
        // Point the image at a real URL.
        image.getElement().setId("cropbox");
        image.setUrl(n.getUrl());

        form.add(image);
        
        final NumberField top = new NumberField();
        top.setName("top");
        top.setId("top");
        top.setVisible(false);
        form.add(top);
        final NumberField left = new NumberField();
        left.setName("left");
        left.setId("left");
        left.setVisible(false);
        form.add(left);
        
        ButtonBar buttons = new ButtonBar();
        Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });
        Button submit = new Button(Messages.get("label.ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                if (width.getValue().intValue() > 0 && height.getValue().intValue() > 0) {
                    cropImage(n.getPath(), newname.getValue().toString(), top.getValue().intValue(), left.getValue()
                            .intValue(), width.getValue().intValue(), height.getValue().intValue(), false);
                }
            }
        });
        buttons.add(submit);
        buttons.add(cancel);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);

        Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent be) {
                if (width.getValue() != null && height.getValue() != null) {
                    setDimensions(width.getValue().intValue(), height.getValue().intValue());
                }
            }
        };
        width.addListener(Events.Change, listener);
        height.addListener(Events.Change, listener);
        
        add(form);
        
        setModal(true);
        setHeaderVisible(true);
        setAutoHide(false);
    }
    
    private native void setDimensions(int w, int h) /*-{
        try {
            var jcapi=$wnd.jQuery('#cropbox').data('Jcrop');
            var c=jcapi.tellSelect();
            jcapi.setSelect([c.x, c.y, c.x + w, c.y + h]);
        } catch (e) { }
    }-*/;
    
    private native void initJcrop() /*-{
        thisic=this;
        $wnd.jQuery('#cropbox').Jcrop({
            boxWidth: 700,
            boxHeight: 400,
            onChange: function(c) {
                $wnd.jQuery('#left-input').val(c.x); $wnd.jQuery('#top-input').val(c.y); $wnd.jQuery('#width-input').val(c.w); $wnd.jQuery('#height-input').val(c.h);
                if (thisic.@org.jahia.ajax.gwt.client.widget.content.ImageCrop::autoName) {
                    var n=$wnd.jQuery('#newname-input').val();
                    var dp=n.lastIndexOf('.');
                    var nv=n.substring(0, n.lastIndexOf('-crop')) + '-crop' + c.w + 'x' + c.h + (dp != -1 ? n.substring(dp, n.length) : '');
                    $wnd.jQuery('#newname-input').val(nv);
                }
            }
        });        
    }-*/;

    private void cropImage(final String path, final String targetName, final int top, final int left, final int width, final int height, final boolean force) {
        JahiaContentManagementService.App.getInstance().cropImage(path, targetName, top, left, width, height, force, new BaseAsyncCallback<Object>() {
            public void onApplicationFailure(Throwable throwable) {
                if (throwable instanceof ExistingFileException) {
                    if (com.google.gwt.user.client.Window.confirm(Messages.get("alreadyExists.label") + "\n" + Messages.get("confirm.overwrite.label"))) {
                        cropImage(path, targetName, top, left, width, height, true);
                    }
                } else {
                    com.google.gwt.user.client.Window.alert(Messages.get("failure.crop.label") + "\n" + throwable.getLocalizedMessage());
                    Log.error(Messages.get("failure.crop.label"), throwable);
                }
            }

            public void onSuccess(Object result) {
                hide();
                linker.refresh(Linker.REFRESH_MAIN, null);
            }
        });
    }
}
