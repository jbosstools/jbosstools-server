package org.jboss.ide.eclipse.archives.ui.providers;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveAction;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.PrefsInitializer;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesContentProviderDelegate.DelayProxy;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesContentProviderDelegate.WrappedProject;

public class ArchivesLabelProvider extends BaseLabelProvider implements ILabelProvider {
	
	
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
				switch (node.getNodeType()) {
					case IArchiveNode.TYPE_ARCHIVE: {
						IArchive pkg = (IArchive) node;
						if (!pkg.isExploded())
							return ArchivesSharedImages.getImage(ArchivesSharedImages.IMG_PACKAGE);
						else
							return ArchivesSharedImages.getImage(ArchivesSharedImages.IMG_PACKAGE_EXPLODED);
					}
					case IArchiveNode.TYPE_ARCHIVE_FOLDER: return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
					case IArchiveNode.TYPE_ARCHIVE_FILESET: {
						return ArchivesSharedImages.getImage(ArchivesSharedImages.IMG_MULTIPLE_FILES);
					}
				}
			}

		}
		return null;
	}

	private String internalGetText(Object element) {
		if( element == ArchivesRootContentProvider.NO_PROJECT)
			return "Please select a project";
		if( element instanceof WrappedProject ) {
			switch(((WrappedProject)element).getType()) {
				case WrappedProject.NAME: 
					return (((WrappedProject)element).getElement().getName());
				case WrappedProject.CATEGORY:
					return "Project Archives";
			}
		}
		if( element instanceof DelayProxy ) 
			return "Loading...";
		if( element instanceof IArchiveNode ) {
			switch (((IArchiveNode)element).getNodeType()) {
				case IArchiveNode.TYPE_ARCHIVE: return getPackageText((IArchive)element);
				case IArchiveNode.TYPE_ARCHIVE_FOLDER: return getPackageFolderText((IArchiveFolder)element);
				case IArchiveNode.TYPE_ARCHIVE_FILESET: return getPackageFileSetText((IArchiveFileSet)element);
				case IArchiveNode.TYPE_ARCHIVE_ACTION: return getArchiveActionText((IArchiveAction)element);
			}

		}
		return element.toString();
	}
	
	
	private String getPackageFolderText (IArchiveFolder folder) {
		return folder.getName();
	}
	private String getPackageText (IArchive pkg) {
		String text = pkg.getName();
		if (PrefsInitializer.getBoolean( PrefsInitializer.PREF_SHOW_PACKAGE_OUTPUT_PATH)) {
			text += " [" + PathUtils.getGlobalLocation(pkg) + "]";
		}
		return text;
	}

	private String getArchiveActionText (IArchiveAction action) {
		return action.toString();
	}

	private String getPackageFileSetText (IArchiveFileSet fileset) {
		boolean showFullPath = PrefsInitializer.getBoolean(
				PrefsInitializer.PREF_SHOW_FULL_FILESET_ROOT_DIR);
		boolean inWorkspace = fileset.isInWorkspace();
		
		String text = "";
		// +[includes] [excludes] : /path/to/root
		text += "+[" + fileset.getIncludesPattern() + "] ";
		
		if (fileset.getExcludesPattern() != null) {
			text += "-[" + fileset.getExcludesPattern() + "] : ";
		}

		if (showFullPath) {
			text += PathUtils.getGlobalLocation(fileset);
		} else if( inWorkspace ){
			text += fileset.getRawSourcePath();
		} else {
			text += PathUtils.getGlobalLocation(fileset).lastSegment();
		}
		
		return text;
	}

}
