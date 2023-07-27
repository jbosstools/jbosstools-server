/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.model;

/**
 * Listeners to the primary model for this extension
 */
public interface IRspCoreChangeListener {
    /**
     * Respond to model changes in the given object or its parents / children.
     * @param item
     */
    public void modelChanged(Object item);
}
