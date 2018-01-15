This is a (slightly) modernized version of some code I published on my blog
way back in 2005:

<http://oldblog.jeff-robertson.com/2005/09/dynamic-delegation-act-2.html>

This provides a quick way to implement only some methods of an interface,
and automatically (via reflection) delegate the rest.

The classical example, and the original reason I wrote this code in 2004, was
wrapping java.sql.Connection, which has a million methods you'd have to delegate.
As you may recall, unclosed database connections was a big damn deal in Java before
try-with-resources. Overriding Object.finalize() was one weird trick to look for
the leaks. With the DynamicDelegator, this becomes near trivial:


```java

    Connection getConnection() {
    
        Connection c = reallyConnectToDatabase();
    
    	  final Throwable stacktrace = new Throwable();

        Connection wrapped = DynamicDelegator<Connection>(c) {
            public void finalize() {
       	       if(!isClosed()) {
       		        LOGGER.error("Were you raised in a barn?!!",stacktrace);
       		    }
            }
         }.getProxy();

         return wrapped;       
    }
    
```

I would have assumed that by 2018, some kind of automatic delegation would
have been added to the Java language or standard library, to facilitate this sort of thing.

Apparently, there is, and it's called "switching from Java to Groovy". But maybe the world
still needs all-java solutions to this problem. I'm sure this code has been
written 1000's of times. This is one of them.

Here's some further discussion of this problem from much more recent days:

<https://stackoverflow.com/questions/30344715/automatically-delegating-all-methods-of-a-java-class>

The name "DuckRabbit" has been chosen for this library in reference to [Duck Typing](https://en.wikipedia.org/wiki/Duck_typing),
which is one of the not-in-Java features being simulated by this code.

