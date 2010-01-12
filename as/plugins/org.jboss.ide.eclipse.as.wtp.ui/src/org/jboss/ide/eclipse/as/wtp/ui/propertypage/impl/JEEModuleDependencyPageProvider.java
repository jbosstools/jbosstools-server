package org.jboss.ide.eclipse.as.wtp.ui.propertypage.impl;

import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.ide.eclipse.as.wtp.ui.propertypage.IDependencyPageProvider;
import org.jboss.ide.eclipse.as.wtp.ui.propertypage.IModuleDependenciesControl;
import org.jboss.ide.eclipse.as.wtp.ui.propertypage.ModuleAssemblyRootPage;

public class JEEModuleDependencyPageProvider implements IDependencyPageProvider {

	public boolean canHandle(IFacetedProject project) {
		return J2EEProjectUtilities.isJEEProject(project.getProject()) 
			|| J2EEProjectUtilities.isLegacyJ2EEProject(project.getProject());
	}

	public IModuleDependenciesControl[] createPages(IFacetedProject project,
			ModuleAssemblyRootPage parent) {
		return new IModuleDependenciesControl[] {
				new JEEModuleDependenciesPropertyPage(project.getProject(), parent)
		};
	}

	public Composite createRootControl(IModuleDependenciesControl[] pages,
			Composite parent) {
		if( pages.length == 1 && pages[0] != null)
			return pages[0].createContents(parent);
		return null;
	}
}
