/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.resolvers;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.internal.variables.StringSubstitutionEngine;
import org.eclipse.core.internal.variables.StringVariableManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.server.IRuntimeProvider;
import org.jboss.tools.foundation.core.expressions.ExpressionResolutionException;
import org.jboss.tools.foundation.core.expressions.ExpressionResolver;
import org.jboss.tools.foundation.core.expressions.IVariableResolver;


/**
 * A RuntimeVariableResolver is a Variable Resolver for use with 
 * an {@link ExpressionResolver} to resolve a string with variables. 
 * 
 * This will delegate the resolution of all variables
 * to the eclipse variables framework. One exception is it will properly
 * append the runtime's name as an argument for any variables that are handled by 
 * ConfigNameResolver.
 * 
 * Example of use:
 *    IRuntime rt = ...
 *    RuntimeVariableResolver resolver = new RuntimeVariableResolver(rt);
 *    ExpressionResolver process = new ExpressionResolver(resolver);
 *    String result = process.resolve("${jboss_config_dir}/deploy/someDeployedFolder");
 * 
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RuntimeVariableResolver implements IVariableResolver {
	private IRuntime rt;
	private IRuntimeProvider rtProvider;
	public RuntimeVariableResolver(IRuntime rt) {
		this.rt = rt;
	}

	public RuntimeVariableResolver(IRuntimeProvider rt) {
		this.rtProvider = rt;
	}

	private IRuntime getRuntime() {
		return rt != null ? rt : rtProvider != null ? rtProvider.getRuntime() : null;
	}
	
	private String getRuntimeName() {
		IRuntime r = getRuntime();
		return r == null ? null : r.getName();
	}
	
	public String resolve(String variable, String argument) throws ExpressionResolutionException {
		List<String> runtimeVars = Arrays.asList(ConfigNameResolver.ALL_VARIABLES);
		if( runtimeVars.contains(variable)) {
			if( argument == null ) 
				argument = getRuntimeName();
		}
		
		// Delegate it to eclipse
		StringSubstitutionEngine engine = new StringSubstitutionEngine();
		try {
			return engine.performStringSubstitution(getVariablePattern(variable, argument), true,
					true, StringVariableManager.getDefault());
		} catch(CoreException ce) {
			throw new ExpressionResolutionException(ce);
		}
	}
	

	private String getVariablePattern(String var, String arg) {
		if( arg != null )
			return "${" + var + ":" + arg + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return "${" + var + "}";//$NON-NLS-1$ //$NON-NLS-2$
	}
}