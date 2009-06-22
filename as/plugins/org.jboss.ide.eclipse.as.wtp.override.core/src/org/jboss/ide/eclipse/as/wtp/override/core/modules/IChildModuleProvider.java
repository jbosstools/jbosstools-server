package org.jboss.ide.eclipse.as.wtp.override.core.modules;

import org.eclipse.wst.server.core.IModule;


public interface IChildModuleProvider {
	/**
	 * Returns the modules contained within this module. The returned modules 
	 * can be either modulecore projects or representations of binary jars
	 * 
	 * @return a possibly empty array of modules contained within this application
	 */
	public IModule[] getModules();

	/**
	 * Returns the URI of the given module within this enterprise application.
	 * 
	 * @param module a module within this application
	 * @return the URI of the given module, or <code>null</code> if the URI could
	 *    not be found
	 */
	public String getURI(IModule module);
}
