package com.houseelectrics.serializer;

/**
 * Created by roberttodd on 01/12/2014.
 */
public class DefaultFinder
{
    public LeafDefaultSet getDefaultsForAllLinkedObjects(Object root, NodeExpander nodeExpander)
    {
        final LeafDefaultSet result = new LeafDefaultSet();

        ObjectExplorerImpl explorer = new ObjectExplorerImpl();

        ExplorationListener listener = new ExplorationListener()
        {
            @Override
            public boolean MoveAway(Object from, String propertyName, Object to, boolean isIndexed, Integer index)
            {
                return (isIndexed || (to != null && !result.getType2Defaults().containsKey(to.getClass())));
            }

            @Override
            public void MoveBack(Object from, String propertyName, Object to, boolean isIndexed)
            {
            }

            @Override
            public void OnLeaf(Object from, String propertyName, Object to, Integer index)
            {
                Defaults4Class defaults = null;
                if (from==null) {return;}
                Class fromType = from.getClass();
                if (!result.getType2Defaults().containsKey(fromType))
                {
                    defaults = new Defaults4Class();
                    defaults.setFullClassName(fromType.getName());
                    result.getType2Defaults().put(fromType, defaults);
                }
                else
                {
                    defaults = result.getType2Defaults().get(fromType);
                }
                defaults.getPropertyName2DefaultValue().put(propertyName, to);
            }
        };

        explorer.setNodeExpander(nodeExpander);
        explorer.explore(root, listener);
        return result;
    }
}
