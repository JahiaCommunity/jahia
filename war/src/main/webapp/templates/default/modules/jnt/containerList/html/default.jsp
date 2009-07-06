<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:forEach items="${currentNode.children}" var="subchild">
<c:if test="${jcr:isNodeType(subchild, 'jnt:container')}">
<p>
    ${currentNode.name}&nbsp;<a href="${pageContext.request.contextPath}/render/default${subchild.path}.html">link</a>
    <template:module node="${subchild}" />
</p>
</c:if>
</c:forEach>
