package com.houseelectrics.serializer;

/**
 * Created by roberttodd on 30/11/2014.
 */
public class Object2JsonJavascript
{
    public final static String lineReturn = "\r\n";
    //todo parameterise resolve leaf default function based on these
    //#pragma warning disable  0414
    String visitedProperty = "_visited_";
    //#pragma warning restore  0414
    public final String Type2DefaultsMapPlaceholder = "_type2Defaults__";
    public final static String ResolveLeafDefaultFunction =
    "function _resolveLeafDefaults(o)" + lineReturn +
    "{" + lineReturn +
    "    if (o._visited_) return o;" + lineReturn +
    "    var typeAlias = o._t_;" + lineReturn +
    "    if (!typeAlias) return o;" + lineReturn +
    "    for (p in o)" + lineReturn +
    "    {" + lineReturn +
    "        if (p=='_t_') continue;" + lineReturn +
    "        var val = o[p];" + lineReturn +
    "        // if its an array iterate and explore" + lineReturn +
    "        if (typeof(val._a_)!='undefined')" + lineReturn +
    "        {" + lineReturn +
    "            for (var done=0; done<val.length; done++)" + lineReturn +
    "            {" + lineReturn +
    "                _resolveLeafDefaults(val[done]);" + lineReturn +
    "            }" + lineReturn +
    "            continue;" + lineReturn +
    "        }" + lineReturn +
    "        else" + lineReturn +
    "        {" + lineReturn +
    "            if (val._t_)" + lineReturn +
    "            {" + lineReturn +
    "                _resolveLeafDefaults(o[p]);" + lineReturn +
    "            }" + lineReturn +
    "" + lineReturn +
    "        }" + lineReturn +
    "    }" + lineReturn +
    "    var _type2Defaults_ = _typeAlias2LeafDefaults_[typeAlias].propertyName2DefaultValue;" + lineReturn +
    "    // do the defaults" + lineReturn +
    "    for (var p in _type2Defaults_)" + lineReturn +
    "    {" + lineReturn +
    "        if (typeof(o[p])==='undefined') {o[p]=_type2Defaults_[p];}" + lineReturn +
    "    }" + lineReturn +
    "    o._visited_=true;" + lineReturn +
    "    return o;" + lineReturn +
    "}";

    public static String getObjectResolverJS(String ObjectResolverFunctionName, String IdTag, String ReferenceTag)
    {
        String js =
        "function " + ObjectResolverFunctionName + "(o, ref2o) " + lineReturn +
        "{" + lineReturn +
        "    if (!o) return;" + lineReturn +
        "    if (!ref2o) ref2o = {};" + lineReturn +
        "    function perSubO(o, subo, ref2o, suboKey)" + lineReturn +
        "    {" + lineReturn +
        "        if (subo && subo._ref_)" + lineReturn +
        "        {" + lineReturn +
        "            o[suboKey]=ref2o[subo._ref_];" + lineReturn +
        "        }" + lineReturn +
        "        else if (subo && subo._id_)" + lineReturn +
        "        {" + lineReturn +
        "            ref2o[subo._id_]=subo;" + lineReturn +
        "            resolveRefs(subo, ref2o);" + lineReturn +
        "        }" + lineReturn +
        "    }" + lineReturn +
"//is array ?" + lineReturn +
"//this condition is superfluous since this sam code write the array !" + lineReturn +
        "    if ((o._a_) /*|| (Array && Array.isArray && Array.isArray(o))*/)" + lineReturn +
        "    {" + lineReturn +
        "        for (var i =0; i<o.length; i++)" + lineReturn +
        "        {" + lineReturn +
        "            var subo=o[i];" + lineReturn +
        "            perSubO(o, subo, ref2o, i);" + lineReturn +
        "        }" + lineReturn +
        "    }" + lineReturn +
        "    else" + lineReturn +
        "    {" + lineReturn +
        "        for (var p in o)" + lineReturn +
        "        {" + lineReturn +
        "            var subo = o[p];" + lineReturn +
        "            perSubO(o, subo, ref2o, p);" + lineReturn +
        "        }" + lineReturn +
        "    }" + lineReturn +
        "    return o;" + lineReturn +
        "}"
        ;
        js = js.replaceAll("_id_", IdTag);
        js = js.replaceAll("_ref_", ReferenceTag);
        return js;
    }

    public static String getMarkAsArrayJSFunction(String MarkAsArrayFunctionName)
    {
        return
        "//mark array so browser specific testing is not required" + lineReturn +
        "function " + MarkAsArrayFunctionName + "(arr) {arr._a_='y'; return arr;}";
    }



}
