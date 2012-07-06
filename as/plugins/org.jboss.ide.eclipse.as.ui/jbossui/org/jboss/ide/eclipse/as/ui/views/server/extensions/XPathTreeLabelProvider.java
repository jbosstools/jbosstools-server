package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.XPathTreeContentProvider.ServerWrapper;

public class XPathTreeLabelProvider extends LabelProvider {
	private Image rootImage;
	public XPathTreeLabelProvider() {
		super();
		ImageDescriptor des = ImageDescriptor.createFromURL(JBossServerUIPlugin.getDefault().getBundle().getEntry("icons/XMLFile.gif")); //$NON-NLS-1$
		rootImage = des.createImage();
	}
//	public boolean useNativeToolTip(Object object) {
//        return true;
//    }
//	public String getToolTipText(Object element) {
//	    return "BLAAAAAAAH";
//	}
	
	public Image getImage(Object element) {
		if( element instanceof ServerWrapper )
			return rootImage;
		
		if (element instanceof XPathCategory)
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_FOLDER);

		if( element instanceof XPathQuery ) 
			return JBossServerUISharedImages.getImage(JBossServerUISharedImages.XPATH_LEVEL_1);
		
		if( element instanceof XPathFileResult )
			return JBossServerUISharedImages.getImage(JBossServerUISharedImages.XPATH_LEVEL_2);
		
		if( element instanceof XPathResultNode ) 
			return JBossServerUISharedImages.getImage(JBossServerUISharedImages.XPATH_LEVEL_3);

		return null;
	}

	public String getText(Object element) {
		if( element instanceof ServerWrapper ) 
			return Messages.XPathTreeLabelProvider_XMLConfigLabel;
		if( element == XPathTreeContentProvider.LOADING ) 
			return Messages.XPathTreeLabelProvider_LoadingLabel;
		
		if (element instanceof XPathCategory) 
			return ((XPathCategory) element).getName();

		if( element instanceof XPathQuery ) 
			return ((XPathQuery)element).getName();
		
		if( element instanceof XPathFileResult )
			return ((XPathFileResult)element).getFileLocation();
		
		if( element instanceof XPathResultNode ) 
			return MessageFormat.format(Messages.XPathTreeLabelProvider_MatchIndexLabel, ((XPathResultNode)element).getIndex());
		
		return ""; //$NON-NLS-1$
	}
	
	public XPathResultNode[] getResultNodes(XPathQuery query) {
		ArrayList<XPathResultNode> l = new ArrayList<XPathResultNode>();
		XPathFileResult[] files = query.getResults();
		for( int i = 0; i < files.length; i++ ) {
			l.addAll(Arrays.asList(files[i].getChildren()));
		}
		return l.toArray(new XPathResultNode[l.size()]);
	}
}
