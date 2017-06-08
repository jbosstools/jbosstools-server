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

import org.eclipse.core.runtime.IStatus;
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
        Root root = getRoot(parent);
        IConnectionWrapper connectionWrapper = root.getConnection();
    	final MBeanInfoWrapper[] array = new MBeanInfoWrapper[1];
    	final ObjectName on2 = on;
    	try {
        	if( mbsc != null ) {
        		MBeanInfo mbi = null;
        		try {
        			mbi = mbsc.getMBeanInfo(on2);
        		} catch(IOException ioe) {
        			// Ignore
        		}
        		if( mbi != null ) {
        			array[0] = new MBeanInfoWrapper(on2, mbi, mbsc, ObjectNameNode.this);
        		}
        	} else {
		    	connectionWrapper.run(new IJMXRunnable() {
		    		@Override
		    		public void run(MBeanServerConnection mbsc) throws Exception {
		        		MBeanInfo mbi = null;
		        		try {
		        			mbi = mbsc.getMBeanInfo(on2);
		        		} catch(IOException ioe) {
		        			// Ignore
		        		}

		        		if( mbi != null ) {
		    				array[0] = new MBeanInfoWrapper(on2, mbi, mbsc, ObjectNameNode.this);
		    			}
		    		}
		    	});
        	}
    	} catch( Exception e ) {
    		JMXActivator.getDefault().getLog().log(new Status(IStatus.ERROR, JMXActivator.PLUGIN_ID, 
    				"Error loading object name details for JMX object: " + on.toString(), e));
    		loadError = e;
    	}
    	wrapper = array[0];
    }

    public boolean hasLoadError() {
    	return loadError != null;
    }
    
    public ObjectName getObjectName() {
        return on;
    }

    public synchronized MBeanInfoWrapper getMbeanInfoWrapper() {
    	if( wrapper == null ) {
    		loadInfo(null);
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
