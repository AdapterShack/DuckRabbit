/*
 * ReflectionUtils.java
 *
 * Created on January 18, 2005, 8:07 PM
 */

package com.adaptershack.duckrabbit;

/**
 * Utility methods for reflection, classes, interfaces, etc.
 * @author jRobertson
 */
public class ReflectionUtils {
    
    /**
     * Recursively walks the graph of all superclasses and interfaces
     * that are related to the passed-in class, and adds all the Class
     * objects representing all the interfaces that are found to the
     * passed-in Set.
     */
    public static java.util.Set<Class<?>> getAllInterfaces(Class<?> c) {
        java.util.Set<Class<?>> s = new java.util.HashSet<>();
        getAllInterfaces(c, s);
        return s;
    }
    
    /**
     * Recursively walks the graph of all superclasses and interfaces
     * that are related to the passed-in class, and adds all the Class
     * objects representing all the interfaces that are found to the
     * passed-in Set.
     */
    public static void getAllInterfaces(Class<?> c, java.util.Set<Class<?>> col) {
        
        if(c == null) {
            return;
        }
        
        Class<?>[] ifaces = c.getInterfaces();
        
        for(int i=0; i< ifaces.length; i++) {
            col.add(ifaces[i]);
            getAllInterfaces(ifaces[i], col);
        }
        
        getAllInterfaces(c.getSuperclass(), col);
        
    }
    
    
}
