package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.tools.as.core.server.controllable.systems.IPortsController;
import org.jboss.tools.foundation.core.expressions.ExpressionResolver;

public class XPathsPortsController extends AbstractSubsystemController implements IPortsController {
	/*
	 * The following constants are in an internal package and not to be used by others
	 */
	
	public static final String JNDI_PORT = "org.jboss.ide.eclipse.as.core.server.jndiPort"; //$NON-NLS-1$
	public static final String JNDI_PORT_DETECT = "org.jboss.ide.eclipse.as.core.server.jndiPortAutoDetect"; //$NON-NLS-1$
	public static final String JNDI_PORT_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.jndiPortAutoDetect.XPath"; //$NON-NLS-1$
	public static final String JNDI_PORT_DEFAULT_XPATH = "Ports" + IPath.SEPARATOR + "JNDI"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final int    JNDI_DEFAULT_PORT = 1099;
	
	public static final String WEB_PORT = "org.jboss.ide.eclipse.as.core.server.webPort"; //$NON-NLS-1$
	public static final String WEB_PORT_DETECT= "org.jboss.ide.eclipse.as.core.server.webPortAutoDetect"; //$NON-NLS-1$
	public static final String WEB_PORT_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.webPortAutoDetect.XPath"; //$NON-NLS-1$
	public static final String WEB_PORT_DEFAULT_XPATH = "Ports" + IPath.SEPARATOR + "JBoss Web"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final int    JBOSS_WEB_DEFAULT_PORT = 8080;
	
	public static final String AS7_MANAGEMENT_PORT = "org.jboss.ide.eclipse.as.core.server.as7.managementPort"; //$NON-NLS-1$
	public static final String AS7_MANAGEMENT_PORT_DETECT= "org.jboss.ide.eclipse.as.core.server.as7.managementPortAutoDetect"; //$NON-NLS-1$
	public static final String AS7_MANAGEMENT_PORT_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.as7.managementPortAutoDetect.XPath"; //$NON-NLS-1$
	public static final String AS7_MANAGEMENT_PORT_DEFAULT_XPATH = "Ports" + IPath.SEPARATOR + "JBoss Management"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final int    AS7_MANAGEMENT_PORT_DEFAULT_PORT = 9999;
	public static final int    WILDFLY8_MANAGEMENT_PORT_DEFAULT_PORT = 9990;
	
	public static final String PORT_OFFSET_KEY = "org.jboss.ide.eclipse.as.core.server.portOffset"; //$NON-NLS-1$
	public static final String PORT_OFFSET_DETECT= "org.jboss.ide.eclipse.as.core.server.portOffsetAutoDetect"; //$NON-NLS-1$
	public static final String PORT_OFFSET_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.portOffsetAutoDetect.XPath"; //$NON-NLS-1$
	public static final String PORT_OFFSET_DEFAULT_XPATH = "Ports" + IPath.SEPARATOR + "Port Offset"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final int    PORT_OFFSET_DEFAULT_PORT = 0;
	

	public static final String JMX_RMI_PORT = "org.jboss.ide.eclipse.as.core.server.jmxrmiport"; //$NON-NLS-1$
	public static final String JMX_RMI_PORT_DETECT = "org.jboss.ide.eclipse.as.core.server.jmxrmiport_AutoDetect"; //$NON-NLS-1$
	public static final String JMX_RMI_PORT_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.jmxrmiport_AutoDetect.XPath"; //$NON-NLS-1$
	public static final String JMX_RMI_PORT_DEFAULT_XPATH = "Ports" + IPath.SEPARATOR + "JMX RMI Port"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final int JMX_RMI_DEFAULT_PORT = 1090;
	
	
	
	@Override
	public int findPort(int id, int defaultVal) {
		switch(id) {
		case KEY_JMX_RMI:
			return getJMXRMIPort(defaultVal);
		case KEY_JNDI:
			return getJNDIPort(defaultVal);
		case KEY_WEB:
			return getJBossWebPort(defaultVal);
		case KEY_MANAGEMENT_PORT:
			return getManagementPort(defaultVal);
		case KEY_PORT_OFFSET:
			return getPortOffset(defaultVal);
		}
		// TODO Auto-generated method stub
		return 0;
	}
	
	protected boolean automaticallyDetect(String attributeKey, String detectKey) {
		return getServer().getAttribute(detectKey, true);
	}
	
	protected int findPort(String attributeKey, String detectKey, String xpathKey, String defaultXPath, int defaultValue) {
		boolean detect = automaticallyDetect(attributeKey, detectKey);
		String result = null;
		if( !detect ) {
			result = getServer().getAttribute(attributeKey, (String)null);
		} else {
			String xpath = getServer().getAttribute(xpathKey, defaultXPath);
			
			// This depends on having a local installation. 
			if( getServer().getRuntime() != null ) {
				XPathQuery query = XPathModel.getDefault().getQuery(getServer(), new Path(xpath));
				if(query!=null) {
					query.refresh(); // Limited refresh only if files have changed
					result = query.getFirstResult();
				}
			}
		}
		
		if( result != null ) {
			result = resolveXPathResult(result);

			try {
				return Integer.parseInt(result);
			} catch(NumberFormatException nfe) {
				return defaultValue;
			}
		}
		return defaultValue;
	}
	
	protected String resolveXPathResult(String result) {
		return new ExpressionResolver().resolveIgnoreErrors(result);
	}

	
	
	private int getJNDIPort(int defaultVal) {
		return getPortOffset(0) + findPort(JNDI_PORT, JNDI_PORT_DETECT, JNDI_PORT_DETECT_XPATH, 
				JNDI_PORT_DEFAULT_XPATH, defaultVal);
	}
	
	private int getJBossWebPort(int defaultVal) {
		return getPortOffset(0) + findPort(WEB_PORT, WEB_PORT_DETECT, WEB_PORT_DETECT_XPATH, 
				WEB_PORT_DEFAULT_XPATH, defaultVal);
	}
	
	private int getManagementPort(int defaultVal) {
		return getPortOffset(0) + findPort(AS7_MANAGEMENT_PORT, AS7_MANAGEMENT_PORT_DETECT, AS7_MANAGEMENT_PORT_DETECT_XPATH, 
				AS7_MANAGEMENT_PORT_DEFAULT_XPATH, defaultVal);
	}
	private int getJMXRMIPort(int defaultVal) {
		return findPort(JMX_RMI_PORT, JMX_RMI_PORT_DETECT, JMX_RMI_PORT_DETECT_XPATH, 
				JMX_RMI_PORT_DEFAULT_XPATH, defaultVal);
	}

	/*
	 * Only truly applicable for AS7.1, EAP6, etc. AS7.0 has no support for this, 
	 * however, the findPort will return 0.
	 */
	private int getPortOffset(int defaultVal) {
		return findPort(PORT_OFFSET_KEY, PORT_OFFSET_DETECT, PORT_OFFSET_DETECT_XPATH, 
				PORT_OFFSET_DEFAULT_XPATH, defaultVal);
	}
	
}
