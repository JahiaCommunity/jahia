/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.logging;

import org.slf4j.profiler.Profiler;

/**
 * This Service offer you to log information for metrics usages (not for information or debugging purposes).
 * <p/>
 * Or to profile some operations.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 24 nov. 2009
 */
public interface MetricsLoggingService {
    
    /**
     * Returns <code>true</code> if the logging is generally enabled; <code>false</code> otherwise.
     * @return <code>true</code> if the logging is generally enabled; <code>false</code> otherwise
     */
    boolean isEnabled();
    
    /**
     * Returns <code>true</code> if the profiler logging is generally enabled; <code>false</code> otherwise.
     * @return <code>true</code> if the profiler logging is generally enabled; <code>false</code> otherwise
     */
    boolean isProfilingEnabled();
    
    /**
     * Log some metric about a node.
     *
     * @param user           user achieving the operation
     * @param ipAddress      IP address of the user
     * @param sessionID      if available, the identifier of the session, otherwise null or an empty string is fine. Note
     *                       that if you use null it will be output verbatim in the log.
     * @param nodeIdentifier if available, the node identifier on which the event took place, otherwise null
     * @param path           the node path on which the operation has been achieved
     * @param nodeType       the type of the node
     * @param logTemplate    the name of the template log you want to use.
     * @param args           variable list of arguments depending of the template you choose
     */
    void logContentEvent(String user, String sessionID, String ipAddress, String nodeIdentifier, String path, String nodeType, String logTemplate, String... args);

    /**
     * Start a profiler and start the associated action (if the profilerName is not found it will create it)
     *
     * @param profilerName name of the profiler you want to use or create
     * @param action       the action you want to profile
     */
    void startProfiler(String profilerName, String action);

    /**
     * Stop all profiling for this profiler name
     *
     * @param profilerName the name of the profiler you want to stop
     */
    void stopProfiler(String profilerName);

    /**
     * Create a sub profiler of an existing profiler
     *
     * @param parentProfilerName the parent profiler name
     * @param nestedProfilerName the sub profiler name
     * @return the nested profiler
     */
    Profiler createNestedProfiler(String parentProfilerName, String nestedProfilerName);

    /**
     * Stop a nested profiler
     *
     * @param parentProfilerName the parent profiler name
     * @param nestedProfilerName the sub profiler name
     */
    void stopNestedProfiler(String parentProfilerName, String nestedProfilerName);

    /**
     * Start a new profiler and return it to the caller.
     *
     * @param profilerName the new profiler you want to start
     * @return the newly created Profiler
     */
    Profiler startProfiler(String profilerName);
}
