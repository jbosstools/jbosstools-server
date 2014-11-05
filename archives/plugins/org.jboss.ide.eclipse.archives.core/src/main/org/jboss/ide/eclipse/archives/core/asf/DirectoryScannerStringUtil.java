/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */


/*
 * This file has been CHANGED, MODIFIED, EDITED.
 * It has been coppied because list(File file) is
 * a private method and is not able to be overridden.
 *
 * For archives, we need to be able to delegate to
 * the eclipse VFS / resource model.
 * rob.stryker@redhat.com
 */
package org.jboss.ide.eclipse.archives.core.asf;

import org.apache.tools.ant.DirectoryScanner;


public class DirectoryScannerStringUtil {
    /* Working with default excludes */
    public static String[] getDefaultExcludes() {
    	return DirectoryScanner.getDefaultExcludes();
    }
    
    public static String implodeStrings(String[] strings) {
    	StringBuilder buffer = new StringBuilder();
		for( int i = 0; i < strings.length; i++ )
			buffer.append(strings[i]).append(',');
		return buffer.toString();
	}
}