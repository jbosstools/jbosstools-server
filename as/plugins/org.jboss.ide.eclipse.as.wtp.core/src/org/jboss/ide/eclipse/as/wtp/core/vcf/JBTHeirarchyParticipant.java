/*******************************************************************************
 * Copyright (c) 2013 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.core.vcf;

import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualArchiveComponent;
import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
import org.eclipse.jst.j2ee.componentcore.util.EARVirtualComponent;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.wst.common.componentcore.internal.flat.AbstractFlattenParticipant;
import org.eclipse.wst.common.componentcore.internal.flat.FlatVirtualComponent.FlatComponentTaskModel;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;

/**
 * This class is a participant registered globally via the 
 * GlobalHeirarchyParticipant list. Specifically, it checks whether
 * either the virtual component that is referenced is a JBT type AND can be nested in an ear, 
 * OR,  it tests whether a utility jar wants to nest inside of us. 
 * 
 * Both of these cases are approved nestings. 
 */
public class JBTHeirarchyParticipant  extends AbstractFlattenParticipant {

	public JBTHeirarchyParticipant() {
	}

	@Override
	public boolean isChildModule(IVirtualComponent rootComponent,
			IVirtualReference reference, FlatComponentTaskModel dataModel) {
		if( isJEEComponent(rootComponent) &&
				(reference.getReferencedComponent() instanceof JBTVirtualComponent)) {
				return ((JBTVirtualComponent)reference.getReferencedComponent()).canNestInsideEar();
		}
		if( (rootComponent instanceof JBTVirtualComponent) &&
				isUtilityProject(reference.getReferencedComponent())) {
			return true;
		}
		return false;
				
	}
	
	private boolean isJEEComponent(IVirtualComponent component) {
		IVirtualComponent tmp = component.getComponent(); // guard against caching type
		return tmp instanceof J2EEModuleVirtualComponent
			|| tmp instanceof J2EEModuleVirtualArchiveComponent
			|| tmp instanceof EARVirtualComponent;
	}

	private boolean isUtilityProject(IVirtualComponent component) {
		IVirtualComponent tmp = component.getComponent();
		if( tmp instanceof J2EEModuleVirtualComponent ) {
			String childType = JavaEEProjectUtilities.getJ2EEComponentType(tmp);
			if( IJ2EEFacetConstants.UTILITY.equals(childType))
			return true;
		}
		return false;
	}
}
