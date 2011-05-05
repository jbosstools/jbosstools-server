package org.jboss.ide.eclipse.as.ui.views;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.log.Messages;

/**
 * Displays details about Log Entry.
 * Event information is split in three sections: details, stack trace and session. Details
 * contain event date, message and severity. Stack trace is displayed if an exception is bound
 * to event. Stack trace entries can be filtered.
 */
public class EventDetailsDialog extends TrayDialog {

	private AbstractEntry entry;
	private LogLabelProvider labelProvider;
	private TreeViewer provider;

	private static int COPY_ID = 22;


	private Label dateLabel;
	private Label severityImageLabel;
	private Label severityLabel;
	private Text msgText;
	private Text stackTraceText;
	private Clipboard clipboard;
	private Button copyButton;

	/**
	 * 
	 * @param parentShell shell in which dialog is displayed
	 * @param selection entry initially selected and to be displayed
	 * @param provider viewer
	 * @param comparator comparator used to order all entries
	 */
	protected EventDetailsDialog(Shell parentShell, AbstractEntry selection, TreeViewer provider) {
		super(parentShell);
		this.provider = provider;
		labelProvider = (LogLabelProvider) this.provider.getLabelProvider();
		this.entry = selection;
		setShellStyle(SWT.MODELESS | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.CLOSE | SWT.BORDER | SWT.TITLE);
		clipboard = new Clipboard(parentShell.getDisplay());
	}

	public void create() {
		super.create();
		getShell().setSize(500, 550);
		applyDialogFont(buttonBar);
		getButton(IDialogConstants.OK_ID).setFocus();
	}

	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId)
			okPressed();
		else if (IDialogConstants.CANCEL_ID == buttonId)
			cancelPressed();
		else if (COPY_ID == buttonId)
			copyPressed();
	}


	protected void copyPressed() {
		StringWriter writer = new StringWriter();
		PrintWriter pwriter = new PrintWriter(writer);

		entry.write(pwriter);
		pwriter.flush();
		String textVersion = writer.toString();
		try {
			pwriter.close();
			writer.close();
		} catch (IOException e) { // do nothing
		}
		// set the clipboard contents
		clipboard.setContents(new Object[] {textVersion}, new Transfer[] {TextTransfer.getInstance()});
	}


	public void updateProperties() {
		if (entry instanceof LogEntry) {
			LogEntry logEntry = (LogEntry) entry;

			String strDate = logEntry.getFormattedDate();
			dateLabel.setText(strDate);
			severityImageLabel.setImage(labelProvider.getColumnImage(entry, 0));
			severityLabel.setText(logEntry.getSeverityText());
			msgText.setText(logEntry.getMessage() != null ? logEntry.getMessage() : ""); //$NON-NLS-1$
			String stack = logEntry.getStack();

			if (stack != null) {
				stack = filterStack(stack);
				stackTraceText.setText(stack);
			} else {
				stackTraceText.setText(Messages.EventDetailsDialog_noStack);
			}
		} else {
			dateLabel.setText(""); //$NON-NLS-1$
			severityImageLabel.setImage(null);
			severityLabel.setText(""); //$NON-NLS-1$
			msgText.setText(""); //$NON-NLS-1$
			stackTraceText.setText(""); //$NON-NLS-1$
		}
	}

	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);

		createDetailsSection(container);
		createStackSection(container);
		updateProperties();
		Dialog.applyDialogFont(container);
		return container;
	}

	private void createToolbarButtonBar(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		//layout.numColumns = 1;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		((GridData) comp.getLayoutData()).verticalAlignment = SWT.BOTTOM;

		Composite container = new Composite(comp, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		copyButton = createButton(container, COPY_ID, "", false); //$NON-NLS-1$
		GridData gd = new GridData();
		copyButton.setLayoutData(gd);
		copyButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));
		copyButton.setToolTipText(Messages.EventDetailsDialog_copy);

		// set numColumns at the end, after all createButton() calls, which change this value
		layout.numColumns = 2;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// create OK button only by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	private void createDetailsSection(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createTextSection(container);
		createToolbarButtonBar(container);
	}

	private void createTextSection(Composite parent) {
		Composite textContainer = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = layout.marginWidth = 0;
		textContainer.setLayout(layout);
		textContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(textContainer, SWT.NONE);
		label.setText(Messages.EventDetailsDialog_date);
		dateLabel = new Label(textContainer, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		dateLabel.setLayoutData(gd);

		label = new Label(textContainer, SWT.NONE);
		label.setText(Messages.EventDetailsDialog_severity);
		severityImageLabel = new Label(textContainer, SWT.NULL);
		severityLabel = new Label(textContainer, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		severityLabel.setLayoutData(gd);

		label = new Label(textContainer, SWT.NONE);
		label.setText(Messages.EventDetailsDialog_message);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		label.setLayoutData(gd);
		msgText = new Text(textContainer, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
		msgText.setEditable(false);
		gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
		gd.horizontalSpan = 2;
		gd.heightHint = 44;
		gd.grabExcessVerticalSpace = true;
		msgText.setLayoutData(gd);
	}

	private void createStackSection(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 6;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		container.setLayoutData(gd);

		Label label = new Label(container, SWT.NONE);
		label.setText(Messages.EventDetailsDialog_exception);
		gd = new GridData();
		gd.verticalAlignment = SWT.BOTTOM;
		label.setLayoutData(gd);

		stackTraceText = new Text(container, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		stackTraceText.setLayoutData(gd);
		stackTraceText.setEditable(false);
	}

	private String filterStack(String stack) {
		return stack;
	}

}
