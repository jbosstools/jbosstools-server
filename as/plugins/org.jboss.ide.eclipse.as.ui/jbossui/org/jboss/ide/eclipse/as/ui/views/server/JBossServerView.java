package org.jboss.ide.eclipse.as.ui.views.server;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;

public class JBossServerView extends ViewPart {

	private static final String TAG_SASHFORM_HEIGHT = "sashformHeight"; 
	public static JBossServerView instance;
	public static JBossServerView getDefault() {
		return instance;
	}
	
	public static interface IServerViewFrame {
		public IAction[] getActionBarActions();
		public int getDefaultSize();
		public void refresh();
	}
	
	public JBossServerView() {
		super();
		instance = this;		
	}

	
	private SashForm form;
	private int[] sashRows;
	private IMemento memento;
	
	private IServerViewFrame[] frames;
	private ServerFrame serverFrame;
	private ServerExtensionFrame extensionFrame;
	
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		ServerUIPlugin.getPreferences().setShowOnActivity(false);
		this.memento = memento;
		int sum = 0;
		sashRows = new int[2];
		for (int i = 0; i < sashRows.length; i++) {
			sashRows[i] = 50;
			if (memento != null) {
				Integer in = memento.getInteger(TAG_SASHFORM_HEIGHT + i);
				if (in != null && in.intValue() > 5)
					sashRows[i] = in.intValue();
			}
			sum += sashRows[i];
		}
	}

	public void saveState(IMemento memento) {
		super.saveState(memento);
		int[] weights = form.getWeights();
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] != 0)
				memento.putInteger(TAG_SASHFORM_HEIGHT + i, weights[i]);
		}
	}

	public void createPartControl(Composite parent) {
		form = new SashForm(parent, SWT.VERTICAL);
		form.setBackground(new Color(parent.getDisplay(), 255, 255, 255));
		form.setLayout(new FillLayout());

		
		Composite topWrapper = new Composite(form, SWT.NONE);
		topWrapper.setLayout(new FillLayout());
		serverFrame = new ServerFrame(topWrapper, this);

		Composite bottomWrapper = new Composite(form, SWT.NONE);
		bottomWrapper.setLayout(new FillLayout());
		extensionFrame = new ServerExtensionFrame(bottomWrapper, this);
		
		
		frames = new IServerViewFrame[] { serverFrame, extensionFrame };

		// add toolbar buttons
		IContributionManager cm = getViewSite().getActionBars().getToolBarManager();
		for( int i = 0; i < frames.length; i++ ) {
			IAction[] actions = frames[i].getActionBarActions();
			for (int j = 0; j < actions.length - 1; j++)
				cm.add(actions[j]);
		}
		
		form.setWeights(sashRows);
	}
	
	public void refreshAll() {
		for( int i = 0; i < frames.length; i++ ) {
			frames[i].refresh();
		}
	}
	
	public ServerFrame getServerFrame() { return this.serverFrame; }
	public ServerExtensionFrame getExtensionFrame() { return this.extensionFrame; }
	
	public void setFocus() {
	}
	
	public IServer getSelectedServer() {
		return serverFrame == null ? null : serverFrame.getSelectedServer();
	}

	public Object getAdapter(Class adaptor) {
		if( adaptor == IPropertySheetPage.class) {
			return extensionFrame.getViewer().getPropertySheet();
		}
		return super.getAdapter(adaptor);
	}
}
