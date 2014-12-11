package com.houseelectrics.serializer;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by roberttodd on 30/11/2014.
 */
public class ObjectExplorerImpl implements Explorer, NodeExpansionListener
{
    NodeExpander nodeExpander;
    public NodeExpander getNodeExpander() { return nodeExpander;}
    public void setNodeExpander(NodeExpander value)  { this.nodeExpander = value;}
    ExplorationListener listener=null;
    /** carries out exploration
     if o is a leaf leaf is called
     if o is a standard object down is called the item is expanded anf then up is called
     if o is a dictionary or indexed object (e.g. array) every contained object is treated as a
     leaf or standard object
     *
     * determination of leaf is in isLeaf and is based on c# properties
     *
     * determination if indexed type is in getEnumeratorIfIndexedType
     *
     * determination of map type is in toDictionary
     **/
    public void explore(Object o, ExplorationListener listener)
    {
        if (getNodeExpander() == null)
            throw new RuntimeException("explore is not possible unless a node expander is specified");
        this.listener = listener;
        OnChildNode(null, null, o);
    }
        public void OnChildNode(Object from, String name, Object to)
{
            Iterator en = null;
            Class toType= to==null?null:to.getClass();
            Map asDictionary=null;
            if (isLeaf(to, toType))
            {
                listener.OnLeaf(from, name, to, null);
            }
            else if (null!=(asDictionary=toDictionary(to, toType)))
            {
                boolean doExpand =
                        listener.MoveAway(from, name, to, false, null);
                en = asDictionary.keySet().iterator();
                for (int done = 0; doExpand && en.hasNext(); done++)
                {
                    Object oKey = en.next();
                    String strKey = dictionaryKey2String(oKey);
                    Object oVal = asDictionary.get(oKey);
                    Class oValType = oVal == null ? null : oVal.getClass();
                    if (isLeaf(oVal, oValType))
                        listener.OnLeaf(to, strKey, oVal, null);
                    else
                    {
                        boolean doSubExpand =  listener.MoveAway(to, strKey, oVal, false, done);
                        if (doSubExpand) getNodeExpander().expand(oVal, this);
                        listener.MoveBack(from, strKey, oVal, false);
                    }
                }
                listener.MoveBack(from, name, to, false);
            }
            else if (null!=(en=getEnumeratorIfIndexedType(to)))
            {
                boolean doExpand =
                        listener.MoveAway(from, name, to, true, null);

                for  (int done=0;doExpand && en.hasNext();done++)
                {
                    Object oVal = en.next();
                    if (isLeaf(oVal, oVal==null?null:oVal.getClass()))
                        listener.OnLeaf(to, null, oVal, null);
                    else
                    {
                        boolean doSubExpand = listener.MoveAway(null, null, oVal, false, done);
                        if (doSubExpand) getNodeExpander().expand(oVal, this);
                        listener.MoveBack(null, null, oVal, false);
                    }
                }
                listener.MoveBack(from, name, to, true);
            }
            else
            {
                boolean doExpand = listener.MoveAway(from, name, to, false, null);
                if (doExpand) getNodeExpander().expand(to, this);
                listener.MoveBack(from, name, to, false);
            }

    }

    Iterator getEnumeratorIfIndexedType(Object o)
    {
        if (isArray(o, o.getClass()))
        {
            return Arrays.asList((Object [])o).iterator();

        }
        else if (List.class.isAssignableFrom(o.getClass()))
        {
            return ((List)o).iterator();
        }
        else return null;
    }

    private boolean isLeaf(Object o, Class theclass)
    {
        if (o == null) { return true; }
        if (theclass.isPrimitive()) return true;
        if (theclass==String.class) return true;
        if (theclass==Character.class) return true;
        if (Number.class.isAssignableFrom(theclass)) return true;
/*        if (theclass==Integer.class) return true;
        if (theclass==Long.class) return true;
        if (theclass==Short.class) return true;
*/
        return false;
    }

    private boolean isArray(Object o, Class theclass)
    {
        if (o == null) { return false; }
        return theclass.isArray();
    }

    private boolean isList(Object o, Class theclass)
    {
        //return (theclass.IsGenericType &&
        //        (theclass.GetGenericTypeDefinition() == typeof(List<>)));
        return List.class.isAssignableFrom(theclass);
    }

    private Map toDictionary(Object o, Class theclass)
    {
        boolean isMap = o != null && Map.class.isAssignableFrom(theclass);
        if (isMap)
        {
            return (Map) o;
        }
        else
        {
            return null;
        }
    }
    String dictionaryKey2String(Object key)
    {
        String str = key.toString();
        return str;
    }


}
