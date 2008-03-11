/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.jboss.ide.eclipse.as.ui.util.PackageTypeSearcher.ResultFilter;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class ServiceXMLEditorUtil {
	public static IType findType(String codeClass ) {
		final String codeClass2 = codeClass;
		if( codeClass == null ) return null;
		ResultFilter filter = new ResultFilter() {
			public boolean accept(Object found) {
				if( found instanceof IType ) {
					IType type = (IType)found;
					if( type.getFullyQualifiedName().equals(codeClass2)) {
						return true;
					}
					return false;
				}
				return true;
			}
		};
		PackageTypeSearcher searcher = new PackageTypeSearcher(codeClass, filter);
		ArrayList foundTypes = searcher.getTypeMatches();
		if( foundTypes.size() == 1 ) {
			return (IType)foundTypes.get(0);
		}
		return null;
	}

	public static IMethod[] getAllMethods(IType type) {
		ArrayList methods = new ArrayList();
		try {
			methods.addAll(Arrays.asList(type.getMethods()));
			String parentTypeName = type.getSuperclassName();
			IType parentType = findType(parentTypeName);
			if( parentType != null ) {
				methods.addAll(Arrays.asList(getAllMethods(parentType)));
			}
		} catch( JavaModelException jme ) {
			jme.printStackTrace();
		}
		return (IMethod[]) methods.toArray(new IMethod[methods.size()]);
	}
	
	public static String[] findAttributesFromMethods(IMethod[] methods, String attributeCurrentValue) {
		ArrayList attributeNames = new ArrayList();
		String getterPrefix = "get" + attributeCurrentValue;
		
		
		for( int i = 0; i < methods.length; i++ ) {
			if( methods[i].getElementName().startsWith(getterPrefix)) {
				String atName = methods[i].getElementName().substring(3);
				String setterName = "set" + atName;
				for( int j = 0; j < methods.length; j++ ) {
					if( methods[j].getElementName().equals(setterName)) {
						// there's a getter and a setter... 
						try {
							if( methods[j].getParameterNames().length == 1 ) {
								// one parameter... 
								String[] paramTypes = methods[j].getParameterTypes();
								String getterReturnType = methods[i].getReturnType();
								if( getterReturnType.equals(paramTypes[0])) {
									attributeNames.add(atName);
								}
							}
						} catch( JavaModelException jme ) {
							
						}
					}
				}
			}
		}
		
		return (String[]) attributeNames.toArray(new String[attributeNames.size()]);
	}
}
