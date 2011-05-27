<%@ page import="au.org.ala.workforce.QuestionModel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>${qset.title}</title>
    </head>
    <body>
        <div class="nav">
            <span class="navButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
        </div>
        <div class="body">
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:if test="${user}">
                <p>Answers for ${user.name} <span style="padding-left:50px;">
                    <g:link action="answers" params="${[set:1,id:31]}">Next>></g:link></span></p>
            </g:if>
            <div class="list">
                <table class="answers">
                    <colgroup><col width="4%"><col width="50%"><col width="46%"></colgroup>
                    <tbody>

                    <g:each var='question' in="${questions}">
                      <wf:report question="${question}"/>
                    </g:each>

                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
