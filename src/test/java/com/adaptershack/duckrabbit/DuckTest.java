package com.adaptershack.duckrabbit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;

import org.junit.Test;

public class DuckTest {
	
	// ok, let's say you have an interface
	public interface Duck {
		String speak();
		boolean canWalk();
		boolean canSwim();
		boolean canFly();
		boolean inSeason();
	}
	
	
	// Now, in real life the classes that
	// implement that interface probably come out
	// of some factory you don't control, from
	// code you can't change. We'll simulate that
	// just by making everything about this class
	// final so you aren't tempted to solve your
	// problem with simple inheritance
	public final class DuckImpl implements Duck {

		@Override
		public final String speak() {
			return "Quack!";
		}

		@Override
		public final boolean canFly() {
			return true;
		}

		@Override
		public boolean canWalk() {
			return true;
		}

		@Override
		public boolean canSwim() {
			return true;
		}
		
		public boolean inSeason() {
			return true;
		}
		
	}	
	
	
	
	/**
	 * This shows what you could need to do if you did
	 * not have the DuckRabbit package at your disposal.
	 */
	@Test
	public void controlCase() {

		// so, you have one of these
		final DuckImpl impl = new DuckImpl();
		
		// now, say you want to override the behavior
		// of one or two methods, while re-using the rest
		
		// when you can't use inheritance, you use delegation
		Duck daffy = new Duck() {

			// first implement the methods
			// you actually wish to override..
			
			public String speak() {
				return "You're despicable";
			}
			
			public boolean canFly() {
				// date when that darn fool duck forgot he could fly
				return LocalDate.now().isBefore( LocalDate.of(1946,1,1));
			}

			// now for the rest of the methods,
			// you just delegate to the backing class
			
			@Override
			public boolean canWalk() {
				return impl.canWalk();
			}

			@Override
			public boolean canSwim() {
				return impl.canSwim();
			}

			@Override
			public boolean inSeason() {
				return impl.inSeason();
			}
			
			// what if there were a LOT more than 3 of these?
			// this could get tedious
			
		};

		// this will call our own implementation
		assertEquals("You're despicable", daffy.speak());
		assertFalse(daffy.canFly());
		
		// these will all tunnel through to the backing object
		assertTrue(daffy.canSwim());
		assertTrue(daffy.canWalk());
		assertTrue(daffy.inSeason());
	
	}
	
	/**
	 * This shows how you use this class to avoid implementing
	 * all those wrapper methods
	 */
	@Test
	public void testOurWay() {
		
		DuckImpl impl = new DuckImpl();
		
		Duck daffy = new DynamicDelegator<Duck>(impl) {

			// implement only the 2 methods you 
			// need to override, forget the rest
			
			@SuppressWarnings("unused")
			public String speak() {
				return "You're despicable";
			}
			
			@SuppressWarnings("unused")
			public boolean canFly() {
				// date when that darn fool duck forgot he could fly
				return LocalDate.now().isBefore( LocalDate.of(1946,1,1));
			}
			
		}.getProxy();

		// this will call our own implementation
		assertEquals("You're despicable", daffy.speak());
		assertFalse(daffy.canFly());
		
		// these will all tunnel through to the backing object
		assertTrue(daffy.canSwim());
		assertTrue(daffy.canWalk());
		assertTrue(daffy.inSeason());
	
	}
	
	@Test
	public void testOurWay2() {
		
		DuckImpl impl = new DuckImpl();
		
		Duck daffy = DynamicDelegator.getProxy(Duck.class,
			new Object() {

				@SuppressWarnings("unused")
				public String speak() {
					return "You're despicable";
				}
				
				@SuppressWarnings("unused")
				public boolean canFly() {
					// date when that darn fool duck forgot he could fly
					return LocalDate.now().isBefore( LocalDate.of(1946,1,1));
				}
			
			}, impl);

		// this will call our own implementation
		assertEquals("You're despicable", daffy.speak());
		assertFalse(daffy.canFly());
		
		// these will all tunnel through to the backing object
		assertTrue(daffy.canSwim());
		assertTrue(daffy.canWalk());
		assertTrue(daffy.inSeason());
	
	}

	@Test
	public void coldDuckTest() {
		
		Duck daffy = new DynamicDelegator<Duck>(Duck.class) {

			// implement only the 2 methods you 
			// need to override, forget the rest
			
			@SuppressWarnings("unused")
			public String speak() {
				return "You're despicable";
			}
			
			@SuppressWarnings("unused")
			public boolean canFly() {
				// date when that darn fool duck forgot he could fly
				return LocalDate.now().isBefore( LocalDate.of(1946,1,1));
			}
			
		}.getProxy();
		
		// this will call our own implementation
		assertEquals("You're despicable", daffy.speak());
		assertFalse(daffy.canFly());
		
		// these will throw
		try {
			assertTrue(daffy.canSwim());
			assertTrue(daffy.canWalk());
			assertTrue(daffy.inSeason());

			fail();
		} catch (Exception e) {
			// don't say we didn't warn you
		}
		
	}
	
	
}
