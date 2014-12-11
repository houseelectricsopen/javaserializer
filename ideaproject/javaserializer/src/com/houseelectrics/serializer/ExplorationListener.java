package com.houseelectrics.serializer;

/**
 * Created by robert on 29/11/2014.
 */
public interface ExplorationListener {
    /**
     * called to indicating imminent downward movement
     */
    public  boolean MoveAway(Object from, String propertyName, Object to, boolean isIndexed, Integer index);
    public  void MoveBack(Object from, String propertyName, Object to, boolean isIndexed);
    public  void OnLeaf(Object from, String propertyName, Object to, Integer index);

}
