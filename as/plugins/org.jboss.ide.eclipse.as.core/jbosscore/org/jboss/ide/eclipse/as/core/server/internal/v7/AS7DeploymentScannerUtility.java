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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;

public class AS7DeploymentScannerUtility {
	public static final String SCANNER_PREFIX = "jbosstoolsscanner"; //$NON-NLS-1$

	public IStatus addDeploymentScanner(final IServer server, String scannerName, final String folder) {
		ModelNode op = new ModelNode();
		op.get("operation").set("add"); //$NON-NLS-1$ //$NON-NLS-2$
		ModelNode addr = op.get("address"); //$NON-NLS-1$
		addr.add("subsystem", "deployment-scanner");  //$NON-NLS-1$//$NON-NLS-2$
		addr.add("scanner", scannerName); //$NON-NLS-1$
		op.get("path").set(folder); //$NON-NLS-1$
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
	
	public HashMap<String, Integer> getDeploymentScannerIntervals(final IServer server) {
		Scanner[] scanners = getDeploymentScanners(server);
		HashMap<String, Integer> retval=new HashMap<String, Integer>();
		for( int i = 0; i < scanners.length; i++ ) {
			retval.put(scanners[i].name, scanners[i].interval);
		}
		return retval;
	}

	public Scanner[] getDeploymentScanners(final IServer server) {
		
		ModelNode op2 = new ModelNode();
		op2.get("operation").set("read-attribute"); //$NON-NLS-1$ //$NON-NLS-2$
		op2.get("name").set("path"); //$NON-NLS-1$ //$NON-NLS-2$
		ModelNode addr2 = op2.get("address"); //$NON-NLS-1$
		addr2.add("subsystem", "deployment-scanner");  //$NON-NLS-1$//$NON-NLS-2$
		addr2.add("scanner", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		final String request2 = op2.toJSONString(true);
		ModelNode response2 = null;
		try {
			response2 = executeWithResult(server, request2);
		} catch(Exception e) {
			return new Scanner[]{};
		}
		// Map name to path
		List<ModelNode> list2 = response2.asList();
		HashMap<String, String> nameToPath = new HashMap<String, String>();
		for( int i = 0; i <list2.size(); i++ ) {
			ModelNode address = list2.get(i).get("address"); //$NON-NLS-1$
			String scannerName = address.asList().get(1).get("scanner").asString(); //$NON-NLS-1$
			ModelNode path = list2.get(i).get("result"); //$NON-NLS-1$
			String path2 = path.asString();
			nameToPath.put(scannerName, path2);
		}
		
		//Load intervals
		ArrayList<Scanner> scanners = new ArrayList<Scanner>();
		ModelNode op = new ModelNode();
		op.get("operation").set("read-attribute"); //$NON-NLS-1$ //$NON-NLS-2$
		op.get("name").set("scan-interval"); //$NON-NLS-1$ //$NON-NLS-2$
		ModelNode addr = op.get("address"); //$NON-NLS-1$
		addr.add("subsystem", "deployment-scanner");  //$NON-NLS-1$//$NON-NLS-2$
		addr.add("scanner", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		final String request = op.toJSONString(true);
		ModelNode response = null;
		try {
			response = executeWithResult(server, request);
		} catch(Exception e) {
			return new Scanner[]{};
		}
		
		List<ModelNode> list = response.asList();
		for( int i = 0; i <list.size(); i++ ) {
			ModelNode address = list.get(i).get("address"); //$NON-NLS-1$
			String scannerName = address.asList().get(1).get("scanner").asString(); //$NON-NLS-1$
			ModelNode intVal = list.get(i).get("result"); //$NON-NLS-1$
			int intVal2 = intVal.asBigInteger().intValue();
			Scanner s = new Scanner();
			s.name = scannerName;
			s.interval = intVal2;
			s.address = nameToPath.get(scannerName);
			scanners.add(s);
		}
		return (Scanner[]) scanners.toArray(new Scanner[scanners.size()]);
	}
	
	public static final class Scanner {
		private String name;
		private int interval;
		private String address;
		public String getName() {
			return name;
		}
		public int getInterval() {
			return interval;
		}
		public void setInterval(int interval) {
			this.interval = interval;
		}
		public String getAddress() {
			return address;
		}
	}
	
	public HashMap<String, String> getDeploymentScannersFromServer(final IServer server, boolean all) {
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
			return new HashMap<String, String>();
		}
		
		HashMap<String, String> retval=new HashMap<String, String>();
		List<ModelNode> list = response.asList();
		for( int i = 0; i <list.size(); i++ ) {
			ModelNode address = list.get(i).get("address"); //$NON-NLS-1$
			String scannerName = address.asList().get(1).get("scanner").asString(); //$NON-NLS-1$
			if( all || scannerName.startsWith(SCANNER_PREFIX)) {
				ModelNode arr = list.get(i).get("result"); //$NON-NLS-1$
				String loc = arr.get("path").toString(); //$NON-NLS-1$
				retval.put(scannerName, loc);
			}
		}
		return retval;
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
