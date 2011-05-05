<html>
    <head>
        <title><g:layoutTitle default="ABRS Surveys" /></title>
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
        <g:layoutBody />
    </body>
</html>