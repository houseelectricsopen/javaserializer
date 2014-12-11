package com.houseelectrics.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roberttodd on 30/11/2014.
 */
public class Defaults4Class
{
    public String fullClassName;
    public String getFullClassName() {  return fullClassName; }
    public void setFullClassName(String value) {this.fullClassName=value; }
    // Map of related properties
    //private Dictionary<string, Defaults4Class> propertyName2NodeType = new Dictionary<string,Defaults4Class>();
    //public Dictionary<string, Defaults4Class> PropertyName2NodeType { get {return propertyName2NodeType;} }
    public Map<String, Object> propertyName2DefaultValue = new HashMap<String, Object>();
    public Map<String, Object> getPropertyName2DefaultValue() {return propertyName2DefaultValue;}
}
