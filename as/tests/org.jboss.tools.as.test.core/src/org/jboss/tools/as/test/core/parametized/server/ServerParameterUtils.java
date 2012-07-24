package org.jboss.tools.as.test.core.parametized.server;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class ServerParameterUtils {

	public static final String ZIPPED = "zipped";
	public static final String UNZIPPED = "unzipped";
	public static final String DEPLOY_META = "metadata";
	public static final String DEPLOY_SERVER = "server";
	public static final String DEPLOY_CUSTOM_REL = "customRelative";
	public static final String DEPLOY_CUSTOM_ABS = "customAbsolute";
	public static final String DEPLOY_CUSTOM_NULL = "customNull";
	public static final String DEPLOY_PERMOD_DEFAULT = "permod_default";
	public static final String DEPLOY_PERMOD_ABS = "permod_absolute";
	public static final String DEPLOY_PERMOD_REL = "permod_relative";
	

	// Turn an array [item1, item2, item3] into a collection of 1-length items
	// ie new Collection<Object[]>() { new Object[]{item1}, new Object[]{item2}, new Object[]{item3}};
	
	public static Collection<Object[]> asCollection(Object[] items) {
		ArrayList<Object[]> ret = new ArrayList<Object[]>();
		for( int i = 0; i < items.length; i++ ) {
			ret.add(new Object[]{items[i]});
		}
		return ret;
	}
	public static Object[] getJBossServerTypeParamterers() {
		return IJBossToolingConstants.ALL_JBOSS_SERVERS;
	}
	public static Object[] getAllJBossServerTypeParamterers() {
		Object[] ret = new Object[IJBossToolingConstants.ALL_JBOSS_SERVERS.length + 1];
		ret[0] = IJBossToolingConstants.DEPLOY_ONLY_SERVER;
		System.arraycopy(IJBossToolingConstants.ALL_JBOSS_SERVERS, 0, ret, 1, IJBossToolingConstants.ALL_JBOSS_SERVERS.length);
		return ret;
	}
	
	/*
	 * Return the most common DIFFERENT server types where impl may matter
	 */
	public static Object[] getPublishServerTypes() {
		return new Object[] { 
				IJBossToolingConstants.DEPLOY_ONLY_SERVER,
				IJBossToolingConstants.SERVER_AS_60, IJBossToolingConstants.SERVER_AS_71
		};
	}
	
	public static Object[] getServerZipOptions() {
		return new String[] { 
				ZIPPED, UNZIPPED
		};
	}
	
	public static Object[] getDefaultDeployOptions() {
		return new String[] { 
				DEPLOY_META, DEPLOY_SERVER, DEPLOY_CUSTOM_NULL,  DEPLOY_CUSTOM_ABS, DEPLOY_CUSTOM_REL
		};
	}

	/* TODO add changing the deploy name (ex: from project.ear to project1.jar */
	public static Object[] getPerModuleOverrideOptions() {
		return new String[] { 
				DEPLOY_PERMOD_DEFAULT, DEPLOY_PERMOD_ABS, DEPLOY_PERMOD_REL
		};
	}

	
}
