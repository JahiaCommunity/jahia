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
package org.jahia.ajax.gwt.client.widget.poller;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;

import org.atmosphere.gwt20.client.*;
import org.atmosphere.gwt20.client.managed.RPCEvent;
import org.atmosphere.gwt20.client.managed.RPCSerializer;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Execute recurrent calls to the server
 */
public class Poller {


    private static Poller instance;

    private Map<Class, ArrayList<PollListener>> listeners = new HashMap<Class, ArrayList<PollListener>>();

    public static Poller getInstance() {
        if (instance == null) {
            instance = new Poller(JahiaGWTParameters.isWebSockets());
        }
        return instance;
    }

    public Poller(final boolean useWebsockets) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {

                RPCSerializer rpc_serializer = GWT.create(RPCSerializer.class);

                AtmosphereRequestConfig rpcRequestConfig = AtmosphereRequestConfig.create(rpc_serializer);
                rpcRequestConfig.setUrl(GWT.getModuleBaseURL() .substring(0,GWT.getModuleBaseURL() .indexOf("/gwt/")) + "/atmosphere/rpc");
                rpcRequestConfig.setTransport(useWebsockets ? AtmosphereRequestConfig.Transport.WEBSOCKET
                        : AtmosphereRequestConfig.Transport.STREAMING);
                rpcRequestConfig.setFallbackTransport(AtmosphereRequestConfig.Transport.LONG_POLLING);
                rpcRequestConfig.setOpenHandler(new AtmosphereOpenHandler() {
                    @Override
                    public void onOpen(AtmosphereResponse response) {
                        GWT.log("RPC Connection opened");
                    }
                });
                rpcRequestConfig.setReopenHandler(new AtmosphereReopenHandler() {
                    @Override
                    public void onReopen(AtmosphereResponse response) {
                        GWT.log("RPC Connection reopened");
                    }
                });
                rpcRequestConfig.setCloseHandler(new AtmosphereCloseHandler() {
                    @Override
                    public void onClose(AtmosphereResponse response) {
                        GWT.log("RPC Connection closed");
                    }
                });
                rpcRequestConfig.setMessageHandler(new AtmosphereMessageHandler() {
                    @Override
                    public void onMessage(AtmosphereResponse response) {
                        List<RPCEvent> messages = response.getMessages();
                        for (RPCEvent event : messages) {
                            for (Map.Entry<Class, ArrayList<PollListener>> entry : listeners.entrySet()) {
                                if (entry.getKey() == event.getClass()) {
                                    for (PollListener pollListener : entry.getValue()) {
                                        pollListener.handlePollingResult(event);
                                    }
                                }
                            }
                        }
                    }
                });

                rpcRequestConfig.setFlags(AtmosphereRequestConfig.Flags.enableProtocol);
                rpcRequestConfig.setFlags(AtmosphereRequestConfig.Flags.trackMessageLength);

                // init atmosphere
                Atmosphere atmosphere = Atmosphere.create();
                atmosphere.subscribe(rpcRequestConfig);
            }
        });
    }

    public void registerListener(PollListener listener, Class eventType) {
        if (!listeners.containsKey(eventType)) {
            listeners.put(eventType, new ArrayList<PollListener>());
        }
        listeners.get(eventType).add(listener);
    }

    public void unregisterListener(PollListener listener, Class eventType) {
        if (listeners.containsKey(eventType)) {
            listeners.get(eventType).remove(listener);
        }
    }

    public interface PollListener<T> {
        public void handlePollingResult(T result);
    }


}
