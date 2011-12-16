/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.XMLMemento;
import org.jboss.ide.eclipse.archives.webtools.filesets.Fileset;
import org.jboss.ide.eclipse.archives.webtools.filesets.FilesetUtil;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class CustomRuntimeClasspathModel implements IJBossToolingConstants, IJBossRuntimeResourceConstants {
	protected static final IPath DEFAULT_CLASSPATH_FS_ROOT = JBossServerCorePlugin.getGlobalSettingsLocation().append("filesets").append("runtimeClasspaths"); //$NON-NLS-1$ //$NON-NLS-2$

	private static CustomRuntimeClasspathModel instance;
	public static CustomRuntimeClasspathModel getInstance() {
		if( instance == null )
			instance = new CustomRuntimeClasspathModel();
		return instance;
	}
	
	public static interface IDefaultPathProvider {
		public IPath[] getAbsolutePaths();
		public void setRuntime(IRuntime rt);
	}
	
	public static class PathProviderFileset extends Fileset implements IDefaultPathProvider {
		public PathProviderFileset(Fileset set) {
			super(set.getName(), set.getRawFolder(), set.getIncludesPattern(), set.getExcludesPattern());
		}
		public PathProviderFileset(String baseFolder) {
			this("", baseFolder, "**/*.jar", "");
		}
		public PathProviderFileset(String name, String folder, String inc, String exc) {
			super(name, folder, inc, exc);
		}
		public IPath[] getAbsolutePaths() {
			IPath[] setPaths = findPaths();
			IPath[] absolute = new IPath[setPaths.length];
			for( int j = 0; j < setPaths.length; j++ ) {
				absolute[j] = new Path(getFolder()).append(setPaths[j]);
			}
			return absolute;
		}
	}

	public IDefaultPathProvider[] getEntries(IRuntimeType type) {
		IDefaultPathProvider[] sets = loadFilesets(type);
		if( sets == null ) {
			return getDefaultEntries(type);
		}
		return sets;
	}
	
	public IDefaultPathProvider[] getDefaultEntries(IRuntimeType type) {
		String rtID = type.getId();
		if(AS_32.equals(rtID)) 
			return getDefaultAS3Entries();
		if(AS_40.equals(rtID)) 
			return getDefaultAS40Entries();
		if(AS_42.equals(rtID)) 
			return getDefaultAS40Entries();
		if(AS_50.equals(rtID)) 
			return getDefaultAS50Entries();
		if(EAP_43.equals(rtID))
			return getDefaultEAP43Entries();
		// Added cautiously, not sure on changes, may change
		if(AS_51.equals(rtID)) 
			return getDefaultAS50Entries();
		if(AS_60.equals(rtID)) 
			return getDefaultAS60Entries();
		if(EAP_50.equals(rtID))
			return getDefaultAS50Entries();

		if(AS_70.equals(type.getId()))
			return getDefaultAS70Entries();
		if(AS_71.equals(type.getId()))
			return getDefaultAS71Entries();
		if(EAP_60.equals(type.getId()))
			return getDefaultAS71Entries();
		
		// NEW_SERVER_ADAPTER add logic for new adapter here
		return new IDefaultPathProvider[]{};
	}
	
	public IPath[] getDefaultPaths(IRuntime rt) {
		return getAllEntries(rt, getDefaultEntries(rt.getRuntimeType()));
	}

	
	protected IDefaultPathProvider[] getDefaultAS3Entries() {
		ArrayList<PathProviderFileset> sets = new ArrayList<PathProviderFileset>();
		String configPath = "${jboss_config_dir}";
		String deployerPath = configPath + "/" + DEPLOYERS;
		String deployPath = configPath + "/" + DEPLOY;
		sets.add(new PathProviderFileset(LIB));
		sets.add(new PathProviderFileset(configPath + "/" + LIB));
		sets.add(new PathProviderFileset(CLIENT));
		return sets.toArray(new PathProviderFileset[sets.size()]);
	}
	
	protected IDefaultPathProvider[] getDefaultAS40Entries() {
		ArrayList<PathProviderFileset> sets = new ArrayList<PathProviderFileset>();
		String configPath = "${jboss_config_dir}";
		String deployerPath = configPath + "/" + DEPLOYERS;
		String deployPath = configPath + "/" + DEPLOY;
		sets.add(new PathProviderFileset(LIB));
		sets.add(new PathProviderFileset(configPath + "/" + LIB));
		sets.add(new PathProviderFileset(deployPath + "/" + JBOSS_WEB_DEPLOYER + "/" + JSF_LIB));
		sets.add(new PathProviderFileset(deployPath + "/" + AOP_JDK5_DEPLOYER));
		sets.add(new PathProviderFileset(deployPath + "/" + EJB3_DEPLOYER));
		sets.add(new PathProviderFileset(CLIENT));
		return sets.toArray(new PathProviderFileset[sets.size()]);
	}

	protected IDefaultPathProvider[] get42() {
		return getDefaultAS40Entries();
	}

	protected IDefaultPathProvider[] getDefaultEAP43Entries() {
		return getDefaultAS40Entries();
	}
	
	protected IDefaultPathProvider[] getDefaultAS50Entries() {
		ArrayList<PathProviderFileset> sets = new ArrayList<PathProviderFileset>();
		String configPath = "${jboss_config_dir}";
		String deployerPath = configPath + "/" + DEPLOYERS;
		String deployPath = configPath + "/" + DEPLOY;
		sets.add(new PathProviderFileset(COMMON + "/" + LIB));
		sets.add(new PathProviderFileset(LIB));
		sets.add(new PathProviderFileset(configPath + "/" + LIB));
		
		sets.add(new PathProviderFileset(deployPath + "/" + JBOSSWEB_SAR + "/" + JSF_LIB));
		sets.add(new PathProviderFileset("", deployPath + "/" + JBOSSWEB_SAR, JBOSS_WEB_SERVICE_JAR, ""));
		sets.add(new PathProviderFileset("", deployPath + "/" + JBOSSWEB_SAR, JSTL_JAR, ""));
		sets.add(new PathProviderFileset(deployerPath + "/" + AS5_AOP_DEPLOYER));
		sets.add(new PathProviderFileset(deployerPath + "/" + EJB3_DEPLOYER));
		sets.add(new PathProviderFileset("", deployerPath + "/" + WEBBEANS_DEPLOYER,JSR299_API_JAR, ""));
		sets.add(new PathProviderFileset(CLIENT));
		return sets.toArray(new PathProviderFileset[sets.size()]);
	}
	
	public IDefaultPathProvider[] getDefaultAS60Entries() {
		ArrayList<IDefaultPathProvider> sets = new ArrayList<IDefaultPathProvider>();
		String configPath = "${jboss_config_dir}";
		sets.addAll(Arrays.asList(getDefaultAS50Entries()));
		sets.add(new PathProviderFileset(configPath + "/" + DEPLOYERS + "/" + REST_EASY_DEPLOYER));
		sets.add(new PathProviderFileset(configPath + "/" + DEPLOYERS + "/" + JSF_DEPLOYER + "/" + MOJARRA_20 + "/" + JSF_LIB));
		return sets.toArray(new PathProviderFileset[sets.size()]);
	}
	public IDefaultPathProvider[] getDefaultAS70Entries() {
		ArrayList<IDefaultPathProvider> sets = new ArrayList<IDefaultPathProvider>();
		sets.add(new PathProviderFileset("", "modules/javax", "**/*.jar", "**/jsf-api-1.2*.jar"));
		sets.add(new PathProviderFileset("modules/org/hibernate/validator"));
		sets.add(new PathProviderFileset("modules/org/resteasy"));
		sets.add(new PathProviderFileset("modules/org/picketbox"));
		sets.add(new PathProviderFileset("modules/org/jboss/as/controller-client/main/"));
		sets.add(new PathProviderFileset("modules/org/jboss/dmr/main/"));
		sets.add(new PathProviderFileset("modules/org/jboss/logging/main"));
		sets.add(new PathProviderFileset("modules/org/jboss/resteasy/resteasy-jaxb-provider/main"));
		sets.add(new PathProviderFileset("modules/org/jboss/resteasy/resteasy-jaxrs/main"));
		sets.add(new PathProviderFileset("modules/org/jboss/resteasy/resteasy-multipart-provider/main"));
		
		return (IDefaultPathProvider[]) sets.toArray(new IDefaultPathProvider[sets.size()]);
	}
	
	public IDefaultPathProvider[] getDefaultAS71Entries() {
		ArrayList<IDefaultPathProvider> sets = new ArrayList<IDefaultPathProvider>();
		sets.addAll(Arrays.asList(getDefaultAS70Entries()));
		sets.add(new PathProviderFileset("modules/org/jboss/ejb3/main"));
		return (IDefaultPathProvider[]) sets.toArray(new IDefaultPathProvider[sets.size()]);
	}
	
	public IPath[] getAllEntries(IRuntime runtime, IDefaultPathProvider[] sets) {
		ArrayList<IPath> retval = new ArrayList<IPath>();
		for( int i = 0; i < sets.length; i++ ) {
			sets[i].setRuntime(runtime);
			IPath[] absolute = sets[i].getAbsolutePaths();
			for( int j = 0; j < absolute.length; j++ ) {
				if( !retval.contains(absolute[j]))
					retval.add(absolute[j]);
			}
		}
		return (IPath[]) retval.toArray(new IPath[retval.size()]);
	}
	
	
	/*
	 * Persistance of the model
	 */
	
	public static IDefaultPathProvider[] loadFilesets(IRuntimeType rt) {
		IPath fileToRead = DEFAULT_CLASSPATH_FS_ROOT.append(rt.getId());
		Fileset[] sets = loadFilesets(fileToRead.toFile(), null);
		if( sets != null ) {
			PathProviderFileset[] newSets = new PathProviderFileset[sets.length];
			for( int i = 0; i < sets.length; i++ ) {
				newSets[i] = new PathProviderFileset(sets[i]);
			}
			return newSets;
		}
		return null;
	}
	public static Fileset[] loadFilesets(File file, IServer server) {
		if( file != null && file.exists()) {
			try {
				return FilesetUtil.loadFilesets(new FileInputStream(file), server);
			} catch( FileNotFoundException fnfe) {}
		}
		return null;
	}

	public static void saveFilesets(IRuntimeType runtime, IDefaultPathProvider[] sets) {
		if( !DEFAULT_CLASSPATH_FS_ROOT.toFile().exists()) {
			DEFAULT_CLASSPATH_FS_ROOT.toFile().mkdirs();
		}
		IPath fileToWrite = DEFAULT_CLASSPATH_FS_ROOT.append(runtime.getId());
		XMLMemento memento = XMLMemento.createWriteRoot("classpathProviders"); //$NON-NLS-1$
		for( int i = 0; i < sets.length; i++ ) {
			if( sets[i] instanceof Fileset) {
				Fileset fs = (Fileset)sets[i];
				XMLMemento child = (XMLMemento)memento.createChild("fileset");//$NON-NLS-1$
				child.putString("name", fs.getName());//$NON-NLS-1$
				child.putString("folder", fs.getRawFolder());//$NON-NLS-1$
				child.putString("includes", fs.getIncludesPattern());//$NON-NLS-1$
				child.putString("excludes", fs.getExcludesPattern());//$NON-NLS-1$	
			} else {
				// TODO
			}
		}
		try {
			memento.save(new FileOutputStream(fileToWrite.toFile()));
		} catch( IOException ioe) {
			IStatus status = new Status(IStatus.ERROR, ClasspathCorePlugin.PLUGIN_ID, "Could not save default classpath entries", ioe);
			ClasspathCorePlugin.getDefault().getLog().log(status);
		}
	}
}
