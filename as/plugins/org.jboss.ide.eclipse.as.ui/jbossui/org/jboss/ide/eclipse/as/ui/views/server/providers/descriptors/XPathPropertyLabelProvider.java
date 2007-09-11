package org.jboss.ide.eclipse.as.ui.views.server.providers.descriptors;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.ui.Messages;

public class XPathPropertyLabelProvider extends LabelProvider implements ITableLabelProvider {
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
	public String getColumnText(Object element, int columnIndex) {
		if( element instanceof XPathQuery) {
			if( columnIndex == 0 ) return ((XPathQuery)element).getName();
			if( columnIndex == 1 ) {
				XPathResultNode[] nodes = getResultNodes(((XPathQuery)element));
				if( nodes.length == 1 )
				return nodes[0].getText();
			}
		}

		if( element instanceof XPathFileResult ) {
			XPathFileResult result = (XPathFileResult)element;
			if( columnIndex == 0 ) {
				return result.getFileLocation().substring(result.getQuery().getBaseDir().length());
			}
			if( result.getChildren().length == 1 ) {
				element = result.getChildren()[0];
			}
		}
		
		if( element instanceof XPathResultNode ) {
			XPathResultNode element2 = (XPathResultNode)element;
			if( columnIndex == 0 ) return Messages.DescriptorXPathMatch + element2.getIndex();
			if( columnIndex == 1 ) return element2.getText();
		}
		
		return null; 
	}

	public XPathResultNode[] getResultNodes(XPathQuery query) {
		int count = 0;
		ArrayList l = new ArrayList();
		XPathFileResult[] files = query.getResults();
		for( int i = 0; i < files.length; i++ ) {
			l.addAll(Arrays.asList(files[i].getChildren()));
		}
		return (XPathResultNode[]) l.toArray(new XPathResultNode[l.size()]);
	}

}
