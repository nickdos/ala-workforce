<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="au.org.ala.workforce.ConfigData; org.codehaus.groovy.grails.commons.ConfigurationHolder" %>
<html>
    <head>
        <title><g:layoutTitle default="ABRS Surveys" /></title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8; IE=EmulateIE9">
        <meta name="app.version" content="${g.meta(name:'app.version')}"/>
        <meta name="app.build" content="${g.meta(name:'app.build')}"/>
        <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
        <link rel="stylesheet" href="${resource(dir:'css',file:'jquery-ui-1.8.14.custom.css')}" />
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <g:javascript library="application" />
        <g:javascript library="jquery-1.5.1.min" />
        <g:javascript library="jquery-ui-1.8.14.custom.min" />
        <g:layoutHead />
    </head>
    <body>
        <div class='header'>
            <img class="left" src="${resource(dir:'/images/abrsskin/', file: 'logo-environment.gif')}"/>
            <img class="right" src="${resource(dir:'/images/abrsskin/', file: 'abrs-logo-white-on-green.png')}"/>
            <span>Australian Biological Resources Study</span>
        </div>
        <div class="login-info">
            <g:set var="username" value="${wf.loggedInName()}"/>
            <g:if test="${username}">
                <span id="logged-in">Logged in as ${username}</span>
                <a href="${resource(file:'logout')}?casUrl=${ConfigurationHolder.config.security.cas.logoutUrl}&appUrl=${ConfigurationHolder.config.security.cas.appServerName}${ConfigurationHolder.config.security.cas.contextPath}/">Logout</a>
            </g:if>
            <g:else>
                <a href="${ConfigurationHolder.config.security.cas.loginUrl}?service=${ConfigurationHolder.config.security.cas.appServerName}${ConfigurationHolder.config.security.cas.contextPath}/">Login</a>
            </g:else>
        </div>
        <g:layoutBody />
        <div class='footer'>
            <a href="http://www.ala.org.au"><img src="${resource(dir:'images',file:'atlas-poweredby_rgb-lightbg.png')}"/></a>
            <p><a href="mailto:${ConfigData.getFeedbackAddress()}">Survey feedback</a></p>
            <p><a href="mailto:${ConfigData.getSupportAddress()}">Technical support</a></p>
        </div>
    </body>
</html>