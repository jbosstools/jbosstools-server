/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ArchivesSharedImages {
	// image ids
	public static final String IMG_PACKAGE = "icons/jar_obj.gif"; //$NON-NLS-1$
	public static final String IMG_PACKAGE_EXPLODED = "icons/jar_exploded_obj.gif";//$NON-NLS-1$
	public static final String IMG_EXTERNAL_FILE = "icons/ext_file_obj.gif";//$NON-NLS-1$
	public static final String IMG_EXTERNAL_FOLDER = "icons/ext_folder_obj.gif";//$NON-NLS-1$
	public static final String IMG_INCLUDES = "icons/includes.gif";//$NON-NLS-1$
	public static final String IMG_EXCLUDES = "icons/excludes.gif";//$NON-NLS-1$
	public static final String IMG_NEW_PACKAGE = "icons/new_package.gif";//$NON-NLS-1$
	public static final String IMG_NEW_JAR_WIZARD = "icons/new_jar_wiz.png";//$NON-NLS-1$
	public static final String IMG_SINGLE_FILE = "icons/single_file.gif";//$NON-NLS-1$
	public static final String IMG_MULTIPLE_FILES = "icons/multiple_files.gif";//$NON-NLS-1$
	public static final String IMG_COLLAPSE_ALL = "icons/collapseall.gif";//$NON-NLS-1$
	public static final String IMG_PACKAGE_EDIT = "icons/jar_src_obj.gif";//$NON-NLS-1$
	public static final String IMG_WAR = "icons/war.gif";//$NON-NLS-1$
	public static final String IMG_EAR = "icons/ear.gif";//$NON-NLS-1$
	public static final String IMG_EJB_JAR = "icons/EJBJar.gif";//$NON-NLS-1$
	public static final String IMG_NEW_WAR_WIZARD="icons/new_war_wiz.png";//$NON-NLS-1$
	public static final String IMG_NEW_EAR_WIZARD="icons/ear-wiz-banner.gif";//$NON-NLS-1$
	public static final String IMG_BUILD_PACKAGES = "icons/build_packages.gif";//$NON-NLS-1$

	public static void register(ImageRegistry registry) {
		registerImages(registry, new String[] {
				IMG_PACKAGE, IMG_PACKAGE_EXPLODED, IMG_EXTERNAL_FILE,
				IMG_EXTERNAL_FOLDER, IMG_INCLUDES, IMG_EXCLUDES,
				IMG_NEW_PACKAGE, IMG_NEW_JAR_WIZARD, IMG_SINGLE_FILE,
				IMG_MULTIPLE_FILES, IMG_COLLAPSE_ALL, IMG_PACKAGE_EDIT,
				IMG_EAR, IMG_EJB_JAR, IMG_WAR, IMG_NEW_EAR_WIZARD,
				IMG_NEW_WAR_WIZARD, IMG_BUILD_PACKAGES
			});
	}

	private static void registerImages (ImageRegistry reg, String ids[]) {
		for (int i = 0; i < ids.length; i++)
			reg.put(ids[i], AbstractUIPlugin.imageDescriptorFromPlugin(PackagesUIPlugin.PLUGIN_ID, ids[i]));
	}

	// helper methods
	public static Image getImage (String id) {
		return PackagesUIPlugin.getDefault().getImageRegistry().get(id);
	}

	public static ImageDescriptor getImageDescriptor (String id) {
		return PackagesUIPlugin.getDefault().getImageRegistry().getDescriptor(id);
	}

}
