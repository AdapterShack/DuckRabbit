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

		Connection wrapped = new DynamicDelegator<Connection>(c) {
			public void finalize() throws Exception {
				if (!c.isClosed()) {
					LOGGER.error("Were you raised in a barn?!!", stacktrace);
				}
			}
		}.getProxy();

		return wrapped;
	}
```

Of course, there's less need to do THIS particular thing anymore.

You can also use this to dynamically create _partial_
interface implementations. Any methods not implemented will throw runtime exceptions. It's better than
having to type "throw new UnsupportedOperationException()" a zillion times, right?

```java
		return new DynamicDelegator<Connection>(Connection.class) {
			@SuppressWarnings("unused")
			public boolean isClosed() {
				return false;
			}
			@SuppressWarnings("unused")
			public Statement createStatement() {
				return new DynamicDelegator<Statement>(Statement.class) {
					public boolean execute(String sql) {
						return true;
					}
				}.getProxy();
			}
		}.getProxy();
```

There are also static versions of the getProxy method that take any object:

```java
	Connection getConnection() {

		Connection c = reallyConnectToDatabase();

		final Throwable stacktrace = new Throwable();

		Connection wrapped = DynamicDelegator.getProxy(
			Connection.class,
			new Object() {
				public void finalize() throws Exception {
					if (!realConnection.isClosed()) {
						LOGGER.error("Were you raised in a barn?!!", stacktrace);
					}
				}
			}, 
			realConnection );

		return wrapped;
	}
			
```

This makes it more obvious what is going on: the anonymous class serving as the second
argument to getProxy is not, actually, a type of Connection. The created proxy is.

You can use these to make a pre-existing object implement a pre-existing interface that
happens to match any of its methods. It's harder to think of a useful example for this,
but imagine for some reason you needed to make a BufferedReader implement DataInput
at least as far as the readLine() method is concerned..

```java

    BufferedReader br = ...

    DataInput di = DynamicDelegator.getProxy(br, DataInput.class);

    String s = di.readLine();	
```

I would have assumed that by 2018, some kind of automatic delegation would
have been added to the Java language or standard library, to facilitate this sort of thing.

Apparently, there is, and it's called "switching from Java to Groovy". So the world
still needs all-java solutions to this problem. I'm sure this code has been
written 1000's of times. This is one of them.

Here's some further discussion of this problem from much more recent days:

<https://stackoverflow.com/questions/30344715/automatically-delegating-all-methods-of-a-java-class>

The name "DuckRabbit" has been chosen for this library in reference to Duck Typing,
which is one of the not-in-Java features being simulated by this code.

