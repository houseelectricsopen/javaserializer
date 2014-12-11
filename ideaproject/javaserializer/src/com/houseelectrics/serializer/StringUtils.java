package com.houseelectrics.serializer;

/**
 * Created by roberttodd on 29/11/2014.
 */
public class StringUtils
{

    public interface Matcher
    {
        boolean match(char c, String str, int pos);
    }

    public static Integer firstCharPosition(String str, Matcher matcher, Integer startPos, Integer endPos)
{
    if (endPos == null) endPos = str.length();
    for (int pos = startPos; pos < endPos + 1; pos++)
    {
        if (matcher.match(str.charAt(pos), str, pos)) return pos;
    }
    return null;
}

    public interface CharPredicate
    {
        public boolean match(char c);
    }

    public static Integer firstCharPosition(String str, CharPredicate match)
    {
        return firstCharPosition(str, match, 0, 0);
    }

    public static Integer firstCharPosition(String str, CharPredicate match, int startPos, Integer endPos)
{
    if (endPos == null) endPos = str.length();
    for (int pos = startPos; pos < endPos+1; pos++)
    {
        if (match.match (str.charAt(pos))) return pos;
    }
    return null;
}

    public static Integer lastCharPosition(String str, CharPredicate match)
    {
        return lastCharPosition(str, match, null, 0);
    }


    public static Integer lastCharPosition(String str, CharPredicate match, Integer startPos, int endPos)
{
    if (startPos == null) startPos = str.length();
    for (int pos = startPos.intValue()-1; pos >= endPos; pos--)
    {
        if (match.match(str.charAt(pos))) return pos;
    }
    return null;
}


}
