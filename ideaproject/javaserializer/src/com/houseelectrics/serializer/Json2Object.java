package com.houseelectrics.serializer;

import java.lang.reflect.*;
import java.util.*;

interface ValueSetter
{
    public String setValueinObject(Object target, String propertyName, Object value, Json2Object.DeserializationListener deserializationListener);
}

/**
 * Created by roberttodd on 29/11/2014.
 */
public class Json2Object implements JsonExploreListener
{
    /*
   * created originally so DataContract
   * support can be added non-intrusively
   */
    public interface DeserializationListener
    {
        void onCreateObject(Object o);
        void onEndObject(Object o);
        void onSetValue(Object o, String propertyName, Object value);
    }

    BroadcastDeserializationListener listener = new BroadcastDeserializationListener();

    public void Add(DeserializationListener l)  { listener.Add(l); }
    public void Remove(DeserializationListener l) { listener.Remove(l);}
    public interface Function
    {
        public Object execute(List<Object> args);
    }

    public Json2Object()
    {

        //setToUseFields();
        //todo connect this function name with serialisation implementation
        final String fname = "_a_";

        Function _a_ = new Function()
        {
            @Override
            public Object execute(List<Object> args)
            {
                if (args.size() != 1)
                    throw new RuntimeException("expected 1 argument to " + fname);
                Class aType = Object[].class;
                Object arg0 = args.get(0);
                if (arg0 != null && aType != arg0.getClass())
                {
                    throw new RuntimeException(fname + " expected arg0 to be type " + aType.getName() + "  but was " + arg0.getClass().getName());
                }
                return arg0;
            }
        };

        addFunction(fname, _a_);
    }

    private TypeAntiAliaser typeAntiAliaser = TypeAliaserUtils.createSimpleTypeAntiAliaser();
    public TypeAntiAliaser getTypeAntiAliaser() {return typeAntiAliaser;}
    public void setTypeAntiAliaser(TypeAntiAliaser value) {this.typeAntiAliaser = value;}

    //todo change this in csharp from erroneous @class
    public final String defaultTypeSpecifier = "_class";

    private String typeSpecifier = defaultTypeSpecifier;
    public String getTypeSpecifier() {return typeSpecifier;}
    public void setTypeSpecifier(String value) {this.typeSpecifier = value;}

    public class UnknownFunctionException extends RuntimeException
    {
        public UnknownFunctionException(String message)
        {
            super(message);
        }
    }

    public class NoClueForTypeException extends RuntimeException
    {
        public NoClueForTypeException(String message)
        {
            super(message);
        }
    }

    public class UnterminatedObjectException extends RuntimeException
    {
        public UnterminatedObjectException(String message)
        {
            super(message);
        }
    }

    public class OverTerminatedObjectException extends RuntimeException
    {
        public OverTerminatedObjectException(String message)
        {
            super(message);
        }
    }

    /**
     * parameters:
     * target propertyName valueAsString - return true if success
     **/
    //Func<object, String, object, DeserializationListener, String> setValueinObject = setValueinObjectAsMember;
    ValueSetter setValueinObject = setValueinObjectAsMember;

    /*   public void setToUseFields()
       {
           //setValueinObject = setValueinObjectAsField;
           setValueinObject = setValueinObjectAsMember;
       }

       public void setToUseProperties()
       {
           //setValueinObject = setValueinObjectAsProperty;
           setValueinObject = setValueinObjectAsMember;
       }
       */
    static boolean setValueAsMap(Object target, String propertyName, Object propertyValue)
    {
        if (!Map.class.isAssignableFrom(target.getClass())) return false;
        Map dict = (Map)target;
        Object key = propertyName;
        dict.put(key, propertyValue);
        return true;
    }

    static ValueSetter setValueinObjectAsMember
            = new ValueSetter()
    {
        @Override
        public String setValueinObject(Object target, String propertyName, Object propertyValue, DeserializationListener deserializationListener)
        {
            if (setValueAsMap(target, propertyName, propertyValue))
            {
                return null;
            }
           // throw new RuntimeException("setValueinObject not implemented");
            Class targetType = target.getClass();

            Method getterMethod=null, setterMethod =null;
            try
            {
                Class []paramTypes = {};
                getterMethod = targetType.getMethod("get" + propertyName, paramTypes);
                if (getterMethod!=null)
                {
                    Class []sParamTypes = {getterMethod.getReturnType()};
                    setterMethod = targetType.getMethod("set" + propertyName, sParamTypes);
                }
            }
            catch (NoSuchMethodException nex)
            {
                //throw new RuntimeException("failed to find field " + propertyName + " in " + target.getClass().getName(), nex);
            }
            if (setterMethod!=null && !Modifier.isPrivate(setterMethod.getModifiers()))
            {
                propertyValue = convertTypeJava(propertyValue, getterMethod.getReturnType());
                Object []params = {propertyValue};
                try
                {
                    setterMethod.invoke(target, params);
                }
                catch (Exception ilex)
                {
                    throw new RuntimeException("failed to call " + setterMethod.getName(), ilex);
                }

                if (deserializationListener != null) deserializationListener.onSetValue(target, propertyName, propertyValue);
                return null;
            }

            Field field =  null;
            try
            {
                field = targetType.getField(propertyName);
            }
            catch (NoSuchFieldException nex)
            {
                throw new RuntimeException("failed to find field " + propertyName + " in " + target.getClass().getName(), nex);
            }
            if (field != null)
            {
                try
                {
                    propertyValue = convertTypeJava(propertyValue, field.getType());
                    field.set(target, propertyValue);
                    if (deserializationListener != null) deserializationListener.onSetValue(target, propertyName, propertyValue);

                }
                catch (IllegalAccessException nex)
                {
                    throw new RuntimeException("failed to set " + propertyName + " in " + target.getClass().getName(), nex);
                }
            }

            return null;

        }
    };


    // todo decide whether to remove this - its not in use but its propbably quicker than setValueinObjectAsMember
    //
/*    static Func<object, String, object, DeserializationListener, String> setValueinObjectAsField = (target, propertyName, propertyValue, listener) =>
    {
        if (setValueAsMap(target, propertyName, propertyValue))
        {
            return null;
        }
        Type targetType = target.GetType();
        FieldInfo fieldInfo = targetType.GetField(propertyName);
        if (fieldInfo == null)
        {
            return "cant find field " + propertyName + " in " + targetType.FullName;
        }
        Type underlyingType = Nullable.GetUnderlyingType(fieldInfo.FieldType);
        if (underlyingType == null)
            underlyingType = fieldInfo.FieldType;
        else
        {
            System.Console.WriteLine("here");
        }
        object value = null;
        if (propertyValue == null) value = null;
        else if (!convertType(ref value, underlyingType, propertyValue))
            value = Convert.ChangeType(propertyValue, underlyingType);
        fieldInfo.SetValue(target, value);
        if (listener != null) listener.onSetValue(target, propertyName, value);
        return null;
    };
*/


    protected static TypeConverter typeConverter = new TypeConverter();

    //todo make this customizable
    public static Object convertTypeJava(Object value, Class type)
    {
        if (value == null ) return null;
        if (type.isAssignableFrom( value.getClass()))
           {
               return value;
           }
        Object result = typeConverter.convertToType(value, type);

       return result;
    }

    Stack<StackFrame> objects;
    Object lastObject;

    Class typeHint;
    String json;

    static Object newInstance(Class type)
    {
        if (type == List.class)
        {
            type = ArrayList.class;
        }
        if (type== Map.class)
        {
            type = HashMap.class;
        }
        Constructor co = null;
        try
        {
            co = type.getConstructor(new Class[0]);
        }
        catch (NoSuchMethodException nex)
        {
            throw new RuntimeException("couldnt construct " + type.getName(), nex);
        }
        if (co==null)
        {
            throw new RuntimeException("no default constructor for " + type.getName());
        }
        //ConstructorInfo co = type.GetConstructor();

        Object newObject = null;
        try
        {
            newObject = co.newInstance(new Object[0]);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("couldnt construct " + type.getName(), ex);
        }
        return newObject;
    }

    public boolean objectTypePending = false;
    public void JsonStartObject(String propertyName, int pos)
    {
        // if the typeHint is not null create theobject
        // otherwise defer until a class attribute property is available

        ObjectFrame objectHolder = new ObjectFrame();
        objectHolder.propertyName = propertyName;
        objects.push(objectHolder);

        if (typeHint != null)
        {
            Object newObject = newInstance(typeHint);
            objectHolder.theObject = newObject;
            if (objects.size()>1) setGenericTypesForNewFrame(objects.get(objects.size() - 2), objectHolder, propertyName);

            listener.onCreateObject(newObject);
            objectTypePending = false;
            typeHint = null;
            return;
        }
        objectTypePending = true;

    }

    public void setGenericTypesForNewFrame(StackFrame parentFrame, StackFrame objectHolder, String propertyName)
    {
            if (!(parentFrame instanceof  ObjectFrame))
            {
                return;
            }
            Object parentObject = ((ObjectFrame) parentFrame).theObject;
            Class parentClass = parentObject.getClass();
            objectHolder.genericClasses = new ArrayList<Class>();

            Type[] genericTypes = ReflectionUtil.getParameterizedTypesForField(parentClass, propertyName);
             if (genericTypes==null) genericTypes = ReflectionUtil.getParameterizedTypesForProperty(parentClass, propertyName);
             if (genericTypes!=null)
                {
                    for (Type type : genericTypes)
                    {
                        Class gClass = Object.class;
                        if (type instanceof Class) gClass = (Class) type;
                        objectHolder.genericClasses.add(gClass);
                    }
                }
    }

    protected Class inferCurrentPropertyType(ObjectFrame parentFrame, ObjectFrame currentFrame)
    {
        Method m = null;
        Class theClass = ReflectionUtil.getPropertyOrReflectionType(parentFrame.theObject.getClass(), currentFrame.propertyName);
        return theClass;
    }


    public void JsonLeaf(String propertyName, String value, boolean isQuoted)
    {
        boolean leafPending = true;
        if (objectTypePending)
        {
            Class attributeTypeHint=null;
            //todo add the type hint infererence to csharp
            if ( propertyName.equals(getTypeSpecifier()) )
            {
                attributeTypeHint = getTypeAntiAliaser().antiAlias(value);
                if (attributeTypeHint == null)
                {
                    String message = "cant load type: \""+ value + "\" in json:"+ json;
                    NoClueForTypeException ex = new NoClueForTypeException(message);
                    throw ex;
                }
                leafPending = false;
            }
            if (attributeTypeHint==null && objects.size()>=2)
            {
                StackFrame stackFrame = objects.get(objects.size()-2);
                if (stackFrame instanceof ArrayFrame)
                {
                    if (((ArrayFrame)stackFrame).genericClasses.size()>0 )
                    {
                        attributeTypeHint = ((ArrayFrame)stackFrame).genericClasses.get(0);
                    }
                }
            }
            if (attributeTypeHint==null && objects.size()>1 )
            {
                StackFrame parentFrame = objects.get(objects.size()-2);
                StackFrame currentFrame = objects.get(objects.size()-1);
                if (parentFrame instanceof ObjectFrame && currentFrame instanceof ObjectFrame)
                    attributeTypeHint = inferCurrentPropertyType((ObjectFrame)parentFrame, (ObjectFrame) currentFrame);
            }


            if (attributeTypeHint==null)
            {
                //todo guess a typeHint based on parent property type

                String message = "encountered a property \"" + propertyName + "\" in subobject  at but dont know parent object type yet ! json==" + json;
                NoClueForTypeException ex = new NoClueForTypeException(message);
                throw ex;
            }

            Object newObject =  newInstance(attributeTypeHint);
            ObjectFrame objectHolder = (ObjectFrame)objects.peek();
            objectHolder.theObject = newObject;
            if (objects.size()>1)
            setGenericTypesForNewFrame(objects.get(objects.size() - 2), objectHolder, objectHolder.propertyName);
            listener.onCreateObject(newObject);
            objectTypePending = false;
            if (!leafPending) return;

        }

        if (objectTypePending || objects.size() == 0)
        {
            String message = "unable to set property " + propertyName + " with value " + value + " because type is not found yet ";
            throw new RuntimeException(message);
        }

        String strError = null;

        if (getTypeSpecifier() != null && propertyName != null && propertyName.equals(getTypeSpecifier()))
        {
            // todo raise a warning here or throw an exception in strict mode
        }
        else
        {
            objects.peek().submitLeafToStack(propertyName, value, setValueinObject, listener);
        }

        if (strError != null) throw new RuntimeException(strError);

    }

    public void JsonEndObject(int pos)
    {
        if (objectTypePending)
        {
            String strMessage = "unable to determine type of object terminated at pos " + pos + " in " + json;
            throw new NoClueForTypeException(strMessage);
        }
        if (objects.size() < 1)
        {
            String strMessage = "too many object terminations at pos " + pos + " in " + json;
            throw new OverTerminatedObjectException(strMessage);
        }
        lastObject = objects.pop();
        ObjectFrame lastFrame =  null;
        if (lastObject instanceof ObjectFrame) lastFrame = (ObjectFrame) lastObject;
        if (lastFrame==null)
        {
            throw new UnterminatedObjectException("unexpected non Object frame " + lastFrame.getClass().getName());
        }
        listener.onEndObject(lastFrame.theObject);
        if (objects.size() > 0)
            objects.peek().submitObjectToStack(((ObjectFrame)lastObject).propertyName, ((ObjectFrame)lastObject).theObject, setValueinObject, listener);

    }

    public void JsonStartFunction(String functionName, int pos, String propertyName)
    {
        FunctionHolder holder = new FunctionHolder();
        holder.paramList = new ArrayList<Object>();
        holder.functionName = functionName;
        holder.propertyName = propertyName;
        objects.push(holder);
    }

    Map<String, Function> functionNameToFunction = new HashMap<String, Function>();

    public void addFunction(String name, Function function)
    {
        functionNameToFunction.put(name, function);
    }

    public void JsonEndFunction(int pos)
    {
        FunctionHolder holder = (FunctionHolder)objects.pop();
        // call the function handler !
        if (!functionNameToFunction.containsKey(holder.functionName))
        {
            throw new UnknownFunctionException("unknown function: \"" + holder.functionName + "\"");
        }
        Function f = functionNameToFunction.get(holder.functionName);
        Object value = f.execute (holder.paramList);
        objects.peek().submitObjectToStack(holder.propertyName, value, setValueinObject, listener);
        //submitObjectToStack(objects.Peek(), holder.propertyName, value);
    }

    public void JsonStartArray(String propertyName, int pos)
    {
        ArrayFrame holder = new ArrayFrame();
        holder.objectList = new ArrayList<Object>();
        holder.propertyName = propertyName;

        objects.push(holder);
        if (objects.size()>=2)
            setGenericTypesForNewFrame(objects.get(objects.size()-2), holder, propertyName);
    }

    //todo - check if can be subsumed into submit to stack - to avoid casting
    public void JsonEndArray(int pos)
    {
        ArrayFrame holder = (ArrayFrame)objects.pop();
        /*Object[] value = new Object[holder.objectList.size()];
        for (int done = 0; done < holder.objectList.size(); done++)
        {
            value[done] = holder.objectList.get(done);
        }
        */
        Object value = holder.objectList;
        StackFrame context = objects.peek();

        if (context instanceof FunctionHolder)
        {
            ((FunctionHolder)context).paramList.add(value);
        }
        else if (context instanceof ObjectFrame)
        {
            String strError = setValueinObject.setValueinObject(((ObjectFrame)context).theObject, holder.propertyName, value, listener);
            if (strError != null) throw new RuntimeException("failed to set value because " + strError);
        }
        else
        {
            throw new RuntimeException("unexpected StackFrame: " + context.getClass().getName() + " for JsonEndArray pos=" +pos);
        }

    }

    /**
     * the current type will be inferred either for the navigation context or the first type !
     * otherwise a full parse will be necessary
     */

    public Object toObject(String json)
    {
        return toObject(json, null);
    }

    public Object toObject(String json, Class typeHint)
    {
        //todo revise this - it makes the serializer single threaded !
        this.json = json;
        if (setValueinObject == null)
        {
            throw new RuntimeException("setValueInObject unset !");
        }

        JSONExplorerImpl impl = new JSONExplorerImpl();
        this.typeHint = typeHint;
        this.objects = new Stack<StackFrame>();
        this.lastObject = null;
        impl.explore(json, this);
        if (lastObject instanceof ObjectFrame) lastObject = ((ObjectFrame)lastObject).theObject;
        return lastObject;
    }

}


abstract class StackFrame
{

    public abstract void submitObjectToStack(String propertyName, Object value, ValueSetter valueSetter, Json2Object.DeserializationListener listener);
    public abstract void submitLeafToStack(String propertyName, String value, ValueSetter valueSetter, Json2Object.DeserializationListener listener);
    public List<Class> genericClasses = new ArrayList<Class>();

}

class ObjectFrame extends StackFrame
        {
public String propertyName;
public Object theObject;
TypeConverter typeConverter = new TypeConverter();

public String setMapValue(Map map, String propertyName, Object value)
{
    Object key = propertyName;
    if (genericClasses.size() >= 2)
       {
           key = typeConverter.convertToType(propertyName, genericClasses.get(0));
           value = typeConverter.convertToType(value, genericClasses.get(1));
       }
    map.put( key, value);
    return null;
}

public String add2List(List list, Object value)
        {
        if (genericClasses.size()>=1)
            {
                value = typeConverter.convertToType(value, genericClasses.get(0));
            }
        list.add(value);
        return null;
        }


//public List<Class> genericTypes=null;
@Override
public void submitObjectToStack(String propertyName, Object value, ValueSetter valueSetter, Json2Object.DeserializationListener listener)
        {
        String strError=null;
        if (this.theObject instanceof Map)
        {
            strError = setMapValue((Map)this.theObject, propertyName, value);
        }
            else
        {
            strError =
                    valueSetter.setValueinObject(this.theObject, propertyName, value, listener);
        }

        if (strError != null) throw new RuntimeException(strError);
        }
            @Override
public  void submitLeafToStack(String propertyName, String value, ValueSetter valueSetter, Json2Object.DeserializationListener listener)
        {
            String strError;
            if (this.theObject instanceof Map)
            {
                strError = setMapValue((Map)this.theObject, propertyName, value);
            }
           else
            {
                if (this.theObject instanceof Date && propertyName.equalsIgnoreCase(NodeExpanderConstants.unixEpochTimeMillisPropertyName))
                {
                    propertyName = "Time";
                }
                strError =
                        valueSetter.setValueinObject(this.theObject, propertyName, value, listener);
            }

        if (strError != null) throw new RuntimeException(strError);
        }
        }

class ArrayFrame extends StackFrame
        {
public ArrayList<Object> objectList;
public String propertyName;
TypeConverter typeConverter = new TypeConverter();

@Override
public  void submitObjectToStack(String propertyName, Object value, ValueSetter valueSetter, Json2Object.DeserializationListener listener)
        {
        if (value!=null && genericClasses.size()>0)
        {
            value = typeConverter.convertToType(value, genericClasses.get(0));
        }
        objectList.add(value);
        }
        @Override
        public  void submitLeafToStack(String propertyName, String value, ValueSetter valueSetter, Json2Object.DeserializationListener listener)
        {
          Object oValue = value;
          if (oValue!=null && genericClasses.size()>0)
          {
              oValue = typeConverter.convertToType(oValue, genericClasses.get(0));
           }
        this.objectList.add(oValue);
        }
        }

class FunctionHolder extends StackFrame
        {
public ArrayList<Object> paramList;
public String functionName;
public String propertyName;

@Override
public  void submitObjectToStack(String propertyName, Object value, ValueSetter valueSetter, Json2Object.DeserializationListener listener)
        {
        this.paramList.add(value);
        }
@Override
public  void submitLeafToStack(String propertyName, String value, ValueSetter valueSetter, Json2Object.DeserializationListener listener)
        {
        throw new RuntimeException("FunctionHolder.submitLeafToStack not implemented");
        }
        }

class BroadcastDeserializationListener implements Json2Object.DeserializationListener
        {
private ArrayList<Json2Object.DeserializationListener> listeners = new ArrayList<Json2Object.DeserializationListener>();
public void Add(Json2Object.DeserializationListener listener)
        {
        listeners.add(listener);
        }
public void Remove(Json2Object.DeserializationListener listener)
        {
        listeners.remove(listener);
        }

public void onCreateObject(Object o)
        {
        for(Json2Object.DeserializationListener listener : listeners)
        {
        listener.onCreateObject(o);
        }
        }
public void onEndObject(Object o)
        {
        for (Json2Object.DeserializationListener listener : listeners)
        {
        listener.onEndObject(o);
        }

        }
public void onSetValue(Object o, String propertyName, Object value)
        {
        for (Json2Object.DeserializationListener listener : listeners)
        {
        listener.onSetValue(o, propertyName, value);
        }
        }

        }





