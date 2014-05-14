package org.jboss.tools.jmx.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.remote.JMXServiceURL;

import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.jmx.core.AbstractConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.jvmmonitor.core.IHost;
import org.jboss.tools.jmx.jvmmonitor.core.IJvmModelChangeListener;
import org.jboss.tools.jmx.jvmmonitor.core.JvmModel;
import org.jboss.tools.jmx.jvmmonitor.core.JvmModelEvent;
import org.jboss.tools.jmx.jvmmonitor.core.mbean.IMBeanServer;
import org.jboss.tools.jmx.ui.internal.localjmx.JvmConnectionWrapper;
import org.jboss.tools.jmx.ui.internal.localjmx.JvmKey;

public class JVMConnectionProvider extends AbstractConnectionProvider implements IConnectionProvider {
	public static final String PROVIDER_ID = "org.jboss.tools.jmx.local.JVMConnectionProvider"; //$NON-NLS-1$


	private HashMap<JvmKey, JvmConnectionWrapper> connections;
	IJvmModelChangeListener listener = null;

	public JVMConnectionProvider() {
		// Required No-arg constructor
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getName(IConnectionWrapper wrapper) {
		return ((JvmConnectionWrapper)wrapper).getName();
	}

	@Override
	public IConnectionWrapper[] getConnections() {
		if( connections == null ) {
			loadConnections();
		}
		if( connections != null ) {
			ArrayList<IConnectionWrapper> result = new ArrayList<IConnectionWrapper>();
			result.addAll(connections.values());
			// Sort based on name
			Collections.sort(result, new Comparator<IConnectionWrapper>(){
				public int compare(IConnectionWrapper o1, IConnectionWrapper o2) {
					String name1 = getName(o1);
					String name2 = getName(o2);
					if( name1 == null )
						return name2 == null ? 0 : -1;
					if( name2 == null )
						return 1;
					return name1.compareTo(name2);
				}
			});
			return result.toArray(new IConnectionWrapper[result.size()]);
		}
		return new IConnectionWrapper[0];
	}

	private void loadConnections() {
		JvmModel model = JvmModel.getInstance();

		// Add a listener
		if (listener == null) {
			listener = new IJvmModelChangeListener() {
				public void jvmModelChanged(JvmModelEvent e) {
					handleJvmModelChanged();
				}
			};
			model.addJvmModelChangeListener(listener);
		}

		HashMap<JvmKey, JvmConnectionWrapper> fromModel = loadFromJVMModel(model);
		connections = fromModel;
	}

	private void handleJvmModelChanged() {
		JvmModel model = JvmModel.getInstance();
		HashMap<JvmKey, JvmConnectionWrapper> fromModel = loadFromJVMModel(model);
		// find added, removed, changed
		JvmConnectionWrapper[] added = findAdded(fromModel);
		JvmConnectionWrapper[] removed = findRemoved(fromModel);
		JvmConnectionWrapper[] changed = findAndUpdateChanged(fromModel);

		if( added.length > 0 || removed.length > 0 || changed.length > 0 ) {
			// Change occurred somewhere
			fireAllAdded(added);
			fireAllRemoved(removed);
			fireAllChanged(changed);
			connections = fromModel;
		}
	}

	private JvmConnectionWrapper[] findAdded(HashMap<JvmKey, JvmConnectionWrapper> newModel) {
		ArrayList<JvmConnectionWrapper> list = new ArrayList<JvmConnectionWrapper>();
		Iterator<JvmKey> newIt = newModel.keySet().iterator();
		JvmKey working;
		while(newIt.hasNext()) {
			working = newIt.next();
			if( connections.get(working) == null ) {
				// added
				list.add(newModel.get(working));
			}
		}
		return (JvmConnectionWrapper[]) list.toArray(new JvmConnectionWrapper[list.size()]);
	}

	private JvmConnectionWrapper[] findRemoved(HashMap<JvmKey, JvmConnectionWrapper> newModel) {
		ArrayList<JvmConnectionWrapper> list = new ArrayList<JvmConnectionWrapper>();
		Iterator<JvmKey> newIt = connections.keySet().iterator();
		JvmKey working;
		while(newIt.hasNext()) {
			working = newIt.next();
			if( newModel.get(working) == null ) {
				// removed
				list.add(newModel.get(working));
			}
		}
		return (JvmConnectionWrapper[]) list.toArray(new JvmConnectionWrapper[list.size()]);
	}

	private JvmConnectionWrapper[] findAndUpdateChanged(HashMap<JvmKey, JvmConnectionWrapper> newModel) {
		ArrayList<JvmConnectionWrapper> list = new ArrayList<JvmConnectionWrapper>();
		Iterator<JvmKey> oldIter = connections.keySet().iterator();
		JvmKey working;
		while(oldIter.hasNext()) {
			working = oldIter.next();
			if( newModel.get(working) != null ) {
				// existing, lets see if changed
				IActiveJvm newJvm = working.getJvm();
				IActiveJvm oldJvm = connections.get(working).getActiveJvm();
				if( !safeEquals(newJvm, oldJvm)) {
					// vm has changed, so not equal
					// we need the new key,  the old wrapper, but the new jvm
					JvmConnectionWrapper oldWrapper = connections.get(working);
					oldWrapper.setActiveJvm(newJvm);
					newModel.put(working, oldWrapper);
					list.add(newModel.get(oldWrapper));
				}
			}
		}
		return (JvmConnectionWrapper[]) list.toArray(new JvmConnectionWrapper[list.size()]);
	}

	private boolean safeEquals(IActiveJvm j1, IActiveJvm j2) {
		if( j1 == null )
			return j2 == null;
		return j1.equals(j2);
	}

	private HashMap<JvmKey, JvmConnectionWrapper> loadFromJVMModel(JvmModel model) {
		HashMap<JvmKey, JvmConnectionWrapper> ret = new HashMap<JvmKey, JvmConnectionWrapper>();

		JvmConnectionWrapper working;
		List<IHost> hosts = model.getHosts();
		for (IHost host : hosts) {
			String hostName = host.getName();
			List<IActiveJvm> jvms = host.getActiveJvms();
			for (IActiveJvm jvm : jvms) {
				int pid = jvm.getPid();
				JvmKey key = new JvmKey(hostName, pid, jvm);
				IMBeanServer mbeanServer = jvm.getMBeanServer();
				JMXServiceURL jmxUrl = mbeanServer.getJmxUrl();
				if (jmxUrl != null) {
					working = new JvmConnectionWrapper(jmxUrl, jvm);
					ret.put(key, working);
				}
			}
		}
		return ret;
	}


	@Override
	public IConnectionWrapper createConnection(Map map) throws CoreException {
		// Called from UI or custom creation via api with flags passed in, not relevant here.
		return null;
	}


	@Override
	public boolean canCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canDelete(IConnectionWrapper wrapper) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addConnection(IConnectionWrapper connection) {
		// primarily called from UI, not relevent for this provider
		// This provider does not allow external contributions
	}

	@Override
	public void removeConnection(IConnectionWrapper connection) {
		// primarily called from UI, not relevent for this provider
		// This provider does not allow external contributions
	}

	@Override
	public void connectionChanged(IConnectionWrapper connection) {
		// primarily called from UI, not relevent for this provider
		// This provider does not allow external contributions
	}

}
