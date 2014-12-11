package com.houseelectrics.serializer;

import javax.print.DocFlavor;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roberttodd on 02/12/2014.
 */
public class TypeConverter
{

    public interface Converter
    {
        public Object convert(Object o, Class destinationType);
        public boolean canConvert(Object o, Class destinationType);
    }

    public static abstract class PrimitiveTypeParser implements Converter
    {
        //public abstract Object parse(String str);
        public Class type;
        public Class primitiveType;
        public boolean canConvert(Object o, Class destinationType)
        {
            return  (destinationType == type || destinationType == primitiveType) &&
                    (o.getClass() == String.class || o.getClass()==type || o.getClass()==destinationType);
        }
        public Object convert(Object o, Class destinationType)
        {
            if (o.getClass()==type || o.getClass()==primitiveType) return o;
            return parse((String)o);
        }
        public abstract Object parse(String str);

    }

    List<Converter> converters = new ArrayList<Converter>();

    public void add(Converter converter)
       {
           this.converters.add(converter);
       }
    public TypeConverter()
    {
        PrimitiveTypeParser sp;
        sp = new PrimitiveTypeParser()
        {
            @Override
            public Object parse(String str)
            {
                return Short.parseShort(str);
            }
        };
        sp.type=Short.class;
        sp.primitiveType = Short.TYPE;
        add(sp);

        sp = new PrimitiveTypeParser()
        {   @Override
            public Object parse(String str)
            {
                return Integer.parseInt(str);
            }
        };
        sp.type=Integer.class;
        sp.primitiveType = Integer.TYPE;
        add(sp);

        sp = new PrimitiveTypeParser()
        {   @Override
            public Object parse(String str)
            {
                return Long.parseLong(str);
            }
        };
        sp.type=Long.class;
        sp.primitiveType = Long.TYPE;
        add(sp);

        sp = new PrimitiveTypeParser()
        {   @Override
            public Object parse(String str)
            {
                return Float.parseFloat(str);
            }
        };
        sp.type=Float.class;
        sp.primitiveType = Float.TYPE;
        add(sp);

        sp = new PrimitiveTypeParser()
        {   @Override
            public Object parse(String str)
            {
                return Double.parseDouble(str);
            }
        };
        sp.type=Double.class;
        sp.primitiveType = Double.TYPE;
        add(sp);

        sp = new PrimitiveTypeParser()
        {   @Override
            public Object parse(String str)
            {
                return str.charAt(0);
            }
        };
        sp.type=Character.class;
        sp.primitiveType = Character.TYPE;
        add(sp);

        sp = new PrimitiveTypeParser()
        {   @Override
            public Object parse(String str)
            {
                return Byte.parseByte(str);
            }
        };
        sp.type=Byte.class;
        sp.primitiveType = Byte.TYPE;
        add(sp);

        sp = new PrimitiveTypeParser()
        {   @Override
            public Object parse(String str)
            {
                boolean result=false;
                if (str!=null && str.length()>0)
                {
                    if (str.charAt(0)=='y' || str.charAt(0)=='Y' || str.charAt(0)=='t' || str.charAt(0)=='T' ||
                            str.charAt(0)=='1' )
                     result = true;
                }
                return result;
            }
        };
        sp.type=Boolean.class;
        sp.primitiveType = Boolean.TYPE;
        add(sp);


        Converter list2Array = new Converter()
        {
            @Override
            public Object convert(Object o, Class destinationType)
            {
                List sourceList = (List) o;
                Class destinationArrayType = destinationType.getComponentType();
                Object result = Array.newInstance(destinationArrayType, sourceList.size());
                for (int done=0; done<sourceList.size(); done++)
                {
                    Array.set(result, done, sourceList.get(done));
                }
                return result;
            }

            @Override
            public boolean canConvert(Object o, Class destinationType)
            {
                boolean destinationIsArray = destinationType.isArray();
                boolean sourceIsList = List.class.isAssignableFrom(o.getClass());
                return sourceIsList;
            }
        };
        add(list2Array);

    }

    public Object convertToType(Object value, Class type)
    {
        if (value.getClass()==type || value==null || type.isAssignableFrom(value.getClass()))
            {return value;}

        for (Converter cv : converters)
        {
           if (cv.canConvert(value, type))
           {
               return cv.convert(value, type);
           }
        }

        throw new RuntimeException("unknown conversion from " + value.getClass().getName()  + " to " + type.getName());
    }
}
