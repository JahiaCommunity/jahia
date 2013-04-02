<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<template:addResources type="javascript" resources="jquery.min.js,jquery.blockUI.js"/>
<template:addResources>
    <script type="text/javascript">
        $(document).ready(function() {
            $('#${currentNode.identifier}-deleteSitesConfirmed').click(function() {
                $.blockUI({ css: {
                    border: 'none',
                    padding: '15px',
                    backgroundColor: '#000',
                    '-webkit-border-radius': '10px',
                    '-moz-border-radius': '10px',
                    opacity: .5,
                    color: '#fff'
                }, message: '<fmt:message key="org.jahia.admin.workInProgressTitle"/>' });
            });
        });
    </script>
</template:addResources>

<p><fmt:message key="label.confirmContinue"/></p>
<p style="color: red; font-weight: bold;"><fmt:message key="serverSettings.manageWebProjects.delete.warning"/></p>
<form action="${flowExecutionUrl}" method="post">
<table border="1" cellpadding="5" cellspacing="0">
        <tr>
            <th>
                <fmt:message key="label.name" />
            </th>
            <th>
                <fmt:message key="serverSettings.manageWebProjects.webProject.siteKey" />
            </th>
            <th>
                <fmt:message key="serverSettings.manageWebProjects.webProject.serverName" />
            </th>
            <th>
                <fmt:message key="serverSettings.manageWebProjects.webProject.templateSet" />
            </th>
        </tr>

        <input name="_sites" type="hidden"/>
        <c:forEach items="${webprojectHandler.sites}" var="site">
            <tr>
                <td>${fn:escapeXml(site.title)}</td>
                <td>${fn:escapeXml(site.siteKey)}</td>
                <td>${fn:escapeXml(site.serverName)}</td>
                <td title="${fn:escapeXml(site.templatePackageName)}">${fn:escapeXml(site.templateFolder)}</td>
            </tr>
        </c:forEach>

</table>
<input type="submit" name="_eventId_cancel" value="<fmt:message key='label.cancel' />"/>
<input type="submit" name="_eventId_deleteSitesConfirmed" id="${currentNode.identifier}-deleteSitesConfirmed" value="<fmt:message key='label.delete' />"/>
</form>


