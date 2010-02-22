package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IServer;
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
import org.jboss.ide.eclipse.as.ui.views.server.extensions.XPathTreeContentProvider.ServerWrapper;

public class XPathActionProvider extends CommonActionProvider {

	private ICommonActionExtensionSite actionSite;
	public Action newXPathCategoryAction, deleteXPathCategoryAction,
			newXPathAction, editXPathAction, deleteXPathAction, editFileAction;
	private XPathChangeValueAction xpathChangeValueAction;
	private CommonViewer cv;

	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		this.actionSite = aSite;
		ICommonViewerSite site = aSite.getViewSite();
		if( site instanceof ICommonViewerWorkbenchSite ) {
			StructuredViewer v = aSite.getStructuredViewer();
			if( v instanceof CommonViewer ) {
				cv = (CommonViewer)v;
				ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite)site;
				createActions(cv, wsSite.getSelectionProvider());
				addDoubleClickHandler(cv);
			}
		}
	}
	public void dispose() {
		super.dispose();
		removeDoubleClickHandler();
	}
	
	private IDoubleClickListener doubleClickListener;
	protected void addDoubleClickHandler(CommonViewer cv) {
		doubleClickListener = new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object o = getSelection();
				if((o instanceof XPathResultNode) || (o instanceof XPathFileResult) || (o instanceof XPathQuery))
					editFileAction.run();
			}
		};
		cv.addDoubleClickListener(doubleClickListener);
	}
	
	protected void removeDoubleClickHandler() {
		cv.removeDoubleClickListener(doubleClickListener);
	}

	public void createActions(CommonViewer tableViewer, ISelectionProvider provider) {
		Shell shell = tableViewer.getTree().getShell();
		newXPathCategoryAction = new Action() {
			public void run() {
				XPathCategoryDialog d = new XPathCategoryDialog(Display
						.getCurrent().getActiveShell(), getServer());
				if (d.open() == Window.OK) {
					XPathModel.getDefault().addCategory(getServer(),
							d.getText());
					XPathModel.getDefault().save(getServer());
					refreshViewer();
				}
			}
		};
		newXPathCategoryAction.setText(Messages.XPathActionProvider_NewCategoryAction);

		deleteXPathCategoryAction = new Action() {
			public void run() {
				int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO;
				MessageBox messageBox = new MessageBox(Display.getCurrent()
						.getActiveShell(), style);
				messageBox
						.setText(Messages.XPathActionProvider_RemoveCategoryQuestion);
				messageBox
						.setMessage(Messages.DescriptorXPathRemoveCategoryDesc);
				if (messageBox.open() == SWT.YES) {
					XPathModel.getDefault().removeCategory(getServer(),
							((XPathCategory) getSelection()).getName());
					XPathModel.getDefault().save(getServer());
					refreshViewer();
				}
			}
		};
		deleteXPathCategoryAction
				.setText(Messages.DescriptorXPathRemoveCategory);

		newXPathAction = new Action() {
			public void run() {
				XPathCategory category = null;
				Object selectedNode = getSelection();
				if(selectedNode instanceof XPathCategory) {
					category = (XPathCategory) selectedNode;
				} else if( selectedNode instanceof XPathQuery) {
					category = ((XPathQuery)selectedNode).getCategory();
				}
				
				if (category != null) {
					String categoryName = category.getName();
					XPathDialog d = new XPathDialog(Display.getCurrent()
							.getActiveShell(), getServer(), null);
					if (d.open() == Window.OK) {
						XPathCategory[] categoryList = XPathModel.getDefault()
								.getCategories(getServer());
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
				Object o = getSelection();
				if (o != null && o instanceof XPathQuery) {
					XPathQuery original = (XPathQuery) o;
					XPathCategory category = original.getCategory();

					XPathDialog d = new XPathDialog(Display.getCurrent()
							.getActiveShell(), getServer(), original);

					if (d.open() == Window.OK) {
						original.setAttribute(d.getAttribute());
						original.setXpathPattern(d.getXpath());
						original.setName(d.getName());
						original.setBaseDir(d.getBaseDir());
						original.setFilePattern(d.getFilePattern());
						category.save();
						refreshViewer();
					}
				}
			}
		};
		editXPathAction.setText(Messages.DescriptorXPathEditXPath);

		deleteXPathAction = new Action() {
			public void run() {
				Object o = getSelection();
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
					Object o = getSelection();
					Path p = null;
					if (o instanceof XPathQuery
							&& ((XPathQuery) o).getResults().length == 1) {
						o = (XPathFileResult) ((XPathQuery) o).getResults()[0];
					}
					if (o instanceof XPathFileResult) {
						p = new Path(((XPathFileResult) o).getFileLocation());
					} else if (o instanceof XPathResultNode) {
						p = new Path(((XPathResultNode) o).getFileLocation());
					}
					if (p != null) {

						IFileStore fileStore = EFS.getLocalFileSystem()
								.getStore(p.removeLastSegments(1));
						fileStore = fileStore.getChild(p.lastSegment());
						IWorkbench wb = PlatformUI.getWorkbench();
						IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
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
					JBossServerUIPlugin.log(Messages.XPathActionProvider_ErrorRunningAction,
							exc);
				}
			}
		};
		editFileAction.setText(Messages.XPathActionProvider_EditFileAction);
		
		xpathChangeValueAction = new XPathChangeValueAction(shell, tableViewer, provider);
	}

	
	protected Object getSelection() {
		ICommonViewerSite site = actionSite.getViewSite();
		IStructuredSelection selection = null;
		if (site instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
			selection = (IStructuredSelection) wsSite.getSelectionProvider()
					.getSelection();
			Object first = selection.getFirstElement();
			return first;
		}
		return null;
	}
	
	public void fillContextMenu(IMenuManager menu) {
		Object first = getSelection();
		if (first == null)
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

		if (first instanceof XPathQuery) {
			menu.add(newXPathAction);
			menu.add(editXPathAction);
			menu.add(deleteXPathAction);
		}
		
		if( xpathChangeValueAction.shouldRun())
			menu.add(xpathChangeValueAction);
		
		if ((first instanceof XPathResultNode || first instanceof XPathFileResult)
				|| (first instanceof XPathQuery && ((XPathQuery) first)
						.getResults().length == 1)) {
			menu.add(editFileAction);
		}
	}

	protected void refreshViewer() {
		actionSite.getStructuredViewer().refresh();
	}

	protected IServer getServer() {
		Object o = getSelection();
		if (o instanceof ServerWrapper)
			return ((ServerWrapper) o).server;
		if (o instanceof XPathCategory)
			return ((XPathCategory) o).getServer();
		if (o instanceof XPathQuery)
			return ((XPathQuery) o).getCategory().getServer();
		if (o instanceof XPathFileResult)
			return ((XPathFileResult) o).getQuery().getCategory().getServer();
		if (o instanceof XPathResultNode)
			return ((XPathResultNode) o).getFile().getQuery().getCategory()
					.getServer();
		return null;
	}

}