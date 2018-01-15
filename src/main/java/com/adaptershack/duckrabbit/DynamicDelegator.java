/*
 * GenericWrapper.java
 *
 * Created on January 13, 2005, 9:31 AM
 */

package com.adaptershack.duckrabbit;

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
     * The inner object that is being wrapped by us.
     */
    protected T wrapped;
    
    /**
     * If the getProxy method has been called, thisProxy
     * is a reference to the proxy object that was returned
     * by getProxy. This reference may be passed to any callbacks
     * or other methods where you would normally pass "this".
     */
    protected T thisProxy;
        
    /** Creates a new instance of GenericWrapper */
    public DynamicDelegator(T wrapped) {
        this.wrapped = wrapped;
    }

    
    /**
     * Uses InvocationChain to tie this wrapper and the wrapped object
     * together as a single proxy instance. The proxy implements all
     * interfaces that are implemented by either this wrapper or by
     * the wrapped Object, as well as any interfaces returned by the
     * getAdditionalInterfaces method. Any method called on this proxy
     * will be invoked on the wrapper if such a method if found there,
     * or on thw wrapped object otherwise.
     * @see InvocationChain
     */
    @SuppressWarnings("unchecked")
	public T getProxy() {
        InvocationChain chain = new InvocationChain();
        chain.add(this);
        chain.add(wrapped);
        chain.addInterfaces(getAdditionalInterfaces());
        thisProxy = (T) chain.newProxyInstance();
        return thisProxy;
    }
    
  
    

    /**
     * Normally this returns a zero-length array. Override to specify
     * that the proxies created by this class should implement the
     * returned interface(s) in addition to any that are implemented
     * by the wrapper or the wrapped object.
     */
     protected Class<?>[] getAdditionalInterfaces() {
         return new Class<?>[0];
     }
 
    
}
