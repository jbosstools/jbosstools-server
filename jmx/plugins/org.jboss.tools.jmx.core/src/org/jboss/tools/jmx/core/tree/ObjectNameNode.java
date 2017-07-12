/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.core.tree;

import java.io.IOException;

import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.jmx.core.IAsyncRefreshable;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXActivator;
import org.jboss.tools.jmx.core.MBeanInfoWrapper;

public class ObjectNameNode extends PropertyNode implements IAsyncRefreshable {

    private ObjectName on;
    private Exception loadError;
    private MBeanInfoWrapper wrapper;

    public ObjectNameNode(Node parent, String key, String value, ObjectName on) {
    	this(parent, key, value, on, null);
    }
    
    public ObjectNameNode(Node parent, String key, String value, ObjectName on, MBeanServerConnection mbsc) {
    	this(parent, key, value, on, mbsc, true);
    }
    
    public ObjectNameNode(Node parent, String key, String value, ObjectName on, MBeanServerConnection mbsc, boolean lazy) {
        super(parent, key, value);
        this.on = on;
        if( !lazy ) {
        	loadInfo(mbsc);
        }
    }
    
    private synchronized void loadInfo(MBeanServerConnection mbsc) {
    	loadInfo(mbsc, new NullProgressMonitor());
    }
    
    private synchronized void loadInfo(MBeanServerConnection mbsc, IProgressMonitor mon) {
    	mon.beginTask("Loading Object Name Node " + on.getCanonicalName(), 100);
    	mon.worked(5);
        Root root = getRoot(parent);
        IConnectionWrapper connectionWrapper = root.getConnection();
    	final MBeanInfoWrapper[] array = new MBeanInfoWrapper[1];
    	try {
        	if( mbsc != null ) {
        		array[0] = loadInfoInternal(mbsc);
        	} else {
		    	connectionWrapper.run(new IJMXRunnable() {
		    		@Override
		    		public void run(MBeanServerConnection mbsc) throws Exception {
		        		array[0] = loadInfoInternal(mbsc);
		    		}
		    	});
        	}
        	mon.worked(95);
    	} catch( Exception e ) {
    		JMXActivator.getDefault().getLog().log(new Status(IStatus.ERROR, JMXActivator.PLUGIN_ID, 
    				"Error loading object name details for JMX object: " + on.toString(), e));
    		loadError = e;
    	}
    	mon.done();
    	wrapper = array[0];
    }

    private synchronized MBeanInfoWrapper loadInfoInternal(MBeanServerConnection mbsc) throws Exception {
    	final ObjectName on2 = on;
    	MBeanInfo mbi = null;
		try {
			mbi = mbsc.getMBeanInfo(on2);
		} catch(IOException ioe) {
			// Ignore
		}
		if( mbi != null ) {
			return new MBeanInfoWrapper(on2, mbi, mbsc, ObjectNameNode.this);
		}
		return null;
    }
    
    public boolean hasLoadError() {
    	return loadError != null;
    }
    
    public ObjectName getObjectName() {
        return on;
    }

    public synchronized MBeanInfoWrapper getMbeanInfoWrapper() {
    	return getMbeanInfoWrapper(new NullProgressMonitor());
    }
    public synchronized MBeanInfoWrapper getMbeanInfoWrapper(IProgressMonitor mon) {
    	if( wrapper == null ) {
    		loadInfo(null, mon);
    	}
        return wrapper;
    }

    public boolean isLoaded() {
    	return wrapper != null || loadError != null;
    }
    
    @Override
    public String toString() {
        return "ObjectNameNode[on=" + on.getKeyPropertyListString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((on == null) ? 0 : on.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof ObjectNameNode))
            return false;
        final ObjectNameNode other = (ObjectNameNode) obj;
        if (on == null) {
            if (other.on != null)
                return false;
        } else if (!on.equals(other.on))
            return false;
        return true;
    }

    private void refreshInternal(final ICallback cb) {
		wrapper = null;
		loadInfo(null);
		cb.refreshComplete();
    }
    
	@Override
	public void refresh(final ICallback cb) {
		new Thread("Refresh JMX Node") {
			@Override
			public void run() {
				refreshInternal(cb);
			}
		}.start();
	}

}
