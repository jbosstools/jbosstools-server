package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author rob Stryker (rob.stryker@redhat.com)
 *
 */
public class PollerSection extends ServerEditorSection {
	private Combo startPollerCombo, stopPollerCombo;
	private Composite pollers;
	private String[] startupTypesStrings, shutdownTypesStrings;
	private ServerStatePollerType[] startupTypes, shutdownTypes;
	private ModifyListener startPollerListener;
	private ModifyListener stopPollerListener;
	protected ServerAttributeHelper helper; 
	
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		helper = new ServerAttributeHelper(server.getOriginal(), server);
	}
	
	public void createSection(Composite parent) {
		super.createSection(parent);
		findPossiblePollers();
		createUI(parent);
		addListeners();
	}
	
	protected void createUI(Composite parent) {
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED|ExpandableComposite.TITLE_BAR);
		section.setText(Messages.PollerSection_ServerPollingSection);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		
		Composite composite = toolkit.createComposite(section);
		composite.setLayout(new FormLayout());
		
		pollers = toolkit.createComposite(composite);
		pollers.setLayout(new GridLayout(2, false));
		
		// create widgets
		Label start, stop;
		start = new Label(pollers, SWT.NONE);
		startPollerCombo = new Combo(pollers, SWT.READ_ONLY);
		stop = new Label(pollers, SWT.NONE);
		stopPollerCombo = new Combo(pollers, SWT.READ_ONLY);

		start.setText(Messages.EditorStartupPollerLabel);
		stop.setText(Messages.EditorShutdownPollerLabel);
		
		// set items
		startPollerCombo.setItems(startupTypesStrings);
		stopPollerCombo.setItems(shutdownTypesStrings);
		
		startPollerCombo.setEnabled(true);
		stopPollerCombo.setEnabled(true);
		String currentStartId = helper.getAttribute(IJBossServerConstants.STARTUP_POLLER_KEY, IJBossServerConstants.DEFAULT_STARTUP_POLLER);
		String currentStopId = helper.getAttribute(IJBossServerConstants.SHUTDOWN_POLLER_KEY, IJBossServerConstants.DEFAULT_SHUTDOWN_POLLER);
		startPollerCombo.select(startPollerCombo.indexOf(ExtensionManager.getDefault().getPollerType(currentStartId).getName()));
		stopPollerCombo.select(stopPollerCombo.indexOf(ExtensionManager.getDefault().getPollerType(currentStopId).getName()));
		
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
	}
	
	protected void findPossiblePollers() {
		startupTypes = ExtensionManager.getDefault().getStartupPollers();
		shutdownTypes = ExtensionManager.getDefault().getShutdownPollers();
		startupTypesStrings = new String[startupTypes.length];
		shutdownTypesStrings = new String[shutdownTypes.length];
		
		for( int i = 0; i < startupTypes.length; i++ ) {
			startupTypesStrings[i] = startupTypes[i].getName();
		}
		for( int i = 0; i < shutdownTypes.length; i++ ) {
			shutdownTypesStrings[i] = shutdownTypes[i].getName();
		}
	}
	
	protected void addListeners() {
		startPollerListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetStartupPollerCommand(server));
			}
		};
		stopPollerListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetStopPollerCommand(server));
			}
		};

		startPollerCombo.addModifyListener(startPollerListener);
		stopPollerCombo.addModifyListener(stopPollerListener);
	}

	public class SetStartupPollerCommand extends SetPollerCommand {
		public SetStartupPollerCommand(IServerWorkingCopy server) {
			super(server, Messages.EditorChangeStartPollerCommandName,  IJBossServerConstants.STARTUP_POLLER_KEY, 
					IJBossServerConstants.DEFAULT_STARTUP_POLLER,
					startupTypes, startPollerCombo, startPollerListener);
		}
	}
	
	public class SetStopPollerCommand extends SetPollerCommand {
		public SetStopPollerCommand(IServerWorkingCopy server) {
			super(server, Messages.EditorChangeStopPollerCommandName,  IJBossServerConstants.SHUTDOWN_POLLER_KEY, 
					IJBossServerConstants.DEFAULT_SHUTDOWN_POLLER,
					shutdownTypes, stopPollerCombo, stopPollerListener);
		}
	}
	
	
	public class SetPollerCommand extends ServerCommand {
		private String preChange;
		private String attributeKey;
		private String defaultValue;
		private ServerStatePollerType[] pollerArray;
		private Combo combo;
		private ModifyListener listener;
		public SetPollerCommand(IServerWorkingCopy server, String name, 
				String attributeKey, String defaultValue, ServerStatePollerType[] pollerArray, 
				Combo pollerCombo, ModifyListener listener) {
			super(server, name);
			this.attributeKey = attributeKey;
			this.defaultValue = defaultValue;
			this.pollerArray = pollerArray;
			this.combo = pollerCombo;
			this.listener = listener;
		}
		public void execute() {
			preChange = helper.getAttribute(attributeKey, defaultValue);
			helper.setAttribute(attributeKey, pollerArray[combo.getSelectionIndex()].getId());
		}
		
		public void undo() {
			helper.setAttribute(attributeKey, preChange);
			combo.removeModifyListener(listener);
			int ind = findIndex(preChange);
			if( ind == -1 )
				combo.clearSelection();
			else
				combo.select(ind);
			combo.addModifyListener(listener);
		}

		protected int findIndex(String id) {
			for( int i = 0; i < pollerArray.length; i++)
				if( pollerArray[i].getId().equals(id))
					return i;
			return -1;
		}
	}

}
