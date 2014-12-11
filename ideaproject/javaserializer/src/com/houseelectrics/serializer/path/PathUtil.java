package com.houseelectrics.serializer.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.houseelectrics.serializer.ExplorationListener;
import com.houseelectrics.serializer.ObjectExplorerImpl;
import com.houseelectrics.serializer.PropertyReflectionNodeExpander;

/**
 * Created by roberttodd on 04/12/2014.
 */
public class PathUtil
{

    public static class InstancePathElement
    {
        private boolean isIndexed;
        public boolean getIsIndexed() {return isIndexed;}
        public void setIsIndexed(boolean value) {this.isIndexed = value;}
    }

    public static class NamePathElement extends InstancePathElement
    {
        public String toString()
        {
            return name;
        }
        private String name;
        public String getName() {return name;}
        public void setName(String value){this.name = value; }
    }
    public static class IndexPathElement extends InstancePathElement
    {
        private int index;
        public int getIndex() { return index; }
        public void setIndex(int value) {this.index = value;}
    public String toString()
{
    return "[" + getIndex() + "]";
}
}

    private boolean found;
    public List<InstancePathElement> findSingleObjectPath(Object root, final Object object2Find)
    {
        this.found=false;
        ObjectExplorerImpl explorer = new ObjectExplorerImpl();
        final Stack<InstancePathElement> currentPath = new Stack<InstancePathElement>();

        ExplorationListener explorationListener = new ExplorationListener()
        {
            @Override
            public boolean MoveAway(Object from, String propertyName, Object to, boolean isIndexed, Integer index)
            {
                if (found) return false;
                InstancePathElement element;
                InstancePathElement thepeek = currentPath.size() > 0 ? currentPath.peek() : null;
                boolean parentIsIndexed = currentPath.size() > 0 && currentPath.peek().getIsIndexed();
                if (parentIsIndexed)
                {
                    IndexPathElement ielement = new IndexPathElement();
                    ielement.setIndex( index.intValue());
                    element = ielement;
                }
                else
                {
                    NamePathElement nElement = new NamePathElement();
                    nElement.setName( propertyName);
                    element = nElement;
                }
                element.setIsIndexed(isIndexed);
                currentPath.push(element);
                found = object2Find == to;
                return (!found);
            }

            @Override
            public void MoveBack(Object from, String propertyName, Object to, boolean isIndexed)
            {
                if (!found) currentPath.pop();
            }

            @Override
            public void OnLeaf(Object from, String propertyName, Object to, Integer index)
            {
            }
        };


        PropertyReflectionNodeExpander expander = new PropertyReflectionNodeExpander();
        expander.setExcludeReadOnlyProperties( true );
        explorer.setNodeExpander(expander);

        explorer.explore(root, explorationListener);
        List<InstancePathElement> result = null;
        if (found)
        {
            result = new ArrayList<InstancePathElement>(currentPath.size());
            for (int index = 0; index<currentPath.size(); index++)
            {
                result.add(currentPath.get(index));
            }
            // remove the first element because it is to the root
            result.remove(0);
        }
        return result;
    }

    public String toString(List<InstancePathElement> path)
    {
        StringBuilder sb = new StringBuilder();
        for (int done =0 ; done<path.size(); done++)
        {
            if (done != 0) sb.append('.');
            sb.append(path.get(done));
        }
        return sb.toString();
    }

}
