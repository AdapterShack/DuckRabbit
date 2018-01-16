/*
 * InvocationChain.java
 *
 * Created on January 18, 2005, 10:46 PM
 */

package com.adaptershack.duckrabbit;

import java.lang.reflect.*;
import java.util.*;

/**
 * Uses reflection to create a chain of responsibility out of a
 * collection of otherwise unrelated classes.
 *
 * @see java.lang.Proxy
 * @see java.lang.InvocationHandler
 * @see DynamicDelegator
 *
 * @author jRobertson
 */
public class InvocationChain implements InvocationHandler {
    
    private Set<Class<?>> interfaces = new HashSet<>();
    private LinkedList<Object> links = new LinkedList<>();
    
    /** Creates a new instance of InvocationChain */
    public InvocationChain() {
    }
    
    /** Creates a new instance of InvocationChain
     *  with the given objects.
     */
    public InvocationChain(Object... toAdd) {
        this();
        for(Object o : toAdd) {
            add(o);
        }
    }


    /**
     * Adds the specified object to the end of the chain.
     */
    public void add(Object o) {
        ChainLink link = new ChainLink(o);
        links.add(link);
        ReflectionUtils.getAllInterfaces(o.getClass(), interfaces);
    }
    
    /**
     * Manually add this interface to this list of interfaces
     * that will be implemented by this chain.
     */
    public void addInterface(Class<?> c) {
        interfaces.add(c);
    }
    
    /**
     * Manually add these interfaces to this list of interfaces
     * that will be implemented by this chain.
     */
    public void addInterfaces(Class<?>[] c) {
        interfaces.addAll(Arrays.asList(c));
    }    
    
    /**
     * This method implements the InvocationHandler interface. The chain
     * of responsibility will be searched until a method of the required
     * name and argument types is found, and that method will then be
     * invoked. The search stops after finding the first occurrence of
     * a method in the chain, so the order in which the objects were
     * added is important. 
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        
        // search the objects in the chain, one by one
        for(Iterator<Object> i=links.iterator(); i.hasNext();) {
            ChainLink link = (ChainLink) i.next();
            
            // if the class implements the interface that 
            // declared this method, run it
            if( method.getDeclaringClass().isAssignableFrom( link.getObject().getClass() ) ) {
                return method.invoke(link.getObject(), args);
            }
            
            // if the class has a method that has the same
            // name and type as this method, find that
            // method and run it instead
            Method match = link.matchMethod(method);
            if( match != null) {
                return match.invoke(link.getObject(), args);
            }
            
        }

        // ok, the fast way of just looking stuff up in a hashmap
        // failed... now try the slow way that involves catching
        // exceptions. not sure if it is really possible to get
        // here, but you never can tell with reflection
        for(Iterator<Object> i=links.iterator(); i.hasNext();) {
            ChainLink link = (ChainLink) i.next();
            
            try {
                return method.invoke(link.getObject(), args);
            } catch (IllegalAccessException e) {
                // nothing
            } catch (IllegalArgumentException e) {
                // nothing
            }
        }
        
        throw new UnsupportedOperationException("couldn't find metho in my chain of respsonsibility: "+method);
    }

    /**
     * Returns the set of all interfaces implemented by
     * any objects in the chain.
     */
    public Class<?>[] getInterfaces() {
        Class<?>[] intArray = new Class[interfaces.size()];
        return (Class<?>[]) interfaces.toArray(intArray);
    }
    
    /**
     * Creates a proxy that implements all interfaces that are
     * implemented by any object in the chain, and with this
     * InvocationChain as the invocation handler.
     */
    public Object newProxyInstance() {
        return Proxy.newProxyInstance(getClass().getClassLoader(), getInterfaces(), this);             
    }
    
    /**
     * Inner class for each delegate in the chain.
     */
    private static class ChainLink {
        private Map<MethodSig, Method> methodMap = new java.util.HashMap<>();
        private Object object;

        ChainLink(Object o) {
            object = o;
            Method[] methods = o.getClass().getMethods();
            for(int i=0; i<methods.length;i++) {
                Method method = methods[i];
                
                // do not count methods that we can't access
                if( Modifier.isPublic(method.getModifiers())) {
                    methodMap.put( new MethodSig(method), method);
                }
            }            
        }
        
        Method matchMethod(Method m) {
            return (Method)methodMap.get( new MethodSig(m));
        }
        
        Object getObject(){ return object; }
        
        
    }
    
    @SuppressWarnings("serial")
	private static class MethodSig extends ArrayList<Object> {
      public MethodSig(Method m) {
	      add( m.getName() );
	      add( Arrays.asList( m.getParameterTypes()) );
      }
    }
    
}
