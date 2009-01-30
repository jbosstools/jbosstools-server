/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.core.model.internal.xb;

import org.jboss.xb.binding.GenericObjectModelProvider;
import org.jboss.xb.binding.MarshallingContext;


/**
 * Necessary class for JBoss XB
 * @author Marshall
 *
 */
public class XbPackagesObjectProvider implements GenericObjectModelProvider {

	public Object getRoot(Object o, MarshallingContext context, String namespaceURI, String localName) {
		return o;
	}

	protected Object getNodeChildren(XbPackageNode node, String name)
	{
		if ("package".equals(name)) { //$NON-NLS-1$
			return node.getChildren(XbPackage.class);
		}
		else if ("folder".equals(name)) {//$NON-NLS-1$
			return node.getChildren(XbFolder.class);
		}
		else if ("fileset".equals(name)) {//$NON-NLS-1$
			return node.getChildren(XbFileSet.class);
		}
		else if ("properties".equals(name) && node instanceof XbPackageNodeWithProperties) {//$NON-NLS-1$
			return ((XbPackageNodeWithProperties)node).getProperties();
		}
		else if ("property".equals(name) && node instanceof XbProperties) {//$NON-NLS-1$
			return ((XbProperties)node).getProperties().getPropertyElements();
		}
		else if( "buildAction".equals(name) && node instanceof XbPackage) {//$NON-NLS-1$
			return ((XbPackage)node).getActions();
		}

		return null;
	}

	public Object getChildren(Object object, MarshallingContext context, String namespaceURI, String localName)
	{
		if (object instanceof XbPackageNode) {
			Object ret = getNodeChildren(((XbPackageNode)object), localName);
			return ret;
		}
		return null;
	}


	public Object getAttributeValue(Object object, MarshallingContext context,
			String namespaceURI, String localName) {
		if( object instanceof XbPackages ) {
			if("version".equals(localName))//$NON-NLS-1$
				return ((XbPackages)object).getVersion();
		}
		else if (object instanceof XbPackage) {
			XbPackage pkg = (XbPackage)object;
			if("id".equals(localName))//$NON-NLS-1$
				return pkg.getId();
			else if ("type".equals(localName))//$NON-NLS-1$
				return pkg.getPackageType();
			else if ("name".equals(localName))//$NON-NLS-1$
				return pkg.getName();
			else if ("exploded".equals(localName))//$NON-NLS-1$
				return Boolean.valueOf(pkg.isExploded());
			else if ("todir".equals(localName))//$NON-NLS-1$
				return pkg.getToDir();
			else if ("inWorkspace".equals(localName))//$NON-NLS-1$
				return ""+pkg.isInWorkspace();//$NON-NLS-1$
		}
		else if (object instanceof XbFolder) {
			XbFolder folder = (XbFolder) object;
			if ("name".equals(localName))//$NON-NLS-1$
				return folder.getName();
		}
		else if (object instanceof XbFileSet) {
			XbFileSet fileset = (XbFileSet)object;
			if ("dir".equals(localName))//$NON-NLS-1$
				return fileset.getDir();
			else if ("includes".equals(localName))//$NON-NLS-1$
				return fileset.getIncludes();
			else if ("excludes".equals(localName))//$NON-NLS-1$
				return fileset.getExcludes();
			else if ("inWorkspace".equals(localName))//$NON-NLS-1$
				return "" + fileset.isInWorkspace();//$NON-NLS-1$
			else if("flatten".equals(localName))//$NON-NLS-1$
				return new Boolean(fileset.isFlattened()).toString();
		}
		else if (object instanceof XbProperty) {
			XbProperty prop = (XbProperty) object;
			if ("name".equals(localName))//$NON-NLS-1$
				return prop.getName();
			else if ("value".equals(localName))//$NON-NLS-1$
				return prop.getValue();
		} else if( object instanceof XbAction ) {
			XbAction action = (XbAction)object;
			if("time".equals(localName))//$NON-NLS-1$
				return action.getTime();
			if("type".equals(localName))//$NON-NLS-1$
				return action.getType();
		}
		return null;
	}

	// do not care ;)
	public Object getElementValue(Object object, MarshallingContext context, String namespaceURI, String localName) {
		return null;
	}
}
