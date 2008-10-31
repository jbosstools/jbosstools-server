package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;

public class XPathDecorator extends LabelProvider implements ILightweightLabelDecorator {
	public void decorate(Object element, IDecoration decoration) {
		String decoration2 = getDecoration(element);
		if( decoration2 != null ) {
			decoration.addSuffix(decoration2);
		}
	}
	
	public static String getDecoration(Object element) {
		if( element instanceof XPathQuery) {
			XPathResultNode[] nodes = XPathModel.getResultNodes((XPathQuery)element);
			if(nodes.length == 1 ) {
				return "   " + nodes[0].getText();
			} 
		}

		if( element instanceof XPathFileResult ) {
			XPathResultNode[] nodes = ((XPathFileResult)element).getChildren();
			if( nodes.length == 1 )
				return "   " + nodes[0].getText();
		}
		
		if( element instanceof XPathResultNode ) {
			return ((XPathResultNode)element).getText();
		}
		return null;
	}
}