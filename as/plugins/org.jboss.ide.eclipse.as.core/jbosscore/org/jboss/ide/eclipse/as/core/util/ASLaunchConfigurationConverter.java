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
package org.jboss.ide.eclipse.as.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.core.DebugCoreMessages;
import org.eclipse.debug.internal.core.LaunchConfiguration;
import org.eclipse.debug.internal.core.LaunchConfigurationInfo;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerType;
import org.jboss.ide.eclipse.as.core.launch.JBossServerStartupLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.runtime.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.runtime.server.AbstractJBossServerRuntime;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class's purpose is to convert pre-JBIDE2.0 server launch configurations
 * into proper server and runtime objects linked with WTP. 
 * 
 * @author rstryker
 */

public class ASLaunchConfigurationConverter {
//
//	public TempLaunchConfiguration[] getConvertableConfigurations() {
//		TempLaunchConfiguration[] cfgs = getAllLaunchConfigurations();
//		ArrayList result = new ArrayList();
//		try {
//			for( int i = 0; i < cfgs.length; i++ ) {
//				int type = cfgs[i].getLaunchType();
//				// If it only has the private key and the type key, its a stub
//				if( type != -1 && cfgs[i].getAttributes().keySet().size() > 2) {
//					result.add(cfgs[i]);
//				}
//			}
//		} catch( CoreException ce) {
//			ce.printStackTrace();
//		}
//		return (TempLaunchConfiguration[]) result.toArray(new TempLaunchConfiguration[result.size()]);
//	}
//	
//	
//	public void convertConfiguration(TempLaunchConfiguration configuration, 
//			String runtimeName, String serverName) throws CoreException {
//		int type = configuration.getLaunchType();
//		
//		// need name, directory, jre, configuration
//		String homeDir = configuration.getAttribute(TempLaunchConfiguration.HOME_DIR_KEY, (String)null);
//		String configName = configuration.getAttribute(TempLaunchConfiguration.CONFIGURATION_KEY, (String)null);
//		IVMInstall vm =  configuration.getJVMItem();
//		
//		IRuntimeType runtimeType = getRuntimeType(type);
//		IServerType serverType = getServerType(type);
//		
//		try {
//			IServerWorkingCopy newServerWC = serverType.createServer(null, null, null, null);
//			IRuntimeWorkingCopy newRuntimeWC = runtimeType.createRuntime("", null);
//
//			newRuntimeWC.setName(runtimeName);
//			newRuntimeWC.setLocation(new Path(homeDir));
//			((RuntimeWorkingCopy)newRuntimeWC).setAttribute(IJBossServerRuntime.PROPERTY_VM_ID, vm.getId());
//			((RuntimeWorkingCopy)newRuntimeWC).setAttribute(IJBossServerRuntime.PROPERTY_VM_TYPE_ID, vm.getVMInstallType().getId());
//			((RuntimeWorkingCopy)newRuntimeWC).setAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, configName);
//			IRuntime runtime = newRuntimeWC.save(true, null);
//			
//			AbstractJBossServerRuntime newJBRuntime = (AbstractJBossServerRuntime)newRuntimeWC
//						.loadAdapter(AbstractJBossServerRuntime.class, null);
//			newJBRuntime.setVMInstall(vm);
//
//			
//			newServerWC.setRuntime(runtime);
//			
//			IFolder configFolder = ServerType.getServerProject().getFolder(serverName);
//			if( !configFolder.exists() ) {
//				configFolder.create(true, true, null);
//			}
//			
//			newServerWC.setServerConfiguration(configFolder);
//			newServerWC.setName(serverName);
//
//			IServer savedServer = newServerWC.save(true, null);
//			
//			ILaunchConfiguration launchConfig = 
//				((Server)savedServer).getLaunchConfiguration(true, new NullProgressMonitor());
//			ILaunchConfigurationWorkingCopy lcwc = launchConfig.getWorkingCopy();
//			
//			// now lets set some launch config properties
//			String startSuffix    = JBossServerStartupLaunchConfiguration.PRGM_ARGS_START_SUFFIX;
//			String stopSuffix     = JBossServerStartupLaunchConfiguration.PRGM_ARGS_STOP_SUFFIX;
//			//String twiddleSuffix  = JBossServerLaunchConfiguration.PRGM_ARGS_TWIDDLE_SUFFIX;
//			String twiddleSuffix = "";
//			
//			String startArgsKey = IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS + startSuffix;
//			String startVMArgsKey = IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS + startSuffix;
//			String startWorkingDirKey = IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY + startSuffix;
//
//			String stopArgsKey = IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS + stopSuffix;
//			String stopVMArgsKey = IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS + stopSuffix;
//			String stopWorkingDirKey = IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY + stopSuffix;
//
//			String twiddleArgsKey = IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS + twiddleSuffix;
//			String twiddleVMArgsKey = IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS + twiddleSuffix;
//			String twiddleWorkingDirKey = IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY + twiddleSuffix;
//
//			String startArgs = configuration.getAttribute("org.rocklet.launcher.userProgramArgs", (String)null);
//			String startVMArgs = configuration.getAttribute("org.rocklet.launcher.UserVMArgs", (String)null);
//
//			String shutdownArgs   = configuration.getAttribute("org.rocklet.launcher.userShutdownProgramArgs", (String)null);
//			String shutdownVMArgs = configuration.getAttribute("org.rocklet.launcher.userShutdownVMArgs", (String)null);
//			
//			String sourceMementoKey="org.eclipse.debug.core.source_locator_memento";
//			String sourceLocatorKey="org.eclipse.debug.core.source_locator_id";
//			String sourceMemento=configuration.getAttribute(sourceMementoKey, (String)null);
//			String sourceLocator=configuration.getAttribute(sourceLocatorKey, "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector");
//
//			lcwc.setAttribute(startArgsKey, startArgs);
//			lcwc.setAttribute(startVMArgsKey, startVMArgs);
//			lcwc.setAttribute(stopArgsKey, shutdownArgs);
//			lcwc.setAttribute(stopVMArgsKey, shutdownVMArgs);
//			lcwc.setAttribute(sourceMementoKey, sourceMemento);
//			lcwc.setAttribute(sourceLocatorKey, sourceLocator);
//			
//			lcwc.doSave();
//			
//			
//		} catch( CoreException ce) {
//			ce.printStackTrace();
//		}
//	}
//	
//	
//	private IRuntimeType getRuntimeType(int version) {
//		String runtimeKey = "org.jboss.ide.eclipse.as.runtime." + version;
//		return ServerCore.findRuntimeType(runtimeKey);
//	}
//	
//	private IServerType getServerType(int version) {
//		String serverKey = "org.jboss.ide.eclipse.as." + version;
//		return ServerCore.findServerType(serverKey);
//	}
//
//	
//	
//	protected TempLaunchConfiguration[] getAllLaunchConfigurations() {
//		IPath containerPath =
//			DebugPlugin.getDefault().getStateLocation().append(".launches");
//		
//		List configs = new ArrayList(10);
//		final File directory = containerPath.toFile();
//		if (directory.isDirectory()) {
//			FilenameFilter filter = new FilenameFilter() {
//				public boolean accept(File dir, String name) {
//					return dir.equals(directory) &&
//							name.endsWith(ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION);
//				}
//			};
//			String[] files = directory.list(filter);
//			for (int i = 0; i < files.length; i++) {
//				TempLaunchConfiguration config = new TempLaunchConfiguration(containerPath.append(files[i]));
//				configs.add(config);
//			}
//		}
//		
//		return (TempLaunchConfiguration[]) configs.toArray(new TempLaunchConfiguration[configs.size()]);
//	}
//	
//	public class TempLaunchConfiguration extends LaunchConfiguration {
//		public static final String JBOSS_32_CONFIG = "org.jboss.ide.eclipse.launcher.configuration.jboss.JBoss32xLaunchConfigurationDelegate";
//		public static final String JBOSS_40_CONFIG = "org.jboss.ide.eclipse.launcher.configuration.jboss.JBoss40xLaunchConfigurationDelegate";
//		public static final String LAUNCH_CONFIG_TYPE = "_LAUNCH_CONFIG_TYPE_";
//
//		public static final String HOME_DIR_KEY = "org.jboss.rocklet.HomeDir";
//		public static final String CONFIGURATION_KEY = "org.jboss.rocklet.ServerConfiguration";
//		
//		
//		private TempLaunchConfigurationInfo info = null;
//		
//		protected TempLaunchConfiguration(IPath location) {
//			super(location);
//		}
//		
//		protected LaunchConfigurationInfo getInfo() throws CoreException {
//			return managerGetInfo(this);
//		}
//		
//		// returns 32, 40, or -1
//		public int getLaunchType() {
//			try {
//				if( info == null ) managerGetInfo(this);
//				
//				String type = getAttribute(LAUNCH_CONFIG_TYPE, (String)null);
//				if( type == null ) return -1;
//				
//				if( type.equals(JBOSS_32_CONFIG)) return 32;
//				if( type.equals(JBOSS_40_CONFIG)) return 40;
//			} catch( Exception e ) {
//			}
//			return -1;
//		}
//		
//		public IVMInstall getJVMItem() throws CoreException {
//			List cp = getAttribute("org.rocklet.launcher.JDKClasspath", (List)null);
//			ArrayList vms = new ArrayList();
//			IVMInstallType[] vmTypes = JavaRuntime.getVMInstallTypes();
//			for( int i = 0; i < vmTypes.length; i++ ) {
//				vms.addAll(Arrays.asList(vmTypes[i].getVMInstalls()));
//			}
//			IVMInstall[] vms2 = (IVMInstall[]) vms.toArray(new IVMInstall[vms.size()]);
//			
//			Iterator i = cp.iterator();
//			while(i.hasNext()) {
//				Path cpItemPath = new Path((String)i.next());
//				for( int j = 0; j < vms2.length; j++ ) {
//					try {
//						Path vmPath = new Path(vms2[j].getInstallLocation().toURL().getPath());
//						IPath vmPath2 = vmPath.append("lib").append("tools.jar");
//						if( cpItemPath.equals(vmPath2) ) {
//							return vms2[j];
//						}
//					} catch( Exception e ) {
//						e.printStackTrace();
//					}
//				}
//			}
//			
//			return JavaRuntime.getDefaultVMInstall();
//		}
//		
//		
//		protected LaunchConfigurationInfo managerGetInfo(ILaunchConfiguration config) throws CoreException {
//			if (info == null) {
//				if (config.exists()) {
//					InputStream stream = null;
//					try {
//						if (config.isLocal()) {
//							IPath path = config.getLocation();
//							File file = path.toFile();				
//							stream = new FileInputStream(file);
//						} else {
//							IFile file = ((LaunchConfiguration) config).getFile();
//							if (file == null) {
//								throw createDebugException(MessageFormat.format(DebugCoreMessages.LaunchManager_30, new String[] {config.getName()}), null); 
//							}
//							stream = file.getContents();
//						}
//						info = (TempLaunchConfigurationInfo)createInfoFromXML(stream);
//					} catch (FileNotFoundException e) {
//						throwException(config, e);					
//					} catch (SAXException e) {
//						throwException(config, e);					
//					} catch (ParserConfigurationException e) {
//						throwException(config, e);					
//					} catch (IOException e) {
//						throwException(config, e);					
//					} finally {
//						if (stream != null) {
//							try {
//								stream.close();
//							} catch (IOException e) {
//								throwException(config, e);					
//							}
//						}
//					}
//			
//				} else {
//					throw createDebugException(
//						MessageFormat.format(DebugCoreMessages.LaunchManager_does_not_exist, new String[]{config.getName(), config.getLocation().toOSString()}), null); 
//				}
//			}
//			return info;
//		}
//
//		private void throwException(ILaunchConfiguration config, Throwable e) throws DebugException {
//			IPath path = config.getLocation();
//			throw createDebugException(MessageFormat.format(DebugCoreMessages.LaunchManager__0__occurred_while_reading_launch_configuration_file__1___1, new String[]{e.toString(), path.toOSString()}), e); 
//		}
//
//		protected DebugException createDebugException(String message, Throwable throwable) {
//			return new DebugException(
//						new Status(
//						 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
//						 DebugException.REQUEST_FAILED, message, throwable 
//						)
//					);
//		}
//		
//		
//		protected LaunchConfigurationInfo createInfoFromXML(InputStream stream) throws CoreException,
//		 ParserConfigurationException,
//		 IOException,
//		 SAXException {
//			Element root = null;
//			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//			parser.setErrorHandler(new DefaultHandler());
//			root = parser.parse(new InputSource(stream)).getDocumentElement();
//			TempLaunchConfigurationInfo info = new TempLaunchConfigurationInfo();
//			info.initializeFromXML(root);
//			return info;
//		}	
//
//
//	}
//	
//	protected class TempLaunchConfigurationInfo extends LaunchConfigurationInfo {
//
//		protected void initializeFromXML(Element root) throws CoreException {
//			
//			
////			boolean val = true;
////			if( val ) {
////				super.initializeFromXML(root);
////				return;
////			}
//			
//			
//			if (!root.getNodeName().equalsIgnoreCase("launchConfiguration")) { //$NON-NLS-1$
//				throw getInvalidFormatDebugException();
//			}
//			
//			// read type
//			String id = root.getAttribute("type"); //$NON-NLS-1$
//			if (id == null) {
//				throw getInvalidFormatDebugException();
//			} 
//			
//			// Commenting out as it causes an exception. (Configuration type not found)
////			ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(id);
////			if (type == null) {
////				String message= MessageFormat.format(DebugCoreMessages.LaunchConfigurationInfo_missing_type, new Object[]{id}); 
////				throw new DebugException(
////						new Status(
////						 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
////						 DebugException.MISSING_LAUNCH_CONFIGURATION_TYPE, message, null)
////					);
////			}
////			setType(type);
//			
//			// instead just set an attribute right now.
//			setAttribute(TempLaunchConfiguration.LAUNCH_CONFIG_TYPE, id);
//			
//			NodeList list = root.getChildNodes();
//			int length = list.getLength();
//			for (int i = 0; i < length; ++i) {
//				Node node = list.item(i);
//				short nodeType = node.getNodeType();
//				if (nodeType == Node.ELEMENT_NODE) {
//					Element element = (Element) node;
//					String nodeName = element.getNodeName();
//					
//					if (nodeName.equalsIgnoreCase("stringAttribute")) { //$NON-NLS-1$
//						setStringAttribute(element);
//					} else if (nodeName.equalsIgnoreCase("intAttribute")) { //$NON-NLS-1$
//						setIntegerAttribute(element);
//					} else if (nodeName.equalsIgnoreCase("booleanAttribute"))  { //$NON-NLS-1$
//						setBooleanAttribute(element);
//					} else if (nodeName.equalsIgnoreCase("listAttribute")) {   //$NON-NLS-1$
//						setListAttribute(element);					
//					} else if (nodeName.equalsIgnoreCase("mapAttribute")) {    //$NON-NLS-1$
//						setMapAttribute(element);										
//					}
//				}
//			}
//		}
//	}

}
