package com.houseelectrics.serializer.test.path;

import java.util.ArrayList;
import java.util.List;

import com.houseelectrics.serializer.path.PathUtil;
import org.junit.*;

/**
 * Created by roberttodd on 04/12/2014.
 */
public class PathTest
{

    public static class PathTestType
    {
        public static class Subtype
        {
            public static class MoreSub {
                private Object theItem;
                public Object getTheItem() {return theItem;}
                public void setTheItem(Object value) {this.theItem=value;}
            }
            public Subtype() { theSubSubs = new ArrayList<MoreSub>(); }
            private List<MoreSub> theSubSubs;
            public List<MoreSub> getTheSubSubs() { return theSubSubs; }
            public void setTheSubSubs(List<MoreSub> value) {this.theSubSubs=value;}
        }
        public Subtype theSub;
        public Subtype getTheSub() { return theSub; }
        public void setTheSub(Subtype value) {this.theSub = value;}
        public PathTestType() { theSub = new Subtype(); }

    }
    @Test
    public void testPath()
    {
        PathTestType testItem = new PathTestType();
        List<PathTestType.Subtype.MoreSub> subsubs = testItem.getTheSub().getTheSubSubs();
        PathTestType.Subtype.MoreSub moreSub;
        Object object2find = new Object();

        moreSub = new PathTestType.Subtype.MoreSub();
        moreSub.setTheItem(new Object());
        subsubs.add(moreSub);

        moreSub = new PathTestType.Subtype.MoreSub();
        moreSub.setTheItem(object2find);
        subsubs.add(moreSub);

        moreSub = new PathTestType.Subtype.MoreSub();
        moreSub.setTheItem( new Object());
        subsubs.add(moreSub);

        PathUtil util = new PathUtil();

        List<PathUtil.InstancePathElement> path = util.findSingleObjectPath(testItem, object2find);
        //path is TheSub.TheSubSubs.[1]
        String strPath = util.toString(path);
        System.out.println("path= " + strPath);
        Assert.assertEquals( "expected path", 4, path.size());
        String expectedPathString = "TheSub.TheSubSubs.[1].TheItem";
        Assert.assertEquals("expected path string ", expectedPathString, strPath);

    }

}
