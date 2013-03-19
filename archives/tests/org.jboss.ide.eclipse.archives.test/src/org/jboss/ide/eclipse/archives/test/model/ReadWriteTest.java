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
package org.jboss.ide.eclipse.archives.test.model;

import java.util.List;

import junit.framework.TestCase;

import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackage;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackages;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbProperty;

public class ReadWriteTest extends TestCase {
	/*
	 * We definitely need more tests like this. 
	 * This is not enough. 
	 */
	
	
	public void testReadWritePackageWithProperties() {
		XbPackages packs = new XbPackages();
		XbPackage pack = new XbPackage();
		pack.setName("name1");
		pack.setToDir("toDir1");
		packs.addChild(pack);
		XbProperty innerProp = new XbProperty();
		innerProp.setName("key5");
		innerProp.setValue("val5");
		
		pack.getProperties().addProperty(innerProp);
		String s = XBMarshallTest.writeToString(packs, true);
		XbPackages packs2 = XBUnmarshallTest.parseFromString(s, true, null);
		List packs2List = packs2.getChildren(XbPackage.class);
		assertEquals(1, packs2List.size());
		XbPackage pack2 = (XbPackage)packs2List.get(0);
		assertEquals(pack.getName(), pack2.getName());
		assertEquals(pack.getToDir(), pack2.getToDir());
		assertEquals(pack.getProperties().getProperties().get("key5"), "val5");
		assertEquals(pack2.getProperties().getProperties().get("key5"), "val5");
	}
}
