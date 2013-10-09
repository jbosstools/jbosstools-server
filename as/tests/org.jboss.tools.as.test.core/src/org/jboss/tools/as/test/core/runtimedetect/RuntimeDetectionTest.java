/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.test.core.runtimedetect;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.TestCase;

import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.junit.Test;

public class RuntimeDetectionTest extends TestCase {
	@Test
	public void testLegacyIds() {
		HashMap<String, String> legacy = new HashMap<String, String>();
		loadLegacy(legacy);
		
		
		Collection<String> newIds = legacy.keySet();
		Iterator<String> legacyIdIterator = newIds.iterator();
		// Verify old and new apis work here 
		while(legacyIdIterator.hasNext()) {
			String stacksId = legacyIdIterator.next();
			String legacyId = legacy.get(stacksId);
			assertNotNull(stacksId + " not found.", RuntimeCoreActivator.getDefault().findDownloadRuntime(stacksId));
			assertNotNull(legacyId + " not found.", RuntimeCoreActivator.getDefault().findDownloadRuntime(legacyId));
			assertNotNull(legacyId + " not found.", RuntimeCoreActivator.getDefault().getDownloadRuntimes().get(legacyId));
		}
	}
	
	private synchronized void loadLegacy(HashMap<String, String> LEGACY_HASHMAP) {
		LEGACY_HASHMAP.put("jboss-as328SP1runtime", "org.jboss.tools.runtime.core.as.328" );
		LEGACY_HASHMAP.put("jboss-as405runtime", "org.jboss.tools.runtime.core.as.405" );
		LEGACY_HASHMAP.put("jboss-as423runtime", "org.jboss.tools.runtime.core.as.423" );
		LEGACY_HASHMAP.put("jboss-as501runtime", "org.jboss.tools.runtime.core.as.501" );
		LEGACY_HASHMAP.put("jboss-as510runtime", "org.jboss.tools.runtime.core.as.510" );
		LEGACY_HASHMAP.put("jboss-as610runtime", "org.jboss.tools.runtime.core.as.610" );
		LEGACY_HASHMAP.put("jboss-as701runtime", "org.jboss.tools.runtime.core.as.701" );
		LEGACY_HASHMAP.put("jboss-as702runtime", "org.jboss.tools.runtime.core.as.702" );
		LEGACY_HASHMAP.put("jboss-as710runtime", "org.jboss.tools.runtime.core.as.710" );
		LEGACY_HASHMAP.put("jboss-as711runtime", "org.jboss.tools.runtime.core.as.711" );
	}
}
