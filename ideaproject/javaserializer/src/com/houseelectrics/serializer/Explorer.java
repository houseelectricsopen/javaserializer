package com.houseelectrics.serializer;

/**
 * explore the root object supplied - the root is considered be at the top -
 * callback <b>down</b> is called prior to moving down into a related object.
 * Downward movement can be vetoed by down returning false
 */
public interface Explorer
{
    void explore(Object root, ExplorationListener listener);
    public NodeExpander getNodeExpander();
    public void setNodeExpander(NodeExpander nodeExpander);
}
