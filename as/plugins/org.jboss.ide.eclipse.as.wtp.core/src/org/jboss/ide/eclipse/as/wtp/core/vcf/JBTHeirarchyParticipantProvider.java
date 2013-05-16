/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.core.vcf;

import java.util.Properties;

import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualArchiveComponent;
import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
import org.eclipse.jst.j2ee.componentcore.util.EARVirtualComponent;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.wst.common.componentcore.internal.flat.AbstractFlattenParticipant;
import org.eclipse.wst.common.componentcore.internal.flat.FlatVirtualComponent.FlatComponentTaskModel;
import org.eclipse.wst.common.componentcore.internal.flat.IFlattenParticipant;
import org.eclipse.wst.common.componentcore.internal.flat.IFlattenParticipantProvider;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;

/**
 * Prior consumers may now use the 
 * @deprecated  This class was part of a workaround and was not intended to remain
 */
public class JBTHeirarchyParticipantProvider implements IFlattenParticipantProvider {
	public static final String JBT_PROJ_IN_EAR_PARTICIPANT_ID = "jbtProjectInEarHeirarchyParticipant";
	public static final String NESTED_UTILITIES_HEIRARCHY_PARTICIPANT_ID = "allowNestedUtilitiesHeirarchyParticipant";
	
	public JBTHeirarchyParticipantProvider() {
	}
	public IFlattenParticipant findParticipant(String id, Properties properties) {
		if( JBT_PROJ_IN_EAR_PARTICIPANT_ID.equals(id)) {
			return new JBTHeirarchyParticipant();
		}
		if( NESTED_UTILITIES_HEIRARCHY_PARTICIPANT_ID.equals(id)) {
			return new NestedUtilitiesHeirarchyParticipant();
		}
		return null;
	}
	
	public static class JBTHeirarchyParticipant extends AbstractFlattenParticipant {
		public boolean isChildModule(IVirtualComponent rootComponent,
				IVirtualReference referenced, FlatComponentTaskModel dataModel) {
			if( isJEEComponent(rootComponent) && 
					(referenced.getReferencedComponent() instanceof JBTVirtualComponent))
				return ((JBTVirtualComponent)referenced.getReferencedComponent()).canNestInsideEar();
			return false;
		}
		private boolean isJEEComponent(IVirtualComponent component) {
			IVirtualComponent tmp = component.getComponent(); // guard against caching type
			return tmp instanceof J2EEModuleVirtualComponent 
				|| tmp instanceof J2EEModuleVirtualArchiveComponent 
				|| tmp instanceof EARVirtualComponent;
		}
	}

	public static class NestedUtilitiesHeirarchyParticipant extends AbstractFlattenParticipant {
		public boolean isChildModule(IVirtualComponent rootComponent,
				IVirtualReference referenced, FlatComponentTaskModel dataModel) {
			if( (rootComponent instanceof JBTVirtualComponent) && 
					isUtilityProject(referenced.getReferencedComponent())) {
				return true;
			}
			return false;
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
}
