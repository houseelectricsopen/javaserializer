package com.houseelectrics.serializer.test;
import com.houseelectrics.serializer.FieldReflectionNodeExpander;
import org.junit.*;
import com.houseelectrics.serializer.PropertyReflectionNodeExpander;
import com.houseelectrics.serializer.NodeExpander;
import com.houseelectrics.serializer.NodeExpansionListener;


public class TestReflectiveNodeExpander
{
    public class Data
    {
        public String greeting = "hi";
        public String getGreeting() {return greeting;}
        public void setGreeting(String value) {this.greeting = value;}
    }

    @Test
    public void simpleExpandByProperty()
    {
        simpleExpand(new PropertyReflectionNodeExpander(), "Greeting");
    }

    @Test
    public void simpleExpandByField()
    {
        simpleExpand(new FieldReflectionNodeExpander(), "greeting");
    }

    boolean foundhi = false;

    public void simpleExpand(NodeExpander nodeExpander, final String propname)
    {
        final Data data = new Data();
        foundhi = false;

        NodeExpansionListener l = new NodeExpansionListener()
        {
            public void OnChildNode(Object from, String name, Object to)
            {
                if (name.equals(propname) && to.equals(data.getGreeting()))
                {
                    foundhi = true;
                }
            }
        };

        nodeExpander.expand(data, l);

        Assert.assertTrue("expected to find greeting", foundhi);
    }


}

