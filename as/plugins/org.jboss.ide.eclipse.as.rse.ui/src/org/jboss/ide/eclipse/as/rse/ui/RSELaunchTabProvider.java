package org.jboss.ide.eclipse.as.rse.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.as.rse.core.RSELaunchDelegate;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.launch.JBossLaunchConfigurationTabGroup.IJBossLaunchTabProvider;

public class RSELaunchTabProvider implements IJBossLaunchTabProvider {

	public ILaunchConfigurationTab[] createTabs() {
		return new ILaunchConfigurationTab[]{
				new RSERemoteLaunchTab()
		};
	}

	
	public static class RSERemoteLaunchTab extends AbstractLaunchConfigurationTab {
		
		private Text startText;
		private Text stopText;
		public void createControl(Composite parent) {
			createUI(parent);
			addListeners();
		}
		
		public void createUI(Composite parent) {
			Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
			setControl(comp);
			comp.setLayout(new FormLayout());
			Group startGroup = new Group(comp, SWT.NONE);
			startGroup.setText("Start Command");
			FormData data = UIUtil.createFormData2(0, 5, 0, 75, 0, 5, 100, -5);
			startGroup.setLayoutData(data);
			startGroup.setLayout(new FormLayout());
			startText = new Text(startGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			data = UIUtil.createFormData2(0, 5, 100, -5, 0, 5, 100, -5);
			startText.setLayoutData(data);

			Group stopGroup = new Group(comp, SWT.NONE);
			stopGroup.setText("Stop Command");
			data = UIUtil.createFormData2(startGroup, 5, startGroup, 150, 0, 5, 100, -5);
			stopGroup.setLayoutData(data);
			stopGroup.setLayout(new FormLayout());
			stopText = new Text(stopGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP);
			data = UIUtil.createFormData2(0, 5, 100, -5, 0, 5, 100, -5);
			stopText.setLayoutData(data);
		}
		
		protected void addListeners() {
		}

		public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		}
		
		public void initializeFrom(ILaunchConfiguration configuration) {
			try {
				String startCommand = configuration.getAttribute(RSELaunchDelegate.RSE_STARTUP_COMMAND, "");
				startText.setText(startCommand);
				
				String stopCommand = configuration.getAttribute(RSELaunchDelegate.RSE_SHUTDOWN_COMMAND, "");
				stopText.setText(stopCommand);
			} catch( CoreException ce) {
				// TODO
			}
		}
		public void performApply(ILaunchConfigurationWorkingCopy configuration) {
			configuration.setAttribute(RSELaunchDelegate.RSE_STARTUP_COMMAND, startText.getText());
			configuration.setAttribute(RSELaunchDelegate.RSE_SHUTDOWN_COMMAND, stopText.getText());
		}
		public String getName() {
			return "RSE Remote Launch";
		}
		
	}
}
