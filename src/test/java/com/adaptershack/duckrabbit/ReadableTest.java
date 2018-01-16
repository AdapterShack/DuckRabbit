package com.adaptershack.duckrabbit;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class ReadableTest {
	
	@Test
	public void testCoerce() throws IOException {
		
		BufferedReader br = new BufferedReader(new StringReader("Hello, world"));
		
		DataInput di = DynamicDelegator.getProxy(br, DataInput.class);
		
		assertEquals("Hello, world", di.readLine());
		
	}
	
	
	
	
	
	

}
