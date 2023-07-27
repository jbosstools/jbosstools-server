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

import org.eclipse.swt.graphics.Image;

/**
 * Provides basic information for each RSP type and allows an instance of the RSP to be created
 */
public interface IRspType {
    String getId();
    String getName();
    IRsp createRsp();
    IRsp createRsp(String version, String url);
    Image getIcon();
    Image getIcon(String serverTypeId);
    String getServerHome();
}
