/*******************************************************************************
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.runtimes.integration.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.datatools.connectivity.ConnectionProfileConstants;
import org.eclipse.datatools.connectivity.ConnectionProfileException;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.datatools.connectivity.db.generic.IDBConnectionProfileConstants;
import org.eclipse.datatools.connectivity.db.generic.IDBDriverDefinitionConstants;
import org.eclipse.datatools.connectivity.drivers.DriverInstance;
import org.eclipse.datatools.connectivity.drivers.DriverManager;
import org.eclipse.datatools.connectivity.drivers.IDriverMgmtConstants;
import org.eclipse.datatools.connectivity.drivers.IPropertySet;
import org.eclipse.datatools.connectivity.drivers.PropertySetImpl;
import org.eclipse.datatools.connectivity.drivers.models.TemplateDescriptor;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.runtimes.integration.Messages;
import org.jboss.tools.as.runtimes.integration.ServerRuntimesIntegrationActivator;

public class DriverUtility implements IRuntimeIntegrationConstants {
	private static final int HSQL_TYPE = 1;
	private static final int H2_TYPE = 2;
	private static class Pair {
		String loc;
		int type;
		public Pair(String loc, int type) {
			this.loc = loc;
			this.type = type;
		}
	}
	
	
	public static final HashMap<String,Pair> RUNTIME_DRIVER_LOCATION = new HashMap<String, Pair>();
	static {
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.AS_32, new Pair(HSQLDB_DRIVER_3X_4X_LOCATION, HSQL_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.AS_40, new Pair(HSQLDB_DRIVER_3X_4X_LOCATION, HSQL_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.AS_42, new Pair(HSQLDB_DRIVER_3X_4X_LOCATION, HSQL_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.AS_50, new Pair(HSQLDB_DRIVER_5X_LOCATION, HSQL_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.AS_51, new Pair(HSQLDB_DRIVER_5X_LOCATION, HSQL_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.AS_60, new Pair(HSQLDB_DRIVER_5X_LOCATION, HSQL_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.AS_70, new Pair(HSQLDB_DRIVER_7_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.AS_71, new Pair(HSQLDB_DRIVER_7_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_80, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_90, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_100, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_110, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_120, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_130, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_140, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_150, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_160, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_170, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_180, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_210, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_200, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.WILDFLY_190, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.EAP_43, new Pair(HSQLDB_DRIVER_3X_4X_LOCATION, HSQL_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.EAP_50, new Pair(HSQLDB_DRIVER_5X_LOCATION, HSQL_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.EAP_60, new Pair(HSQLDB_DRIVER_7_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.EAP_61, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.EAP_70, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.EAP_71, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.EAP_72, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		RUNTIME_DRIVER_LOCATION.put(IJBossToolingConstants.EAP_73, new Pair(HSQLDB_DRIVER_72_EAP61_LOCATION, H2_TYPE));
		// NEW_SERVER_ADAPTER 
	}

	/**
	 * Creates HSQL DB Driver
	 * @param jbossASLocation location of JBoss AS
	 * @param index 
	 * @throws ConnectionProfileException
	 * @return driver instance
	 */
	public void createDriver(String jbossASLocation, IRuntimeType runtimeType) throws DriverUtilityException {
		try {
			createDriver2(jbossASLocation, runtimeType);
		} catch( ConnectionProfileException cfe ) {
			throw new DriverUtilityException(cfe);
		}
	}
	private void createDriver2(String jbossASLocation, IRuntimeType runtimeType) throws ConnectionProfileException {
		if(ProfileManager.getInstance().getProfileByName(DEFAULT_DS) != null) {
			// Don't create the driver a few times
			return;
		}
		String driverPath;
		try {
			driverPath = getDriverPath(jbossASLocation, runtimeType);
		} catch (IOException e) {
			ServerRuntimesIntegrationActivator.getDefault().getLog().log(new Status(IStatus.ERROR,
					ServerRuntimesIntegrationActivator.PLUGIN_ID, Messages.JBossRuntimeStartup_Cannott_create_new_HSQL_DB_Driver, e));
			return;
		}
		if (driverPath == null) {
			ServerRuntimesIntegrationActivator.getDefault().getLog().log(new Status(IStatus.ERROR,
					ServerRuntimesIntegrationActivator.PLUGIN_ID, Messages.JBossRuntimeStartup_Cannot_create_new_DB_Driver));
		}

		DriverInstance driver = getDriver(runtimeType);
		if (driver == null) {
			TemplateDescriptor descr = getDriverTemplateDescriptor(runtimeType);
			IPropertySet instance = new PropertySetImpl(getDriverName(runtimeType), getDriverDefinitionId(runtimeType));
			instance.setName(getDriverName(runtimeType));
			instance.setID(getDriverDefinitionId(runtimeType));
			Properties props = new Properties();

			IConfigurationElement[] template = descr.getProperties();
			for (int i = 0; i < template.length; i++) {
				IConfigurationElement prop = template[i];
				String id = prop.getAttribute("id"); //$NON-NLS-1$

				String value = prop.getAttribute("value"); //$NON-NLS-1$
				props.setProperty(id, value == null ? "" : value); //$NON-NLS-1$
			}
			props.setProperty(DTP_DB_URL_PROPERTY_ID, getDriverUrl(runtimeType)); 
			props.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE, descr.getId());
			props.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST, driverPath);
			if( usesH2(runtimeType)) {
				props.setProperty(IDBDriverDefinitionConstants.DRIVER_CLASS_PROP_ID, "org.h2.Driver");//$NON-NLS-1$
				props.setProperty(IDBDriverDefinitionConstants.DATABASE_VENDOR_PROP_ID, "H2 driver");//$NON-NLS-1$
				props.setProperty(IDBDriverDefinitionConstants.URL_PROP_ID, "jdbc:h2:mem");//$NON-NLS-1$
				props.setProperty(IDBDriverDefinitionConstants.USERNAME_PROP_ID, "sa");//$NON-NLS-1$
			}
			
			instance.setBaseProperties(props);
			DriverManager.getInstance().removeDriverInstance(instance.getID());
			System.gc();
			DriverManager.getInstance().addDriverInstance(instance);
		}

		driver = DriverManager.getInstance().getDriverInstanceByName(getDriverName(runtimeType));
		if (driver != null && ProfileManager.getInstance().getProfileByName(DEFAULT_DS) == null) {
			// create profile
			Properties props = new Properties();
			props.setProperty(ConnectionProfileConstants.PROP_DRIVER_DEFINITION_ID, 
					getDriverDefinitionId(runtimeType));
			props.setProperty(IDBConnectionProfileConstants.CONNECTION_PROPERTIES_PROP_ID, ""); //$NON-NLS-1$
			props.setProperty(IDBDriverDefinitionConstants.DRIVER_CLASS_PROP_ID, driver.getProperty(IDBDriverDefinitionConstants.DRIVER_CLASS_PROP_ID));
			props.setProperty(IDBDriverDefinitionConstants.DATABASE_VENDOR_PROP_ID,	driver.getProperty(IDBDriverDefinitionConstants.DATABASE_VENDOR_PROP_ID));
			props.setProperty(IDBDriverDefinitionConstants.DATABASE_VERSION_PROP_ID, driver.getProperty(IDBDriverDefinitionConstants.DATABASE_VERSION_PROP_ID));
			props.setProperty(IDBDriverDefinitionConstants.DATABASE_NAME_PROP_ID, "Default"); //$NON-NLS-1$
			props.setProperty(IDBDriverDefinitionConstants.PASSWORD_PROP_ID, ""); //$NON-NLS-1$
			props.setProperty(IDBConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, "false"); //$NON-NLS-1$
			props.setProperty(IDBDriverDefinitionConstants.USERNAME_PROP_ID, driver.getProperty(IDBDriverDefinitionConstants.USERNAME_PROP_ID));
			props.setProperty(IDBDriverDefinitionConstants.URL_PROP_ID, driver.getProperty(IDBDriverDefinitionConstants.URL_PROP_ID));

			if( usesH2(runtimeType)) {
				ProfileManager.getInstance().createProfile(DEFAULT_DS,	Messages.JBossRuntimeStartup_The_JBoss_AS_H2_embedded_database, H2_PROFILE_ID, props, "", false); //$NON-NLS-1$ 
			} else {
				ProfileManager.getInstance().createProfile(DEFAULT_DS,	Messages.JBossRuntimeStartup_The_JBoss_AS_Hypersonic_embedded_database, HSQL_PROFILE_ID, props, "", false); //$NON-NLS-1$
			}
		}
		
	}

	protected static String getDriverUrl(IRuntimeType runtimeType) {
		if( usesH2(runtimeType)) {
			return "jdbc:h2:mem";//$NON-NLS-1$
		} else {
			return "jdbc:hsqldb:.";//$NON-NLS-1$
		}
	}

	private static String getDriverDefinitionId(IRuntimeType runtimeType) {
		if( usesH2(runtimeType)) {
			return H2_DRIVER_DEFINITION_ID;
		} else {
			return HSQL_DRIVER_DEFINITION_ID;
		}
	}

	private static String getDriverName(IRuntimeType runtimeType) {
		if( usesH2(runtimeType)) {
			return H2_DRIVER_NAME;
		} else {
			return HSQL_DRIVER_NAME;
		}
	}

	protected static TemplateDescriptor getDriverTemplateDescriptor(IRuntimeType runtimeType) {
		if( usesH2(runtimeType)) {
			return TemplateDescriptor.getDriverTemplateDescriptor(H2_DRIVER_TEMPLATE_ID);
		} else {
			return TemplateDescriptor.getDriverTemplateDescriptor(HSQL_DRIVER_TEMPLATE_ID);
		}
	}

	protected static DriverInstance getDriver(IRuntimeType runtimeType) {
		if( usesH2(runtimeType)) {
			return DriverManager.getInstance().getDriverInstanceByName(H2_DRIVER_NAME);
		}
		return DriverManager.getInstance().getDriverInstanceByName(HSQL_DRIVER_NAME);
	}

	private static String getDriverPath(String jbossASLocation, IRuntimeType runtimeType)
			throws IOException {
		Pair pair = RUNTIME_DRIVER_LOCATION.get(runtimeType.getId());
		String loc = pair.loc;
		IPath p = new Path(jbossASLocation).append(loc);
		File f = p.toFile().getCanonicalFile();
		if( f.isDirectory()) {
			File[] fileList = f.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".jar")) {//$NON-NLS-1$ //$NON-NLS-2$
						return true;
					}
					return false;
				}
			});
			if (fileList != null && fileList.length > 0) {
				return fileList[0].getCanonicalPath();
			}
		}
		return f.getCanonicalPath();
	}

	private static boolean usesH2(IRuntimeType runtimeType) {
		return RUNTIME_DRIVER_LOCATION.get(runtimeType.getId()) != null 
				&& RUNTIME_DRIVER_LOCATION.get(runtimeType.getId()).type == H2_TYPE;
	}
	
	
	public class DriverUtilityException extends Exception {
		public DriverUtilityException(Exception e) {
			super(e);
		}
	}
	
}
