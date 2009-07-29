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
package org.jboss.ide.eclipse.archives.webtools.filesets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.core.asf.DirectoryScanner;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;

public class FilesetContentProvider implements ITreeContentProvider {
	private static final String FILESET_KEY = "org.jboss.ide.eclipse.as.ui.views.server.providers.FilesetViewProvider.PropertyKey"; //$NON-NLS-1$

	public static class PathWrapper {
		private IPath path;
		private IPath folder;

		public PathWrapper(IPath path, IPath folder) {
			this.path = path;
			this.folder = folder;
		}

		public IPath getFolder() {
			return folder;
		}

		public IPath getPath() {
			return folder.append(path);
		}

		public String getLocalizedResourceName() {
			return path.toOSString();
		}
		
		public boolean equals(Object o) {
			return o == null ? false :
						!(o instanceof PathWrapper) ? false :
							((PathWrapper)o).folder.equals(folder) && ((PathWrapper)o).path.equals(path);
		}
		
		public int hashCode() {
			return path.hashCode() + folder.hashCode();
		}
	}

	public static class FolderWrapper extends PathWrapper {
		private HashMap<String, FolderWrapper> childrenFolders;
		private ArrayList<PathWrapper> children;

		public FolderWrapper(IPath path, IPath folder) {
			super(path, folder);
			children = new ArrayList<PathWrapper>();
			childrenFolders = new HashMap<String, FolderWrapper>();
		}

		public void addChild(IPath path) {
			if (path.segmentCount() == 1) {
				children.add(new PathWrapper(path, getFolder().append(
						getLocalizedResourceName())));
			} else {
				addPath(children, childrenFolders, path, getFolder().append(
						getLocalizedResourceName()));
			}
		}

		public Object[] getChildren() {
			return children.toArray(new Object[children.size()]);
		}

		private void addPath(ArrayList<PathWrapper> children,
				HashMap<String, FolderWrapper> folders, IPath path, IPath folder) {
			try {
				FolderWrapper fw = null;
				if (!folders.containsKey(path.segment(0))) {
					fw = new FolderWrapper(path.removeLastSegments(path
							.segmentCount() - 1), folder);
					folders.put(path.segment(0), fw);
					children.add(fw);
				} else {
					fw = folders.get(path.segment(0));
				}
				fw.addChild(path.removeFirstSegments(1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public class ServerWrapper {
		public IServer server;
		private Fileset[] children;

		public ServerWrapper(IServer server) {
			this.server = server;
		}

		public int hashCode() {
			return server.getId().hashCode();
		}

		public boolean equals(Object other) {
			return other instanceof ServerWrapper
					&& ((ServerWrapper) other).server.getId().equals(
							server.getId());
		}

		public void addFileset(Fileset fs) {
			Fileset[] filesetsNew = new Fileset[children.length + 1];
			System.arraycopy(children, 0, filesetsNew, 0, children.length);
			filesetsNew[filesetsNew.length - 1] = fs;
			children = filesetsNew;
			saveFilesets();
		}

		public void removeFileset(Fileset fs) {
			ArrayList<Fileset> asList = new ArrayList<Fileset>(Arrays
					.asList(children));
			asList.remove(fs);
			children = asList.toArray(new Fileset[asList.size()]);
			saveFilesets();
		}

		public Fileset[] getFilesets() {
			if (children == null)
				children = loadFilesets();
			return children;
		}

		private Fileset[] loadFilesets() {
			if( FilesetUtil.getFile(server).exists()) {
				return FilesetUtil.loadFilesets(server);
			} else {
				return loadFilesets_LEGACY();
			}
		}
		
		@Deprecated
		private Fileset[] loadFilesets_LEGACY() {
			Fileset[] filesets = new Fileset[0];
			if (server != null) {
				ServerAttributeHelper helper = ServerAttributeHelper
						.createHelper(server);
				List<String> tmp = (ArrayList<String>)helper.getAttribute(FILESET_KEY, new ArrayList<String>());
				String[] asStrings = (String[]) tmp.toArray(new String[tmp
						.size()]);
				filesets = new Fileset[asStrings.length];
				for (int i = 0; i < asStrings.length; i++) {
					filesets[i] = new Fileset(asStrings[i]);
					filesets[i].setServer(server);
				}
			}
			return filesets;
		}

		public void saveFilesets() {
			FilesetUtil.saveFilesets(server, children);
		}
	}
	

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IServer) {
			return new Object[] { new ServerWrapper((IServer) parentElement) };
		}
		if (parentElement instanceof ServerWrapper) {
			return ((ServerWrapper) parentElement).getFilesets();
		} else if (parentElement instanceof Fileset) {
			Fileset fs = (Fileset) parentElement;
			IPath[] paths = null;
			try {
				paths = findPaths(fs.getFolder(), fs.getIncludesPattern(), fs
						.getExcludesPattern());
			} catch (BuildException be) {
				return new Object[] {};
			}

			HashMap<String, FolderWrapper> folders = new HashMap<String, FolderWrapper>();
			ArrayList<PathWrapper> wrappers = new ArrayList<PathWrapper>();
			for (int i = 0; i < paths.length; i++) {
				if (paths[i].segmentCount() == 1) {
					wrappers.add(new PathWrapper(paths[i], new Path(fs
							.getFolder())));
				} else {
					addPath(wrappers, folders, paths[i], new Path(fs
							.getFolder()));
				}
			}
			return wrappers.toArray(new Object[wrappers.size()]);
		} else if (parentElement instanceof FolderWrapper) {
			return ((FolderWrapper) parentElement).getChildren();
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0 ? true : false;
	}

	public Object[] getElements(Object inputElement) {
		return null;
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	private IPath[] findPaths(String dir, String includes, String excludes) {
		IPath[] paths = new IPath[0];
		try {
			if (dir != null) {
				DirectoryScanner scanner = DirectoryScannerFactory
						.createDirectoryScanner(dir, null, includes, excludes,
								null, false, 1, true);
				if (scanner != null) {
					String[] files = scanner.getIncludedFiles();
					paths = new IPath[files.length];
					for (int i = 0; i < files.length; i++) {
						paths[i] = new Path(files[i]);
					}
				}
			}
		} catch (IllegalStateException ise) {
		}
		return paths;
	}

	private static void addPath(ArrayList<PathWrapper> children,
			HashMap<String, FolderWrapper> folders, IPath path, IPath folder) {
		try {
			FolderWrapper fw = null;
			if (!folders.containsKey(path.segment(0))) {
				fw = new FolderWrapper(path.removeLastSegments(path
						.segmentCount() - 1), folder);
				folders.put(path.segment(0), fw);
				children.add(fw);
			} else {
				fw = folders.get(path.segment(0));
			}
			fw.addChild(path.removeFirstSegments(1));
		} catch (Exception e) {
			// ignore
		}
	}
}