package org.jboss.ide.eclipse.as.ui.packages;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.jboss.ide.eclipse.ui.util.ActionWithDelegate;

public class NewEARAction extends ActionWithDelegate implements IViewActionDelegate {

	public NewEARAction() {
		// TODO Auto-generated constructor stub
	}

	public void run() {
		// TODO Auto-generated method stub
		System.out.println("test");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
	}

}
