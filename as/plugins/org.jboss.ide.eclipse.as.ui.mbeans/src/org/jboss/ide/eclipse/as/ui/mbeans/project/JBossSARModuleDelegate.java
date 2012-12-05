/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.mbeans.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jst.common.internal.modulecore.AddMappedOutputFoldersParticipant;
import org.eclipse.jst.common.internal.modulecore.IgnoreJavaInSourceFolderParticipant;
import org.eclipse.jst.j2ee.internal.common.exportmodel.JEEHeirarchyExportParticipant;
import org.eclipse.wst.common.componentcore.internal.flat.FlattenParticipantModel;
import org.eclipse.wst.common.componentcore.internal.flat.IFlattenParticipant;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.wtp.core.modules.IJBTModule;
import org.jboss.ide.eclipse.as.wtp.core.modules.JBTFlatModuleDelegate;
import org.jboss.ide.eclipse.as.wtp.core.modules.JBTFlatProjectModuleFactory;
import org.jboss.ide.eclipse.as.wtp.core.vcf.JBTHeirarchyParticipantProvider;

public class JBossSARModuleDelegate extends JBTFlatModuleDelegate implements IJBTModule {

	public JBossSARModuleDelegate(IProject project,
			IVirtualComponent aComponent, JBTFlatProjectModuleFactory myFactory) {
		super(project, aComponent, myFactory);
	}

	public IModuleResource[] members() throws CoreException {
		return super.members();
	}
	
	@Override
	public IFlattenParticipant[] getParticipants() {
		IFlattenParticipant nestedUtils = 
				FlattenParticipantModel.getDefault().getParticipant(
				JBTHeirarchyParticipantProvider.NESTED_UTILITIES_HEIRARCHY_PARTICIPANT_ID);
		List<IFlattenParticipant> participants = new ArrayList<IFlattenParticipant>();
		participants.add(nestedUtils);
		participants.add(new JEEHeirarchyExportParticipant());
		participants.add(new AddMappedOutputFoldersParticipant());
		participants.add(new IgnoreJavaInSourceFolderParticipant());
		return participants.toArray(new IFlattenParticipant[participants.size()]);
	}
}
