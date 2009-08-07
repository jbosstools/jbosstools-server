/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui;

import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.osgi.framework.Bundle;

/**
 * @author Marshall
 *
 * A class that keeps references and disposes of the UI plugin's images
 */
public class JBossServerUISharedImages {

	public static final String IMG_JBOSS = "jboss"; //$NON-NLS-1$
	public static final String IMG_JBOSS_CONFIGURATION = "jbossconfiguration"; //$NON-NLS-1$

	public static final String WIZBAN_JBOSS_LOGO = "jbossLogo"; //$NON-NLS-1$
	public static final String TWIDDLE_IMAGE = "TWIDDLE_IMAGE"; //$NON-NLS-1$
	public static final String INACTIVE_CATEGORY_IMAGE = "INACTIVE_CATEGORY_IMAGE"; //$NON-NLS-1$
	public static final String GENERIC_SERVER_IMAGE = "GENERIC_SERVER_IMAGE"; //$NON-NLS-1$
	public static final String PUBLISH_IMAGE = "PUBLISH_IMAGE"; //$NON-NLS-1$
	public static final String UNPUBLISH_IMAGE = "UNPUBLISH_IMAGE"; //$NON-NLS-1$
	public static final String JMX_IMAGE = "JMX_IMAGE"; //$NON-NLS-1$
	public static final String EXPLORE_IMAGE = "EXPLORE_IMAGE"; //$NON-NLS-1$
	public static final String XPATH_LEVEL_1 = "xpath_level_1"; //$NON-NLS-1$
	public static final String XPATH_LEVEL_2 = "xpath_level_2"; //$NON-NLS-1$
	public static final String XPATH_LEVEL_3 = "xpath_level_3"; //$NON-NLS-1$
	
	
	
	private static JBossServerUISharedImages instance;
	
	private Hashtable<String, Object> images, descriptors;
	
	private JBossServerUISharedImages () {
		instance = this;
		images = new Hashtable<String, Object>();
		descriptors = new Hashtable<String, Object>();
		Bundle pluginBundle = JBossServerUIPlugin.getDefault().getBundle();
		
		descriptors.put(IMG_JBOSS, createImageDescriptor(pluginBundle, "/icons/jboss.gif")); //$NON-NLS-1$
		descriptors.put(IMG_JBOSS_CONFIGURATION, createImageDescriptor(pluginBundle, "/icons/jboss-configuration.gif")); //$NON-NLS-1$
		
		descriptors.put(WIZBAN_JBOSS_LOGO, createImageDescriptor(pluginBundle, "/icons/JBoss_byRH_logo_rgb.png")); //$NON-NLS-1$
		descriptors.put(TWIDDLE_IMAGE, createImageDescriptor(pluginBundle, "icons/twiddle.gif")); //$NON-NLS-1$
		descriptors.put(INACTIVE_CATEGORY_IMAGE, createImageDescriptor(pluginBundle, "/icons/inactiveCat.gif")); //$NON-NLS-1$
		descriptors.put(PUBLISH_IMAGE, createImageDescriptor(pluginBundle, "/icons/publish.gif")); //$NON-NLS-1$
		descriptors.put(UNPUBLISH_IMAGE, createImageDescriptor(pluginBundle, "/icons/unpublish.gif")); //$NON-NLS-1$
		descriptors.put(JMX_IMAGE, createImageDescriptor(pluginBundle, "/icons/jmeth_obj.gif")); //$NON-NLS-1$
		descriptors.put(EXPLORE_IMAGE, createImageDescriptor(pluginBundle, "/icons/actions/xpl/explore.gif")); //$NON-NLS-1$
		descriptors.put(XPATH_LEVEL_1, createImageDescriptor(pluginBundle, "icons/xpath_level_1.gif")); //$NON-NLS-1$
		descriptors.put(XPATH_LEVEL_2, createImageDescriptor(pluginBundle, "icons/xpath_level_2.gif")); //$NON-NLS-1$
		descriptors.put(XPATH_LEVEL_3, createImageDescriptor(pluginBundle, "icons/xpath_level_3.gif")); //$NON-NLS-1$
		
		
		Iterator<String> iter = descriptors.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			ImageDescriptor descriptor = descriptor(key);
			images.put(key,  descriptor.createImage());	
		}
		
		images.put(GENERIC_SERVER_IMAGE, ImageResource.getImageDescriptor(ImageResource.IMG_CTOOL_NEW_SERVER).createImage());
		descriptors.put(GENERIC_SERVER_IMAGE, ImageDescriptor.createFromImage((Image)images.get(GENERIC_SERVER_IMAGE)));
	}
	
	private ImageDescriptor createImageDescriptor (Bundle pluginBundle, String relativePath)
	{
		return ImageDescriptor.createFromURL(pluginBundle.getEntry(relativePath));
	}
	
	private static JBossServerUISharedImages instance() {
		if (instance == null)
			return new JBossServerUISharedImages();
		
		return instance;
	}
	
	public static Image getImage(String key)
	{
		return instance().image(key);
	}
	
	public static ImageDescriptor getImageDescriptor(String key)
	{
		return instance().descriptor(key);
	}
	
	public Image image(String key)
	{
		return (Image) images.get(key);
	}
	
	public ImageDescriptor descriptor(String key)
	{
		return (ImageDescriptor) descriptors.get(key);
	}
	
	protected void finalize() throws Throwable {
		Iterator<String> iter = images.keySet().iterator();
		while (iter.hasNext())
		{
			Image image = (Image) images.get(iter.next());
			image.dispose();
		}
		super.finalize();
	}
}
