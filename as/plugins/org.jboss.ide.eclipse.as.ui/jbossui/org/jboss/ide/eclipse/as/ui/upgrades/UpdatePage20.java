/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui.upgrades;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;

/*
 * This class is non-functional right now. I'm leaving it in case I get
 * time to fix it, but with all the changes to the server, this isn't
 * working. 
 */
public class UpdatePage20 { //extends AbstractFirstRunPage {

	protected UpdatePage20(String pageName, String title,
			ImageDescriptor titleImage) {
//		super(pageName, title, titleImage);
		// TODO Auto-generated constructor stub
	}

	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		
	}

	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	public void performFinishWithProgress(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

//	private ASLaunchConfigurationConverter converter;
//	private TempLaunchConfiguration[] configs;
//	
//	private ArrayList rows;
//	private boolean pageComplete;
//	private String errorMessage;
//	
//	private Label descLabel;
//	
//	private ConfigDataComposite configDataComposite;
//	
//	
//	public UpdatePage20() {
//		super("", Messages.UpgradeWizardTitle, null);
//		rows = new ArrayList();
//	}
//
//	public void createControl(Composite parent) {
//		Composite main = new Composite(parent, SWT.NONE);
//		main.setLayout(new FormLayout());
//		
//		Composite main2 = new Composite(main, SWT.NONE);
//		FormData main2Data = new FormData();
//		main2Data.left = new FormAttachment(0,5);
//		main2Data.right = new FormAttachment(100,-5);
//		main2Data.top = new FormAttachment(0,5);
//		main2Data.bottom = new FormAttachment(100,-5);
//		main2.setLayoutData(main2Data);
//		
//		main2.setLayout(new FormLayout());
//		
//		descLabel = new Label(main2, SWT.NONE);
//		FormData descLabelData = new FormData();
//		descLabelData.left = new FormAttachment(0,5);
//		descLabelData.right = new FormAttachment(100,-5);
//		descLabelData.top = new FormAttachment(0,5);
//		descLabel.setLayoutData(descLabelData);
//		
//		descLabel.setText(Messages.UpgradeWizardDescription);
//		
//		if( configs.length != 0 ) {
//			createFullControl(main2);
//		} else {
//			createEmptyControl(main2);
//		}
//		setControl(main);
//	}
//	
//	protected void createEmptyControl(Composite main2) {
//		Label noConfigsLabel = new Label(main2, SWT.NONE);
//		FormData noConfigsData = new FormData();
//		noConfigsData.left = new FormAttachment(0,5);
//		noConfigsData.right = new FormAttachment(100,-5);
//		noConfigsData.top = new FormAttachment(descLabel,8);
//		noConfigsLabel.setLayoutData(noConfigsData);
//		
//		noConfigsLabel.setText(Messages.UpgradeWizardNoConvertableConfigs);
//		
//		pageComplete = true;
//	}
//	
//	protected void createFullControl(Composite main2) {
//		ScrolledComposite sc = new ScrolledComposite(main2, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
//		FormData scData = new FormData();
//		scData.left = new FormAttachment(0,5);
//		scData.right = new FormAttachment(100,-5);
//		scData.top = new FormAttachment(descLabel,5);
//		scData.bottom = new FormAttachment(50,-2);
//		sc.setLayoutData(scData);
//		
//		fillScrolledComposite(sc);
//		
//		
//		configDataComposite = new ConfigDataComposite(main2, SWT.NONE);
//		FormData cdcData = new FormData();
//		cdcData.left = new FormAttachment(0,5);
//		cdcData.right = new FormAttachment(100,-5);
//		cdcData.top = new FormAttachment(sc,5);
//		cdcData.bottom = new FormAttachment(100,-5);
//		configDataComposite.setLayoutData(cdcData);
//		checkPageComplete();
//	}
//	
//	protected class ConfigDataComposite extends Composite {
//		private TempLaunchConfiguration currentConfig;
//		
//		private Label launchConfigName, homeDir, jbConfig, startArgs, stopArgs, jre;
//		
//		public ConfigDataComposite(Composite parent, int style) {
//			super(parent, style);
//			
//			setLayout(new FormLayout());
//			
//			
//			Label detailLabel = new Label(this, SWT.NONE);
//			FormData dld = new FormData();
//			dld.left = new FormAttachment(0,5);
//			dld.top = new FormAttachment(0,5);
//			detailLabel.setLayoutData(dld);
//			detailLabel.setText(Messages.UpgradeWizardDetails);
//			
//			Composite detailComposite = new Composite(this, SWT.NONE);
//			detailComposite.setLayout(new GridLayout(2, false));
//			FormData detailCompositeData = new FormData();
//			detailCompositeData.left = new FormAttachment(0,20);
//			detailCompositeData.top = new FormAttachment(detailLabel, 5);
//			detailComposite.setLayoutData(detailCompositeData);
//			
//			
//			
//			new Label(detailComposite, SWT.NONE).setText(Messages.UpgradeWizardLaunchConfigName);
//			launchConfigName = new Label(detailComposite, SWT.NONE);
//			
//			new Label(detailComposite, SWT.NONE).setText(Messages.UpgradeWizardLaunchHomeDir);
//			homeDir = new Label(detailComposite, SWT.NONE);
//			
//			new Label(detailComposite, SWT.NONE).setText(Messages.UpgradeWizardLaunchJBossConfig);
//			jbConfig = new Label(detailComposite, SWT.NONE);
//			
//			new Label(detailComposite, SWT.NONE).setText(Messages.UpgradeWizardLaunchStartArgs);
//			startArgs = new Label(detailComposite, SWT.NONE);
//			
//			new Label(detailComposite, SWT.NONE).setText(Messages.UpgradeWizardLaunchShutdownArgs);
//			stopArgs = new Label(detailComposite, SWT.NONE);
//			
//			new Label(detailComposite, SWT.NONE).setText(Messages.UpgradeWizardLaunchJRE);
//			jre = new Label(detailComposite, SWT.NONE);
//		}
//		
//		public void setInput(TempLaunchConfiguration config) {
//			if( config != currentConfig) {
//				currentConfig = config;
//				try {
//					launchConfigName.setText(config.getName());
//					homeDir.setText(config.getAttribute("org.jboss.rocklet.HomeDir", ""));
//					jbConfig.setText(config.getAttribute("org.jboss.rocklet.ServerConfiguration", ""));
//					startArgs.setText(config.getAttribute("org.rocklet.launcher.userProgramArgs", ""));
//					stopArgs.setText(config.getAttribute("org.rocklet.launcher.userShutdownProgramArgs", ""));
//
//					IVMInstall vm = config.getJVMItem();
//					String vmName = vm == null ? "" : vm.getName();
//					jre.setText(vmName);
//					layout();
//				} catch( CoreException ce ) {
//					ce.printStackTrace();
//				}
//			}
//		}
//	}
//	
//	protected void fillScrolledComposite(ScrolledComposite sc) {
//	    
//		Composite main = new Composite(sc, SWT.NONE);
//		main.setLayout(new GridLayout(4, false));
//		sc.setContent(main);
//		
//		
//
//		// add headings
//		Label convertLabel = new Label(main, SWT.NONE);
//		convertLabel.setText(Messages.UpgradeWizardLaunchConvert);
//		Label configName = new Label(main, SWT.NONE);
//		configName.setText(Messages.UpgradeWizardLaunchConfigName2);
//		Label runtimeLabel = new Label(main, SWT.NONE);
//		runtimeLabel.setText(Messages.UpgradeWizardLaunchRuntimeName);
//		Label serverLabel = new Label(main, SWT.NONE);
//		serverLabel.setText(Messages.UpgradeWizardLaunchServerName);
//		
//		for( int i = 0; i < configs.length; i++ ) {
//			// should add the 4 widgets itself
//			ConfigurationRow row = new ConfigurationRow(configs[i], main);
//			rows.add(row);
//		}
//
//		
//		// force a layout
//		main.pack();
//		
//        int locY = main.getLocation().y;
//        int locX = main.getLocation().x;
//        int sY = main.getSize().y;
//        int sX = main.getSize().x;
//
//		
//	    sc.setExpandHorizontal(true);
//	    sc.setExpandVertical(true);
//	    sc.setMinHeight(locY + sY);
//	    sc.setMinWidth(locX + sX);
//	    
//		sc.setSize(300,150);
//	}
//	
//
//	public void initialize() {
//		converter = new ASLaunchConfigurationConverter();
//		configs = converter.getConvertableConfigurations();
//	}
//
//	public void performFinishWithProgress(IProgressMonitor monitor) {
//		monitor.beginTask(Messages.UpgradeWizardLaunchMonitorMainTask, rows.size() * 100);
//		monitor.setTaskName(Messages.UpgradeWizardLaunchMonitorMainTask);
//		Iterator i = rows.iterator();
//		while(i.hasNext()) {
//			ConfigurationRow r = (ConfigurationRow)i.next();
//			if( r.getSelected() ) {
//				// now convert
//				monitor.subTask(Messages.UpgradeWizardLaunchMonitorConverting + r.getLaunchConfig().getName());
//				try {
//					converter.convertConfiguration(r.getLaunchConfig(), 
//							r.getRuntimeName(), r.getServerName());
//				} catch( CoreException ce ) {
//					ce.printStackTrace();
//				}
//			}
//		}
//		monitor.done();
//	}
//	
//    public boolean isPageComplete() {
//    	return pageComplete;
//    }
//
//    protected void checkPageComplete() {
//    	pageComplete = true; // start it off true. If it remains true, great
//    	HashMap map = new HashMap();
//    	
//    	
//    	
//    	String server_prefix = "__SP__";
//    	String rt_prefix = "__RT__";
//    	
//    	IRuntime[] rts = ServerCore.getRuntimes();
//    	IServer[] servers = ServerCore.getServers();
//    	
//    	for( int i = 0; i < rts.length; i++ ) {
//    		map.put(rt_prefix + rts[i].getName(), "");
//    	}
//    	for( int i = 0; i < servers.length; i++ ) {
//    		map.put(server_prefix + servers[i].getName(), "");
//    	}
//    	
//    	ArrayList runtimeCollisions = new ArrayList();
//    	ArrayList serverCollisions = new ArrayList();
//    	
//    	Iterator i = rows.iterator();
//    	while(i.hasNext()) {
//    		ConfigurationRow r = (ConfigurationRow)i.next();
//    		if( r.getSelected()) {
//    			if( !map.containsKey(rt_prefix + r.getRuntimeName())
//    					&& !map.containsKey(server_prefix + r.getServerName())) {
//    				map.put(rt_prefix + r.getRuntimeName(), "");
//    				map.put(server_prefix + r.getServerName(), "");
//	    		} else {
//	    			if( map.containsKey(rt_prefix + r.getRuntimeName()))
//	    				runtimeCollisions.add(r.getRuntimeName());
//	    			if( map.containsKey(server_prefix + r.getServerName()))
//	    				serverCollisions.add(r.getServerName());
//	    				
//	    			pageComplete = false;
//	    		}
//    		}
//    	}
//    	
//    	if( runtimeCollisions.size() == 0 && serverCollisions.size() == 0 ) {
//    		errorMessage = null;
//    	} else {
//    		errorMessage = "";
//    		String[] sc = (String[]) serverCollisions.toArray(new String[serverCollisions.size()]);
//    		String[] rtc = (String[]) runtimeCollisions.toArray(new String[runtimeCollisions.size()]);
//    		if( rtc.length > 0 ) 
//    			errorMessage += Messages.UpgradeWizardLaunchRuntimeNamesInUse + implode(rtc, ", ") + "\n";
//    		if( sc.length > 0 ) 
//    			errorMessage += Messages.UpgradeWizardLaunchServerNamesInUse + implode(sc, ", ");
//    	}
//    	setErrorMessage(errorMessage);
//    }
//    
//    protected String implode(String[] array, String separator) {
//    	if( array.length == 0 ) return "";
//    	String ret = "";
//    	for( int i = 0; i < array.length; i++ ) {
//    		ret += array[i] + separator;
//    	}
//    	return ret.substring(0, ret.length() - separator.length());
//    }
//    
//	protected class ConfigurationRow {
//		private TempLaunchConfiguration config;
//		
//		private Button shouldConvert;
//		private Label nameLabel;
//		private Text runtimeText;
//		private Text serverText;
//		
//		public ConfigurationRow(TempLaunchConfiguration config, Composite parent) {
//			this.config = config;
//
//			shouldConvert = new Button(parent, SWT.CHECK);
//			shouldConvert.setSelection(true);
//			GridData shouldConvertData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
//			shouldConvert.setLayoutData(shouldConvertData);
//			
//			nameLabel = new Label(parent, SWT.NONE);
//			nameLabel.setText(config.getName());
//			
//			runtimeText = new Text(parent, SWT.BORDER);
//			runtimeText.setText(config.getName() + " Runtime");
//			
//			GridData rtgd = new GridData(GridData.FILL_HORIZONTAL);
//			runtimeText.setLayoutData(rtgd);
//			
//			serverText = new Text(parent, SWT.BORDER);
//			serverText.setText(config.getName());
//			GridData stgd = new GridData(GridData.FILL_HORIZONTAL);
//			serverText.setLayoutData(stgd);
//			
//			
//			
//			// listeners
//			SelectionListener listener = new SelectionListener() { 
//				public void widgetDefaultSelected(SelectionEvent e) {
//				}
//
//				public void widgetSelected(SelectionEvent e) {
//					checkPageComplete();
//					getContainer().updateButtons();
//				} 
//			};
//			ModifyListener modListener = new ModifyListener() {
//				public void modifyText(ModifyEvent e) {
//					checkPageComplete();
//					getContainer().updateButtons();
//				} 
//			};
//			shouldConvert.addSelectionListener(listener);
//			runtimeText.addModifyListener(modListener);
//			serverText.addModifyListener(modListener);
//			
//			final TempLaunchConfiguration config2 = config;
//			Listener mmListener = new Listener() {
//				public void handleEvent(Event event) {
//					if( event.type == SWT.MouseMove) {
//						configDataComposite.setInput(config2);
//					}
//				}
//			};
//			shouldConvert.addListener(SWT.MouseMove, mmListener);
//			runtimeText.addListener(SWT.MouseMove, mmListener);
//			serverText.addListener(SWT.MouseMove, mmListener);
//			nameLabel.addListener(SWT.MouseMove, mmListener);
//		}
//		
//		public boolean getSelected() {
//			return shouldConvert.getSelection();
//		}
//		public String getName() {
//			return nameLabel.getText();
//		}
//		public String getRuntimeName() {
//			return runtimeText.getText();
//		}
//		public String getServerName() {
//			return serverText.getText();
//		}
//		public TempLaunchConfiguration getLaunchConfig() {
//			return config;
//		}
//	}
}
