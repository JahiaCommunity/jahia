<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="social.css"/>

<script type="text/javascript">

    function tabCallback() {

        $(".messageDetailLink").click(function() {
            $.ajax({
                url: $(this).attr('urlToMessage'),
                type : 'get',
                success : function (data) {
                    $(".social-message-detail").html(data);
                }
            });
        });
    
    }
</script>

<div class='grid_8 alpha'><!--start grid_12-->

    <jcr:sql var="receivedMessages"
             sql="select * from [jnt:userMessage] as uC where isdescendantnode(uC,['${currentNode.path}/inboundMessages'])"/>

    <h3 class="social-title-icon titleIcon"><a href="#"><fmt:message key="receivedMessages"/></a><a href="#"><img title="" alt=""
                                                                                    src="${url.currentModule}/images/mailbox.png"/></a>
    </h3>
    <ul class="social-list">
        <c:forEach items="${receivedMessages.nodes}" var="userMessage">
            <li>
                <template:module path="${userMessage.path}" />
            </li>
        </c:forEach>
    </ul>

</div>
<!--stop grid_12-->
<div class='grid_8 omega'><!--start grid_4-->

    <div id="socialMessageDetail" class="social-message-detail">
        
    </div>

    <div class='clear'></div>

</div>
<!--stop grid_4-->

<div class='clear'></div>

<div id="divSendMessage">
    <div class="popup-bodywrapper">
        <h3 class="boxmessage-title"><fmt:message key="message.new"/></h3>

        <form class="formMessage" id="sendMessage" method="post" action="">
            <input type="hidden" name="j:to" value="{jcr}sjobs" />
            <fieldset>
                <legend><fmt:message key="message.label.creation"/></legend>
                <p id="login_error" style="display:none;">Please, enter data</p>

                <p><label for="messagesubject" class="left"><fmt:message key="message.label.subject"/></label>
                    <input type="text" name="j:subject" id="messagesubject" class="field" value=""
                           tabindex="20"/></p>


                <p><label for="messagebody" class="left"><fmt:message
                        key="message.label.body"/> :</label>
                    <textarea name="j:body" id="messagebody" cols="45" rows="3"
                              tabindex="21"></textarea></p>
                <input class="button" type="button" value="<fmt:message key="message.label.send"/>"
                       tabindex="28"
                       id="messagesendbutton" onclick="$('#sendMessage').submit();">
            </fieldset>
        </form>
    </div>
</div>

