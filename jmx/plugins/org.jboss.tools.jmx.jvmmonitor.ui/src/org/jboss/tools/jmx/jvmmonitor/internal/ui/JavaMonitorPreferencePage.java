/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.jvmmonitor.core.IHost;
import org.jboss.tools.jmx.jvmmonitor.core.JvmModel;
import org.jboss.tools.jmx.jvmmonitor.ui.Activator;
import org.osgi.service.prefs.BackingStoreException;


/**
 * The preference page at preference dialog: Java > Monitor.
 */
public class JavaMonitorPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    /** The minimum value of update period. */
    private static final int MIN_UPDATE_PERIOD = 100;

    /** The update period text field. */
    private Text updatePeriodText;

    /** The legend visibility check box. */
    private Button legendVisibilityButton;

    /** The check box to take stack traces into account when filtering threads. */
    private Button wideScopeThreadFilterButton;

    /**
     * The check box to take stack traces into account when filtering SWT
     * resources.
     */
    private Button wideScopeSWTResourcesFilterButton;
    
    /** The text field to specify update period in milliseconds. */
    private Text updatePeriodTextTools;

    /** The max number of classes for heap. */
    private Text maxNumberOfClassesText;

    
    /*
     * @see PreferencePage#createContents(Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));

        Composite formComposite = new Composite(composite, SWT.NONE);
        formComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
        formComposite.setLayout(new FormLayout());
        
    	Control mon = createContentsMonitor(formComposite);
    	Control tools = createContentsTools(formComposite);
    	
    	FormData fd = new FormData();
    	fd.top = new FormAttachment(0,5); 
    	fd.left = new FormAttachment(0,5); 
    	fd.right = new FormAttachment(100,-5);
    	mon.setLayoutData(fd);
    	
    	fd = new FormData();
    	fd.top = new FormAttachment(mon,20); 
    	fd.left = new FormAttachment(0,5); 
    	fd.right = new FormAttachment(100,-5);
    	tools.setLayoutData(fd);
    	
    	return composite;
    }
    protected Control createContentsMonitor(Composite parent) {
        Group composite = new Group(parent, SWT.NONE);
        composite.setText("Properties View");
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        composite.setLayout(layout);

        createUpdatePeriodText(composite);
        createTimelineGroup(composite);
        createThreadsGroup(composite);
        createMemoryGroup(composite);

        applyDialogFont(composite);

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(parent, IHelpContextIds.JAVA_MONITOR_PREFERENCE_PAGE);

        return composite;
    }

    /*
     * @see IWorkbenchPreferencePage#init(IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    /*
     * @see PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
    	return performOkMonitor() && performOkTools();
    }
    public boolean performOkMonitor() {
        getPreferenceStore().setValue(IConstants.UPDATE_PERIOD,
                updatePeriodText.getText());
        getPreferenceStore().setValue(IConstants.LEGEND_VISIBILITY,
                legendVisibilityButton.getSelection());
        getPreferenceStore().setValue(IConstants.WIDE_SCOPE_THREAD_FILTER,
                wideScopeThreadFilterButton.getSelection());
        getPreferenceStore().setValue(
                IConstants.WIDE_SCOPE_SWT_RESOURCE_FILTER,
                wideScopeSWTResourcesFilterButton.getSelection());

        applyChanges();
        return true;
    }

    /**
     * Applies the changes.
     */
    private void applyChanges() {
        Integer period = Integer.valueOf(updatePeriodText.getText());
        for (IHost host : JvmModel.getInstance().getHosts()) {
            for (IActiveJvm jvm : host.getActiveJvms()) {
            	if( jvm.getMBeanServer() != null ) {
            		jvm.getMBeanServer().setUpdatePeriod(period);
            	} else {
            		// This should never happen
            		Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Active JVM has no MBean Server declared: " + jvm.getHost().getName() + ":" + jvm.getPort()));
            	}
            }
        }
    }

    /*
     * @see PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
    	performDefaultsMonitor();
    	performDefaultsTools();
    }
    
    protected void performDefaultsMonitor() {
        String updatePeriod = String.valueOf(getPreferenceStore()
                .getDefaultInt(IConstants.UPDATE_PERIOD));
        updatePeriodText.setText(updatePeriod);
        legendVisibilityButton.setSelection(getPreferenceStore()
                .getDefaultBoolean(IConstants.LEGEND_VISIBILITY));
        wideScopeThreadFilterButton.setSelection(getPreferenceStore()
                .getDefaultBoolean(IConstants.WIDE_SCOPE_THREAD_FILTER));
        wideScopeSWTResourcesFilterButton.setSelection(getPreferenceStore()
                .getDefaultBoolean(IConstants.WIDE_SCOPE_SWT_RESOURCE_FILTER));
        super.performDefaults();
    }

    /**
     * Creates the update period text field.
     * 
     * @param parent
     *            The parent composite
     */
    private void createUpdatePeriodText(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.updatePeriodLabel);

        updatePeriodText = new Text(composite, SWT.BORDER);
        updatePeriodText.setText(String.valueOf(getPreferenceStore().getInt(
                IConstants.UPDATE_PERIOD)));
        updatePeriodText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        updatePeriodText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateUpdatePeriod();
            }
        });
        updatePeriodText.addVerifyListener(createIntegerVerifier());
    }
    
    private VerifyListener createIntegerVerifier() {
    	return new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                Text text = (Text)e.getSource();
                // get old text and create new text by using the VerifyEvent.text
                final String oldS = text.getText();
                String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
                boolean isInt = true;
                try {
                    Integer.parseInt(newS);
                } catch(NumberFormatException ex) {
                    isInt = false;
                }
                if(!isInt)
                    e.doit = false;
            }
        };
    }
    

    /**
     * Creates the timeline group.
     * 
     * @param parent
     *            The parent composite
     */
    private void createTimelineGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.timelineGroupLabel);
        GridLayout layout = new GridLayout(1, false);
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        legendVisibilityButton = new Button(group, SWT.CHECK);
        legendVisibilityButton.setText(Messages.showLegendLabel);
        legendVisibilityButton.setSelection(getPreferenceStore().getBoolean(
                IConstants.LEGEND_VISIBILITY));

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        legendVisibilityButton.setLayoutData(gridData);
    }

    /**
     * Creates the threads group.
     * 
     * @param parent
     *            The parent composite
     */
    private void createThreadsGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.threadsGroupLabel);
        GridLayout layout = new GridLayout(1, false);
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        wideScopeThreadFilterButton = new Button(group, SWT.CHECK);
        wideScopeThreadFilterButton
                .setText(Messages.wideScopeThreadFilterLabel);
        wideScopeThreadFilterButton.setSelection(getPreferenceStore()
                .getBoolean(IConstants.WIDE_SCOPE_THREAD_FILTER));

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        wideScopeThreadFilterButton.setLayoutData(gridData);
    }

    /**
     * Creates the memory group.
     * 
     * @param parent
     *            The parent composite
     */
    private void createMemoryGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.memoryGroupLabel);
        GridLayout layout = new GridLayout(1, false);
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        wideScopeSWTResourcesFilterButton = new Button(group, SWT.CHECK);
        wideScopeSWTResourcesFilterButton
                .setText(Messages.wideScopeSWTResourceFilterLabel);
        wideScopeSWTResourcesFilterButton.setSelection(getPreferenceStore()
                .getBoolean(IConstants.WIDE_SCOPE_SWT_RESOURCE_FILTER));

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        wideScopeSWTResourcesFilterButton.setLayoutData(gridData);
    }

    /**
     * Validates the update period.
     */
    void validateUpdatePeriod() {

        // check if text is empty
        String period = updatePeriodText.getText();
        if (period.isEmpty()) {
            setMessage(Messages.updatePeriodNotEnteredMsg,
                    IMessageProvider.WARNING);
            return;
        }

        // check if text is integer
        try {
            Integer.parseInt(period);
        } catch (NumberFormatException e) {
            setMessage(Messages.illegalUpdatePeriodMsg, IMessageProvider.ERROR);
            return;
        }

        // check if the value is within valid range
        if (Integer.valueOf(period) < MIN_UPDATE_PERIOD) {
            setMessage(Messages.updatePeriodOutOfRangeMsg,
                    IMessageProvider.ERROR);
            return;
        }

        setMessage(null);
    }
    
    
    protected Control createContentsTools(Composite parent) {
        Group composite = new Group(parent, SWT.NONE);
        composite.setText("Tools.jar");
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        composite.setLayout(layout);

        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.widthHint = 300;
        
        createupdatePeriodTextToolsField(composite);
        createMemoryGroupTools(composite);

        applyDialogFont(composite);
        
        return composite;
    }

    public boolean performOkTools() {
    	IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
    	prefs.putLong(org.jboss.tools.jmx.jvmmonitor.internal.tools.IConstants.UPDATE_PERIOD, Long.valueOf(updatePeriodTextTools.getText()));
    	prefs.putInt(org.jboss.tools.jmx.jvmmonitor.internal.tools.IConstants.MAX_CLASSES_NUMBER, Integer.valueOf(maxNumberOfClassesText.getText()));
    	try {
    		prefs.flush();
    	} catch(BackingStoreException bse) {
    		
    	}
        return true;
    }

    /*
     * @see PreferencePage#performDefaults()
     */
    protected void performDefaultsTools() {
    	IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        String updatePeriod = String.valueOf(prefs.get(org.jboss.tools.jmx.jvmmonitor.internal.tools.IConstants.UPDATE_PERIOD, 
        		String.valueOf(org.jboss.tools.jmx.jvmmonitor.internal.tools.IConstants.DEFAULT_UPDATE_PERIOD)));
        String maxClasses = String.valueOf(prefs.get(org.jboss.tools.jmx.jvmmonitor.internal.tools.IConstants.MAX_CLASSES_NUMBER, String.valueOf(org.jboss.tools.jmx.jvmmonitor.internal.tools.IConstants.DEFAULT_MAX_CLASSES_NUMBER)));
        updatePeriodTextTools.setText(updatePeriod);
        maxNumberOfClassesText.setText(maxClasses);

        super.performDefaults();
    }

    /**
     * Creates the update period text field.
     *
     * @param parent
     *            The parent composite
     */
    private void createupdatePeriodTextToolsField(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.autoDetectGroupLabel);

        Label label = new Label(group, SWT.NONE);
        label.setText(Messages.prefPageUpdatePeriodLabel);

        updatePeriodTextTools = new Text(group, SWT.BORDER);
    	IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        String updatePeriod = String.valueOf(prefs.get(org.jboss.tools.jmx.jvmmonitor.internal.tools.IConstants.UPDATE_PERIOD, String.valueOf(org.jboss.tools.jmx.jvmmonitor.internal.tools.IConstants.DEFAULT_UPDATE_PERIOD)));
        updatePeriodTextTools.setText(String.valueOf(updatePeriod));
        updatePeriodTextTools.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        updatePeriodTextTools.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
            	validateUpdatePeriodTools();
            }
        });
        updatePeriodTextTools.addVerifyListener(createIntegerVerifier());
    }

    /**
     * Creates the memory group.
     *
     * @param parent
     *            The parent composite
     */
    private void createMemoryGroupTools(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(Messages.memoryGroupLabel);
        GridLayout layout = new GridLayout(2, false);
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    	IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        String maxClasses = String.valueOf(prefs.get(org.jboss.tools.jmx.jvmmonitor.internal.tools.IConstants.MAX_CLASSES_NUMBER, String.valueOf(org.jboss.tools.jmx.jvmmonitor.internal.tools.IConstants.DEFAULT_MAX_CLASSES_NUMBER)));

        
        Label label = new Label(group, SWT.NONE);
        label.setText(Messages.maxNumberOfClassesLabel);
        maxNumberOfClassesText = new Text(group, SWT.BORDER);
        maxNumberOfClassesText.setText(maxClasses);
        maxNumberOfClassesText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateMaxNumberOfClasses();
            }
        });
        maxNumberOfClassesText.addVerifyListener(createIntegerVerifier());
        maxNumberOfClassesText.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));
    }

    /**
     * Validates the update period.
     */
    void validateUpdatePeriodTools() {

        // check if text is empty
        String period = updatePeriodTextTools.getText();
        if (period.isEmpty()) {
            setMessage(Messages.updatePeriodNotEnteredMsg,
                    IMessageProvider.WARNING);
            return;
        }

        // check if text is integer
        try {
            Integer.parseInt(period);
        } catch (NumberFormatException e) {
            setMessage(Messages.illegalUpdatePeriodMsg, IMessageProvider.ERROR);
            return;
        }

        // check if the value is within valid range
        if (Integer.valueOf(period) < MIN_UPDATE_PERIOD) {
            setMessage(Messages.updatePeriodOutOfRangeMsg,
                    IMessageProvider.ERROR);
            return;
        }

        setMessage(null);
    }

    /**
     * Validates the max number of classes.
     *
     */
    void validateMaxNumberOfClasses() {

        // check if text is empty
        String period = maxNumberOfClassesText.getText();
        if (period.isEmpty()) {
            setMessage(Messages.enterMaxNumberOfClassesMsg,
                    IMessageProvider.WARNING);
            return;
        }

        // check if text is integer
        try {
            Integer.parseInt(period);
        } catch (NumberFormatException e) {
            setMessage(Messages.maxNumberOfClassesInvalidMsg,
                    IMessageProvider.ERROR);
            return;
        }

        // check if the value is within valid range
        if (Integer.valueOf(period) <= 0) {
            setMessage(Messages.maxNumberOfClassesOutOfRangeMsg,
                    IMessageProvider.ERROR);
            return;
        }

        setMessage(null);
    }
}
