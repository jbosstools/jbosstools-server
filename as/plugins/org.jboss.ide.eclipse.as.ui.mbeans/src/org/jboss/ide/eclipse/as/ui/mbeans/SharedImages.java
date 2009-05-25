/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.mbeans;

import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class SharedImages {
	public static final String IMG_MBEAN = "mbean16"; //$NON-NLS-1$

	private static SharedImages instance;
	
	private Hashtable images, descriptors;
	
	private SharedImages () {
		instance = this;
		images = new Hashtable();
		descriptors = new Hashtable();
		
		
		Bundle pluginBundle = Activator.getDefault().getBundle();
		descriptors.put(IMG_MBEAN, createImageDescriptor(pluginBundle, "icons/mbean16")); //$NON-NLS-1$
		
		Iterator iter = descriptors.keySet().iterator();

		while (iter.hasNext()) {
			String key = (String) iter.next();
			ImageDescriptor descriptor = descriptor(key);
			images.put(key,  descriptor.createImage());	
		}
	}
	
	private ImageDescriptor createImageDescriptor (Bundle pluginBundle, String relativePath)
	{
		return ImageDescriptor.createFromURL(pluginBundle.getEntry(relativePath));
	}
	
	private static SharedImages instance() {
		if (instance == null)
			return new SharedImages();
		
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
		Iterator iter = images.keySet().iterator();
		while (iter.hasNext())
		{
			Image image = (Image) images.get(iter.next());
			image.dispose();
		}
		super.finalize();
	}

}
