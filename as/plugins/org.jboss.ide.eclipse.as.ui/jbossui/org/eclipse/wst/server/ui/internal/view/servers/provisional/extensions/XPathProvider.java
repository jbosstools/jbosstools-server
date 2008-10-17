package org.eclipse.wst.server.ui.internal.view.servers.provisional.extensions;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.view.servers.provisional.extensions.XPathProvider.XPathTreeContentProvider.ServerWrapper;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.dialogs.XPathDialogs;
import org.jboss.ide.eclipse.as.ui.dialogs.XPathDialogs.XPathCategoryDialog;
import org.jboss.ide.eclipse.as.ui.dialogs.XPathDialogs.XPathDialog;

public class XPathProvider {
	public static class XPathTreeContentProvider implements
			ITreeContentProvider {
		
		public static class DelayProxy {}
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
				return other instanceof ServerWrapper && 
					((ServerWrapper)other).server.getId().equals(server.getId());
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
			
			if( parentElement instanceof XPathCategory) {
				if( ((XPathCategory)parentElement).isLoaded())
					return ((XPathCategory)parentElement).getQueries();

				if( !loading.contains((XPathCategory)parentElement))
					launchLoad((XPathCategory)parentElement);
				
				return new Object[] { LOADING };
			}
			
			// we're the named element (JNDI)
			if( parentElement instanceof XPathQuery) {
				if( XPathProvider.getResultNodes((XPathQuery)parentElement).length == 1 ) {
					return new Object[0];
				} else {
					return ((XPathQuery)parentElement).getResults();
				}
			}

			// we're a file node (blah.xml) 
			if( parentElement instanceof XPathFileResult ) {
				if( ((XPathFileResult)parentElement).getChildren().length == 1 ) 
					return new Object[0];
				return ((XPathFileResult)parentElement).getChildren();
			}

			if( parentElement instanceof XPathResultNode ) {
				return new Object[0];
			}
			
			
			return new Object[0];
		}


		protected void launchLoad(final XPathCategory cat) {
			new Job("Loading XPaths") {
				protected IStatus run(IProgressMonitor monitor) {
					loading.add(cat);
					XPathQuery[] queries = cat.getQueries();
					XPathFileResult[] results;
					for( int i = 0; i < queries.length; i++ ) {
						results = queries[i].getResults();
						for( int j = 0; j < results.length; j++ ) {
							results[j].getChildren();
						}
					}
					Display.getDefault().asyncExec(new Runnable() { 
						public void run() {
							loading.remove(cat);
							((StructuredViewer)viewer).refresh(cat.getServer());
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

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.viewer = viewer;
		}
	}

	public static class XPathTreeLabelProvider extends LabelProvider {
		private Image rootImage;
		public XPathTreeLabelProvider() {
			super();
			ImageDescriptor des = ImageDescriptor.createFromURL(JBossServerUIPlugin.getDefault().getBundle().getEntry("icons/XMLFile.gif"));
			rootImage = des.createImage();
		}

		public Image getImage(Object element) {
			if( element instanceof ServerWrapper )
				return rootImage;
			
			if (element instanceof XPathCategory)
				return PlatformUI.getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_OBJ_FOLDER);

			return null;
		}

		public String getText(Object element) {
			if( element instanceof ServerWrapper ) 
				return "XML Configuration";
			if( element == XPathTreeContentProvider.LOADING ) 
				return "Loading...";
			
			if (element instanceof XPathCategory) 
				return ((XPathCategory) element).getName();

			if( element instanceof XPathQuery ) 
				return ((XPathQuery)element).getName();
			
			if( element instanceof XPathFileResult )
				return ((XPathFileResult)element).getFileLocation();
			
			return "";
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

	public static class XMLActionProvider extends CommonActionProvider {

		private ICommonActionExtensionSite actionSite;
		public Action newXPathCategoryAction, deleteXPathCategoryAction,
				newXPathAction, editXPathAction, deleteXPathAction,
				editFileAction;
		private Object selectedNode;
		
		public void init(ICommonActionExtensionSite aSite) {
			super.init(aSite);
			this.actionSite = aSite;
			createActions();
		}

		public void createActions() {
			newXPathCategoryAction = new Action() {
				public void run() {
					XPathCategoryDialog d = new XPathCategoryDialog(Display
							.getCurrent().getActiveShell(), getServer());
					if (d.open() == Window.OK) {
						XPathModel.getDefault()
								.addCategory(getServer(), d.getText());
						XPathModel.getDefault().save(getServer());
						refreshViewer();
					}
				}
			};
			newXPathCategoryAction.setText("New Category");

			deleteXPathCategoryAction = new Action() {
				public void run() {
					int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO;
					MessageBox messageBox = new MessageBox(Display.getCurrent()
							.getActiveShell(), style);
					messageBox.setText(Messages.DescriptorXPathRemoveCategory
							+ "?");
					messageBox
							.setMessage(Messages.DescriptorXPathRemoveCategoryDesc);
					if (messageBox.open() == SWT.YES) {
						XPathModel.getDefault().removeCategory(getServer(),
								((XPathCategory)selectedNode).getName());
						XPathModel.getDefault().save(getServer());
						refreshViewer();
					}
				}
			};
			deleteXPathCategoryAction
					.setText(Messages.DescriptorXPathRemoveCategory);

			newXPathAction = new Action() {
				public void run() {
					XPathCategory category = (XPathCategory)selectedNode;
					if (category != null) {
						String categoryName = category.getName();
						XPathDialog d = new XPathDialog(Display.getCurrent()
								.getActiveShell(), getServer(), categoryName);
						if (d.open() == Window.OK) {
							XPathCategory[] categoryList = XPathModel
									.getDefault().getCategories(getServer());
							XPathCategory categoryItem = null;
							for (int i = 0; i < categoryList.length; i++) {
								if (categoryList[i].getName().equals(
										category.getName()))
									categoryItem = categoryList[i];
							}
							if (categoryItem != null) {
								XPathQuery query = new XPathQuery(d.getName(),
										XPathDialogs.getConfigFolder(getServer()),
										null, d.getXpath(), d.getAttribute());
								categoryItem.addQuery(query);
								XPathModel.getDefault().save(getServer());
								refreshViewer();
							}
						}
					}
				}
			};
			newXPathAction.setText(Messages.DescriptorXPathNewXPath);

			editXPathAction = new Action() {
				public void run() {
					Object o = selectedNode;
					if (o != null && o instanceof XPathQuery) {
						XPathQuery original = (XPathQuery) o;
						XPathCategory category = original.getCategory();

						XPathDialog d = new XPathDialog(Display.getCurrent()
								.getActiveShell(), getServer(), category.getName(),
								original.getName());
						d.setAttribute(original.getAttribute());
						d.setXpath(original.getXpathPattern());

						if (d.open() == Window.OK) {
							original.setAttribute(d.getAttribute());
							original.setXpathPattern(d.getXpath());
							original.setName(d.getName());
							category.save();
							refreshViewer();
						}
					}
				}
			};
			editXPathAction.setText(Messages.DescriptorXPathEditXPath);

			deleteXPathAction = new Action() {
				public void run() {
					Object o = selectedNode;
					if (o instanceof XPathQuery) {
						XPathCategory cat = ((XPathQuery) o).getCategory();
						cat.removeQuery((XPathQuery) o);
						cat.save();
						refreshViewer();
					}
				}
			};
			deleteXPathAction.setText(Messages.DescriptorXPathDeleteXPath);

			editFileAction = new Action() {
				public void run() {
					try {
						Object o = selectedNode;
						Path p = null;
						if (o instanceof XPathQuery
								&& ((XPathQuery) o).getResults().length == 1) {
							o = (XPathFileResult) ((XPathQuery) o).getResults()[0];
						}
						if (o instanceof XPathFileResult) {
							p = new Path(((XPathFileResult) o)
									.getFileLocation());
						} else if (o instanceof XPathResultNode) {
							p = new Path(((XPathResultNode) o)
									.getFileLocation());
						}
						if (p != null) {

							IFileStore fileStore = EFS.getLocalFileSystem()
									.getStore(p.removeLastSegments(1));
							fileStore = fileStore.getChild(p.lastSegment());
							IWorkbench wb = PlatformUI.getWorkbench();
							IWorkbenchWindow win = wb
									.getActiveWorkbenchWindow();
							IWorkbenchPage page = win.getActivePage();

							if (!fileStore.fetchInfo().isDirectory()
									&& fileStore.fetchInfo().exists()) {
								try {
									IDE.openEditorOnFileStore(page, fileStore);
								} catch (PartInitException e) {
								}
							}
						}
					} catch (Exception exc) {
						JBossServerUIPlugin.log(
								"Error running edit file action", exc);
					}
				}
			};
			editFileAction.setText("Edit File");
		}

		public void fillContextMenu(IMenuManager menu) {
			ICommonViewerSite site = actionSite.getViewSite();
			IStructuredSelection selection = null;
			if (site instanceof ICommonViewerWorkbenchSite) {
				ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
				selection = (IStructuredSelection) wsSite
						.getSelectionProvider().getSelection();
				Object first = selection.getFirstElement();
				if( first == null )
					return;
				
				if (first instanceof ServerWrapper) {
					menu.add(newXPathCategoryAction);
					menu.add(new Separator());
					return;
				}

				if (first instanceof XPathCategory) {

					menu.add(newXPathAction);
					menu.add(deleteXPathCategoryAction);
					menu.add(new Separator());
					return;
				}
				
				if( first instanceof XPathQuery) {
					selectedNode = first;
					menu.add(newXPathAction);
					menu.add(editXPathAction);
					menu.add(deleteXPathAction);
				}
				if( (first instanceof XPathResultNode || first instanceof XPathFileResult ) || 
					(first instanceof XPathQuery && ((XPathQuery)first).getResults().length == 1)) {
					menu.add(editFileAction);
				}
			}
		}

		protected void refreshViewer() {
			actionSite.getStructuredViewer().refresh();
		}
		
		protected IServer getServer() {
			Object o = selectedNode;
			if( o instanceof ServerWrapper) return ((ServerWrapper)o).server;
			if( o instanceof XPathCategory) return ((XPathCategory)o).getServer();
			if( o instanceof XPathQuery) return ((XPathQuery)o).getCategory().getServer();
			if( o instanceof XPathFileResult) return ((XPathFileResult)o).getQuery().getCategory().getServer();
			if( o instanceof XPathResultNode) return ((XPathResultNode)o).getFile().getQuery().getCategory().getServer();
			return null;
		}
	}

	
	public static class XMLDecorator extends LabelProvider implements ILightweightLabelDecorator {
		public void decorate(Object element, IDecoration decoration) {
			if( element instanceof XPathQuery) {
				XPathResultNode[] nodes = getResultNodes((XPathQuery)element);
				if(nodes.length == 1 ) {
					decoration.addSuffix("   " + nodes[0].getText());
				} 
				return;
			}

			// we're a file node (blah.xml) 
			if( element instanceof XPathFileResult ) {
				XPathResultNode[] nodes = ((XPathFileResult)element).getChildren();
				if( nodes.length == 1 )
					decoration.addSuffix("   " + nodes[0].getText());
			}
		}
	}
	
	public static XPathResultNode[] getResultNodes(XPathQuery query) {
		ArrayList<XPathResultNode> l = new ArrayList<XPathResultNode>();
		XPathFileResult[] files = query.getResults();
		for( int i = 0; i < files.length; i++ ) {
			l.addAll(Arrays.asList(files[i].getChildren()));
		}
		return l.toArray(new XPathResultNode[l.size()]);
	}

}
