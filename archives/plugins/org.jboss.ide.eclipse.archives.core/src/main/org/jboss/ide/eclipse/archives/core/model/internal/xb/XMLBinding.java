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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveImpl;


/**
 * This class is responsible for binding some xml file to it's proper
 * objects. In short, it marshalls and unmarshalls the data.
 * @author Marshall
 * @author Rob Stryker
 */
public class XMLBinding {	
	
	public static class XbException extends Exception {
		private Exception parent;
		public XbException(Exception e) {
			super();
			parent = e;
		}
		public Exception getException() {
			return parent;
		}
		public String getMessage() {
			return parent.getCause() == null ? parent.getMessage() : parent.getCause().getMessage();
		}
		public Throwable getCause() {
			return parent;
		}
	}
	
 	public static String serializePackages(XbPackages packages, IProgressMonitor monitor) throws XbException {
 		try {
 			StringWriter sw = new StringWriter();
			XMLBinding.marshall(packages, sw, monitor);
 			return new String(sw.toString());
 		} catch( Exception e ) {
 			throw new XbException(e);
 		}
 	}
	public static void marshallToFile(XbPackages element, IPath filePath, IProgressMonitor monitor) throws XbException {
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(filePath.toFile()));
			XMLBinding.marshall(element, writer, monitor);
		} catch( XbException xbe ) {
			throw xbe;
		} catch( IOException ioe ) {
			throw new XbException(ioe);
		}
		finally {
			try {
				if( writer != null ) writer.close();
			} catch( IOException ioe) {}
		}
	}

	public static String marshall(IArchive topLevelArchive, IProgressMonitor monitor ) throws XbException {
		if( topLevelArchive.isTopLevel() && topLevelArchive instanceof ArchiveImpl ) {
			XbPackages packs = (XbPackages)((ArchiveImpl)topLevelArchive).getNodeDelegate().getParent();
			StringWriter sw = new StringWriter();
			marshall(packs, sw, monitor);
			return sw.toString();
		}
		return null;
	}

	public static void marshall (final XbPackages element, final Writer writer,
			final IProgressMonitor monitor) throws XbException {
		XMLMemento root = XMLMemento.createWriteRoot("packages"); //$NON-NLS-1$
		root.putString("version", new Double(element.getVersion()).toString()); //$NON-NLS-1$
		List packagesToAdd = element.getChildren(XbPackage.class);
		List props = element.getChildren(XbProperties.class);
		marshallAddPackages(root, nullSafe(packagesToAdd));
		marshallAddProperties(root, nullSafe(props));
		try{
			String s = root.saveToString();
			writer.write(s);
		} catch(IOException ioe) {
			throw new XbException(ioe);
		}
	}
	private static List nullSafe(List list) {
		return list == null ? new ArrayList() : list;
	}
	private static boolean isEmpty(String s) { 
		return (s == null || s.isEmpty());
	}
	private static void marshallAddPackages(XMLMemento memento, List packages) throws XbException {
		Iterator i = packages.iterator();
		while(i.hasNext()) {
			XMLMemento childMemento = (XMLMemento)memento.createChild("package"); //$NON-NLS-1$
			XbPackage childXb = (XbPackage)i.next();
			if( childXb.getName() == null ) 
				throw new XbException(new Exception("Element 'package' missing attribute 'name'")); //$NON-NLS-1$
			childMemento.putString("name", childXb.getName()); //$NON-NLS-1$
			if( !isEmpty(childXb.getPackageType()) )
				childMemento.putString("type", childXb.getPackageType()); //$NON-NLS-1$
			if( !isEmpty( childXb.getToDir()) )
				childMemento.putString("todir", childXb.getToDir()); //$NON-NLS-1$
			if( !isEmpty(childXb.getId() ) )
				childMemento.putString("id", childXb.getId()); //$NON-NLS-1$
			childMemento.putString("exploded", new Boolean(childXb.isExploded()).toString()); //$NON-NLS-1$
			childMemento.putString("inWorkspace", new Boolean(childXb.isInWorkspace()).toString()); //$NON-NLS-1$

			// Add children
			marshallAddPackages(childMemento, nullSafe(childXb.getChildren(XbPackage.class)));
			addFileset(childMemento, nullSafe(childXb.getChildren(XbFileSet.class)));
			addLibFileset(childMemento, nullSafe(childXb.getChildren(XbLibFileSet.class)));
			addFolders(childMemento, nullSafe(childXb.getChildren(XbFolder.class)));
			marshallAddProperties(childMemento, nullSafe(childXb.getChildren(XbProperties.class)));
		}
	}

	private static void addFolders(XMLMemento memento, List folders) throws XbException {
		Iterator i = folders.iterator();
		while(i.hasNext()) {
			XMLMemento childMemento = (XMLMemento)memento.createChild("folder"); //$NON-NLS-1$
			XbFolder childXb = (XbFolder)i.next();
			if( childXb.getName() == null ) 
				throw new XbException(new Exception("Element 'folder' missing attribute 'name'")); //$NON-NLS-1$
			childMemento.putString("name", childXb.getName()); //$NON-NLS-1$

			// Add children
			marshallAddPackages(childMemento, nullSafe(childXb.getChildren(XbPackage.class)));
			addFileset(childMemento, nullSafe(childXb.getChildren(XbFileSet.class)));
			addLibFileset(childMemento, nullSafe(childXb.getChildren(XbLibFileSet.class)));
			addFolders(childMemento, nullSafe(childXb.getChildren(XbFolder.class)));
			marshallAddProperties(childMemento, nullSafe(childXb.getChildren(XbProperties.class)));
		}
	}

	private static void addFileset(XMLMemento memento, List xbList) throws XbException {
		Iterator i = xbList.iterator();
		while(i.hasNext()) {
			XMLMemento fsMemento = (XMLMemento)memento.createChild("fileset"); //$NON-NLS-1$
			XbFileSet fsXb = (XbFileSet)i.next();
			if( fsXb.getDir() == null ) 
				throw new XbException(new Exception("Element 'fileset' missing attribute 'dir'")); //$NON-NLS-1$
			if( fsXb.getIncludes() == null ) 
				throw new XbException(new Exception("Element 'fileset' missing attribute 'includes'")); //$NON-NLS-1$

			fsMemento.putString("dir", fsXb.getDir()); //$NON-NLS-1$
			fsMemento.putString("includes", fsXb.getIncludes()); //$NON-NLS-1$
			if( !isEmpty(fsXb.getExcludes()))
				fsMemento.putString("excludes", fsXb.getExcludes()); //$NON-NLS-1$
			fsMemento.putString("inWorkspace",new Boolean(fsXb.isInWorkspace()).toString()); //$NON-NLS-1$ 
			fsMemento.putString("flatten", new Boolean(fsXb.isFlatten()).toString()); //$NON-NLS-1$
			marshallAddProperties(fsMemento, nullSafe(fsXb.getChildren(XbProperties.class)));
		}
	}

	private static void addLibFileset(XMLMemento memento, List xbList) {
		Iterator i = xbList.iterator();
		while(i.hasNext()) {
			XMLMemento fsMemento = (XMLMemento)memento.createChild("lib-fileset"); //$NON-NLS-1$
			XbLibFileSet fsXb = (XbLibFileSet)i.next();
			if( !isEmpty(fsXb.getId()))
				fsMemento.putString("name", fsXb.getId()); //$NON-NLS-1$
			marshallAddProperties(fsMemento, nullSafe(fsXb.getChildren(XbProperties.class)));
		}
	}

	private static void marshallAddProperties(XMLMemento memento, List properties) {
		// should only have one "properties"
		XbProperties propsObj = properties.size() == 0 ? null : (XbProperties)properties.get(0);
		XMLMemento props = (XMLMemento)memento.createChild("properties"); //$NON-NLS-1$
		if( propsObj != null ) {
			List indivProps = propsObj.getAllChildren();
			Iterator j = indivProps.iterator();
			while(j.hasNext()) {
				XbProperty prop = (XbProperty)j.next();
				XMLMemento propMemento = (XMLMemento) props.createChild("property"); //$NON-NLS-1$
				propMemento.putString("name", prop.getName()); //$NON-NLS-1$
				propMemento.putString("value", prop.getValue()); //$NON-NLS-1$
			}
		}
	}

	public static XbPackages unmarshal(File file, IProgressMonitor monitor) throws XbException {
		try {
			FileInputStream fis = new FileInputStream(file);
			return unmarshal(fis, monitor);
		} catch( FileNotFoundException fnfe ) {
			throw new XbException(fnfe);
		} catch( XbException xbe) {
			throw xbe;
		}
	}

	protected static XbPackages unmarshal (final InputStream in,
			final IProgressMonitor monitor) throws XbException {
		XMLMemento root = XMLMemento.createReadRoot(in);
		if( root == null ) {
			throw new XbException(new Exception("Unable to parse xml string")); //$NON-NLS-1$
		}
		String versionString = root.getString("version"); //$NON-NLS-1$
		System.out.println("unmarshalling: " + versionString); //$NON-NLS-1$
		XbPackages packs = new XbPackages();
		Double d = IArchiveModelRootNode.DESCRIPTOR_VERSION_1_0;
		if( versionString != null ) {
			try {
				d = Double.parseDouble(versionString);
			} catch(NumberFormatException nfe ) {
				throw new XbException(nfe);
			}
		}
		packs.setVersion(d);

		IMemento[] packageChildren = root.getChildren("package"); //$NON-NLS-1$
		unmarshallPackageList(packs, packageChildren);
		IMemento[] propertiesChild = root.getChildren("properties"); //$NON-NLS-1$
		if( propertiesChild != null && propertiesChild.length == 1)
			unmarshallProperties(packs, propertiesChild[0]);
		return packs;
	}

	private static void unmarshallPackageList(XbPackageNodeWithProperties packs, IMemento[] packageChildren) throws XbException {
		for( int i = 0; i < packageChildren.length; i++ ) {
			XbPackage pack = new XbPackage();
			// name, type, toDir, exploded, inWorkspace, id
			String name = packageChildren[i].getString("name"); //$NON-NLS-1$
			String type = packageChildren[i].getString("type"); //$NON-NLS-1$
			String toDir = packageChildren[i].getString("todir"); //$NON-NLS-1$
			String id = packageChildren[i].getString("id"); //$NON-NLS-1$
			String exploded = packageChildren[i].getString("exploded"); //$NON-NLS-1$
			String inWorkspace = packageChildren[i].getString("inWorkspace"); //$NON-NLS-1$
			String exploded2 = exploded == null ? "" : exploded; //$NON-NLS-1$
			String inWorkspace2 = inWorkspace == null ? "" : inWorkspace; //$NON-NLS-1$
			boolean explodedValid = ("true".equals(exploded2.toLowerCase()) || "false".equals(exploded2.toLowerCase())); //$NON-NLS-1$ //$NON-NLS-2$
			boolean inWorkspaceValid = ("true".equals(inWorkspace2.toLowerCase()) || "false".equals(inWorkspace2.toLowerCase())); //$NON-NLS-1$ //$NON-NLS-2$
			boolean bExploded = explodedValid ? Boolean.parseBoolean(exploded) : false;
			boolean bInWorkspace = inWorkspaceValid ? Boolean.parseBoolean(inWorkspace) : true;
			pack.setName(name);
			pack.setPackageType(type);
			pack.setToDir(toDir);
			pack.setId(id);
			pack.setExploded(bExploded);
			pack.setInWorkspace(bInWorkspace);
			
			if( name == null )
				throw new XbException(new Exception("Element 'package' missing required attribute 'name'")); //$NON-NLS-1$

			// package
			IMemento[] inner = packageChildren[i].getChildren("package"); //$NON-NLS-1$
			if( inner != null && inner.length == 1)
				unmarshallPackageList(pack, inner);

			// fileset
			IMemento[] fsets = packageChildren[i].getChildren("fileset"); //$NON-NLS-1$
			if( fsets != null && fsets.length == 1)
				unmarshallFilesets(pack, fsets);

			// lib-fileset
			IMemento[] libfsets = packageChildren[i].getChildren("lib-fileset"); //$NON-NLS-1$
			if( libfsets != null && libfsets.length == 1)
				unmarshallLibFilesets(pack, libfsets);

			// folder
			IMemento[] folders = packageChildren[i].getChildren("folder"); //$NON-NLS-1$
			if( folders != null && folders.length == 1)
				unmarshallFolders(pack, folders);

			// properties
			IMemento[] propertiesChild = packageChildren[i].getChildren("properties"); //$NON-NLS-1$
			if( propertiesChild != null && propertiesChild.length == 1)
				unmarshallProperties(packs, propertiesChild[0]);

			packs.addChild(pack);
		}
	}
	private static void unmarshallFolders(XbPackageNodeWithProperties node, IMemento[] folders) throws XbException {
		for( int i = 0; i < folders.length; i++ ) {
			XbFolder folder = new XbFolder();
			// name, type, toDir, exploded, inWorkspace, id
			String name = folders[i].getString("name"); //$NON-NLS-1$
			folder.setName(name);
			if( name == null )
				throw new XbException(new Exception("Element 'folder' missing required attribute 'name'")); //$NON-NLS-1$

			// package
			IMemento[] inner = folders[i].getChildren("package"); //$NON-NLS-1$
			if( inner != null && inner.length == 1)
				unmarshallPackageList(folder, inner);

			// fileset
			IMemento[] fsets = folders[i].getChildren("fileset"); //$NON-NLS-1$
			if( fsets != null && fsets.length == 1)
				unmarshallFilesets(folder, fsets);

			// lib-fileset
			IMemento[] libfsets = folders[i].getChildren("lib-fileset"); //$NON-NLS-1$
			if( libfsets != null && libfsets.length == 1)
				unmarshallLibFilesets(folder, libfsets);

			// folder
			IMemento[] folders2 = folders[i].getChildren("folder"); //$NON-NLS-1$
			if( folders2 != null && folders2.length == 1)
				unmarshallFolders(folder, folders2);

			// properties
			IMemento[] propertiesChild = folders[i].getChildren("properties"); //$NON-NLS-1$
			if( propertiesChild != null && propertiesChild.length == 1)
				unmarshallProperties(folder, propertiesChild[0]);

			node.addChild(folder);
		}
	}
	private static void unmarshallFilesets(XbPackageNodeWithProperties node, IMemento[] fs) throws XbException {
		for( int i = 0; i < fs.length; i++ ) {
			XbFileSet fileset = new XbFileSet();
			// name, type, toDir, exploded, inWorkspace, id
			String dir = fs[i].getString("dir"); //$NON-NLS-1$
			String inc = fs[i].getString("includes"); //$NON-NLS-1$
			String exc = fs[i].getString("excludes"); //$NON-NLS-1$
			String inWorkspace = fs[i].getString("inWorkspace"); //$NON-NLS-1$
			String inWorkspace2 = inWorkspace == null ? "" : inWorkspace; //$NON-NLS-1$
			String flatten = fs[i].getString("exploded"); //$NON-NLS-1$
			String flatten2 = flatten == null ? "" : flatten; //$NON-NLS-1$
			boolean explodedValid = ("true".equals(flatten2.toLowerCase()) || "false".equals(flatten2.toLowerCase())); //$NON-NLS-1$ //$NON-NLS-2$
			boolean inWorkspaceValid = ("true".equals(inWorkspace2.toLowerCase()) || "false".equals(inWorkspace2.toLowerCase())); //$NON-NLS-1$ //$NON-NLS-2$
			boolean bFlat = explodedValid ? Boolean.parseBoolean(flatten) : false;
			boolean bInWorkspace = inWorkspaceValid ? Boolean.parseBoolean(inWorkspace) : true;
			fileset.setDir(dir);
			fileset.setIncludes(inc);
			if( exc != null && !"".equals(exc)) //$NON-NLS-1$
				fileset.setExcludes(exc);
			fileset.setInWorkspace(bInWorkspace);
			fileset.setFlatten(bFlat);

			if( dir == null )
				throw new XbException(new Exception("Element 'fileset' missing required attribute 'dir'")); //$NON-NLS-1$
			if( inc == null )
				throw new XbException(new Exception("Element 'fileset' missing required attribute 'includes'")); //$NON-NLS-1$
			
			node.addChild(fileset);
		}
	}
	private static void unmarshallLibFilesets(XbPackageNodeWithProperties node, IMemento[] fs) {
		for( int i = 0; i < fs.length; i++ ) {
			XbLibFileSet fileset = new XbLibFileSet();
			String id = fs[i].getString("id"); //$NON-NLS-1$
			fileset.setId(id);
			node.addChild(fileset);
		}

	}
	private static void unmarshallProperties(XbPackageNodeWithProperties node, IMemento propNode) throws XbException {
		XbProperties propsWrapper = new XbProperties();
		node.addChild(propsWrapper);
		String[] names = ((XMLMemento)propNode).getChildNames();
		Set<String> set = new TreeSet<String>();
		set.addAll(Arrays.asList(names));
		if( set.size() == 0 )
			return;  // no error, no props set
		if( set.size() > 1 || !set.iterator().next().equals("property")) //$NON-NLS-1$
			throw new XbException(new Exception("Element 'property' contains unknown attribute " + set.iterator().next())); //$NON-NLS-1$
		IMemento[] allProps = propNode.getChildren("property"); //$NON-NLS-1$
		for( int i = 0; i < allProps.length; i++ ) {
			XbProperty p = new XbProperty();
			String name = allProps[i].getString("name"); //$NON-NLS-1$
			String val = allProps[i].getString("value"); //$NON-NLS-1$
			if( name == null )
				throw new XbException(new Exception("Element 'property' missing required attribute 'name'")); //$NON-NLS-1$
			if( val == null )
				throw new XbException(new Exception("Element 'property' missing required attribute 'value'")); //$NON-NLS-1$
			if( allProps[i].getNames().size() > 2 )
				throw new XbException(new Exception("Element 'property' contains unknown attribute key")); //$NON-NLS-1$
			p.setName(name);
			p.setValue(val);
			propsWrapper.addChild(p);
		}
	}
}
