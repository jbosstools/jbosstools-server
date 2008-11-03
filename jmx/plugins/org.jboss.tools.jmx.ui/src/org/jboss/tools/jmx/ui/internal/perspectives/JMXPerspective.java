/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.ui.internal.perspectives;


import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.jboss.tools.jmx.ui.internal.views.navigator.Navigator;

public class JMXPerspective implements IPerspectiveFactory {

    private IPageLayout factory;

    public JMXPerspective() {
        super();
    }

    public void createInitialLayout(IPageLayout factory) {
        this.factory = factory;
        factory.setEditorAreaVisible(true);
        addViews();
        addViewShortcuts();
    }

    private void addViews() {
        IFolderLayout left = factory.createFolder("left", //$NON-NLS-1$
                IPageLayout.LEFT, 0.2f, factory.getEditorArea());
        left.addView(Navigator.VIEW_ID);
    }

    private void addViewShortcuts() {
        factory.addShowViewShortcut(Navigator.VIEW_ID);
        factory.addShowViewShortcut("org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
    }

}
