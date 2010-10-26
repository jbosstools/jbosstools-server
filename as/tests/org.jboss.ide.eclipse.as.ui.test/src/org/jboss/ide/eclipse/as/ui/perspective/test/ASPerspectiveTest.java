package org.jboss.ide.eclipse.as.ui.perspective.test;

import junit.framework.TestCase;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.as.ui.perspective.ASPerspective;
import org.jboss.ide.eclipse.as.ui.views.ServerLogView;

public class ASPerspectiveTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage().setPerspective(
				PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(ASPerspective.ID));
	}

	public void testASPerspective() {
		IPerspectiveDescriptor perspective = PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage().getPerspective();
		assertEquals(perspective.getLabel(),"JBoss AS");
	}

	public void testServerLogView() throws PartInitException {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().showView(ServerLogView.VIEW_ID);
	}

}
