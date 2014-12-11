package com.houseelectrics.serializer.security;

import com.houseelectrics.serializer.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by roberttodd on 04/12/2014.
 */
public class PermissionFilterExplorer implements Explorer
{
    private boolean throwException = false;
    public boolean getThrowException() { return throwException; }
    public void setThrowException(boolean value) { throwException = value; }

    private Explorer underlyingExplorer=null;
    public Explorer getUnderlyingExplorer() { return underlyingExplorer; }
    public void setUnderlyingExplorer(Explorer value) { this.underlyingExplorer = value; }

    public NodeExpander getNodeExpander()  { return underlyingExplorer.getNodeExpander(); }
    public void setNodeExpander(NodeExpander value) {underlyingExplorer.setNodeExpander(value); }

    public interface CheckPermissionByName
    {
        public boolean check(String name);
    }

    /*
     * required code to check permission by name
     */
    private CheckPermissionByName checkPermissionByName;
    public CheckPermissionByName getCheckPermissionByName() {return checkPermissionByName;}
    public void setCheckPermissionByName(CheckPermissionByName value)
    {
        this.checkPermissionByName = value;
    }


    public void explore(Object root, final ExplorationListener explorationListener)
    {
        if (getCheckPermissionByName()==null)
        {
            //todo write test for this
            throw new SecurityException("cant check permissions no Check Permission By Name code");
        }
        if (getUnderlyingExplorer() == null)
        {
            throw new SecurityException("cant check permissions no UnderlyingExplorer");
        }

        ExplorationListener filteredListener = new ExplorationListener()
        {
            @Override
            public boolean MoveAway(Object from, String propertyName, Object to, boolean isIndexed, Integer index)
            {
                if (!isDeniedByPermission(from, propertyName))
                {
                    return explorationListener.MoveAway(from, propertyName, to, isIndexed, index);
                }
                else
                {
                    return false;
                }
            }

            @Override
            public void MoveBack(Object from, String propertyName, Object to, boolean isIndexed)
            {
                if (!isDeniedByPermission(from, propertyName))
                {
                    explorationListener.MoveBack(from, propertyName, to, isIndexed);
                }
            }

            @Override
            public void OnLeaf(Object from, String propertyName, Object to, Integer index)
            {
                if (!isDeniedByPermission(from, propertyName))
                {
                    explorationListener.OnLeaf(from, propertyName, to, index);
                }
            }
        };

        getUnderlyingExplorer().explore(root, filteredListener);
    }

    boolean isDeniedByPermission(Object from, String propertyName)
    {
        if (from == null) { return false; }
        Class fromType = from.getClass();
        boolean searchComplete=false;
        PermissionRequirement permissionRequirement = null;
        try
        {
            Method method = ReflectionUtil.getMethodForPropertyName(fromType, propertyName);
           permissionRequirement = method.getAnnotation(PermissionRequirement.class);
            searchComplete = true;
        }
        catch (NoSuchMethodException nmex)
        {
            //throw new RuntimeException("unable to locate property " + fromType.getName() + "." + propertyName);
        }

        if (!searchComplete)
        {
            try {
                Field f= fromType.getField(propertyName);
                permissionRequirement = f.getAnnotation(PermissionRequirement.class);
                searchComplete = true;
            }
            catch (NoSuchFieldException nsfex)
            {
                throw new RuntimeException("unable to find field or getter for propertyName==" + propertyName);
            }
        }


        if (permissionRequirement!=null )
        {
            boolean hasPermission = getCheckPermissionByName().check (permissionRequirement.permissionName());
            if (!hasPermission)
            {
                if (getThrowException())
                {
                    throw new SecurityException("cannot access property " +
                            fromType.getName() + "." + propertyName + " without permisson " + permissionRequirement.permissionName());
                }
                else
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static void injectPermissionFilter(Object2Json object2Json, final PermissionFilterExplorer.CheckPermissionByName securityCheck, boolean throwException)
    {
        final PermissionFilterExplorer pfe = new PermissionFilterExplorer();
        pfe.setThrowException( throwException);

        object2Json.setExplorerFactory( new Object2Json.ExplorerFactory()
        {
            @Override
            public Explorer create()
            {
                pfe.setUnderlyingExplorer(new ObjectExplorerImpl());
                pfe.setCheckPermissionByName( securityCheck);
                return pfe;
            }
        }
        );
    }


}
