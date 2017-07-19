/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.internal.Messages;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.Fileset;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.LayeredProductPathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.RuntimePathProviderFileset;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.tools.foundation.core.xml.IMemento;
import org.jboss.tools.foundation.core.xml.XMLMemento;

/**
 * Input / output for a the custom runtime classpath
 * model from a saved / persisted file. 
 */
public class RuntimeClasspathModelIO {
	/**
	 * A location to store these default settings.
	 * Use of the term 'filesets' is legacy but must be maintained. 
	 * 
	 * Each runtime in {metadata}/org.jboss.ide.eclipse.as.server.core/filesets/runtimeClasspaths 
	 * gets its own file.  This allows the format to change (if required) for newer runtime types,
	 * if such a thing is needed. 
	 * 
	 * 
	 */
	protected static final IPath DEFAULT_CLASSPATH_FS_ROOT = JBossServerCorePlugin.getGlobalSettingsLocation().append("filesets").append("runtimeClasspaths"); //$NON-NLS-1$ //$NON-NLS-2$

	protected static final String VERSION = "version"; //$NON-NLS-1$
	
	public InternalRuntimeClasspathModel readModel(IRuntimeType rtt) {
		return loadPathProviders(rtt);
	}

	
	private InternalRuntimeClasspathModel loadPathProviders(IRuntimeType rt) {
		IPath fileToRead = DEFAULT_CLASSPATH_FS_ROOT.append(rt.getId());
		File f = fileToRead.toFile();
		if( f != null && f.exists()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				XMLMemento memento = XMLMemento.createReadRoot(fis);
				String version = memento.getString(VERSION);
				InternalRuntimeClasspathModel model = new InternalRuntimeClasspathModel();
				if( version == null ) {
					// 1.0 of this file had no version set
					fillVersion1PathProviders(model, memento);
				} else if( "2.0".equals(version)){ //$NON-NLS-1$
					// 1.0 of this file had no version set
					fillVersion2PathProviders(model, memento);
				}
				return model;
			} catch(IOException ioe) {
				
			}
		}
		return null;
	}

	private IRuntimePathProvider layeredProduct(XMLMemento layeredProductMemento) {
		String modName = layeredProductMemento.getString(LayeredProductPathProvider.PROP_MODULE_NAME);
		String slot = layeredProductMemento.getString(LayeredProductPathProvider.PROP_SLOT);
		return new LayeredProductPathProvider(modName, slot);
	}
	
	private IRuntimePathProvider fileset(XMLMemento filesetStyleMemento) {
		String name = filesetStyleMemento.getString("name"); //$NON-NLS-1$
		String folder = filesetStyleMemento.getString("folder");//$NON-NLS-1$
		String includes = filesetStyleMemento.getString("includes");//$NON-NLS-1$
		String excludes = filesetStyleMemento.getString("excludes");//$NON-NLS-1$
		return new RuntimePathProviderFileset(new Fileset(name, folder, includes, excludes));
	}
	
	private IRuntimePathProvider[] loadProvidersFromMemento(XMLMemento parent) {
		IMemento[] children = parent.getChildren();
		ArrayList<IRuntimePathProvider> collector = new ArrayList<IRuntimePathProvider>();
		for( int i = 0; i < children.length; i++ ) {
			String name = children[i].getNodeName();
			if( name.equals("fileset")) { //$NON-NLS-1$
				collector.add(fileset((XMLMemento)children[i]));
			} else if( name.equals("layeredProductPath")) { //$NON-NLS-1$
				collector.add(layeredProduct((XMLMemento)children[i]));
			}
		}
		IRuntimePathProvider[] list = collector.toArray(new IRuntimePathProvider[collector.size()]);
		return list;
	}
	
	private void fillVersion1PathProviders(InternalRuntimeClasspathModel model, XMLMemento memento) {
		// In version 1 we load them directly from the read root
		model.addProviders(loadProvidersFromMemento(memento));
	}

	private void fillVersion2PathProviders(InternalRuntimeClasspathModel model, XMLMemento memento) {
		IMemento nofacet = memento.getChild("nofacet"); //$NON-NLS-1$
		IRuntimePathProvider[] noFacetProviders = loadProvidersFromMemento(((XMLMemento)nofacet));
		model.addProviders(noFacetProviders);
		
		
		IMemento[] facets = memento.getChildren("facet");//$NON-NLS-1$
		for( int i = 0; i < facets.length; i++ ) {
			String id = facets[i].getString("id");//$NON-NLS-1$
			String version = facets[i].getString("version");//$NON-NLS-1$
			IRuntimePathProvider[] result = loadProvidersFromMemento((XMLMemento)facets[i]);
			model.addProviders(id, version, result);
		}
	}
	
	
	

	public void saveModel(IRuntimeType runtime, InternalRuntimeClasspathModel model) {
		if( !DEFAULT_CLASSPATH_FS_ROOT.toFile().exists()) {
			DEFAULT_CLASSPATH_FS_ROOT.toFile().mkdirs();
		}
		IPath fileToWrite = DEFAULT_CLASSPATH_FS_ROOT.append(runtime.getId());
		XMLMemento memento = XMLMemento.createWriteRoot("classpathProviders"); //$NON-NLS-1$
		memento.putString(VERSION, "2.0"); //$NON-NLS-1$
		
		// Save the no facet set
		XMLMemento nofacet = (XMLMemento)memento.createChild("nofacet");//$NON-NLS-1$
		IRuntimePathProvider[] noFacetProviders = model.getStandardProviders();
		for( int i = 0; i < noFacetProviders.length; i++ ) {
			noFacetProviders[i].saveInMemento(nofacet);
		}
		
		
		// Save each facet which has been customized
		String[] customized = model.getCustomizedFacets();
		for( int i = 0; i < customized.length; i++ ) {
			String facetName = customized[i];
			XMLMemento facet = (XMLMemento)memento.createChild("facet");//$NON-NLS-1$
			facet.putString("id", facetName);//$NON-NLS-1$
			IRuntimePathProvider[] providers = model.getProvidersForFacet(facetName);
			for( int j = 0; j < providers.length; j++ ) {
				providers[j].saveInMemento(facet);
			}
		}
		
		
		// Save each facet version which has been customized
		String[] customizedFacetVersions = model.getCustomizedFacetVersions();
		for( int i = 0; i < customized.length; i++ ) {
			String facetName = customizedFacetVersions[i];
			String[] versions = model.getCustomizedFacetVersions(facetName);
			for( int j = 0; j < versions.length; j++ ) {
				String v = versions[j];
				IRuntimePathProvider[] providers = model.getProvidersForFacet(facetName, v);
				XMLMemento facet = (XMLMemento)memento.createChild("facet");//$NON-NLS-1$
				facet.putString("id", facetName);//$NON-NLS-1$
				facet.putString("version", v);//$NON-NLS-1$
				for( int k = 0; k < providers.length; k++ ) {
					providers[k].saveInMemento(facet);
				}
			}
		}
		
		try {
			memento.save(new FileOutputStream(fileToWrite.toFile()));
		} catch( IOException ioe) {
			IStatus status = new Status(IStatus.ERROR, ClasspathCorePlugin.PLUGIN_ID, 
					NLS.bind(Messages.CouldNotSaveDefaultClasspathEntries, runtime.getId()), ioe);
			ClasspathCorePlugin.getDefault().getLog().log(status);
		}
	}
}
