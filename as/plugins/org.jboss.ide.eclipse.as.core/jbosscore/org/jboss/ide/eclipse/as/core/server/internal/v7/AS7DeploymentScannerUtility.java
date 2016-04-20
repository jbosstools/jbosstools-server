/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;

public class AS7DeploymentScannerUtility {
	public static final int DEFAULT_INTERVAL = 5000;
	public static final int IGNORE = -20;
	
	public static final String SCANNER_PREFIX = "jbosstoolsscanner"; //$NON-NLS-1$

	public IStatus addDeploymentScanner(final IServer server, String scannerName, final String folder) {
		return addDeploymentScanner(server, scannerName, folder, DEFAULT_INTERVAL, IGNORE);
	}
	public IStatus addDeploymentScanner(final IServer server, String scannerName, final String folder, 
			int interval, int timeout) {

		ModelNode op = new ModelNode();
		op.get("operation").set("add"); //$NON-NLS-1$ //$NON-NLS-2$
		ModelNode addr = op.get("address"); //$NON-NLS-1$
		addr.add("subsystem", "deployment-scanner");  //$NON-NLS-1$//$NON-NLS-2$
		addr.add("scanner", scannerName); //$NON-NLS-1$
		op.get("path").set(folder); //$NON-NLS-1$
		if( interval != IGNORE) 
			op.get("scan-interval").set(interval); //$NON-NLS-1$
		if( timeout != IGNORE)
			op.get("deployment-timeout").set(timeout); //$NON-NLS-1$
		final String request = op.toJSONString(true);
		return execute(server, request);
	}

	public IStatus removeDeploymentScanner(final IServer server, String scannerName) {
		ModelNode op = new ModelNode();
		op.get("operation").set("remove"); //$NON-NLS-1$ //$NON-NLS-2$
		ModelNode addr = op.get("address"); //$NON-NLS-1$
		addr.add("subsystem", "deployment-scanner");  //$NON-NLS-1$//$NON-NLS-2$
		addr.add("scanner", scannerName); //$NON-NLS-1$
		final String request = op.toJSONString(true);
		return execute(server, request);
	}
	
	public boolean updateDeploymentScannerInterval(final IServer server, String scannerName, int newValue) {
		ModelNode op = new ModelNode();
		op.get("operation").set("write-attribute"); //$NON-NLS-1$ //$NON-NLS-2$
		op.get("name").set("scan-interval"); //$NON-NLS-1$ //$NON-NLS-2$
		op.get("value").set(newValue); //$NON-NLS-1$
		ModelNode addr = op.get("address"); //$NON-NLS-1$
		addr.add("subsystem", "deployment-scanner");  //$NON-NLS-1$//$NON-NLS-2$
		addr.add("scanner", scannerName); //$NON-NLS-1$ 
		final String request = op.toJSONString(true);
		try {
			executeWithResult(server, request);
		} catch(Exception e) {
			return false;
		}
		return true;
	}
	
	public boolean setScannerEnabled(final IServer server, String scannerName, boolean enabled) {
		ModelNode op = new ModelNode();
		op.get("operation").set("write-attribute"); //$NON-NLS-1$ //$NON-NLS-2$
		op.get("name").set("scan-enabled"); //$NON-NLS-1$ //$NON-NLS-2$
		op.get("value").set(enabled); //$NON-NLS-1$
		ModelNode addr = op.get("address"); //$NON-NLS-1$
		addr.add("subsystem", "deployment-scanner");  //$NON-NLS-1$//$NON-NLS-2$
		addr.add("scanner", scannerName); //$NON-NLS-1$ 
		final String request = op.toJSONString(true);
		try {
			executeWithResult(server, request);
		} catch(Exception e) {
			JBossServerCorePlugin.log(e);
			return false;
		}
		return true;
	}
	
	
	public HashMap<String, Integer> getDeploymentScannerIntervals(final IServer server) {
		Scanner[] scanners = getDeploymentScanners(server);
		HashMap<String, Integer> retval=new HashMap<String, Integer>();
		for( int i = 0; i < scanners.length; i++ ) {
			retval.put(scanners[i].name, scanners[i].interval);
		}
		return retval;
	}

	public Scanner[] getDeploymentScanners(final IServer server) {
		return getDeploymentScanners(server, true);
	}
	
	public Scanner[] getDeploymentScannersBlocking(final IServer server, boolean all) {
		return getDeploymentScannersBlocking(server, all, 5, 1500);
	}
	public Scanner[] getDeploymentScannersBlocking(final IServer server, boolean all, int maxTries, long sleep) {

		Scanner[] scanners = null;
		int attempt = 0;
		Exception ie2 = null;
		while( scanners == null && attempt < maxTries) {
			attempt++;
			try {
				scanners = getDeploymentScanners(server, all, false, true);
			} catch(Exception e) {
				try {
					Thread.sleep(sleep);
				} catch(InterruptedException ie) {
					ie2 = ie;
				}
			}
		}
		if( scanners != null )
			return scanners;
		
		JBossServerCorePlugin.getDefault().getLog().log(new Status(
				IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
				NLS.bind("Unable to retrieve a list of remote deployment scanners for server {0}", server.getName()),ie2)); //$NON-NLS-1$
		return null;
	}
	
	public Scanner[] getDeploymentScanners(final IServer server, boolean allScanners) {
		try {
			return getDeploymentScanners(server, allScanners, true, false);
		} catch(Exception e) {
			// Should never happen
			return new Scanner[0];
		}
	}
	
	public Scanner[] getDeploymentScanners(final IServer server, boolean allScanners, boolean log, boolean rethrow) throws Exception {

		ArrayList<Scanner> list = new ArrayList<Scanner>();
		
		ModelNode op = new ModelNode();
		op.get("operation").set("read-resource"); //$NON-NLS-1$ //$NON-NLS-2$
		ModelNode addr = op.get("address"); //$NON-NLS-1$
		addr.add("subsystem", "deployment-scanner");  //$NON-NLS-1$//$NON-NLS-2$
		addr.add("scanner", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		final String request = op.toJSONString(true);
		ModelNode response = null;
		try {
			response = executeWithResult(server, request);
		} catch(Exception e) {
			if( log )
				JBossServerCorePlugin.getDefault().getLog().log(new Status(
						IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
						NLS.bind("Unable to retrieve a list of remote deployment scanners for server {0}", server.getName()),e)); //$NON-NLS-1$
			if( rethrow) {
				throw e;
			}
			return null;
		}
		
		List<ModelNode> mnList = response.asList();
		for( int i = 0; i < mnList.size(); i++ ) {
			ModelNode listElement = mnList.get(i);
			ModelNode address = listElement.get("address"); //$NON-NLS-1$
			ModelNode result = listElement.get("result"); //$NON-NLS-1$

			String scannerName = address.asList().get(1).get("scanner").asString(); //$NON-NLS-1$
			if( allScanners || scannerName.startsWith(SCANNER_PREFIX)) {
				int interval = result.get("scan-interval").asBigInteger().intValue();//$NON-NLS-1$
				int timeout = result.get("deployment-timeout").asBigInteger().intValue(); //$NON-NLS-1$
				String path5 = result.get("path").toString();//$NON-NLS-1$
				boolean enabled = result.get("scan-enabled").asBoolean(); //$NON-NLS-1$
				Scanner s = new Scanner();
				s.name = scannerName;
				s.address = path5;
				s.interval = interval;
				s.timeout = timeout;
				s.enabled = enabled;
				list.add(s);
			}
		}
		
		return (Scanner[]) list.toArray(new Scanner[list.size()]);
	}

	
	public static final class Scanner {
		private String name;
		private int interval;
		private int timeout;
		private String address;
		private String relativeTo;
		private boolean enabled;

		public String getName() {
			return name;
		}
		public int getInterval() {
			return interval;
		}
		public String getRelativeTo() {
			return relativeTo;
		}
		public int getTimeout() {
			return timeout;
		}
		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}
		public void setInterval(int interval) {
			this.interval = interval;
		}
		public String getAddress() {
			return address;
		}
		public boolean getEnabled() {
			return enabled;
		}
	}
	
	/**
	 * Returns a hashmap of scanner names to their path. 
	 * 
	 * @param server
	 * @param all
	 * @return
	 */
	public HashMap<String, String> getDeploymentScannersFromServer(final IServer server, boolean all) {
		try {
			return getDeploymentScannersFromServer(server, all, true, false);
		} catch(Exception e) {
			// Never happen
		}
		return new HashMap<String, String>();
	}
	
	public HashMap<String, String> getDeploymentScannersFromServer(final IServer server, boolean all, boolean log, boolean rethrow) throws Exception {
		Scanner[] scanners = getDeploymentScanners(server, all, log, rethrow);
		return getDeploymentScannersFromServer(server, scanners);
	}

	public HashMap<String, String> getDeploymentScannersFromServer(final IServer server, Scanner[] scanners) {
		HashMap<String, String> retval=new HashMap<String, String>();
		for( int i = 0; i < scanners.length; i++ ) {
			retval.put(scanners[i].getName(), scanners[i].getAddress());
		}
		return retval;
	}
	
	/**
	 * Get the deployment scanner with path=deployments and relative-to=jboss.server.base.dir
	 * @param server
	 * @return
	 */
	public Scanner getDefaultDeploymentScanner(final IServer server) {
		Scanner[] scanners = getDeploymentScanners(server, true);
		for( int i = 0; i < scanners.length; i++ ) {
//			if( "deployments".equals(scanners[i].address) && //$NON-NLS-1$
//			"jboss.server.base.dir".equals(scanners[i].relativeTo)) {  //$NON-NLS-1$ 
			if( "default".equals(scanners[i].name)) { //$NON-NLS-1$
				return scanners[i];
			}
		}
		return null;
	}

	
	protected IStatus execute(final IServer server, final String request) {
		try {
			ModelNode node = executeWithResult(server, request);
			return Status.OK_STATUS;
		} catch( Exception e ) {
			// TODO Throw new checked exception
			return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, e.getMessage(), e);
		}
	}
	protected ModelNode executeWithResult(final IServer server, final String request) throws Exception {
        String resultJSON = JBoss7ManagerUtil.executeWithService(new JBoss7ManagerUtil.IServiceAware<String>() {
            public String execute(IJBoss7ManagerService service) throws Exception {
                return service.execute(new AS7ManagementDetails(server), request);
            }
        }, server);
        ModelNode result = ModelNode.fromJSONString(resultJSON);
        return result;
	}
}
