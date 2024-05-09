<%@ page import="core.log.conf.reloadable.ReloadableConfiguration" %>
<%@ page import="core.log.logger.SL" %>
<%-- /** * Author : song insup * email : insup74@empal.com * version : 1.0 * Date Time: 2007. 11. 17 ¿ÀÈÄ 12:00:47 * Content : * Usage : */--%>
<%@ page contentType="text/html;charset=EUC-KR" language="java" %><%
    if(request.getParameterMap().containsKey("hLogLevel") && !request.getParameter("hLogLevel").equals(""))
        ReloadableConfiguration.getInstance().setLogLevel(new Integer(request.getParameter("hLogLevel")).intValue());
    if(request.getParameterMap().containsKey("hSelectFix") && !request.getParameter("hSelectFix").equals(""))
        ReloadableConfiguration.getInstance().setFixedSelectPosition(new Boolean(request.getParameter("hSelectFix")).booleanValue());
    if(request.getParameterMap().containsKey("hSelectPosition") && !request.getParameter("hSelectPosition").equals(""))
        ReloadableConfiguration.getInstance().setSelectPosition(new Integer(request.getParameter("hSelectPosition")).intValue());
    if(request.getParameterMap().containsKey("hNoneSelectFix") && !request.getParameter("hNoneSelectFix").equals(""))
        ReloadableConfiguration.getInstance().setFixedNoneSelectPosition(new Boolean(request.getParameter("hNoneSelectFix")).booleanValue());
    if(request.getParameterMap().containsKey("hNoneSelectPosition") && !request.getParameter("hNoneSelectPosition").equals(""))
        ReloadableConfiguration.getInstance().setNoneSelectPosition(new Integer(request.getParameter("hNoneSelectPosition")).intValue());
    if(request.getParameterMap().containsKey("hViewPosition") && request.getParameterMap().containsKey("hViewPosition") && !request.getParameter("hViewPosition").equals(""))
        ReloadableConfiguration.getInstance().setViewPosition(new Boolean(request.getParameter("hViewPosition")).booleanValue());
    if(request.getParameterMap().containsKey("hViewInternalException") && !request.getParameter("hViewInternalException").equals(""))
        ReloadableConfiguration.getInstance().setViewInternalException(new Boolean(request.getParameter("hViewInternalException")).booleanValue());
    if(request.getParameterMap().containsKey("hViewAppoint") && !request.getParameter("hViewAppoint").equals(""))
        ReloadableConfiguration.getInstance().setViewAppoint(request.getParameter("hViewAppoint"));
    if(request.getParameterMap().containsKey("hAsynchronous") && !request.getParameter("hAsynchronous").equals(""))
        ReloadableConfiguration.getInstance().setAsynchronous(new Boolean(request.getParameter("hAsynchronous")).booleanValue());
%>
<html><head><title>Configuration log4sql</title><style
        type="text/css"> .left {
    text-align: left;
}

.right {
    text-align: right;
}

.usage {
    text-align: left;
}

h1 {
    font-size: 1em;
    color: #f06;
    background-color: #eee;
    display: block;
    padding: 0.8em;
}

h2 {
    font-size: 1.2em;
    color: #f06;
    background-color: #eee;
    display: block;
    height: 2em;
    padding-top: 0.8em;
}

a {
    text-decoration: none;
}

a:link {
    color: #f06;
}

a:visited {
    color: #f06;
}

p {
    padding: 10px;
}

span.file {
    font-weight: bold;
}

ul {
    padding: 10px;
    list-style: square
}

ol {
    padding: 10px;
    list-style: decimal;
}

ul li {
    margin-left: 2em;
    padding: 0.3em;
}

ol li {
    margin-left: 2em;
    padding: 0.3em;
}

li p {
    margin-left: 0;
}

code, pre {
    color: navy;
    background-color: beige;
    font-size: 11pt;
}

pre {
    border: 2px dashed navy;
    margin-left: 3em;
    margin-right: 4em;
    margin-top: 1em;
    margin-bottom: 1em;
    white-space: pre-line;
}

p.ad {
    font-size: 1em;
    color: blue;
}

.sflogo {
    float: right;
    margin-right: 30px;
}

/*reset.css*/
body, div, dl, dt, dd, ul, ol, li, h1, h2, h3, h4, h5, h6, pre, form, fieldset, input, textarea, p, blockquote, th, td {
    margin: 0;
    padding: 0;
}

table {
    border-collapse: collapse;
    border-spacing: 0;
}

fieldset, img {
    border: 0;
}

address, caption, cite, code, dfn, em, strong, th, var {
    font-style: normal;
    font-weight: normal;
}

ol, ul {
    list-style: none;
}

caption, th {
    text-align: left;
}

h1, h2, h3, h4, h5, h6 {
    font-size: 100%;
    font-weight: normal;
}

q:before, q:after {
    content: '';
}

abbr, acronym {
    border: 0;
}

/*fonts.css*/
body {
    font: 13px arial, helvetica, clean, sans-serif;
* font-size : small;
* font : x-small;
}

table {
    font-size: inherit;
    font: 100%;
}

select, input, textarea {
    font: 99% arial, helvetica, clean, sans-serif;
}

pre, code {
    font: 115% monospace;
* font-size : 100 %;
}

body * {
    line-height: 1.70em;
}

/*grids.css*/
body {
    text-align: center;
}

#ft {
    clear: both;
}

#doc, #doc2, #doc3, .yui-t1, .yui-t2, .yui-t3, .yui-t4, .yui-t5, .yui-t6, .yui-t7 {
    margin: auto;
    text-align: left;
    width: 57.69em;
* width : 56.3 em;
    min-width: 750px;
}

#doc2 {
    width: 73.074em;
* width : 71.313 em;
    min-width: 950px;
}

#doc3 {
    margin: auto 10px;
    width: auto;
}

.yui-b {
    position: relative;
}

.yui-b {
    _position: static;
}

#yui-main .yui-b {
    position: static;
}

#yui-main {
    width: 100%;
}

.yui-t1 #yui-main, .yui-t2 #yui-main, .yui-t3 #yui-main {
    float: right;
    margin-left: -25em;
}

.yui-t4 #yui-main, .yui-t5 #yui-main, .yui-t6 #yui-main {
    float: left;
    margin-right: -25em;
}

.yui-t1 .yui-b {
    float: left;
    width: 12.3207em;
* width : 12.0106 em;
}

.yui-t1 #yui-main .yui-b {
    margin-left: 13.3207em;
* margin-left : 13.0106 em;
}

.yui-t2 .yui-b {
    float: left;
    width: 13.8456em;
* width : 13.512 em;
}

.yui-t2 #yui-main .yui-b {
    margin-left: 14.8456em;
* margin-left : 14.512 em;
}

.yui-t3 .yui-b {
    float: left;
    width: 23.0759em;
* width : 22.52 em;
}

.yui-t3 #yui-main .yui-b {
    margin-left: 24.0759em;
* margin-left : 23.52 em;
}

.yui-t4 .yui-b {
    float: right;
    width: 13.8456em;
* width : 13.512 em;
}

.yui-t4 #yui-main .yui-b {
    margin-right: 14.8456em;
* margin-right : 14.512 em;
}

.yui-t5 .yui-b {
    float: right;
    width: 18.4608em;
* width : 18.016 em;
}

.yui-t5 #yui-main .yui-b {
    margin-right: 19.4608em;
* margin-right : 19.016 em;
}

.yui-t6 .yui-b {
    float: right;
    width: 23.0759em;
* width : 22.52 em;
}

.yui-t6 #yui-main .yui-b {
    margin-right: 24.0759em;
* margin-right : 23.52 em;
}

.yui-t7 #yui-main .yui-b {
    display: block;
    margin: 0 0 1em 0;
}

#yui-main .yui-b {
    float: none;
    width: auto;
}

.yui-g .yui-u, .yui-g .yui-g, .yui-gc .yui-u, .yui-gc .yui-g .yui-u, .yui-ge .yui-u, .yui-gf .yui-u {
    float: right;
    dsplay: inline;
}

.yui-g div.first, .yui-gc div.first, .yui-gc div.first div.first, .yui-gd div.first, .yui-ge div.first, .yui-gf div.first {
    float: left;
}

.yui-g .yui-u, .yui-g .yui-g {
    width: 49.1%;
}

.yui-g .yui-g .yui-u, .yui-gc .yui-g .yui-u {
    width: 48.1%;
}

.yui-gb .yui-u, .yui-gc .yui-u, .yui-gd .yui-u {
    float: left;
    margin-left: 2%;
* margin-left : 1.895 %;
    width: 32%;
}

.yui-gb div.first, .yui-gc div.first, .yui-gd div.first {
    margin-left: 0;
}

.yui-gc div.first, .yui-gd .yui-u {
    width: 66%;
}

.yui-gd div.first {
    width: 32%;
}

.yui-ge .yui-u {
    width: 24%;
}

.yui-ge div.first, .yui-gf .yui-u {
    width: 74.2%;
}

.yui-gf div.first {
    width: 24%;
}

.yui-ge div.first {
    width: 74.2%;
}

#bd:after, .yui-g:after, .yui-gb:after, .yui-gc:after, .yui-gd:after, .yui-ge:after, .yui-gf:after {
    content: ".";
    display: block;
    height: 0;
    clear: both;
    visibility: hidden;
}

#bd, .yui-g, .yui-gb, .yui-gc, .yui-gd, .yui-ge, .yui-gf {
    zoom: 1;
}</style>
<script type="text/javascript">function logL() {
    document.all.hLogLevel.value=document.all.logLevel.selectedIndex;
    document.all.confForm.submit();
} function selectF() {
    document.all.hSelectFix.value=document.all.selectFix[selectFix.selectedIndex].value;
    document.all.confForm.submit();
} function selectP() {
    document.all.hSelectPosition.value=document.all.selectPosition.value;
    document.all.confForm.submit();
} function noneSF() {
    document.all.hNoneSelectFix.value=document.all.noneSelectFix[noneSelectFix.selectedIndex].value;
    document.all.confForm.submit();
} function noneSP() {
    document.all.hNoneSelectPosition.value=document.all.noneSelectPosition.value;
    document.all.confForm.submit();
} function viewPo() {
    document.all.hViewPosition.value=document.all.viewPosition[viewPosition.selectedIndex].value;
    document.all.confForm.submit();
} function viewIE() {
    document.all.hViewInternalException.value=document.all.viewInternalException[viewInternalException.selectedIndex].value;
    document.all.confForm.submit();
} function viewAppoint() {
    document.all.hViewAppoint.value=document.all.viewAppoint.value;
    document.all.confForm.submit();
}function asynchronous() {
    document.all.hAsynchronous.value=document.all.Asynchronous[Asynchronous.selectedIndex].value;
    document.all.confForm.submit();
}
</script></head><body align=center valign=middle><form method="post"
                                                       action="<%=request.getContextPath()%>/log4sql_conf.jsp"
                                                       name="confForm"><input
        type="hidden" name="hLogLevel"><input type="hidden"
                                              name="hSelectFix"><input
        type="hidden" name="hSelectPosition"><input type="hidden"
                                                    name="hNoneSelectFix"><input
        type="hidden" name="hNoneSelectPosition"><input type="hidden"
                                                        name="hViewPosition"><input
        type="hidden" name="hViewInternalException"><input
        type="hidden" name="hViewAppoint"><input
        type="hidden" name="hAsynchronous"></form> &nbsp;&nbsp;&nbsp; <table
        border=1 valign=center><tr><td colspan=3 align=center>&nbsp;<font
        color=red><b>log4sql Runtime Configuration</b></font></td></tr><tr><td
        align=center>&nbsp;&nbsp;&nbsp;&nbsp;<font color=gray><b>Configuration Contents</b></font>&nbsp;&nbsp;&nbsp;</td><td
        align=center><font color=gray><b>Value</b></font></td><td
        align=center><font color=gray><b>Description</b></font></td></tr><tr><td
        align=center>Log Level</td><td>&nbsp;&nbsp;<select id="logLevel"
                                                           onchange="javascript:logL();"><%for(int i=0; i<SL.getInstance().LEVEL.length; i++) {%><option
        value="<%=SL.getInstance().LEVEL[i]%>" <%=ReloadableConfiguration.getInstance().getLogLevel() == SL.getInstance().LEVEL[i] ? "selected" : ""%>><%=(String) ReloadableConfiguration.getInstance().get(new Integer(SL.getInstance().LEVEL[i]))%></option><%}%></select></td><td
        align="left"><ol
        class="left"><pre><b>&nbsp;&nbsp;Log Level means:</b><br>&nbsp;&nbsp;&nbsp;&nbsp;Shows your SQL in the log parameter matched. <br>&nbsp;&nbsp;&nbsp;&nbsp;SQL executed within a prepared statement(ofcourse statement too!) is automatically show with <br>&nbsp;&nbsp;&nbsp;&nbsp;it's bind arguments replaced with the data bound at that position, for greatly increased readability. <br>&nbsp;&nbsp;&nbsp;&nbsp;DEBUG=0,INFO=1,WARNING=2,FATAL=3, ERROR=4, LOGGING_OFF=5 </pre></ol></td></tr><tr><td
        align=center>Select Fix</td><td>&nbsp;&nbsp;<select name="selectFix"
                                                            onchange="javascript:selectF();"><option
        value="true" <%=ReloadableConfiguration.getInstance().getFixedSelectPosition() ? "selected" : ""%>>True</option><option
        value="false" <%=ReloadableConfiguration.getInstance().getFixedSelectPosition() ? "" : "selected"%>>False</option></select></td><td><ol
        class="left"><pre><b>&nbsp;&nbsp;Select Fix means :</b><br>&nbsp;&nbsp;&nbsp;&nbsp;In select query... <br>&nbsp;&nbsp;&nbsp;&nbsp;this library can use many case developement. so we don't know your source arch. <br>&nbsp;&nbsp;&nbsp;&nbsp;if you chang value 'false' then log will trace excutioning. </pre></ol></td></tr><tr><td
        align=center>Select Position</td><td>&nbsp;&nbsp;
    <input type="text" size=10 name="selectPosition" value="<%=ReloadableConfiguration.getInstance().getSelectPosition()%>">
    <input type="button" value="confirm" onclick="javascript:selectP();"/></td><td><ol class="left"><pre><b>&nbsp;&nbsp;Select Position means :</b><br>&nbsp;&nbsp;&nbsp;&nbsp;shows your SQL change logging position. <br>&nbsp;&nbsp;&nbsp;&nbsp;If you change value "false" then log will starting [Position Check Start: ...] like below. <br>&nbsp;&nbsp;&nbsp;&nbsp;This is only affected 'INSERT, UPDATE, DELETE' query. </pre></ol></td></tr><tr><td
        align=center>None Select Fix</td><td>&nbsp;&nbsp;<select
        name="noneSelectFix" onchange="javascript:noneSF();"><option
        value="true" <%=ReloadableConfiguration.getInstance().getFixedNoneSelectPosition() ? "selected" : ""%>>True</option><option
        value="false" <%=ReloadableConfiguration.getInstance().getFixedNoneSelectPosition() ? "" : "selected"%>>False</option></select></td><td
        align=left><ol class="left"><pre><b>&nbsp;&nbsp;None Select Fix means :</b><br>&nbsp;&nbsp;&nbsp;&nbsp;the logging position what you want to see. <br>&nbsp;&nbsp;&nbsp;&nbsp;this example's 'query.logging.position.select=8' so you can see below </pre></ol></td></tr><tr><td
        align=center>None Select Position</td><td>&nbsp;&nbsp;
    <input type="text" size=10 name="noneSelectPosition" value="<%=ReloadableConfiguration.getInstance().getNoneSelectPosition()%>">
    <input type="button" value="confirm" onclick="javascript:noneSP();"/></td><td><ol class="left"><pre><b>&nbsp;&nbsp;query.logging.position.none_select means :</b><br>&nbsp;&nbsp;&nbsp;&nbsp;same upper in insert, update, delete. </pre></ol></td></tr><tr><td
        align=center>View Position</td><td>&nbsp;&nbsp;<select
        name="viewPosition" onchange="javascript:viewPo();"><option
        value="true" <%=ReloadableConfiguration.getInstance().getViewPosition() ? "selected" : ""%>>True</option><option
        value="false" <%=ReloadableConfiguration.getInstance().getViewPosition() ? "" : "selected"%>>False</option></select></td><td><ol
        class="left"><pre><b>&nbsp;&nbsp;View Position means :</b><br>&nbsp;&nbsp;&nbsp;&nbsp;Our aim to logging sql is target preparedstatement's level(also logging statement). <br>&nbsp;&nbsp;&nbsp;&nbsp;Parameters matched in query But SM developers dont know where is parameter where is hardcoded. <br>&nbsp;&nbsp;&nbsp;&nbsp;so we suppose that. if you make 'true' then show the sign '/**P*/' right of parameter. </pre></ol></td></tr><tr><td
        align=center>View Internal Exception</td><td>&nbsp;&nbsp;<select
        name="viewInternalException" onchange="javascript:viewIE();"><option
        value="true" <%=ReloadableConfiguration.getInstance().getViewInternalException() ? "selected" : ""%>>True</option><option
        value="false" <%=ReloadableConfiguration.getInstance().getViewInternalException() ? "" : "selected"%>>False</option></select></td><td><ol
        class="left"><pre><b>&nbsp;&nbsp;View Internal Exception means :</b><br>&nbsp;&nbsp;&nbsp;&nbsp;If you make 'true' then log4sql's internal exception(core.log.exception.InternalException)'s trace will show. <br>&nbsp;&nbsp;&nbsp;&nbsp;Otherwise do not </pre></ol></td></tr>
        <tr>
            <td align=center>Log to Asynchronous</td>
            <td>&nbsp;&nbsp;
                <select name="Asynchronous"
                        onchange="javascript:asynchronous();">
                    <option value="true" <%=ReloadableConfiguration.getInstance().getAsynchronous() ? "selected" : ""%>>True</option>
                    <option value="false" <%=ReloadableConfiguration.getInstance().getAsynchronous() ? "" : "selected"%>>False</option>
                </select>
            </td>
            <td><ol class="left">
                <pre><b>&nbsp;&nbsp;Log to Asynchronous means :</b>
&nbsp;&nbsp;&nbsp;&nbsp;If you make 'true' then log4sql's log will leave asynchronous.<br>
&nbsp;&nbsp;&nbsp;&nbsp;To asynchronous, your application excution and log will seperate.</pre>
                </ol>
            </td>
       </tr>
       <tr>
            <td align=center>View Appointed Packages</td>
            <td>&nbsp;&nbsp;
                <input type="text" name="viewAppoint" size=10
                       value="<%=ReloadableConfiguration.getInstance().getViewAppoint()%>">
                <input type="button" value="confirm" onclick="javascript:viewAppoint();"/>
            </td>
            <td><ol class="left">
                <pre><b>&nbsp;&nbsp;View Appointed Packages means :</b>
&nbsp;&nbsp;&nbsp;&nbsp;Maybe all developers do not develope all alone<br>
&nbsp;&nbsp;&nbsp;&nbsp;Therefore, you want to see only your developing package or class or ...
We bring forth way to use</pre>
                </ol>
            </td>
       </tr>
</table></body></html>