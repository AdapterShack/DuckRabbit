package com.adaptershack.duckrabbit;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

public class ConnectionTest {

	Connection getConnection() {

		Connection realConnection = reallyConnectToDatabase();

		final Throwable stacktrace = new Throwable();

		Connection wrapped = new DynamicDelegator<Connection>(realConnection) {
			public void finalize() throws Exception {
				if (!realConnection.isClosed()) {
					LOGGER.error("Were you raised in a barn?!!", stacktrace);
				}
			}
		}.getProxy();

		return wrapped;
	}

	Connection getConnection2() {

		Connection realConnection = reallyConnectToDatabase();

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
	
	
	
    //@SuppressWarnings("deprecation")
	@Test
    public void testConnectionPoolLeak() {

    	try {
    		Connection c = getConnection();
    		
    		boolean result = c.createStatement().execute("delete foo from bar");
    		
    		assertTrue(result);
    		
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}

    	// simulate what would happen when the JVM garbage-collects the connection
    	// System.runFinalizersOnExit(true);
    }
    
    //@SuppressWarnings("deprecation")
	@Test
    public void testConnectionPoolLeak2() {

    	try {
    		Connection c = getConnection2();
    		
    		boolean result = c.createStatement().execute("delete foo from bar");
    		
    		assertTrue(result);
    		
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}

    	// simulate what would happen when the JVM garbage-collects the connection
    	//System.runFinalizersOnExit(true);
    }
    
    
    
    
	private Connection reallyConnectToDatabase() {
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
	}	
	
	
	
	
	// allows the code to pretend we have a logging framework..
	static class LOGGER {
		public static void error(String s, Throwable t) {
			System.err.print(s);
			System.err.print(" ");
			t.printStackTrace();
		}
	}
	
	
}
