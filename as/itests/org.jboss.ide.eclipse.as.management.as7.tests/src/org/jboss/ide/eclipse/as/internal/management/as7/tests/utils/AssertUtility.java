package org.jboss.ide.eclipse.as.internal.management.as7.tests.utils;

import java.lang.reflect.Array;

import junit.framework.Assert;

public class AssertUtility extends Assert {
	   public static boolean areArraysEqual(Object o1, Object o2) {
	        return areArrayLengthsEqual(o1, o2)
	            && areArrayElementsEqual(o1, o2);
	    }

	    public static boolean areArrayLengthsEqual(Object o1, Object o2) {
	        return Array.getLength(o1) == Array.getLength(o2);
	    }

	    public static boolean areArrayElementsEqual(Object o1, Object o2) {
	        for (int i = 0; i < Array.getLength(o1); i++) {
	            if (!areEqual(Array.get(o1, i), Array.get(o2, i))) return false;
	        }
	        return true;
	    }

	    public static boolean isArray(Object o) {
	        return o.getClass().isArray();
	    }

	    public static boolean areEqual(Object o1, Object o2) {
	        if (o1 == null) {
	            return o2 == null;
	        } else if (o2 != null && isArray(o1)) {
	            return isArray(o2) && areArraysEqual(o1, o2);
	        } else {
	            return o1.equals(o2);
	        }
	    }

}
