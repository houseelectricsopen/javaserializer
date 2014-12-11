package com.houseelectrics.serializer.test;
import javax.script.*;
import java.lang.reflect.Method;
import java.util.*;
import java.io.*;
import org.junit.Test;
/**
 * Created by roberttodd on 01/12/2014.
 */
public class JSExecuteUtil
{
    public static ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    public static final String NewLine = JSExecuteUtilJavascript.NewLine;

    String testdir = "testtemp";

    private List<JsonValueSet> history = new ArrayList<JsonValueSet>();

    public static class JsonValueSet
    {
        public StackTraceElement stackFrame;
        public Test testAttribute;
        public String testDescription;
        public String getDescription()
        {
            StringBuilder sb = new StringBuilder();

            if (testDescription != null ) sb.append(testDescription);
            if (significance != null) sb.append(" - " + significance);
            return sb.toString();
        }
        public String getSourceDescription()
        {
            return stackFrame.getFileName()  + stackFrame.getClassName() +
                    "::" + stackFrame.getMethodName();
        }

        public String testId;
        public String filename;
        public String varname;
        public String json;
        public String significance;
        public Map<String, Object> expressions2ExpectedValue = new HashMap<String, Object>();
        public List<String> extraFunctions = new ArrayList<String>();
        public String ToString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("testId=" + testId);
        sb.append(" filename=" + filename);
        sb.append(" varname=" + varname);
        sb.append(" json==" + json);
        sb.append("\r\n extraFunctions=");
        for (String strF : extraFunctions)
        {
            sb.append( "\r\n" + strF);
        }
        return sb.toString();
    }
    }

    public String testId2FunctionName(String testId)
    {
        return "test_" + testId.replaceAll("/", "_").replaceAll("\\.", "_");
    }

    public void writeTestFunction(PrintWriter sw, JsonValueSet valueSet)
    {
        sw.println("function " + testId2FunctionName(valueSet.testId) + "()" + NewLine +
        "{");
            sw.println("var " + valueSet.varname + "=" + valueSet.json + ";");

            for (String expr : valueSet.expressions2ExpectedValue.keySet())
            {
                Object expected = valueSet.expressions2ExpectedValue.get(expr);
                if (expected == null) expected = "null";
                else if (expected.getClass() == String.class|| expected.getClass()==Character.class) expected = ("'" + expected + "'");
                String fullexpression = valueSet.varname + "." + expr;
                sw.println("assertEqual(" + expected + "," + fullexpression +
                        ", '" + fullexpression.replaceAll("'", "\\\\'") + "');");
            }
            sw.println(
            "" + NewLine +
        "} ");
    }

    public void writeTestHtml(JsonValueSet valueSet) throws IOException
    {

        valueSet.filename = testdir + File.separator + valueSet.testId + ".html";
        System.out.println("writing test to " + valueSet.filename);

        File fi = new File(valueSet.filename);
        System.out.println("writing test to " + fi.getAbsolutePath());
        File parentFile = new File(testdir);
        parentFile.mkdirs();
        PrintWriter sw = new PrintWriter(new FileWriter(valueSet.filename));

        sw.println(
        "<html>" + NewLine +
        "    <script>" + NewLine +
        "    ");
        for (String f : valueSet.extraFunctions)
        {
            sw.println(f);
            sw.println();
        }

        sw.println("var " + valueSet.varname + "=" + valueSet.json + ";");
        sw.println();
        //writeTestFunction(sw, valueSet);


        sw.println("</script>" + NewLine +
        "    <body>" + NewLine +
        "<pre>");

        sw.println(valueSet.varname + "=>" + valueSet.json);
        for (String expression : valueSet.expressions2ExpectedValue.keySet())
                {
                        String strFullExpression = valueSet.varname + "." + expression;
        sw.println(strFullExpression + "==<script>document.write(" + strFullExpression + ")</script>");
        }

        sw.println();
        sw.println("</pre>");
        sw.flush();
        sw.close();
    }

    Object getAttributeFromFrame(StackTraceElement frame, Class attributeType)
    {
        try
        {
            Class theClass = Class.forName(frame.getClassName());
            Method m = theClass.getMethod(frame.getMethodName());
            Object attribute = m.getAnnotation(attributeType);
            if (attribute!=null)
            {
                return attribute;
            }
        }
        catch (Exception ex)
        {

        }
        return null;
    }

    public StackTraceElement getCurrentNunitStackFrame()
    {
        StackTraceElement frames[] = Thread.currentThread().getStackTrace();

            for (int done = 0; done < frames.length; done++)
            {
                StackTraceElement frame = frames[done];
                if (null!=getAttributeFromFrame(frame, Test.class)) return frame;

            }
        return null;
    }

    public void writeTestTablePage() throws IOException
    {
        String filename = testdir + File.separator + "testTable" + ".html";
        System.out.println("writing test table to " + (new File(filename)).getAbsolutePath());

        File parentFile = new File(testdir);
        parentFile.mkdirs();

        PrintWriter sw = new PrintWriter(new FileWriter(filename));
        writeTestTablePage(sw, history);
        sw.close();
    }

    public void writeTestTablePage(PrintWriter sw, List<JsonValueSet> tests)
    {
        sw.println(
                "<html>" + NewLine +
                        "    <script>" + NewLine +
                        "    ");
        Map<String, String> extraFunctionsPrinted = new HashMap<String, String>();
        sw.println(JSExecuteUtilJavascript.AssertEqualFunction);

        for (JsonValueSet valueSet : tests)
        {
            for (String f : valueSet.extraFunctions)
            {
                if (!extraFunctionsPrinted.containsKey(f))
                {
                    extraFunctionsPrinted.put(f, f);
                    sw.println(f);
                    sw.println();
                }
            }
            writeTestFunction(sw, valueSet);
            sw.println();
        }

        sw.println();
        sw.println(JSExecuteUtilJavascript.getRunTest4TableRowFunction(TablePageRunTestFunctionName, crossimagesrc, tickimagesrc, questionmarkimagesrc));
        sw.println("</script>");
        String runAllTestsFunctionName = "runAllTests";
        sw.println("<body onload=" + "\"" + runAllTestsFunctionName + "(false)\"" +  ">" + NewLine +
            "<h1>" + NewLine +
            "Serializer Browser Compatibility Tests - created by " + getClass().getName() + " at " + (new Date()) +" " + NewLine +
            "</h1>");
        String[] headers = { "pass<br>fail", "name", "description", ".net source", "java script<br> test source" };
        List<List<String>> rows = new ArrayList<List<String>>();
        sw.println("<script> " + NewLine +
        "function " + runAllTestsFunctionName + "(clear) {");
        for (int index = 0; index < tests.size(); index++ )
        {
            JsonValueSet test = tests.get(index);
            List<String> row = new ArrayList<String>();
            String imageid = "row" + index + ".pass.image";
            String testfunctionname = testId2FunctionName(test.testId);
            String runtestcall = TablePageRunTestFunctionName +"('" + imageid + "', " + testfunctionname + ", clear)";
            sw.println(runtestcall + ";");
            String runrow = "<img id='" + imageid + "' width='20' height='24'/>" +
                    "<a href=\"javascript:"+ runtestcall + "\">run</a>";
            row.add(runrow);
            row.add(test.testId);
            row.add(test.getDescription());
            row.add(test.getSourceDescription());

            row.add("<a href=\"javascript:" + JSExecuteUtilJavascript.ShowSourceFunctionName + "(" + testfunctionname + ")\">show</a>");
            rows.add(row);
        }
        sw.println("}" + NewLine +
        "var clear=false;" + NewLine +
             JSExecuteUtilJavascript.ShowSourceFunction  + NewLine +
        "    </script>");
        sw.println("<a href=\"javascript:" + runAllTestsFunctionName + "()\">run all tests</a>");
        sw.println("<br>");
        sw.println("<a href=\"javascript:" + runAllTestsFunctionName + "(true)\">clear all tests</a>");
        sw.println("<br>");
        writeHTMLtable(sw, headers, rows);
        sw.println();
//page default is to show result
//sw.WriteLine("<script> debugger; " + runAllTestsFunctionName + "(false);</script>");
        sw.println("</body>");
        sw.println("</html>");
        }


       public Map<String, Object> extractValuesFromJson(JsonValueSet valueSet, String testId)
       {
           try {
               return extractValuesFromJsonChecked(valueSet, testId);
           }
           catch (IOException ioex)
           {
               throw new RuntimeException("io issue extracting values " + ioex.getMessage(), ioex);
           }
       }

        public Map<String, Object> extractValuesFromJsonChecked(JsonValueSet valueSet, String testId) throws IOException
        {
            // try to look up JunitAttribute in stack here !

            valueSet.stackFrame = getCurrentNunitStackFrame();
            if (valueSet.stackFrame!=null)
            {
                Test ta = (Test) getAttributeFromFrame(valueSet.stackFrame, Test.class);
                valueSet.testAttribute = ta;
                TestDescription testDescription = (TestDescription) getAttributeFromFrame(valueSet.stackFrame, TestDescription.class);
                if (testDescription!=null)  valueSet.testDescription = testDescription.description();
            }

            history.add(valueSet);

            valueSet.testId = testId;

            writeTestHtml(valueSet);

            Map<String, Object> expressions2ActualValue = new HashMap<String, Object>();

            for (String expression : valueSet.expressions2ExpectedValue.keySet())
                    {
                    String js;
                    if (valueSet.extraFunctions.size() > 0)
                    {
                      js = "(function() {";
                      for (String f : valueSet.extraFunctions)
                       {
                          js = js + "\r\n" + f;
                       }
                     js = js + "\r\n return (" + valueSet.json + ");";
                     js = js + " })()." + expression;
            }
            else
            {
                js = "(" + valueSet.json + ")." + expression;
            }
            Object result = execute(js);
            //todo maybe System.DBNull actually means undefined !
            //if (result != null && result.GetType()==(typeof(System.DBNull)) ) result = null;
            expressions2ActualValue.put(expression, result);
            }


            return expressions2ActualValue;
        }

        //assume only 1 table for time being
    private String getTableCellId(int row, String header)
    {
        return "row" + row + "." + header;
    }

    private void writeHTMLtable(PrintWriter sw, String[] headers, List<List<String>> rows)
    {
        sw.println("" + NewLine +
        "    <table border=1>" + NewLine +
        "<tr>");
        for(String header : headers)
        {
            sw.write("<th>" + header + "</th>");
        }
        sw.println("</tr>");
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++ )
        {
            List<String> row=rows.get(rowIndex);
            sw.write("<tr>");
            for (int done = 0; done < headers.length; done++)
            {
                String value = (row.size() - 1) < done ? "" : row.get(done);
                sw.write("<td>" + value + "</td>");
            }
            sw.println("</tr>");
        }
        sw.println("</table>");
    }

    public Object execute(String strJS)
    {
        Object result;
        try
        {

            result =   scriptEngine.eval(strJS);  //Eval.JScriptEvaluate(strJS, vsaEngine);
        }
        catch (Exception ex)
        {
            System.out.println("failed to execute:\r\n" + strJS + " because " + ex.getMessage());
            result = ex;
        }
        return result;
    }

    public static String TablePageRunTestFunctionName="runtest";

    /**derived from media/tick.png derived from tick.svg all put through
     http://websemantics.co.uk/online_tools/image_to_data_uri_convertor/   **/

    final static String tickimagesrc = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAbCAYAAAB836/YAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAAagAAAGoB3Bi5tQAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAHGSURBVEiJpdRfaI1hGADw305alGg3rlYu3MmFO8uFuXDhzm4kXMjCLoQWtbJLuUBNboiWG8Ra5LBShG0yWWlIlAxN8iex2s6ZY+x1s17ny9nOOd/53t6b93m+X8/3fc/zCSGoZWu20KB+WQeQqQkLIZB1WJhdg+5k1HDVHapbrkl7PKg3VFt1D5yP1X3zQZuG9NgpzfJ+RfCe9hCCdBh1nhmI2EevrLEoPXjJnogFQVZrjFWNbdFgzLuIjXqMTHrwruMRmxFc0JKIV4UdtcoPkxF84fZ/OVWBw65FrOC3M9anBnVrMW0mgiN6SuZVhK1U77WnEcuZcszq9GCfjkSbPHJuztyyWIdGX3yN2HfjOq1IDw7pTlR334l58+cNnrZWrmheP/tkt2UVgU7aYaslieBz/Ynq+nSWfaIQAl02GJfz0MUY6NFa1CTBe2+ss7gsaL9GY0bjKN100C5LjXmbqO6yvRV1hGG3Ejfm5PUXTUQQvDSCBZWBN+xTMJ0AiteM4KxtlWD/3uF1baYUSoJPDFSKJb/yVTvl/UxgBX902ZgKDCHQa7ucfNGIZavBSja2KzabMGlCwRFNNYOz6Ca9c/8A5tt/AWXIMuPZc9CQAAAAAElFTkSuQmCC";
    final static String  crossimagesrc="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAYCAYAAAD6S912AAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAAagAAAGoB3Bi5tQAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAIVSURBVDiNndXfa81xHMfxx3fHcCPJr1IjpcXFJHdu/AWyEnPHXFIuFkWZtZqk1SIsUy6IZhdkjGwxy4XQXGzKMooxTqwR2sxm28fNzuns7JzNOZ9617fer56fz/vzfr++HyEEuQbmPeTWbSpRMC2XD/AedYEQCM+5d4iivIFX2TfKRAIYCP30NVCaM/AMW77zIxWWiGHGmqgtkLJqoqikOYoqoyiKSVtHo2hVKZeXsDg9BwsonORXcvcjFPfxOhC6aK1mdSK3mcIXPMh0skS0cT1Z8gGK3tKVKojz4QrbQghaOT8brIvOchaFEChnZQ/PMgl/M9bBjVEms8E+Eq9mfbLL7XOUMlv85M9Ftk+bw3p2DPAtV9g4oZFjM4Z+ahy2fqI/F2AbjRldlPg4Rcn7qS7PFb8YP8HuWYEhBMdY84bn/wMdYvQmJxGb1cv7WdrD4/8tvZNHVazLCqynOJ7jfcaJX6ZsBvA4K97Rnc8IjTB+l7NJL++NooVlNK1lY7pP/zI5yGAmDyfWQmLDjCRP94TGbLu3UHeJDXFeZtN00IyYEII2arMJn9KS6GQNy3u5n655RVcZy0II3OHgWBav9tK9h+Wp97yT+Z1cmJzSfOZLFZuSXW7hYibYF76eTBGmRzuHBxg6x66090Z0i9MTaUPbwM65/uAVbMjqlCaqx5iYIFyjKp/Ha8YcXqXiJtfyhYUQ/AM+zdCyi+sN3QAAAABJRU5ErkJggg==";
    final static String  questionmarkimagesrc="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABEAAAAYCAYAAAAcYhYyAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAAVQAAAFUBceom8QAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAGQSURBVDiNpdS/axRBGMbxz56JMXhY2aRR7AwIsbstgo34J1io2NgottpZCtqp2AviHyBoK9poMSJI/NFoECws0khQ/MGBl9di57jNsXe74gvDPvvOw3femXmZIiL8byw0ZstiAadwFAewjGd4KsVo2l7sqqQs9uMGzuJgA34Ll6R41Awpi0N4jLU8t43nGGIdKzk/wgUpHuyGlEUPb3AMgWu4JcUwL7CIq7lK+I4VKX6BiBADZ2Ig8rgeERrHwN2a7/w438vki/k7xJ2GsxjH/Zo+Mha9qcSmFF/nQOr9sDWBVOexhA94MgcAJ2r681gUnZutLA7jPfp4h+NS7FSVdAMUuJcBI1weA7pDqus9mfVNKV7UJ9u3UxZreIVFvMS6FH/qli6VXMmAHzg3DWiHlMVenM5/D6X41GRrq2QZ+7LemGVqg+yp6f4sU/N7Monfqs58nUdjdG+2OdFWSRXVS7eKL1JsT0+3X3FZ3FZd71uThvtHSNXmS1mvNhm6bOcjdrCJb02GLm3fR0jxc5blL/VsmgRQaoWoAAAAAElFTkSuQmCC";

}
