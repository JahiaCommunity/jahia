<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<div class="columns2"><!--start 2columns -->
    <c:forEach items="${currentNode.children}" var="subchild">
    <template:module node="${subchild}" template="small" nodeTypes="web_templates:promoAdvancedContainer"/>
</c:forEach>
    
        <div class="clear"> </div>
</div>