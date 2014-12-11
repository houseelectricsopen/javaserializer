package com.houseelectrics.serializer.test;

/**
 * Created by roberttodd on 30/11/2014.
 */
public class AllPrimitiveLeafTypes
{
    public AllPrimitiveLeafTypes()
    {
        setALong(123);
        setALongRef(234l);
        setAInt(324);
        setAIntRef(345);
        setAShort((short)124);
        setAChar('c');
        setAString("hi");
        setAByte((byte)127);
    }

    public long aLong = 23;
    public Long aLongRef = 789l;
    public int aInt = 463;
    public Integer aIntRef = 247;
    public short aShort = 124;
    public char aChar = 'c';
    public String aString, aString2 = "hi";
    public byte aByte=126;

    private long _aLong = 23;
    private Long _aLongRef = 789l;
    private int _aInt = 463;
    private Integer _aIntRef = 247;
    private short _aShort = 124;
    private char _aChar = 'c';
    private String _aString = "hi";
    private byte _aByte;



    public static String[] testFieldExpressions = { "aLong", "aLongRef", "aInt", "aIntRef", "aShort", "aChar", "aString", "aString2", "aByte" };
    public static Object[] testExpectedFieldValues(AllPrimitiveLeafTypes template)
    {
        return new Object[] { template.aLong, template.aLongRef, template.aInt, template.aIntRef, template.aShort, template.aChar, template.aString, template.aString2, template.aByte };
    }
    public static String[] testPropertyExpressions = { "ALong", "ALongRef", "AInt", "AIntRef", "AShort", "AChar", "AString", "AByte" };
    public static Object[] testExpectedPropertyValues(AllPrimitiveLeafTypes template)
    {
        return new Object[] { template.getALong(), template.getALongRef(), template.getAInt(), template.getAIntRef(),
                 template.getAShort(), template.getAChar(), template.getAString(), template.getAByte() };
    }


    public long getALong() {return _aLong;}
    public void setALong(long value) { this._aLong = value; }

    public Long getALongRef()
    {
        return _aLongRef;
    }

    public void setALongRef(Long _aLongRef)
    {
        this._aLongRef = _aLongRef;
    }

    public int getAInt()
    {
        return _aInt;
    }

    public void setAInt(int _aInt)
    {
        this._aInt = _aInt;
    }

    public Integer getAIntRef()
    {
        return _aIntRef;
    }

    public void setAIntRef(Integer _aIntRef)
    {
        this._aIntRef = _aIntRef;
    }

    public short getAShort()
    {
        return _aShort;
    }

    public void setAShort(short _aShort)
    {
        this._aShort = _aShort;
    }

    public char getAChar()
    {
        return _aChar;
    }

    public void setAChar(char _aChar)
    {
        this._aChar = _aChar;
    }

    public String getAString()
    {
        return _aString;
    }

    public void setAString(String _aString)
    {
        this._aString = _aString;
    }

    public void setAByte(byte value) {this._aByte = value;}
    public byte getAByte() {return _aByte;}

}
