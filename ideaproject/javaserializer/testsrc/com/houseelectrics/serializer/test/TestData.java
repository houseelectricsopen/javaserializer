package com.houseelectrics.serializer.test;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by roberttodd on 01/12/2014.
 */
public class TestData
{
    public Sub sub = new Sub();
    public Sub getTheSub()  { return sub; }
    public void setTheSub(Sub value) {this.sub=value;  }
    public class Sub
    {
        public SubSub subSub = new SubSub();
        public SubSub getTheSubSub() { return subSub; }
        public class SubSub
        {
            public String greeting = "hi";
            public String getGreeting()  {return greeting;}
            public void setGreeting(String value) { this.greeting = value; }
            public int ageYears = 32;
            public int getAgeYears() {return ageYears;}
            public double heightMetres = 1.23;
            public double getHeightMetres() { return heightMetres; }
            public String[] seasons = { "Spring", "Summer", "Autumn", "Winter" };
            public String[] getSeasons() { return seasons; }
            private List<Integer> goodYears = new ArrayList<Integer> ();
            public List<Integer> getGoodYears() {return goodYears;}
            public SubSub()
            {
                goodYears.add(1966);
                goodYears.add(1973);
            }
        }
    }

}
