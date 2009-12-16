package org.jboss.ide.eclipse.archives.webtools;

import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public class SharedImages {
	private static SharedImages instance;
	public static final String FILESET_IMAGE = "multiple_files"; //$NON-NLS-1$

	private Hashtable<String, Object> images, descriptors;
	
	private SharedImages () {
		instance = this;
		images = new Hashtable<String, Object>();
		descriptors = new Hashtable<String, Object>();
		Bundle pluginBundle = IntegrationPlugin.getDefault().getBundle();
		
		descriptors.put(FILESET_IMAGE, createImageDescriptor(pluginBundle, "/icons/multiple_files.gif")); //$NON-NLS-1$
		Iterator<String> iter = descriptors.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			ImageDescriptor descriptor = descriptor(key);
			images.put(key,  descriptor.createImage());	
		}
	}
	
	private ImageDescriptor createImageDescriptor (Bundle pluginBundle, String relativePath) {
		return ImageDescriptor.createFromURL(pluginBundle.getEntry(relativePath));
	}
	
	public static SharedImages instance() {
		if (instance == null)
			instance = new SharedImages();
		return instance;
	}
	
	public static Image getImage(String key) {
		return instance().image(key);
	}
	
	public static ImageDescriptor getImageDescriptor(String key) {
		return instance().descriptor(key);
	}
	
	public Image image(String key) {
		return (Image) images.get(key);
	}
	
	public ImageDescriptor descriptor(String key) {
		return (ImageDescriptor) descriptors.get(key);
	}
	
	public void cleanup() {
		Iterator<String> iter = images.keySet().iterator();
		while (iter.hasNext()) {
			Image image = (Image) images.get(iter.next());
			image.dispose();
		}
		images = null;
		instance = null;
	}
	
	protected void finalize() throws Throwable {
		cleanup();
		super.finalize();
	}
	
}
