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
package org.jboss.ide.eclipse.archives.ui.providers;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveAction;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveStandardFileSet;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveFileSetImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveFolderImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveImpl;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;
import org.jboss.ide.eclipse.archives.ui.ExtensionManager;
import org.jboss.ide.eclipse.archives.ui.PrefsInitializer;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesContentProviderDelegate.DelayProxy;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesContentProviderDelegate.WrappedProject;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class ArchivesLabelProvider extends BaseLabelProvider implements ILabelProvider {
	public static final int IGNORE_FULL_PATHS = 1;
	public static final int SHOW_FULL_PATHS = 2;
	public static final int FOLLOW_PREFS_FULL_PATHS = 3;
	
	private int showFullPaths;
	public ArchivesLabelProvider() {
		this(FOLLOW_PREFS_FULL_PATHS);
	}
	
	public ArchivesLabelProvider(int showFullPaths) {
		this.showFullPaths = showFullPaths;
	}

	/*
	 * Important snippets to save
	 * image = PlatformUI.getWorkbench().getDecoratorManager().decorateImage(image, element);
	 * text = PlatformUI.getWorkbench().getDecoratorManager().decorateText(text, element);
	 */

	public Image getImage(Object element) {
		Image image = internalGetImage(element);

		if (image != null) {
			image = PlatformUI.getWorkbench().getDecoratorManager().decorateImage(image, element);
		}

		return image;
	}

	public String getText(Object element) {
		String text = internalGetText(element);

		if (text != null) {
			text = PlatformUI.getWorkbench().getDecoratorManager().decorateText(text, element);
		}
		return text;
	}

	private Image internalGetImage(Object element) {
		if( element instanceof WrappedProject ) {
			switch(((WrappedProject)element).getType()) {
				case WrappedProject.NAME:
					return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT);
				case WrappedProject.CATEGORY:
					return ArchivesSharedImages.getImage(ArchivesSharedImages.IMG_PACKAGE);
			}
		}

		if( element instanceof IArchiveNode ) {
			IArchiveNode node = (IArchiveNode) element;
			if (node != null) {
				if( isInternal(node)) {
					switch (node.getNodeType()) {
						case IArchiveNode.TYPE_ARCHIVE: {
							if( node instanceof ArchiveImpl ) {
								IArchive pkg = (IArchive) node;
								if (!pkg.isExploded())
									return ArchivesSharedImages.getImage(ArchivesSharedImages.IMG_PACKAGE);
								else
									return ArchivesSharedImages.getImage(ArchivesSharedImages.IMG_PACKAGE_EXPLODED);
							}
						}
						case IArchiveNode.TYPE_ARCHIVE_FOLDER: 
							if( node instanceof ArchiveFolderImpl) 
								return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
						case IArchiveNode.TYPE_ARCHIVE_FILESET: {
							if( node instanceof ArchiveFileSetImpl )
								return ArchivesSharedImages.getImage(ArchivesSharedImages.IMG_MULTIPLE_FILES);
						}
					} // end switch
				} else {
					ILabelProvider delegate = ExtensionManager.findLabelProvider(element);
					if( delegate != null ) 
						return delegate.getImage(element);
				}
			}

		}
		return null;
	}

	private String internalGetText(Object element) {
		if( element == ArchivesRootContentProvider.NO_PROJECT)
			return ArchivesUIMessages.SelectAProject;
		if( element instanceof WrappedProject ) {
			switch(((WrappedProject)element).getType()) {
				case WrappedProject.NAME:
					return (((WrappedProject)element).getElement().getName());
				case WrappedProject.CATEGORY:
					return ArchivesUIMessages.ProjectArchives;
			}
		}
		if( element instanceof DelayProxy )
			return ArchivesUIMessages.Loading;
		if( element instanceof IArchiveNode ) {
			if( isInternal((IArchiveNode)element)) {
				switch (((IArchiveNode)element).getNodeType()) {
					case IArchiveNode.TYPE_ARCHIVE: return getPackageText((IArchive)element);
					case IArchiveNode.TYPE_ARCHIVE_FOLDER: return getPackageFolderText((IArchiveFolder)element);
					case IArchiveNode.TYPE_ARCHIVE_ACTION: return getArchiveActionText((IArchiveAction)element);
					case IArchiveNode.TYPE_ARCHIVE_FILESET: return getPackageFileSetText((IArchiveFileSet)element);
				}
			} else {
				ILabelProvider delegate = ExtensionManager.findLabelProvider(element);
				if( delegate != null ) 
					return delegate.getText(element);
			}
		}
		return element.toString();
	}

	private boolean isInternal(IArchiveNode node) {
		if( node instanceof ArchiveImpl || 
				node instanceof ArchiveFolderImpl || 
				node instanceof ArchiveFileSetImpl)
			return true;
		return false;
	}
	

	private String getPackageFolderText (IArchiveFolder folder) {
		return folder.getName();
	}
	private String getPackageText (IArchive pkg) {
		String text = pkg.getName();
		if (showFullPaths == SHOW_FULL_PATHS || 
				(showFullPaths == FOLLOW_PREFS_FULL_PATHS &&
						PrefsInitializer.getBoolean( PrefsInitializer.PREF_SHOW_PACKAGE_OUTPUT_PATH))) {
			text += " [" + PathUtils.getAbsoluteLocation(pkg) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return text;
	}

	private String getArchiveActionText (IArchiveAction action) {
		return action.toString();
	}

	private String getPackageFileSetText (IArchiveFileSet fileset2) {
		if( fileset2 instanceof IArchiveStandardFileSet) {
			IArchiveStandardFileSet fileset = (IArchiveStandardFileSet)fileset2;
			boolean showFullPath = showFullPaths == SHOW_FULL_PATHS || 
					(showFullPaths == FOLLOW_PREFS_FULL_PATHS && 
				PrefsInitializer.getBoolean(
					PrefsInitializer.PREF_SHOW_FULL_FILESET_ROOT_DIR));
			boolean inWorkspace = fileset.isInWorkspace();
	
			String text = ""; //$NON-NLS-1$
			// +[includes] [excludes] : /path/to/root
			text += "+[" + fileset.getIncludesPattern() + "] "; //$NON-NLS-1$ //$NON-NLS-2$
	
			if (fileset.getExcludesPattern() != null) {
				text += "-[" + fileset.getExcludesPattern() + "] : "; //$NON-NLS-1$ //$NON-NLS-2$
			}
	
			if (showFullPath) {
				text += PathUtils.getAbsoluteLocation(fileset);
			} else if( inWorkspace ){
				text += fileset.getRawSourcePath();
			} else {
				text += new Path(PathUtils.getAbsoluteLocation(fileset)).lastSegment();
			}
	
			return text;
		}
		return null;
	}

}
