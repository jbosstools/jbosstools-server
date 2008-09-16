/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.ide.eclipse.archives.core.xpl;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IVariableManager;

/**
 * Performs string substitution for context and value variables.
 */
public class StringSubstitutionEngineClone {
	
	// delimiters
	private static final String VARIABLE_START = "${"; 
	private static final char VARIABLE_END = '}';
	// parsing states
	private static final int SCAN_FOR_START = 0;
	private static final int SCAN_FOR_END = 1;
	
	/**
	 * Resulting string
	 */
	private StringBuffer fResult;
	
	class VariableReference {
		private StringBuffer fText;
		public VariableReference() {
			fText = new StringBuffer();
		}
		public void append(String text) {
			fText.append(text);
		}
		public String getText() {
			return fText.toString();
		}
	}
	
	public String performStringSubstitution(String expression, boolean reportUndefinedVariables, IVariableManager manager ) throws CoreException {
		substitute(expression, reportUndefinedVariables,manager );
		return fResult.toString();
	}
	
	public void validateStringVariables(String expression, IVariableManager manager ) throws CoreException {
		performStringSubstitution(expression, true, manager );
	}
	
	private HashSet substitute(String expression, boolean reportUndefinedVariables, IVariableManager manager) throws CoreException {
		Stack fStack;
		fResult = new StringBuffer(expression.length());
		fStack = new Stack();
		
		HashSet resolvedVariables = new HashSet();

		int pos = 0;
		int state = SCAN_FOR_START;
		while (pos < expression.length()) {
			switch (state) {
				case SCAN_FOR_START:
					int start = expression.indexOf(VARIABLE_START, pos);
					if (start >= 0) {
						int length = start - pos;
						// copy non-variable text to the result
						if (length > 0) {
							fResult.append(expression.substring(pos, start));
						}
						pos = start + 2;
						state = SCAN_FOR_END;

						fStack.push(new VariableReference());						
					} else {
						// done - no more variables
						fResult.append(expression.substring(pos));
						pos = expression.length();
					}
					break;
				case SCAN_FOR_END:
					// be careful of nested variables
					start = expression.indexOf(VARIABLE_START, pos);
					int end = expression.indexOf(VARIABLE_END, pos);
					if (end < 0) {
						// variables are not completed
						VariableReference tos = (VariableReference)fStack.peek();
						tos.append(expression.substring(pos));
						pos = expression.length();
					} else {
						if (start >= 0 && start < end) {
							// start of a nested variable
							int length = start - pos;
							if (length > 0) {
								VariableReference tos = (VariableReference)fStack.peek();
								tos.append(expression.substring(pos, start));
							}
							pos = start + 2;
							fStack.push(new VariableReference());	
						} else {
							// end of variable reference
							VariableReference tos = (VariableReference)fStack.pop();
							String substring = expression.substring(pos, end);							
							tos.append(substring);
							resolvedVariables.add(substring);
							
							pos = end + 1;
							String value= resolve(tos, reportUndefinedVariables, manager);
							if (value == null) {
								value = ""; //$NON-NLS-1$
							}
							if (fStack.isEmpty()) {
								// append to result
								fResult.append(value);
								state = SCAN_FOR_START;
							} else {
								// append to previous variable
								tos = (VariableReference)fStack.peek();
								tos.append(value);
							}
						}
					}
					break;
			}
		}
		// process incomplete variable references
		while (!fStack.isEmpty()) {
			VariableReference tos = (VariableReference)fStack.pop();
			if (fStack.isEmpty()) {
				fResult.append(VARIABLE_START);
				fResult.append(tos.getText());
			} else {
				VariableReference var = (VariableReference)fStack.peek();
				var.append(VARIABLE_START);
				var.append(tos.getText());
			}
		}
		

		return resolvedVariables;
	}

	/**
	 * Resolve and return the value of the given variable reference,
	 * possibly <code>null</code>. 
	 * 
	 * @param var
	 * @param reportUndefinedVariables whether to report undefined variables as
	 *  an error
	 *  @param manager Someone to call back to for the variable's values
	 * @return variable value, possibly <code>null</code>
	 * @exception CoreException if unable to resolve a value
	 */
	private String resolve(VariableReference var, boolean reportUndefinedVariables, IVariableManager manager) throws CoreException {
		String text = var.getText();
		String name = null;
		name = text;
		if( !manager.containsVariable(name)) {
			if( reportUndefinedVariables )
				throw new CoreException(new Status(IStatus.ERROR, ArchivesCore.PLUGIN_ID, "Variable " + name + " undefined")); 
			return getOriginalVarText(var);
		}
		
		String ret = manager.getVariableValue(name);
		if(ret == null)
			return getOriginalVarText(var);
		return ret;
	}

	private String getOriginalVarText(VariableReference var) {
		StringBuffer res = new StringBuffer(var.getText());
		res.insert(0, VARIABLE_START);
		res.append(VARIABLE_END);
		return res.toString();
	}
}
