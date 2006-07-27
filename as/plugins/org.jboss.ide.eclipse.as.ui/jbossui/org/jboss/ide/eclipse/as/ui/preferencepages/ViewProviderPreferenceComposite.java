package org.jboss.ide.eclipse.as.ui.preferencepages;

import org.eclipse.swt.widgets.Composite;

public abstract class ViewProviderPreferenceComposite extends Composite {
	public ViewProviderPreferenceComposite(Composite parent, int style) {
		super(parent, style);
	}
	public abstract boolean isValid();
	public abstract boolean performOk();
	public abstract boolean performCancel();
}
