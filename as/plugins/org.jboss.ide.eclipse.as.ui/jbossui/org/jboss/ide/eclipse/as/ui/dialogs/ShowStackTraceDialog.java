package org.jboss.ide.eclipse.as.ui.dialogs;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;

public class ShowStackTraceDialog extends Dialog {

	private EventLogTreeItem selected;
	public ShowStackTraceDialog(IShellProvider parentShell, EventLogTreeItem selected) {
		super(parentShell);
		this.selected = selected;
	}

	public ShowStackTraceDialog(Shell shell, EventLogTreeItem selected) {
		super(shell);
		this.selected = selected;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		setShellStyle(SWT.SHELL_TRIM);
		newShell.setText("Exception Stack Trace");
		//newShell.setImage(JBossServerUISharedImages.getImage(JBossServerUISharedImages.TWIDDLE_IMAGE));
		newShell.setBounds(300, 300, 500, 300);
	}

	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite)super.createDialogArea(parent);
		c.setLayout(new FillLayout());
		Text t = new Text(c, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		Object exception = selected.getProperty(EventLogModel.EXCEPTION_PROPERTY);
		if( exception instanceof Throwable) {
			Throwable aThrowable = (Throwable)exception;
			ArrayList<Throwable> seen = new ArrayList<Throwable>();
		    final StringBuilder result = new StringBuilder();
		    boolean first = true;
		    while( aThrowable != null && !seen.contains(aThrowable)) {
				seen.add(aThrowable);
				if( !first ) result.append("nested:" + Text.DELIMITER);
		    	appendDetails(aThrowable, result);
		    	aThrowable = aThrowable.getCause();
		    	first = false;
		    }
			t.setText(result.toString());
		}
		return c;
	}
	
	protected void appendDetails(Throwable t, StringBuilder result) {
	    result.append(t.toString());
	    result.append(Text.DELIMITER);

	    //add each element of the stack trace
	    for (StackTraceElement element : t.getStackTrace() ){
	    	result.append("     at ");
	    	result.append( element );
	    	result.append( Text.DELIMITER );
	    }
	    result.append(Text.DELIMITER );
	}
}
