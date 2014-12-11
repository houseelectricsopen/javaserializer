package com.houseelectrics.serializer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import java.util.Stack;

/**
 * Created by roberttodd on 30/11/2014.
 */
public class Object2Json implements ExplorationListener
{

    public interface ExplorerFactory
    {
        public Explorer create();
    }

    public Object2Json()
    {
        explorerFactory= new ExplorerFactory()
        {
            @Override
            public Explorer create()
            {
                return new ObjectExplorerImpl();
            }
        };
    }

    private boolean alwaysDoubleQuotePropertyNames = false;
    public boolean getAlwaysDoubleQuotePropertyNames() {return alwaysDoubleQuotePropertyNames;}
    public void setAlwaysDoubleQuotePropertyNames(boolean value) {this.alwaysDoubleQuotePropertyNames=value;}

    private TypeAliaser typeAliaser=null;
    public TypeAliaser getTypeAliaser() {return typeAliaser;}
    public void setTypeAliaser(TypeAliaser value) {this.typeAliaser=value;}

    private boolean omitMarkAsArrayFunction = true;
    public boolean getOmitMarkAsArrayFunction() {return omitMarkAsArrayFunction;}
    public void setOmitMarkAsArrayFunction(boolean value) {this.omitMarkAsArrayFunction=value;}

    private boolean omitDefaultLeafValuesInJs = false;
    public boolean getOmitDefaultLeafValuesInJs() {  return omitDefaultLeafValuesInJs; }
    public void setOmitDefaultLeafValuesInJs(boolean value) { this.omitDefaultLeafValuesInJs = value; }

    private LeafDefaultSet leafDefaultSet = null;
    public LeafDefaultSet getLeafDefaultSet() {  return this.leafDefaultSet; }
    public void setLeafDefaultSet(LeafDefaultSet value) { this.leafDefaultSet = value; }

    String objectResolverFunctionName = "resolveRefs";
    public String getObjectResolverFunctionName() { return objectResolverFunctionName; }
    public void setObjectResolverFunctionName(String value) { this.objectResolverFunctionName = value; }

    String leafDefaultResolverFunctionName = "_resolveLeafDefaults";
    public String getLeafDefaultResolverFunctionName() { return leafDefaultResolverFunctionName; }
    public void setLeafDefaultResolverFunctionName(String value) { this.leafDefaultResolverFunctionName = value; }

    String attachId2ArrayFunctionName="attachId2Array";
    public String getAttachId2ArrayFunctionName() { return attachId2ArrayFunctionName; }
    public void setAttachId2ArrayFunctionName(String value) { this.attachId2ArrayFunctionName = value; }
    public String getAttachId2ArrayJSFunction()
    {
        return "function " + getAttachId2ArrayFunctionName() +  "(id, arr) {arr." + idTag + "=id; arr." + isArrayIndicator + "='y'; return arr;}";
    }

    String markAsArrayFunctionName = "_a_";
    public String getMarkAsArrayFunctionName() { return markAsArrayFunctionName; }
    public void setMarkAsArrayFunctionName(String value) { markAsArrayFunctionName = value; }
    public String getMarkAsArrayJSFunction()
    {
        return Object2JsonJavascript.getMarkAsArrayJSFunction(getMarkAsArrayFunctionName());
    }

    String idTag = "_id_";
    String getIdTag() { return idTag; }
    public void setIdTag(String value) { idTag = value; }
    String referenceTag = "_ref_";
    String getReferenceTag()  { return referenceTag; }
    public void setReferenceTag(String value) { referenceTag = value; }
    boolean useReferences = false;
    public boolean getUseReferences()  { return useReferences; }
    public void setUseReferences(boolean value) { useReferences = value; }
    public final String NewLine = "\r\n";
    NodeExpander nodeExpander;
    public NodeExpander getNodeExpander() {return nodeExpander;}
    public void setNodeExpander(NodeExpander value) {this.nodeExpander=value;}
    public String toJson(Object o)
    {
        return toJson(o, getTypeAliaser());
    }

    public String toJson(Object o, TypeAliaser typeAliaser)
    {
       try
       {
           StringWriter sw = new StringWriter();
           writeAsJson(o, sw, typeAliaser);
           sw.flush();
           String str = sw.toString();
           sw.close();
           return str;
       }
       catch (IOException ioex)
       {
           throw new RuntimeException("io failed to write json", ioex);
       }
    }

    ExplorerFactory explorerFactory;
    public void setExplorerFactory(ExplorerFactory value) { explorerFactory = value; }
    public ExplorerFactory getExplorerFactory() { return explorerFactory; }

    private void writePropertyName(String propertyName, Writer writer) throws IOException
    {
        boolean escapeRequired = this.getAlwaysDoubleQuotePropertyNames() || (propertyName.indexOf('.') >= 0);
        if (escapeRequired) writer.write('\"');
        writer.write(propertyName);
        if (escapeRequired) writer.write('\"');
    }

    public boolean MoveAway(Object from, String propertyName, Object to, boolean isIndexed, Integer index)
    {
        try
        {
            return MoveAwayUnsafe(from, propertyName, to, isIndexed, index);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Mode away failed", ex);
        }
    }

    public  boolean MoveAwayUnsafe(Object from, String propertyName, Object to, boolean isIndexed, Integer index) throws IOException
    {
        ExploreStackFrame currentFrame = exploreStack.size()>0 ? exploreStack.peek():null;
        if (currentFrame != null)
        {
            if (currentFrame.propertyCount > 0) writer.write(", ");
            currentFrame.propertyCount++;
        }
        if (from != null && propertyName != null)
        {
            writePropertyName(propertyName, writer);
            writer.write(":");
        }
        ExploreStackFrame childFrame = new ExploreStackFrame();
        exploreStack.push(childFrame);
        writeIndent(writer, exploreStack);
        if (getUseReferences())
        {

            ObjectIDGenerator.IdResult idResult = idGenerator.getId(to);
            int objectid = idResult.id;
            boolean firstTime = idResult.isNew;
            if (firstTime)
            {
                // could be done like this ! (function() {var x=[1,2]; x.id="uuu";return x;})()
                if (!isIndexed)
                {
                    writer.write("{" + this.getIdTag() + ":" + objectid + ' ');
                    childFrame.propertyCount++;
                    childFrame.propertyCount += writeTypeAliasProperty(writer, to, typeAliaser, childFrame.propertyCount);

                }
                else
                {
                    // no need for type alias
                    writer.write(getAttachId2ArrayFunctionName() + "(" + objectid + ",[");
                }

            }
            else
            {
                writer.write("{" + this.getReferenceTag() + ":" + objectid);
                return false;
            }
        }
        else // !Use References
        {
            if (!isIndexed)
            {
                writer.write('{');
                // todo -- check this out ............
                childFrame.propertyCount += writeTypeAliasProperty(writer, to, typeAliaser, childFrame.propertyCount);
            }
            else
            {
                if (!getOmitMarkAsArrayFunction())
                {
                    writer.write(markAsArrayFunctionName);
                    writer.write("([");
                }
                else
                {
                    writer.write("[");
                }
            }
        }

        return true;
    }

    public  void MoveBack(Object from, String propertyName, Object to, boolean isIndexed)
    {
        try
        {
            if (!isIndexed) writer.write('}');
            else
            {
                writer.write(']');
                // is there a function wrapper ?
                if (!getOmitMarkAsArrayFunction() || getUseReferences()) writer.write(")");
            }
            exploreStack.pop();
            writeIndent(writer, exploreStack);
        }
        catch (IOException ioex)
        {
            throw new RuntimeException("failed to move back from " + from.getClass().getName() + "." + propertyName);
        }
    }

    public  void OnLeaf(Object from, String propertyName, Object to, Integer index)
    {
        try {
            OnLeafUnsafe(from,  propertyName,  to,  index);
        }
        catch (IOException ioex)
        {
            throw new RuntimeException("OnLeaf failed for " + from.getClass().getName() + "." + propertyName + "->" + to + " index=" + index);
        }
    }

    public  void OnLeafUnsafe(Object from, String propertyName, Object to, Integer index) throws IOException
    {
        //check for default leaf values
        if (!this.getOmitDefaultLeafValuesInJs() ||
                !isDefaultLeafValue(from, propertyName, to, getLeafDefaultSet()))
        {
            ExploreStackFrame currentFrame = exploreStack.peek();
            if (currentFrame.propertyCount > 0) writer.write(", ");
            currentFrame.propertyCount++;
            if (propertyName!=null)
            {
                writePropertyName(propertyName, writer);
                writer.write(":");
            }
            leafWriter.writeLeafValue(writer, to, propertyName);
        }

    }

    ObjectIDGenerator idGenerator = null;

class ExploreStackFrame { public int propertyCount;}

    public void writeAsJson(Object o, StringWriter writer)
    {
        writeAsJson(o, writer, null);
    }


    Stack<ExploreStackFrame> exploreStack = null;

    Writer writer;

    public void writeAsJson(Object o, StringWriter writer, TypeAliaser typeAliaser)
    {
        if (getLeafDefaultSet() != null && getOmitDefaultLeafValuesInJs() && getOmitMarkAsArrayFunction())
        {
            throw new RuntimeException("Leaf defaulting requires Array marker for js code");
        }
        this.writer = writer;
        //this.typeAliaser = typeAliaser;

         idGenerator = null;

        if (getUseReferences())
        {
            idGenerator=new ObjectIDGenerator();
        }
        exploreStack = new Stack<ExploreStackFrame>();
        Explorer explorerImpl = getExplorerFactory().create();
        ((Explorer)explorerImpl).setNodeExpander(getNodeExpander());

        explorerImpl.explore(o, this);
    }

    int  writeTypeAliasProperty(Writer writer, Object to, TypeAliaser typeAliaser, int currentFramePropertyCount) throws IOException
    {
        if (typeAliaser != null)
        {
            if (currentFramePropertyCount > 0) writer.write(',');
            writer.write(this.getTypeAliasProperty() + ":'" + typeAliaser.alias (to.getClass()) + "'");
            return 1;
        }
        else
        {
            return 0;
        }
    }

    //public Func<Object, String, Object, LeafDefaultSet, boolean> isDefaultLeafValue = isDefaultLeafValueDefault;

    protected boolean isDefaultLeafValue(Object from, String propertyName, Object leafValue, LeafDefaultSet LeafDefaultSet)
    {
        if ( LeafDefaultSet == null)
        {
            throw new RuntimeException("OmitDefaultLeafValuesInJs==true but LeafDefaultSet is unspecified - unable to determine what values are default !");
        }

        Class t = from.getClass();
        if (LeafDefaultSet.getType2Defaults().containsKey(t))
        {
            Defaults4Class defaults = LeafDefaultSet.getType2Defaults().get(t);
            if (!defaults.getPropertyName2DefaultValue().containsKey(propertyName))
            {
                return false;
            }
            Object defaultValue = defaults.getPropertyName2DefaultValue().get(propertyName);
            return (leafValue == defaultValue) || leafValue != null && leafValue.equals(defaultValue);
        }
        {
            // is this an error ?
            return false;
        }
    }

    void writeIndent(Writer writer, Stack<ExploreStackFrame> exploreStack) throws IOException
    {
        if (getIndentSize() != null)
        {
            writer.write(NewLine);
            int fullIndentSize = (getIndentSize().intValue()) * exploreStack.size();
            for (int done = 0; done < fullIndentSize; done++)
            {
                writer.write(' ');
            }
        }
    }

    private String isArrayIndicator = "_a_";

    public interface LeafWriter
    {
        public void writeLeafValue(Writer writer, Object to, String propertyName) throws IOException;
    }

    private LeafWriter defaultLeafWriter = new LeafWriter()
    {
        @Override
        public void writeLeafValue(Writer writer, Object to, String propertyName) throws IOException
        {
            if (to == null)
            {
                writer.write("null");
            }
            else if (to.getClass() == String.class || to.getClass() == Character.class)
            {
                writer.write("\"");
                writer.write(to.toString());
                writer.write("\"");
            }//todo simplify below
            else if (to.getClass() == Byte.class)
            {
                writer.write(to.toString());
            }
            else if (to.getClass() == Integer.class)
            {
                writer.write(to.toString());
            }
            else if (to.getClass() == Long.class)
            {
                writer.write(((Long)to).toString());
            }
            else if (to.getClass() == Float.class)
            {
                writer.write(((Float)to).toString());
            }
            else if (to.getClass() == Double.class)
            {
                writer.write(((Double)to).toString());
            }

            else if (to.getClass() == Boolean.class)
            {
                boolean bval = ((Boolean) to).booleanValue();
                writer.write(bval ? "true" : "false");
            }
            else if (to.getClass() == Short.class)
            {
                writer.write(((Short)to).toString());
            }
            else throw new RuntimeException("not implemented writeLeafValue:" + to.getClass().getName() + " " + propertyName);

        }
    };

    public LeafWriter getDefaultLeafWriter() {return defaultLeafWriter;}
    private LeafWriter leafWriter = getDefaultLeafWriter();
    public void setLeafWriter(LeafWriter value)
    {
        this.leafWriter = value;
    }
    public LeafWriter getLeafWriter()
    {
        return this.leafWriter;
    }


    private Integer indentSize=null;
    public Integer getIndentSize() { return indentSize; }
    public void setIndentSize(Integer value) { indentSize = value; }

    public String  getObjectResolverJS()
    {
        return Object2JsonJavascript.getObjectResolverJS(getObjectResolverFunctionName(), getIdTag(), getReferenceTag());
    }

    String typeAliasProperty = "_t_";
    public String getTypeAliasProperty() {return typeAliasProperty; }
    public void setTypeAliasProperty(String value) { this.typeAliasProperty = value; }

    public String getLeafDefaultResolverJS()
    {
        return getLeafDefaultResolverJS(getTypeAliaser());
    }

    public String getLeafDefaultResolverJS(TypeAliaser typeAliaser)
    {
        //switch off type aliasing

        String defaultsJS = toJson( getLeafDefaultSet().getAlias2Defaults(typeAliaser), null);
        String defaultsVarName = "_typeAlias2LeafDefaults_";

        String js =
        "var "+ defaultsVarName +" = " + defaultsJS + ";\r\n" +
         Object2JsonJavascript.ResolveLeafDefaultFunction;
        return js;
    }

}
