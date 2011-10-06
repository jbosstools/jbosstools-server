package org.jboss.tools.openshift.express.internal.core.behaviour;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;

/**
 * This class holds the attribute names whose values will be
 * stored inside a server object, as well as the utility methods
 * used to get and set them for a server. 
 *
 */
public class ExpressServerUtils {
	public static final String ATTRIBUTE_EXPRESS_MODE = "org.jboss.tools.openshift.express.internal.core.behaviour.ExpressMode";
	public static final String EXPRESS_BINARY_MODE =  "org.jboss.tools.openshift.express.internal.core.behaviour.ExpressBinaryMode";
	public static final String EXPRESS_SOURCE_MODE =  "org.jboss.tools.openshift.express.internal.core.behaviour.ExpressSourceMode";
	public static final String ATTRIBUTE_APPLICATION =  "org.jboss.tools.openshift.express.internal.core.behaviour.Application";
	public static final String ATTRIBUTE_DOMAIN =  "org.jboss.tools.openshift.express.internal.core.behaviour.Domain";
	public static final String ATTRIBUTE_USERNAME =  "org.jboss.tools.openshift.express.internal.core.behaviour.Username";
	public static final String ATTRIBUTE_PASSWORD =  "org.jboss.tools.openshift.express.internal.core.behaviour.Password";
	
	public static String getExpressMode(IServerAttributes attributes ) {
		return attributes.getAttribute(ATTRIBUTE_EXPRESS_MODE, EXPRESS_SOURCE_MODE);
	}
	
	public static IServer setExpressMode(IServer server, String val) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(ATTRIBUTE_EXPRESS_MODE, val);
		return wc.save(false, new NullProgressMonitor());
	}
	
	public static String getExpressApplication(IServerAttributes attributes ) {
		return attributes.getAttribute(ATTRIBUTE_APPLICATION, (String)null);
	}
	
	public static IServer setExpressApplication(IServer server, String val) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(ATTRIBUTE_APPLICATION, val);
		return wc.save(false, new NullProgressMonitor());
	}


	public static String getExpressDomain(IServerAttributes attributes ) {
		return attributes.getAttribute(ATTRIBUTE_DOMAIN, (String)null);
	}

	public static IServer setExpressDomain(IServer server, String val) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(ATTRIBUTE_DOMAIN, val);
		return wc.save(false, new NullProgressMonitor());
	}

	public static String getExpressUsername(IServerAttributes attributes ) {
		return attributes.getAttribute(ATTRIBUTE_USERNAME, (String)null);
	}

	public static IServer setExpressUsername(IServer server, String val) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(ATTRIBUTE_USERNAME, val);
		return wc.save(false, new NullProgressMonitor());
	}

	// TODO Must secure this!!! 
	public static String getExpressPassword(IServerAttributes attributes ) {
		return attributes.getAttribute(ATTRIBUTE_PASSWORD, (String)null);
	}
	
	public static IServer setExpressPassword(IServer server, String val) throws CoreException {
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(ATTRIBUTE_PASSWORD, val);
		return wc.save(false, new NullProgressMonitor());
	}

}
