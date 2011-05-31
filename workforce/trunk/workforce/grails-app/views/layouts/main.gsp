<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" %>
<html>
    <head>
        <title><g:layoutTitle default="ABRS Surveys" /></title>
        <meta name="app.version" content="${g.meta(name:'app.version')}"/>
        <meta name="app.build" content="${g.meta(name:'app.build')}"/>
        <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <g:layoutHead />
        <g:javascript library="application" />
        <style type="text/css" media="screen">
            div.header {
                background-color: #497136;
                height: 120px;
            }
            img.left {
                padding-right: 15px;
                padding-top: 20px;
                padding-left: 25px;
            }
            img.right {
                padding-right: 15px;
                padding-top: 10px;
                padding-left: 20px;
                float: right;
            }
            div.header span {
                font-size: 17px;
                font-weight: bold;
                padding-top: 50px;
                color: white;
                float: right;
            }
        </style>

    </head>
    <body>
        <div class='header'>
            <img class="left" src="${resource(dir:'/images/abrsskin/', file: 'logo-environment.gif')}"/>
            <img class="right" src="${resource(dir:'/images/abrsskin/', file: 'abrs-logo-white.png')}"/>
            <span>Australian Biological Resources Study</span>
        </div>
        <div class="login-info">
            <g:set var="username" value="${wf.loggedInName()}"/>
            <g:if test="${username}">
                <span id="logged-in">Logged in as ${username}</span>
                <a href="${ConfigurationHolder.config.security.cas.logoutUrl}?url=${ConfigurationHolder.config.security.cas.serverName}${ConfigurationHolder.config.security.cas.contextPath}/">Logout</a>
            </g:if>
            <g:else>
                <a href="${ConfigurationHolder.config.security.cas.loginUrl}?service=${ConfigurationHolder.config.security.cas.serverName}${ConfigurationHolder.config.security.cas.contextPath}/">Login</a>
            </g:else>
        </div>
        <g:layoutBody />
        <div class='footer'>
            <img src="${resource(dir:'images',file:'atlas-poweredby_rgb-lightbg.png')}"/>
        </div>
    </body>
</html>