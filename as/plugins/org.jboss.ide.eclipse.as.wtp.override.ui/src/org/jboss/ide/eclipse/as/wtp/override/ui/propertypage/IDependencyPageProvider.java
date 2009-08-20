package org.jboss.ide.eclipse.as.wtp.override.ui.propertypage;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;

public interface IDependencyPageProvider {
	public boolean canHandle(IFacetedProject project);
	public IModuleDependenciesControl[] createPages(IFacetedProject project, ModuleAssemblyRootPage parent);
	public Composite createRootControl(IModuleDependenciesControl[] pages, Composite parent);
}
