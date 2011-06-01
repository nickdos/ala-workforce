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
                <div style='float: left; padding: 25px 0px'>Answers for ${user.name}</div>
                <wf:reportNavigation users= "${users}" user="${user}"/>
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
            <wf:reportNavigation users= "${users}" user="${user}"/>
        </div>
    </body>
</html>
