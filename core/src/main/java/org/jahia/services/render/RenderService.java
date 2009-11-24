package org.jahia.services.render;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.operations.valves.EngineValve;
import org.jahia.params.ParamBean;
import org.jahia.services.JahiaService;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.TemplateScriptFilter;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import javax.jcr.RepositoryException;
import javax.jcr.PropertyIterator;
import javax.jcr.Property;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Service to render node
 *  
 * @author toto
 *
 */
public class RenderService extends JahiaService {

    private static volatile RenderService instance;
    
    private static final Logger logger = Logger.getLogger(RenderService.class);

    public synchronized static RenderService getInstance() {
        if (instance == null) {
            instance = new RenderService();
        }
        return instance;
    }

    private JCRStoreService storeService;

    private Collection<ScriptResolver> scriptResolvers;

    private List<RenderFilter> filters;

    public JCRStoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(JCRStoreService storeService) {
        this.storeService = storeService;
    }

    public void setScriptResolvers(Collection<ScriptResolver> scriptResolvers) {
        this.scriptResolvers = scriptResolvers;
    }

    public void setFilters(List<RenderFilter> filters) {
        this.filters = filters;
        for (RenderFilter filter : filters) {
            filter.setService(this);
        }
    }

    public void start() throws JahiaInitializationException {

    }

    public void stop() throws JahiaException {

    }

    /**
     * Render a specific resource and returns it as a StringBuffer.
     *
     * @param resource Resource to display
     * @param context The render context
     * @return The rendered result
     * @throws RepositoryException when jcr issue
     * @throws IOException when input/output issue
     * @throws TemplateNotFoundException when template issue
     */
    public String render(Resource resource, RenderContext context) throws RepositoryException, TemplateNotFoundException, IOException {
        final HttpServletRequest request = context.getRequest();

        if (context.getResourcesStack().contains(resource)) {
            return "loop";
        }

        final Map<String, Object> old = new HashMap<String, Object>();
        JCRNodeWrapper node = resource.getNode();

        request.setAttribute("renderContext", context);

        final Script script = resolveScript(resource, context);

        pushAttribute(request, "script", script, old);
        pushAttribute(request, "scriptInfo", script.getTemplate().getInfo(), old);
        pushAttribute(request, "currentNode", node, old);
        pushAttribute(request, "workspace", node.getSession().getWorkspace().getName(), old);
        pushAttribute(request, "currentResource", resource, old);
        pushAttribute(request, "url",new URLGenerator(context, resource, storeService), old);

        // Resolve params
        Map<String,Object> params = new HashMap<String,Object>();
        Map<String, Object> moduleParams = context.getModuleParams();
        for (Map.Entry<String, Object> entry : moduleParams.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("forced")) {
                key = StringUtils.uncapitalize(StringUtils.substringAfter(key,"forced"));
                params.put(key, entry.getValue());
            } else if (!moduleParams.containsKey("forced"+ StringUtils.capitalize(key))) {
                params.put(key, entry.getValue());
            }
        }
        PropertyIterator pi = node.getProperties();
        while (pi.hasNext()) {
            Property property = pi.nextProperty();
            if (property.getDefinition().getDeclaringNodeType().isNodeType("jmix:layout")) {
                String key = StringUtils.substringAfter(property.getName(), ":");
                if (!moduleParams.containsKey("forced"+ StringUtils.capitalize(key))) {
                    params.put(key, property.getString());
                }
            }
        }

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            pushAttribute(request, entry.getKey(), entry.getValue(), old);
        }


        String output = "";
        try {
            setJahiaAttributes(request, node, (ParamBean) Jahia.getThreadParamBean());

            RenderChain renderChain = new RenderChain();
            renderChain.addFilters(filters);

            renderChain.addFilter(new TemplateScriptFilter());

            output = renderChain.doFilter(context, resource);
        } finally {
            popAttributes(request, old);
        }

        if (request.getAttribute("currentResource") != null) {
            ((Resource)request.getAttribute("currentResource")).getDependencies().addAll(resource.getDependencies());
        }

        return output;
    }

    private void pushAttribute(HttpServletRequest request, String key, Object value, Map<String,Object> oldMap) {
        oldMap.put(key, request.getAttribute(key));
        request.setAttribute(key, value);
    }

    private void popAttributes(HttpServletRequest request, Map<String,Object> oldMap) {
        for (Map.Entry<String,Object> entry : oldMap.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    /**
     * This resolves the executable script from the resource object. This should be able to find the proper script
     * depending of the template / template type. Currently resolves only simple RequestDispatcherScript.
     *
     * If template cannot be resolved, fall back on default template
     *
     * @param resource The resource to display
     * @param context
     * @return An executable script
     * @throws RepositoryException
     * @throws IOException
     */
    public Script resolveScript(Resource resource, RenderContext context) throws RepositoryException, TemplateNotFoundException {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            Script s = scriptResolver.resolveScript(resource,  context);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    /**
     * This set Jahia context attributes, so that legacy jahia tags can still be used in the templates
     * @param request Request where the attributes will be set
     * @param node Node to display
     * @param threadParamBean The "param bean"
     */
    private void setJahiaAttributes(HttpServletRequest request, JCRNodeWrapper node, ParamBean threadParamBean) {
        try {
            if (request.getAttribute(JahiaData.JAHIA_DATA) == null) {
                request.setAttribute(JahiaData.JAHIA_DATA,new JahiaData(threadParamBean, false));
            }
            if (request.getAttribute("jahia") == null) {
                // expose beans into the request scope  
                EngineValve.setContentAccessBeans(threadParamBean);
            }
            
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean hasTemplate(ExtendedNodeType nt, String key) {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            if (scriptResolver.hasTemplate(nt,key)) {
                return true;
            }
        }
        return false;
    }

    public SortedSet<Template> getAllTemplatesSet() {
        SortedSet<Template> set = new TreeSet<Template>();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            set.addAll(scriptResolver.getAllTemplatesSet());
        }
        return set;
    }

    public SortedSet<Template> getTemplatesSet(ExtendedNodeType nt) {
        SortedSet<Template> set = new TreeSet<Template>();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            set.addAll(scriptResolver.getTemplatesSet(nt));
        }
        return set;
    }

}
