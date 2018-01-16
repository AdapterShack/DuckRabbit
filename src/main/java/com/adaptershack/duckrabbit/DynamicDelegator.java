/*
 * GenericWrapper.java
 *
 * Created on January 13, 2005, 9:31 AM
 */

package com.adaptershack.duckrabbit;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to make it easy to write dynamic proxies that
 * wrap existing classes. Simply write methods that have
 * the same names and signatures as the methods that you
 * want to override. All other methods will be tunnelled
 * straight through to the wrapped object. Uses reflection
 * heavily, obviously. 
 * <p>
 * It is not neccessary for the wrapper class to implement the same
 * interface(s) as the wrapped object at compile time. Method name matching
 * takes place dynamically at run time.
 * <p>
 * For a simple example, a wrapper around java.util.Map might look like this:
 * <pre>
 *   public class MapWrapper extends DynamicWrapper {
 *
 *       public MapWrapper(Object o) {
 *           super(o);
 *       }
 *
 *       public Object put(Object key, Object value) {
 *           System.out.println("Put called!");
 *           return ((Map)wrapped).put(key,value);
 *       }
 *   }
 * </pre>
 * Another class might use MapWrapper like this:
 * <pre>
 *      Map m = (Map) new MapWrapper(new HashMap()).getProxy();
 *      m.put("foo","bar");
 *      System.out.println( m.get("foo"));
 * </pre>
 * When <code>put</code> is called, the method declared in MapWrapper
 * will be executed. When <code>get</code> is called, the method executed
 * will be the one from HashMap. It so happens that in this case, MapWrapper's
 * <code>put</code> method also calls the method on the underlying Map after
 * doing some logging output.
 * <p>
 * Here is an example that illustrates the introduction of an extra interface
 * (in this case, Runnable) that is not implemented by the wrapped class:
 *<pre>
 *   public class MapWrapper extends GenericWrapper implements Runnable {
 *
 *       public MapWrapper(Object o) {
 *           super(o);
 *       }
 *
 *       public Object put(Object key, Object value) {
 *           System.out.println("Put called!");
 *           return ((Map)wrapped).put(key,value);
 *       }
 *
 *       public void run() {
 *          System.out.println("Whoohoo!");
 *       }
 *   }
 * </pre>
 * Obviiously the above is highly contrived.
 * The code to use it might look like this:
 * <pre>
 *      Map m = (Map) new MapWrapper(new HashMap()).getProxy();
 *      m.put("foo","bar");
 *      System.out.println( m.get("foo"));
 *      ((Runnable)m).run();
 * </pre>
 * @see java.lang.reflect.Proxy
 * @see java.lang.reflect.InvocationHandler
 * @author jRobertson
 */
public class DynamicDelegator<T> {
    
    /**
     * The inner object that is being wrapped by us. This may be null.
     * Depending on how we were constructed, this object may or may not
     * actually implement any of the interfaces.
     */
    protected T wrapped;
    
    /**
     * If the getProxy method has been called, thisProxy
     * is a reference to the proxy object that was returned
     * by getProxy. This reference may be passed to any callbacks
     * or other methods where you would normally pass "this".
     */
    protected T thisProxy;

    /**
     * Contains any additional interfaces to be implemented
     * by the proxy in additional to those automatically taken
     * from the wrapped object or the DynamicDelegator itself.
     */
    protected Class<?>[] additionalInterfaces = { };
    
    /** Creates a new instance of DynamicDelegator wrapping an object of type T */
    public DynamicDelegator(T wrapped, Class<?> ... interfaces) {
        this.wrapped = wrapped;
        additionalInterfaces = interfaces;
    }

    
    /** Creates a new instance of DynamicDelegator not wrapping any object
     *  but implementing the specified interface(s).
     **/
    public DynamicDelegator(Class<T> mainInterface, Class<?>... extras) {
    	List<Class<?>> clazzes = new ArrayList<>();
    	clazzes.add(mainInterface);
    	clazzes.addAll(clazzes);
    	this.additionalInterfaces = clazzes.toArray( new Class<?>[clazzes.size()] );
    }

    /**
     * Static utility method that takes a given object and uses it to
     * "implement" all of the interfaces based on method matching.
     * The object need not be declared to implement any of them.
     * Unimplemented methods will throw UnsupportedOperationException.
     * 
     * @param impl
     * @param mainInterface
     * @param extras
     * @return
     */
    @SuppressWarnings("unchecked")
	public static <T> T getProxy(Object impl, Class<T> mainInterface, Class<?>... extras ) {
    	InvocationChain chain = new InvocationChain();
    	chain.add(impl);
    	chain.addInterface(mainInterface);
    	chain.addInterfaces(extras);
    	return (T) chain.newProxyInstance();
    }

	/**
     * Static utility method that takes two objects and constructs
     * a delegation relation between them in order to "implement"
     * all of the interfaces based on method matching.
     * The objects need not be declared to implement any of the interfaces.
     */
	public static <T> T getProxy(Class<T> mainInterface, Object... delegates ) {
		return getProxy(mainInterface, null, delegates);
	}
	
	/**
     * Static utility method that takes any number objects and constructs
     * a delegation chain along all of them in order to "implement"
     * all of the interfaces based on method matching.
     * The objects need not be declared to implement any of the interfaces.
     * 
     * @param impl
     * @param mainInterface
     * @param extras
     * @return
     */
    @SuppressWarnings("unchecked")
	public static <T> T getProxy(Class<T> mainInterface, Class<?>[] extras, Object... delegates ) {
    	InvocationChain chain = new InvocationChain();
    	for(Object o : delegates) {	
    		chain.add(o);
    	}
    	chain.addInterface(mainInterface);
    	if(extras != null) {
    		chain.addInterfaces(extras);
    	}
    	return (T) chain.newProxyInstance();
    }
    
    
    /**
     * Uses InvocationChain to tie this wrapper and the wrapped object
     * together as a single proxy instance. The proxy implements all
     * interfaces that are implemented by either this wrapper or by
     * the wrapped Object, as well as any interfaces returned by the
     * getAdditionalInterfaces method. Any method called on this proxy
     * will be invoked on the wrapper if such a method if found there,
     * or on the wrapped object otherwise.
     * @see InvocationChain
     */
    @SuppressWarnings("unchecked")
	public T getProxy() {
        InvocationChain chain = new InvocationChain();
        chain.add(this);
        if( wrapped != null) {
        	chain.add(wrapped);
        }
        chain.addInterfaces(getAdditionalInterfaces());
        thisProxy = (T) chain.newProxyInstance();
        return thisProxy;
    }
    
  
    

    /**
     * Returns any additional interfaces to be implemented
     * by the proxy in additional to those automatically taken
     * from the wrapped object or the DynamicDelegator itself.
     */
     protected Class<?>[] getAdditionalInterfaces() {
         return additionalInterfaces;
     }
 
    
}
