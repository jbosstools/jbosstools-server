package org.jboss.ide.eclipse.as.wtp.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jst.j2ee.internal.deployables.J2EEFlexProjDeployable;
import org.eclipse.jst.jee.internal.deployables.JEEFlexProjDeployable;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.web.internal.deployables.FlatComponentDeployable;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;
import org.jboss.ide.eclipse.as.wtp.core.vcf.JBTHeirarchyParticipantProvider;

public class CustomProjectInEarWorkaroundUtil {
	private static final String JBT_IN_EAR_PARTICIPANT = JBTHeirarchyParticipantProvider.JBT_PROJ_IN_EAR_PARTICIPANT_ID;
	private static final String SINGLE_ROOT_PARTICIPANT = "JEESingleRootParticipant";
	
	/**
	 * Returns a workaround module delegate that can handle
	 * esb / sar / etc children to ears.
	 * 
	 * If the provided module is not an ear module, the 
	 * standard ModuleDelegate is returned instead.
	 * 
	 * @param module
	 * @return
	 */
	public static ModuleDelegate getCustomProjectSafeModuleDelegate(IModule module) {
		if( module.getModuleType().getId().equals(IWTPConstants.FACET_EAR)) {
			FlatComponentDeployable dep = (FlatComponentDeployable)module.loadAdapter(FlatComponentDeployable.class, null);
			J2EEFlexProjDeployable hack = null;
			
			if( dep instanceof JEEFlexProjDeployable ) {
				hack = new JEEFlexProjDeployable(module.getProject(), dep.getComponent()) {
					public String[] getParticipantIds() {
						return addRequiredParticipants(super.getParticipantIds());
					}
				};
			} else if( dep instanceof J2EEFlexProjDeployable ) {
				hack = new J2EEFlexProjDeployable(module.getProject(), dep.getComponent()) {
					public String[] getParticipantIds() {
						return addRequiredParticipants(super.getParticipantIds());
					}
				};
			}
			if( hack != null )
				return hack;
		}
		return (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, null);
	}
	
	public static IModule[] getSafeChildrenModules(IModule module) {
		ModuleDelegate safeModule = getCustomProjectSafeModuleDelegate(module);
		IModule[] children = safeModule.getChildModules(); 
		return children == null ? new IModule[0] : children;
	}
	
	// manually add the id of a participant which recognizes jbt projects
	// as children of ears
	private static String[] addRequiredParticipants(String[] parentParticipants) {
		List<String> l = new ArrayList<String>(Arrays.asList(parentParticipants));
		if( !l.contains(JBT_IN_EAR_PARTICIPANT))
			l.add(JBT_IN_EAR_PARTICIPANT);
		l.remove(SINGLE_ROOT_PARTICIPANT);
		return (String[]) l.toArray(new String[l.size()]);
	}

}
