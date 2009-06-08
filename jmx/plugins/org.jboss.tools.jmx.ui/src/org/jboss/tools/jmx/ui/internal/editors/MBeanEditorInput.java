/*******************************************************************************
 * Copyright (c) 2007 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.internal.editors;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.jboss.tools.jmx.core.MBeanInfoWrapper;

public class MBeanEditorInput implements IEditorInput {

    private MBeanInfoWrapper wrapper;

    public MBeanEditorInput(MBeanInfoWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public boolean exists() {
        return false;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return wrapper.getObjectName().toString();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return wrapper.getObjectName().getCanonicalName();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        return null;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MBeanEditorInput)) {
            return false;
        }
        MBeanEditorInput other = (MBeanEditorInput) obj;
        return other.wrapper.getObjectName().equals(wrapper.getObjectName());
    }

    public MBeanInfoWrapper getWrapper() {
        return wrapper;
    }

}
