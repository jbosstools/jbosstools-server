/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.ui.view.server;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.ui.views.properties.IPropertySource;

public class ServerViewPropertiesSectionFilter implements IFilter {

    public boolean select(Object toTest) {
    	if( toTest instanceof IAdaptable ) {
    		return ((IAdaptable)toTest).getAdapter(IPropertySource.class) != null;
    	}
        IPropertySource  properties = (IPropertySource) Platform.getAdapterManager().getAdapter(toTest, IPropertySource.class);
        return properties != null;
    }

}