package com.houseelectrics.serializer.test;
import java.util.*;
/**
 * Created by roberttodd on 01/12/2014.
 */
public class TestTreeNode
{
    public String name;

    public String getName()
    {
        return name;
    }

    public void setName(String value)
    {
        name = value;
    }

    public List<TestTreeNode> branches = new ArrayList<TestTreeNode>();

    public List<TestTreeNode> getBranches()
    {
        return branches;
    }

    /*creates a tree structure of specified depth and width*/
    public static TestTreeNode createTestHierarchy(int requiredDepth, int branchesPerParent)
    {
        List<TestTreeNode> nodes2Expand;
        nodes2Expand = new ArrayList<TestTreeNode>();
        TestTreeNode rootNode = new TestTreeNode();
        nodes2Expand.add(rootNode);
        for (int level = 0; level < requiredDepth; level++)
        {
            List<TestTreeNode> nextNodesToExpand = new ArrayList<TestTreeNode>();
            for (int done = 0; done < nodes2Expand.size(); done++)
            {
                TestTreeNode node = nodes2Expand.get(done);
                for (int bdone = 0; bdone < branchesPerParent; bdone++)
                {
                    TestTreeNode subNode = new TestTreeNode();
                    subNode.setName("level" + level + "_index" + bdone);
                    node.getBranches().add(subNode);
                    nextNodesToExpand.add(subNode);
                }
            }
            nodes2Expand = nextNodesToExpand;
        }
        return rootNode;
    }

    public static class TestTreeLeaf
    {
        String name;
        public String getName() { return name; }
        public void setName(String value) { name = value; }
}


}

