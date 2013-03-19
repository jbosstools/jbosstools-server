/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package my.pack;

import javax.ejb.Stateless;

import util.pack.MyModel;

public @Stateless class TigerBeanBean implements TigerBean {
	public void doSomething() {
		MyModel model = new MyModel();
		System.out.println(model);
	}
}
