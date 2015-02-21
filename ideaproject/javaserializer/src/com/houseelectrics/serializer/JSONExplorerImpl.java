package com.houseelectrics.serializer;

/**
 * Created by roberttodd on 29/11/2014.
 */
public class JSONExplorerImpl
{
    public final String JavaScriptNullDesignator = "null";

    final char ObjectStart = '{';
    final char ObjectEnd = '}';


    Integer firstNonWhiteSpaceCharacterIndex(String str)
    {
        return firstNonWhiteSpaceCharacterIndex(str, 0, null);
    }

    Integer firstNonWhiteSpaceCharacterIndex(String str, int startPos, Integer endPos)
{
    StringUtils.CharPredicate matcher= new StringUtils.CharPredicate()
    {
        public boolean match(char c)
        {
            return !Character.isWhitespace(c);
        }
    };
    return StringUtils.firstCharPosition(str, matcher, startPos, endPos);
}

    Integer lastNonWhiteSpaceCharacterIndex(String str)
{
    StringUtils.CharPredicate matcher= new StringUtils.CharPredicate()
    {
        public boolean match(char c)
        {
            return !Character.isWhitespace(c);
        }
    };
    return StringUtils.lastCharPosition(str, matcher);

}

    public void explore(String json, JsonExploreListener listener)
    {
        Integer firstNWSCharIndex = firstNonWhiteSpaceCharacterIndex(json);
        Integer lastNonWSCharIndex = lastNonWhiteSpaceCharacterIndex(json);
        if (firstNWSCharIndex == null || lastNonWSCharIndex == null || json.charAt(firstNWSCharIndex.intValue()) != ObjectStart || json.charAt(lastNonWSCharIndex.intValue()) != ObjectEnd)
        {
            throw new RuntimeException("expected json to start with " + ObjectStart +" and end with " + ObjectEnd);
        }
        listener.JsonStartObject(null, 0);
        explore(json, firstNWSCharIndex.intValue() + 1, lastNonWSCharIndex.intValue(), listener);
    }


    //look for a propertyName terminated by :
    //look for a leaf value or object start
    // or continuation , or object end

    Integer readEscapedQuotedString(String json, int valueStartPos, char quoteChar, int to, StringBuilder result)
{
    //StringBuilder sb = new StringBuilder();
    int pos = valueStartPos;
    for (; pos < to; pos++)
    {
        char c = json.charAt(pos);
        if (c == '\\')
        {
            pos++;
            if (pos >= to) break;
            else c = json.charAt(pos);
        }
        else
        {
            if (c == quoteChar) break;
        }
        result.append(c);
    }

    if (pos == to) return null;
    else return pos;
}

    interface TerminationListener
    {
        void terminate(int position);
    }

    protected int exploreFunctionCall(String json, int from, int to, final JsonExploreListener listener,
                                     String functionName)
{
    TerminationListener terminator = new TerminationListener()
    {
        @Override
        public void terminate(int p)
        {
            listener.JsonEndFunction(p);
        }
    };

    return exploreList(json, from, to, listener, ')', terminator, "function call");
}

    protected int exploreArray(String json, int from, int to, final JsonExploreListener listener)
{
    TerminationListener terminator = new TerminationListener()
    {
        @Override
        public void terminate(int p)
        {
            listener.JsonEndArray(p);
        }
    };

    return exploreList(json, from, to, listener, ']', terminator, "array");
}

    protected int exploreList(String json, int from, int to, JsonExploreListener listener, char terminatingChar,
                              TerminationListener terminator, String listType)
{
    // read arguments
    for (int pos = from+1; pos <= to; pos++)
    {
        Integer nextPos =  firstNonWhiteSpaceCharacterIndex(json, pos, to);
        if (nextPos == null)
        {
            String strError = "expected whitespace char after char " + pos;
            throw new RuntimeException(strError);
        }
        pos = nextPos.intValue();
        char c = json.charAt(pos);
        if (c==',')
        {
            //throw new Exception("multiple javascript list items not supported");
            continue;
        }
        if (c == terminatingChar)
        {
            //listener.JsonEndFunction(pos);
            terminator.terminate(pos);
            return pos;
        }
        else if (c == ObjectStart)
        {
            listener.JsonStartObject(/*currentPropertyName*/null, pos);
            pos = explore(json, pos + 1, to, listener, null/*currentPropertyName*/);
        }
        else // process a leaf !
        {
            pos = readNonObject(json, pos, to, listener, null);
        }
    }
    throw new RuntimeException(listType + " not terminated");
}

    boolean isQuoteChar(char cin)
    {
        return cin == '"' || cin == '\'';
    }


    protected int readNonObject(String json, int valueStartPos, int to, JsonExploreListener listener, String currentPropertyName)
{
    int pos=-1;
    Integer valueEndPos;
    //Predicate<char> valueEndCondition;

    //Predicate<char> isQuoteChar = cin => cin == '"' || cin == '\'';
    StringUtils.Matcher valueEndCondition;
    //Predicate<char> isQuoteChar = cin=> cin=='"' || cin == '\'';
    char c = json.charAt(valueStartPos);
    boolean isQuoted = isQuoteChar(c);
    String value = null;
    boolean isFunctionCall = false;
    boolean isArray = false;
    if (!isQuoted)
    {
        valueEndCondition = new StringUtils.Matcher()
        {
            public boolean match(char cin, String strin, int posin)
            {
                return Character.isWhitespace(cin) || cin == ','
                        || cin == ObjectEnd || cin == '(' || cin==')' || cin==']';
            }
        };

        valueEndPos = StringUtils.firstCharPosition(json, valueEndCondition, valueStartPos, to);

        if (valueEndPos != null)
        {
            value = json.substring(valueStartPos, valueEndPos.intValue() /*- valueStartPos*/);
            // is this a function start ?
            char lastchar = json.charAt(valueEndPos.intValue());
            if (lastchar == '(')
            {
                listener.JsonStartFunction(value, valueEndPos.intValue(), currentPropertyName);
                isFunctionCall = true;
                pos = exploreFunctionCall(json, valueEndPos.intValue(), to, listener, value);
            }
            else if (value.charAt(0) == '[')
            {
                listener.JsonStartArray(currentPropertyName, valueStartPos);
                isArray = true;
                pos = exploreArray(json, valueStartPos, to, listener);
            }
            if (value.equals(JavaScriptNullDesignator)) value = null;
        }
    }
    else
    {
        // this is wrong - whabout double, triple escaping !
        // read escaped quoted value
        // todo - remove object creation !
        StringBuilder sbValue = new StringBuilder();
        valueEndPos = readEscapedQuotedString(json, valueStartPos + 1, c, to, sbValue);
        //valueEndCondition = (cin, strin, posin) => isQuoteChar(cin) && !(posin>0 && strin[posin-1]=='\\' );
        //valueStartPos++;
        if (valueEndPos != null) value = sbValue.toString();
    }

    if (null == valueEndPos)
    {
        String strError = "no end  found for property " + currentPropertyName + " at position " + valueStartPos+ " in json " +  json;
        throw new RuntimeException(strError);
    }

    // check here if it is a function call
    if (!isFunctionCall && !isArray)
    {
        listener.JsonLeaf(currentPropertyName, value, isQuoted);
        pos = valueEndPos.intValue() - 1; // point to the last char processed
    }
    if (pos == -1) throw new RuntimeException("pos not assigned correctly");
    if (isQuoted) pos++;
    return pos;
}

    public int explore(String json, int from, int to, JsonExploreListener listener
                       )
    {
        return explore(json, from, to, listener, null);
    }

    public int explore(String json, int from, int to, JsonExploreListener listener,
                       String currentPropertyName)
    {
        int pos = from;
        for (; pos <= to; pos++)
        {
            StringUtils.Matcher matcher;
            matcher = new StringUtils.Matcher()
            {
                public boolean match(char cin, String str, int pos)
                {
                      return cin != ',' && !Character.isWhitespace(cin);
                };
            };

            Integer nextPos =    StringUtils.firstCharPosition(json, matcher, pos, to); //firstNonWhiteSpaceCharacterIndex(json, pos, to);
            if (nextPos == null)
            {
                String strError = "expected whitespace char after char " +pos;
                throw new RuntimeException(strError);
            }
            pos = nextPos.intValue();
            char c = json.charAt(pos);
            if (c == '}')
            {
                listener.JsonEndObject(pos);
                return pos;
            }
            // read a property Name

            matcher = new StringUtils.Matcher()
            {
                public boolean match(char cin, String str, int pos)
                {
                    return cin == ':';
                };
            };

            Integer colonIndex = StringUtils.firstCharPosition(json, matcher, pos, to);

            if (null == colonIndex)
            {
                throw new RuntimeException("no end found to property name at char "+ pos + " property starts with " + json.substring(pos) + " in json " +   json.substring(from));
            }

            StringUtils.CharPredicate nonWhitespaceOrColon =
                    new StringUtils.CharPredicate()
                    {
                        @Override
                        public boolean match(char c)
                        {
                            return !Character.isWhitespace(c) && c!=':';
                        }
                    };

            int propertyNameEnd = StringUtils.lastCharPosition(json, nonWhitespaceOrColon, colonIndex, 0);
            // propertyNameEnd
            currentPropertyName = json.substring(pos, propertyNameEnd+1 /*- pos*/);
            if (currentPropertyName.charAt(0) == '"') currentPropertyName = currentPropertyName.substring(1);
            if (currentPropertyName.endsWith("\"")) currentPropertyName = currentPropertyName.substring(0, currentPropertyName.length() - 1);

            pos = colonIndex + 1;
            Integer valueStartPos = firstNonWhiteSpaceCharacterIndex(json, pos, to);
            if (valueStartPos == null)
            {
                String strError = "no value found for property " + currentPropertyName + " at position " + pos + " in json " + json;
                throw new RuntimeException(strError);
            }
            c = json.charAt(valueStartPos.intValue());
            if (c == ObjectStart)
            {
                listener.JsonStartObject(currentPropertyName, valueStartPos.intValue());
                pos = explore(json, valueStartPos.intValue() + 1, to, listener, currentPropertyName);
            }
            else // read a non object
            {
                pos = readNonObject(json, valueStartPos.intValue(), to, listener, currentPropertyName);
            }

        }
        return pos;
    }


}
