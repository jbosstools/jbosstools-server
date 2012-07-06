package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.tools.as.wst.server.ui.xpl.ServerToolTip;

public class XPathTreeContentProvider implements ITreeContentProvider {

	public static class DelayProxy {
	}

	public static final DelayProxy LOADING = new DelayProxy();

	private Viewer viewer;
	private ArrayList<XPathCategory> loading = new ArrayList<XPathCategory>();

	public class ServerWrapper {
		public IServer server;

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
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement == null)
			return new Object[] {};
		if (parentElement instanceof IServer) {
			return new Object[] { new ServerWrapper((IServer) parentElement) };
		}

		if (parentElement instanceof ServerWrapper) {
			return XPathModel.getDefault().getCategories(
					((ServerWrapper) parentElement).server);
		}

		if (parentElement instanceof XPathCategory) {
			if (((XPathCategory) parentElement).isLoaded())
				return ((XPathCategory) parentElement).getQueries();

			if (!loading.contains((XPathCategory) parentElement))
				launchLoad((XPathCategory) parentElement);

			return new Object[] { LOADING };
		}

		// we're the named element (JNDI)
		if (parentElement instanceof XPathQuery) {
			if (XPathModel.getResultNodes((XPathQuery) parentElement).length == 1) {
				return new Object[0];
			} else {
				return ((XPathQuery) parentElement).getResults();
			}
		}

		// we're a file node (blah.xml)
		if (parentElement instanceof XPathFileResult) {
			if (((XPathFileResult) parentElement).getChildren().length == 1)
				return new Object[0];
			return ((XPathFileResult) parentElement).getChildren();
		}

		if (parentElement instanceof XPathResultNode) {
			return new Object[0];
		}

		return new Object[0];
	}

	protected void launchLoad(final XPathCategory cat) {
		new Job(Messages.XPathTreeContentProvider_JobName) {
			protected IStatus run(IProgressMonitor monitor) {
				loading.add(cat);
				XPathQuery[] queries = cat.getQueries();
				XPathFileResult[] results;
				for (int i = 0; i < queries.length; i++) {
					results = queries[i].getResults();
					for (int j = 0; j < results.length; j++) {
						results[j].getChildren();
					}
				}
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						loading.remove(cat);
						((StructuredViewer) viewer).refresh(cat.getServer());
					}
				});
				return Status.OK_STATUS;
			}
		}.schedule(200);
	}

	
	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0 ? true : false;
	}

	public Object[] getElements(Object inputElement) {
		return new Object[0];
	}

	public void dispose() {
	}

	private ServerToolTip tooltip = null;
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if( tooltip != null )
			tooltip.deactivate();
		
		this.viewer = viewer;
		tooltip = new ServerToolTip(((TreeViewer)viewer).getTree()) {
			
			@Override
			protected boolean isMyType(Object selected) {
				return selected instanceof ServerWrapper;
			}
			@Override
			protected void fillStyledText(Composite parent, StyledText sText, Object o) {
				sText.setText("Quickly modify xpath values in your server installation.");
			}
		};
		tooltip.setShift(new Point(15, 8));
		tooltip.setPopupDelay(500); // in ms
		tooltip.setHideOnMouseDown(true);
		tooltip.activate();
	}
}