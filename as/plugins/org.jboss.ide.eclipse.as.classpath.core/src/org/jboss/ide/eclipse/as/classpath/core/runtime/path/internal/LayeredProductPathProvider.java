/*******************************************************************************
 * Copyright (c) 2011-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlot;
import org.jboss.ide.eclipse.as.core.resolvers.ConfigNameResolver;
import org.jboss.tools.foundation.core.expressions.ExpressionResolver;
import org.jboss.tools.foundation.core.expressions.IVariableResolver;
import org.jboss.tools.foundation.core.xml.XMLMemento;
/**
 * This is an implementation of {@link IRuntimePathProvider}
 * which is capable of checking various different paths for a 
 * jboss module path. 
 */
public class LayeredProductPathProvider implements IRuntimePathProvider {
	public static final String PROP_MODULE_NAME = "moduleName"; //$NON-NLS-1$
	public static final String PROP_SLOT = "slot"; //$NON-NLS-1$
	
	
	private IVariableResolver resolver;
	
	private String moduleName;
	private String slot;
	/**
	 * Constructor takes a module name, and a slot. 
	 * 
	 * The name is the name of a given jboss module, 
	 * such as javax.el.api   or  org.jboss.as.controller
	 * 
	 * The slot is either null (to be interpreted as 'main'),
	 * 'main', or a version number. 
	 * 
	 * The path will be resolved first according to 
	 * the patch overlays. If no jar is found in the
	 * patch overlays, it will be looked for in 
	 * the appropriate folder in either
	 * modules, or modules/system/layers/base
	 * 
	 * @param name
	 * @param path
	 */
	public LayeredProductPathProvider(String moduleName, String slot) {
		this.moduleName = moduleName;
		this.slot = (slot != null && slot.trim().isEmpty() ? null : slot);
	}
	
	public LayeredProductPathProvider(ModuleSlot ms) {
		this.moduleName = ms.getModule();
		this.slot = (ms.getSlot() != null && ms.getSlot().trim().isEmpty() ? null : ms.getSlot());
	}

	public String getModule() {
		return moduleName;
	}
	
	public String getSlot() {
		return slot;
	}
	
	public IPath[] getAbsolutePaths() {
		String runtimeHomePattern = "${" + ConfigNameResolver.JBOSS_SERVER_HOME + "}"; //$NON-NLS-1$ //$NON-NLS-2$
		String runtimeHome = new ExpressionResolver(resolver).resolve(runtimeHomePattern);
		IPath modulesFolder = new Path(runtimeHome).append("modules"); //$NON-NLS-1$
		return new ModuleSlot(moduleName, slot).getJars(modulesFolder);
	}
	
	@Override
	public void setVariableResolver(IVariableResolver resolver) {
		this.resolver = resolver;
	}
	
	@Override
	public String getDisplayString() {
		return "JBoss Module: " + moduleName + " [" + (slot == null ? "main" : slot) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	@Override
	public void saveInMemento(XMLMemento memento) {
		XMLMemento child = (XMLMemento)memento.createChild("layeredProductPath");//$NON-NLS-1$
		child.putString(PROP_MODULE_NAME, moduleName);
		child.putString(PROP_SLOT, slot == null ? "main" : slot);//$NON-NLS-1$
	}

}