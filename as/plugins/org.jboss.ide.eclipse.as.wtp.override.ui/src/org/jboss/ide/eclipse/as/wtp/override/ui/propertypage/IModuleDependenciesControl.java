package org.jboss.ide.eclipse.as.wtp.override.ui.propertypage;

import org.eclipse.swt.widgets.Composite;

public interface IModuleDependenciesControl {
	/**
	 * Creates the Composite associated with this control.
	 * @param parent Parent Composite.
	 * @return Composite for the control.
	 */
	Composite createContents(Composite parent);
	
	/**
	 * Called when the property page's <code>performOk()</code> method is called.
	 * @return
	 */
	boolean performOk();
	
	/**
	 * Called when the property page's <code>performDefaults()</code> method is called.
	 * @return
	 */
	void performDefaults();
	
	/**
	 * Called when the property page's <code>performCancel()</code> method is called.
	 * @return
	 */
	boolean performCancel();
	
	/**
	 * Called when the property page's <code>setVisible()</code> method is called.
	 * @return
	 */
	void setVisible(boolean visible);
	
	/**
	 * Called when the property page's <code>dispose()</code> method is called.
	 * @return
	 */
	void dispose();
}
