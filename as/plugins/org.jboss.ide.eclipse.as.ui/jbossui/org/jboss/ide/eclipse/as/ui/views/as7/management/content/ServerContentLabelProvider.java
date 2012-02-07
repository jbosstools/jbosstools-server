/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.views.as7.management.content;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.as.ui.Messages;

import com.ibm.icu.text.MessageFormat;

/**
 * ServerContentLabelProvider
 * 
 * <p/>
 * Label provider implementation for content nodes.
 * 
 * @author Rob Cernich
 */
public class ServerContentLabelProvider extends LabelProvider {

    /**
     * Maps node names to any label that should be used.
     * <p/>
     * TODO: this should probably be contributed by an extension,, which should
     * provide icons as well.
     */
    private static Map<String, String> LABELS;

    public String getText(Object element) {
        if (element instanceof IAttributeNode) {
            IAttributeNode attrNode = (IAttributeNode) element;
            String value = attrNode.getValue();
            if (value == null || IAttributeNode.UNDEFINED_VALUE.equals(value)) {
                value = Messages.ServerContent_Value_undefined;
            }
            return MessageFormat.format(Messages.ServerContent_Label_Attribute_Value, attrNode.getName(), value);
        } else if (element instanceof IErrorNode) {
            return ((IErrorNode) element).getText();
        } else if (element instanceof IContentNode) {
            return getMappedName(((IContentNode<?>) element).getName());
        } else if (element == ServerContentTreeContentProvider.PENDING) {
            return Messages.ServerContent_Label_Loading;
        }
        return super.getText(element);
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IContainerNode<?>) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
        } else if (element instanceof IErrorNode) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
        } else if (element instanceof IContentNode<?>) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
        }
        return super.getImage(element);
    }

    private String getMappedName(String name) {
        if (name == null) {
            return Messages.ServerContent_Value_undefined;
        }
        if (LABELS.containsKey(name)) {
            return LABELS.get(name);
        }
        return name;
    }

    static {
        LABELS = new HashMap<String, String>();
        LABELS.put(IAttributesContainer.ATTRIBUTES_TYPE, Messages.ServerContent_Type_attributes);
        LABELS.put("core-service", Messages.ServerContent_Type_core_service); //$NON-NLS-1$
        LABELS.put("deployment", Messages.ServerContent_Type_deployment); //$NON-NLS-1$
        LABELS.put("extension", Messages.ServerContent_Type_extension); //$NON-NLS-1$
        LABELS.put("interface", Messages.ServerContent_Type_interface); //$NON-NLS-1$
        LABELS.put("path", Messages.ServerContent_Type_path); //$NON-NLS-1$
        LABELS.put(IResourceNode.ROOT_TYPE, Messages.ServerContent_Type_root);
        LABELS.put("socket-binding-group", Messages.ServerContent_Type_socket_binding_group); //$NON-NLS-1$
        LABELS.put("subsystem", Messages.ServerContent_Type_subsystem); //$NON-NLS-1$
        LABELS.put("system-property", Messages.ServerContent_Type_system_property); //$NON-NLS-1$
    }
}
