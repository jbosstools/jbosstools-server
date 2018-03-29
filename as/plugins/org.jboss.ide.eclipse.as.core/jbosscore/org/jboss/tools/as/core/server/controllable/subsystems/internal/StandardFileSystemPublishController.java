/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.core.server.controllable.subsystems.internal;

/**
 * This class drives the standard publishing operation.
 * It will also delegate to legacy or override controllers if one is found 
 * that matches the given module type. 
 * It has been demonstrated to work with legacy publishers. 
 * 
 * @deprecated please use the non-internal version
 */
public class StandardFileSystemPublishController extends org.jboss.tools.as.core.server.controllable.subsystems.StandardFileSystemPublishController {
}
