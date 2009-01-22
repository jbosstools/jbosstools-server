/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * ken.ryall@nokia.com - 157506 drop from external sources does not work on Linux/Mac
 *******************************************************************************/
package org.jboss.tools.as.wst.server.ui.xpl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.dnd.CommonDropAdapterDescriptor;
import org.eclipse.ui.internal.navigator.dnd.CommonDropDescriptorManager;
import org.eclipse.ui.internal.navigator.dnd.NavigatorPluginDropAction;
import org.eclipse.ui.navigator.CommonDragAdapter;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorDnDService;
import org.eclipse.ui.part.PluginDropAdapter;
import org.eclipse.ui.part.PluginTransfer;

/*
 * THIS CLASS IS A CLONE of CommonDropAdapter.
 * It is necessary because dragged items must be acceptable children.
 * Stupid restriction
 * 
 * I use a delegate and push all methods to the delegate, 
 * even the ones I override. 
 * 
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=261606
 * 
 * Maybe one day they'll fix that ;) 
 */

/**
 * Provides an implementation of {@link PluginDropAdapter} which uses the
 * extensions provided by the associated {@link INavigatorContentService}.
 * 
 * <p>
 * Clients should not need to create an instance of this class unless they are
 * creating their own custom viewer. Otherwise, {@link CommonViewer} configures
 * its drop adapter automatically.
 * </p>
 *  
 * 
 * @see INavigatorDnDService
 * @see CommonDragAdapter
 * @see CommonDragAdapterAssistant
 * @see CommonDropAdapterAssistant
 * @see CommonViewer
 * @since 3.2
 */
public final class ServersDropAdapter extends PluginDropAdapter {
	private static final Transfer[] SUPPORTED_DROP_TRANSFERS = new Transfer[] {
		LocalSelectionTransfer.getTransfer(), FileTransfer.getInstance(),
		PluginTransfer.getInstance() };
	private final INavigatorContentService contentService;
	private final INavigatorDnDService dndService;
	
	public ServersDropAdapter(INavigatorContentService aContentService,
			StructuredViewer aStructuredViewer) {
		super(aStructuredViewer);
		contentService = aContentService;
		dndService = contentService.getDnDService();
	}

	public Transfer[] getSupportedDropTransfers() {
		return SUPPORTED_DROP_TRANSFERS;
	}

	public void dragEnter(DropTargetEvent event) {
		super.dragEnter(event);

		for (int i = 0; i < event.dataTypes.length; i++) {
			if (LocalSelectionTransfer.getTransfer().isSupportedType(
					event.dataTypes[i])) {
				event.currentDataType = event.dataTypes[i]; 
				return;
			}
		}

		for (int i = 0; i < event.dataTypes.length; i++) {
			if (FileTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
				event.currentDataType = event.dataTypes[i];
				event.detail = DND.DROP_COPY; 
				return;
			}
		}

		for (int i = 0; i < event.dataTypes.length; i++) {
			if (PluginTransfer.getInstance()
					.isSupportedType(event.dataTypes[i])) {
				event.currentDataType = event.dataTypes[i]; 
				return;
			}
		}

		event.detail = DND.DROP_NONE; 

	}

	public void dragLeave(DropTargetEvent event) {
		super.dragLeave(event);
		if (LocalSelectionTransfer.getTransfer().isSupportedType(
				event.currentDataType)) {
			event.data = NavigatorPluginDropAction
					.createTransferData(contentService);
		}
	}

	/*
	 * Changed from CommonDropAdapter to extract the findCommonDropAdapterAssistant section
	 * since I do not like the impl in dnd service
	 */
	public void drop(DropTargetEvent event) {
		// Must validate the drop here because on some platforms (Linux, Mac) the event 
		// is not populated with the correct currentDataType until the drop actually
		// happens, and validateDrop sets the currentTransfer based on that.  The 
		// call to validateDrop in dragAccept is too early.
		validateDrop(getCurrentTarget(), getCurrentOperation(), event.currentDataType);
		if (PluginTransfer.getInstance().isSupportedType(event.currentDataType)) {
			super.drop(event);
		} else {

			Object target = getCurrentTarget() != null ? 
							getCurrentTarget() : getViewer().getInput();
							
			CommonDropAdapterAssistant[] assistants = 
				findCommonDropAdapterAssistants(target, getCurrentTransfer());
			IStatus valid = null;
			for (int i = 0; i < assistants.length; i++) {
				try {
					valid = assistants[i].validateDrop(getCurrentTarget(),
							getCurrentOperation(), getCurrentTransfer());
					if (valid != null && valid.isOK()) {
						assistants[i].handleDrop(null, event,
								getCurrentTarget());
	                    event.detail = DND.DROP_NONE;
						return;
					} 
				} catch (Throwable t) {
					NavigatorPlugin.logError(0, t.getMessage(), t);
				}
			}
            event.detail = DND.DROP_NONE;
		}
	}

	/*
	 * Changed from CommonDropAdapter to extract the findCommonDropAdapterAssistant section
	 * since I do not like the impl in dnd service
	 */
	public boolean validateDrop(Object aDropTarget, int theDropOperation,
			TransferData theTransferData) {
		boolean result = false;
		IStatus valid = null;
		if (super.validateDrop(aDropTarget, theDropOperation, theTransferData)) {
			result = true; 
		} else {
			Object target = aDropTarget != null ? aDropTarget : getViewer().getInput();
			CommonDropAdapterAssistant[] assistants = 
					findCommonDropAdapterAssistants(target,
							theTransferData);
			for (int i = 0; i < assistants.length; i++) {
				try { 
					valid = assistants[i].validateDrop(target,
							theDropOperation, theTransferData); 
				} catch (Throwable t) {
					NavigatorPlugin.logError(0, t.getMessage(), t);
				}
				if (valid != null && valid.isOK()) {
					result = true;
					break;
				}
			}
		}
		setScrollExpandEnabled(true);
		return result;

	}

	public Rectangle getBounds(Item item) {
		return super.getBounds(item);
	}

	public int getCurrentLocation() {
		return super.getCurrentLocation();
	}

	public int getCurrentOperation() {
		return super.getCurrentOperation();
	}

	public Object getCurrentTarget() {
		return super.getCurrentTarget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.PluginDropAdapter#getCurrentTransfer()
	 */
	public TransferData getCurrentTransfer() {
		return super.getCurrentTransfer();
	}
	
    /**
     * Returns the position of the given event's coordinates relative to its target.
     * The position is determined to be before, after, or on the item, based on
     * some threshold value.
     *
     * @param event the event
     * @return one of the <code>LOCATION_* </code>constants defined in this class
     */
    protected int determineLocation(DropTargetEvent event) {
        if (!(event.item instanceof Item)) {
            return LOCATION_NONE;
        }
        return LOCATION_ON;
    }

    
	private CommonDropAdapterAssistant[] findCommonDropAdapterAssistants(
			Object aDropTarget, TransferData theTransferType) {
		CommonDropAdapterAssistant[] result = 
				findCommonDropAdapterAssistants2(aDropTarget, theTransferType);
		return result;
	}
	
	
	
	/*
	 * Stolen from DND Service
	 */
	
	private static final CommonDropAdapterAssistant[] NO_ASSISTANTS = new CommonDropAdapterAssistant[0];
	private final Map dropAssistants = new HashMap();
	
	public CommonDropAdapterAssistant[] findCommonDropAdapterAssistants2(
			Object aDropTarget, TransferData aTransferType) {
 
		// TODO Make sure descriptors are sorted by priority 
		CommonDropAdapterDescriptor[] descriptors = CommonDropDescriptorManager
				.getInstance().findCommonDropAdapterAssistants(aDropTarget,
						contentService);

		if (descriptors.length == 0) {
			return NO_ASSISTANTS;
		}
		if (LocalSelectionTransfer.getTransfer().isSupportedType(aTransferType)  
						&& LocalSelectionTransfer.getTransfer().getSelection() instanceof IStructuredSelection) {
			return getAssistants(descriptors);
		} 
		return getAssistantsByTransferData(descriptors, aTransferType);
	}
	
	private CommonDropAdapterAssistant[] getAssistants(CommonDropAdapterDescriptor[] descriptors) {
		Set assistants = new LinkedHashSet(); 
		for (int i = 0; i < descriptors.length; i++) {
			assistants.add(getAssistant(descriptors[i]));
		}  
		return (CommonDropAdapterAssistant[]) assistants
				.toArray(new CommonDropAdapterAssistant[assistants.size()]);
	}
	
	private CommonDropAdapterAssistant[] getAssistantsByTransferData(
			CommonDropAdapterDescriptor[] descriptors,
			TransferData aTransferType) {

		Set assistants = new LinkedHashSet();
		for (int i = 0; i < descriptors.length; i++) {
			CommonDropAdapterAssistant asst = getAssistant(descriptors[i]);
			if (asst.isSupportedType(aTransferType)) {
				assistants.add(asst);
			}
		}
		return (CommonDropAdapterAssistant[]) assistants
				.toArray(new CommonDropAdapterAssistant[assistants.size()]);

	}

	private CommonDropAdapterAssistant getAssistant(
			CommonDropAdapterDescriptor descriptor) {
		CommonDropAdapterAssistant asst = (CommonDropAdapterAssistant) dropAssistants
				.get(descriptor);
		if (asst != null) {
			return asst;
		}
		synchronized (dropAssistants) {
			asst = (CommonDropAdapterAssistant) dropAssistants.get(descriptor);
			if (asst == null) {
				dropAssistants.put(descriptor, (asst = descriptor
						.createDropAssistant()));
				asst.init(contentService);
			}
		}
		return asst;
	}

}
