package com.houseelectrics.serializer.test;

/**
 * Created by roberttodd on 01/12/2014.
 */
public class JSExecuteUtilJavascript
{
    public final static String NewLine = "\r\n";
    public final static String ShowSourceFunctionName = "showjssource";
    public final static String ShowSourceFunction =
    "function " + ShowSourceFunctionName + "(f)" + NewLine +
    "{" + NewLine +
    "    var w = window.open('','','width=400, resizable=yes, scrollbars');" + NewLine +
    "    w.document.write('<pre>' + f + '</pre>');" + NewLine +
    "    w.document.close();" + NewLine +
    "    w.focus();" + NewLine +
    "};";
    public final static String AssertEqualFunction =
    "var assertEqual = function(expected, actual, message)" + NewLine +
    "{" + NewLine +
    "    if (expected!=actual)" + NewLine +
    "    {" + NewLine +
    "        throw message +  ' expected:' + expected + ' actual:' + actual;" + NewLine +
    "    }" + NewLine +
    "};";

    public static String getRunTest4TableRowFunction(String TablePageRunTestFunctionName, String crossimagesrc, String tickimagesrc, String questionmarkimagesrc)
    {
        String strJs = "function " + TablePageRunTestFunctionName + "(passimageid, testFunction, clear)" + NewLine +
        "{" + NewLine +
        "    var theimage = document.getElementById(passimageid);" + NewLine +
        "    if (clear)" + NewLine +
        "    {" + NewLine +
        "        theimage.src=runtest.questionmarkimagesrc;" + NewLine +
        "        return;" + NewLine +
        "    }" + NewLine +
        "    try" + NewLine +
        "    {" + NewLine +
        "        testFunction();" + NewLine +
        "        theimage.src=runtest.tickimagesrc;" + NewLine +
        "    }" + NewLine +
        "    catch (err)" + NewLine +
        "    {" + NewLine +
        "        theimage.src=runtest.crossimagesrc;" + NewLine +
        "    }" + NewLine +
        "}" + NewLine +
        "runtest.crossimagesrc='" + crossimagesrc + "';" + NewLine +
        "runtest.tickimagesrc='" + tickimagesrc + "';" + NewLine +
        "runtest.questionmarkimagesrc='" + questionmarkimagesrc + "';" + NewLine;

        return strJs;
    }

}
