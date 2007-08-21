package org.jboss.ide.eclipse.as.classpath.ui;

import org.eclipse.osgi.util.NLS;

public class Messages {
	public static String jeeClasspathAdding;
	public static String jeeClasspathBody1;
	public static String jeeClasspathBody2;
	public static String jeeClasspathDescription;
	public static String ejb3ClasspathPageTitle;
	public static String ejb3ClasspathPageDescription;
	static {
		NLS.initializeMessages(ClasspathUIPlugin.PLUGIN_ID + ".ClasspathUIMessages", Messages.class);
	}
}
