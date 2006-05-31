package org.jboss.ide.eclipse.as.core.util;

public class ASDebug {
	public static boolean on = true;
	
	public static void p(String output, Object caller) {
		if( on ) {
			if( caller instanceof Class ) {
				System.out.println("[" + ((Class)caller).getSimpleName() + "] " + output);
			} else {
				System.out.println("[" + caller.getClass().getSimpleName() + "] " + output);
			}
		}
	}
}
