/*******************************************************************************
 * Copyright (c) 2011-2104 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.ui.containers.custom;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlot;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.LayeredProductPathProvider;
import org.jboss.ide.eclipse.as.core.server.jbossmodules.LayeredModulePathFactory;
import org.jboss.tools.foundation.ui.xpl.taskwizard.IWizardHandle;
import org.jboss.tools.foundation.ui.xpl.taskwizard.WizardFragment;

public class LayeredPathProviderFragment extends WizardFragment {

    private static String KEY_PRESS = "Ctrl+Space"; //$NON-NLS-1$
    
	private IWizardHandle handle;
	private Text moduleText, slotText;
	private String moduleId, slot;
	public boolean hasComposite() {
		return true;
	}

	/**
	 * Creates the composite associated with this fragment.
	 * This method is only called when hasComposite() returns true.
	 * 
	 * @param parent a parent composite
	 * @param handle a wizard handle
	 * @return the created composite
	 */
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		setComplete(false);
		this.handle = handle;
		handle.setTitle("Create a JBoss modules classpath entry."); //$NON-NLS-1$
		handle.setDescription("This classpath entry will search all available modules folders for the chosen module." +  //$NON-NLS-1$
		"This ensures patches are picked up properly. Example: module name = javax.faces.api,  slot=1.2"); //$NON-NLS-1$
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		
		Label moduleLabel = new Label(c, SWT.NONE);
		moduleLabel.setText("Module ID: "); //$NON-NLS-1$
		moduleText = new Text(c, SWT.SINGLE | SWT.BORDER);

		Label slotLabel = new Label(c, SWT.NONE);
		slotLabel.setText("Slot: "); //$NON-NLS-1$
		slotText = new Text(c, SWT.SINGLE | SWT.BORDER);
		slotText.setText("main"); //$NON-NLS-1$

		GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		gd.widthHint = 200;
		moduleText.setLayoutData(gd);
		slotText.setLayoutData(gd);
		
		// Add decorators and auto-completion if a runtime exists
		boolean hasRuntimes = runtimesExist();
		if( hasRuntimes ) {
			addAutocompleteDecorators();
		}
		
		addListeners(hasRuntimes);
		LayeredPathProviderFragment.this.handle.update();
		return c;
	}
	
	private void addListeners(boolean hasRuntimes) {

		ModifyListener ml = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				moduleId = (moduleText == null ? null : moduleText.getText());
				setComplete(moduleId != null && moduleId.length() > 0);
				LayeredPathProviderFragment.this.handle.update();
			}
		};
		moduleText.addModifyListener(ml);
		
		ModifyListener slotml = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				slot = (slotText == null ? null : slotText.getText());
				LayeredPathProviderFragment.this.handle.update();
			}
		};
		slotText.addModifyListener(slotml);
		
		if( hasRuntimes ) {
			setAutoCompletionSlot(slotText, null);
			setAutoCompletionModule(moduleText, null);
	        slotText.addKeyListener( new KeyAdapter() {
	            public void keyReleased(KeyEvent ke) {
	                //Method for autocompletion
	                setAutoCompletionSlot(slotText, slotText.getText());
	            }
	        });
	
	        moduleText.addKeyListener( new KeyAdapter() {
	            public void keyReleased(KeyEvent ke) {
	                //Method for autocompletion
	                setAutoCompletionModule(moduleText, moduleText.getText());
	            }
	        });
		}
	}
	
	private boolean runtimesExist() {
		IRuntimeType rtt = (IRuntimeType)getTaskModel().getObject(RuntimeClasspathProviderWizard.RUNTIME_TYPE);
		String rtType = (rtt == null ? null : rtt.getId());
		if( rtType != null ) {
			IRuntime[] all = ServerCore.getRuntimes();
			for( int i = 0; i < all.length; i++ ) {
				if( all[i].getRuntimeType().equals(rtt)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void addAutocompleteDecorators() {

		final ControlDecoration moduleDecoration = new ControlDecoration(moduleText, SWT.TOP | SWT.LEAD);
		final ControlDecoration slotDecoration = new ControlDecoration(slotText, SWT.TOP | SWT.LEAD);
		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
		FieldDecoration fd = registry.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		moduleDecoration.setImage(fd.getImage());
		moduleDecoration.setDescriptionText(fd.getDescription());
		slotDecoration.setImage(fd.getImage());
		slotDecoration.setDescriptionText(fd.getDescription());
	}
	
	
	private AvailableModuleSlotsModel model;
	private String[] getModuleProposals( String text ) {
		if( model == null ) {
			model = new AvailableModuleSlotsModel();
		}
		IRuntimeType rtt = (IRuntimeType)getTaskModel().getObject(RuntimeClasspathProviderWizard.RUNTIME_TYPE);
		String rtType = (rtt == null ? null : rtt.getId());
		ModuleSlot[] all = model.getModuleSlots(rtType, true);
		// Now filter
		Set<String> filtered = new HashSet<String>();
		for( int i = 0; i < all.length; i++ ) {
			if( all[i].getModule().startsWith(text)) {
				filtered.add(all[i].getModule());
			}
		}
		List<String> list = new ArrayList<String>(filtered);
		java.util.Collections.sort(list);
		return (String[]) list.toArray(new String[list.size()]);
	}
	private String[] getSlotProposals( String text ) {
		if( model == null ) {
			model = new AvailableModuleSlotsModel();
		}
		IRuntimeType rtt = (IRuntimeType)getTaskModel().getObject(RuntimeClasspathProviderWizard.RUNTIME_TYPE);
		String rtType = (rtt == null ? null : rtt.getId());
		ModuleSlot[] all = model.getModuleSlots(rtType, true);
		// Now filter
		// ignore the text and just return all valid slots for the given module
		Set<String> slots = new HashSet<String>();
		for( int i = 0; i < all.length; i++ ) {
			if( all[i].getModule().equals(moduleText.getText())) {
				slots.add(all[i].getSlot());
			}
		}
		return (String[]) slots.toArray(new String[slots.size()]);
	}
	
    /**
     * This method is used to provide the implementaion
     * of eclipse autocompletion feature. User has to press
     * "CTRL+Space" to see the autocompletion effect.
     *
     * @param text of type {@link Text}
     * @param value of type String
     * @author Debadatta Mishra (PIKU)
     */
    private void setAutoCompletionSlot( Text text , String value ) {
        setAutoCompletion(text, value, getSlotProposals(value));
    }
    private void setAutoCompletionModule( Text text , String value ) {
        setAutoCompletion(text, value, getModuleProposals(value));
    }
    private void setAutoCompletion( Text text , String value, String[] possible ) {
        try {
            ContentProposalAdapter adapter = null;
            SimpleContentProposalProvider scp = new SimpleContentProposalProvider( possible );
            scp.setProposals(possible);
            scp.setFiltering(true);
            KeyStroke ks = KeyStroke.getInstance(KEY_PRESS);
            adapter = new ContentProposalAdapter(text, new TextContentAdapter(),
                    scp,ks,null);
            adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		LayeredProductPathProvider prov = new LayeredProductPathProvider(moduleId, slot); 
		getTaskModel().putObject(RuntimeClasspathProviderWizard.CREATED_PATH_PROVIDER, prov);
	}
	
	
	
	private class AvailableModuleSlotsModel {
		private HashMap<String, Job> loading;
		private HashMap<String, Set<ModuleSlot>> slots;
		
		public AvailableModuleSlotsModel() {
			loading = new HashMap<String, Job>();
			slots = new HashMap<String, Set<ModuleSlot>>();
		}
		
		boolean isLoaded(String runtimeType) {
			if( slots.get(runtimeType) == null )
				return false;
			return true;
		}
		boolean isLoading(String runtimeType) {
			if( loading.get(runtimeType) == null )
				return false;
			return true;
		}
		
		ModuleSlot[] getModuleSlots(String rtType, boolean load) {
			if( isLoaded(rtType)) {
				Set<ModuleSlot> ret = slots.get(rtType);
				return (ModuleSlot[]) ret.toArray(new ModuleSlot[ret.size()]);
			}
			if( !isLoading(rtType) && load) {
				fireLoad(rtType);
			}
			return new ModuleSlot[0];
		}
		
		private void fireLoad(final String rtType) {
			Job j = new Job("Loading Modules") { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						IRuntime[] rt = ServerCore.getRuntimes();
						Set<ModuleSlot> ms = new HashSet<ModuleSlot>();
						for( int i = 0; i < rt.length; i++ ) {
							if( rt[i].getRuntimeType().getId().equals(rtType)) {
								IPath modulesFolder = rt[i].getLocation().append("modules"); //$NON-NLS-1$
								File[] layeredPaths = LayeredModulePathFactory.resolveLayeredModulePath(modulesFolder.toFile());
								// Find every module.xml 
								for( int j = 0; j < layeredPaths.length; j++ ) {
									ArrayList<File> ignored = new ArrayList<File>(Arrays.asList(layeredPaths));
									ignored.remove(layeredPaths[j]);
									ModuleSlot[] slots = findSlots(layeredPaths[j], ignored);
									ms.addAll(Arrays.asList(slots));
								}
							}
						}
						slots.put(rtType, ms);
					} finally {
						loading.remove(rtType);
					}
					return Status.OK_STATUS;
				}
				private ModuleSlot[] findSlots(File root, List<File> ignored) {
					Set<ModuleSlot> collector = new HashSet<ModuleSlot>();
					findSlots(collector, root, root, ignored);
					return (ModuleSlot[]) collector.toArray(new ModuleSlot[collector.size()]);
				}
				private void findSlots(Set<ModuleSlot> collector, File working, File root, List<File> ignored) {
					if( ignored.contains(working)) {
						return;
					}
					if( working.isFile()) {
						if( working.getName().equals("module.xml")) { //$NON-NLS-1$
							// handle found
							int rootSegCount = new Path(root.getAbsolutePath()).segmentCount();
							IPath relative = new Path(working.getAbsolutePath()).removeFirstSegments(rootSegCount);
							relative = relative.removeLastSegments(1);
							String slot = relative.lastSegment();
							IPath module = relative.removeLastSegments(1);
							StringBuffer modBuffer = new StringBuffer();
							for( int i = 0; i < module.segmentCount(); i++ ) {
								modBuffer.append(module.segment(i));
								if( i < module.segmentCount()-1)
									modBuffer.append("."); //$NON-NLS-1$
							}
							ModuleSlot ms= new ModuleSlot(modBuffer.toString(), slot);
							collector.add(ms);
						}
					} else {
						File[] kids = working.listFiles();
						if( kids != null ) {
							for( int i = 0; i < kids.length; i++ ) {
								findSlots(collector, kids[i], root, ignored);
							}
						}
					}
				}
			};
			loading.put(rtType, j);
			j.schedule();
		}
		
	}
}
