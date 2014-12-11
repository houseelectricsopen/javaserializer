package com.houseelectrics.serializer;

/**
 * Created by roberttodd on 02/12/2014.
 */
public class DefaultJsonMapping implements JsonMapping
{

    private Json2Object json2Object;
    private Object2Json object2Json;
    public DefaultJsonMapping()
    {
        json2Object = new Json2Object();
        //json2Object.setToUseFields();
        object2Json = new Object2Json();
        object2Json.setNodeExpander( new FieldReflectionNodeExpander());
        object2Json.setIndentSize( 2 );

        //todo json 2 object should understand TypeAliaser
        object2Json.setTypeAliaser(
                new TypeAliaser()
                {
                    @Override
                    public String alias(Class type)
                    {
                        return type.getName();
                    }
                }
        );

        object2Json.setTypeAliasProperty(json2Object.getTypeSpecifier());
    }

    public Json2Object getJson2Object()
    {
        return json2Object;
    }

    public Object2Json getObject2Json()
    {
        return object2Json;
    }

}
