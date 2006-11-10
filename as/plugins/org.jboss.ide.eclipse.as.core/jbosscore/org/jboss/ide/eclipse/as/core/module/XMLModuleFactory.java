package org.jboss.ide.eclipse.as.core.module;


public class XMLModuleFactory extends PathModuleFactory {

	private static String XML_FILE = "jboss.xml";
	private static String VERSION = "1.0";
	
	public static final String FACTORY_ID = "org.jboss.ide.eclipse.as.core.XMLFactory";

	private static XMLModuleFactory factory;
	public static XMLModuleFactory getDefault() {
		if( factory == null ) {
			factory = (XMLModuleFactory)PathModuleFactory.getDefaultInstance(FACTORY_ID);
		}
		return factory;
	}

	public boolean supports(String path) {
		if( path.endsWith(".xml"))
			return true;
		return false;
	}
	
	public String getModuleType(String path) {
		return XML_FILE;
	}

	public String getModuleVersion(String path) {
		return VERSION;
	}

	
}
