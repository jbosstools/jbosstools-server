package org.jboss.ide.eclipse.as.ui.wizards;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;

public class CopyConfigurationDialog extends TitleAreaDialog {
	private String origHome, origDest, origConfig;
	private String newDest, newConfig;
	private Text destText;
	protected CopyConfigurationDialog(Shell parentShell, String home, 
			String dir, String config) {
		super(new Shell(parentShell));
		origHome = home;
		origDest = dir;
		origConfig = config;
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		Composite main = new Composite(c, SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new FormLayout());

		setCleanMessage();

		Label nameLabel = new Label(main, SWT.NONE);
		nameLabel.setText(Messages.wf_NameLabel);

		final Text nameText = new Text(main, SWT.BORDER);
		
		Label destLabel = new Label(main, SWT.NONE);
		destLabel.setText(Messages.rwf_DestinationLabel);

		destText = new Text(main, SWT.BORDER);

		Button browse = new Button(main, SWT.PUSH);
		browse.setText(Messages.browse);

		Point nameSize = new GC(nameLabel).textExtent(nameLabel.getText());
		Point destSize = new GC(destLabel).textExtent(destLabel.getText());
		Control wider = nameSize.x > destSize.x ? nameLabel : destLabel;
		
		nameText.setLayoutData(FormDataUtility.createFormData2(
				0,13,null,0,wider,5,100,-5));
		nameLabel.setLayoutData(FormDataUtility.createFormData2(
				0,15,null,0,0,5,null,0));
		destText.setLayoutData(FormDataUtility.createFormData2(
				nameText,5,null,0,wider,5,browse,-5));
		destLabel.setLayoutData(FormDataUtility.createFormData2(
				nameText,7,null,0,0,5,null,0));
		browse.setLayoutData(FormDataUtility.createFormData2( 
				nameText,5,null,0,null,0,100,-5));
		
		nameText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				newConfig = nameText.getText();
				validate();
			}
		});
		destText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				newDest = destText.getText();
				validate();
			}
		});
		browse.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				IPath p = new Path(newDest);
				if( !p.isAbsolute())
					p = new Path(origHome).append(newDest);
				File file = p.toFile();
				if (!file.exists()) { 
					file = null;
				}

				File directory = getDirectory(file, getShell());
				if (directory != null) {
					IPath newP = new Path(directory.getAbsolutePath());
					IPath result;
					if( newP.toOSString().startsWith(new Path(origHome).toOSString()))
						result = newP.removeFirstSegments(new Path(origHome).segmentCount());
					else
						result = newP;
					destText.setText(result.toString());
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		
		
		destText.setText(origDest);
		// we could localize the string _copy, but it would probably cause more trouble than it's worth
		nameText.setText(findNewest(origConfig + "_copy")); // TODO increment //$NON-NLS-1$
		return c;
	}
	protected static File getDirectory(File startingDirectory, Shell shell) {
		DirectoryDialog fileDialog = new DirectoryDialog(shell, SWT.OPEN);
		if (startingDirectory != null) {
			fileDialog.setFilterPath(startingDirectory.getPath());
		}

		String dir = fileDialog.open();
		if (dir != null) {
			dir = dir.trim();
			if (dir.length() > 0) {
				return new File(dir);
			}
		}
		return null;
	}
	public void validate() {
		boolean valid = false;
		IPath p = null;
		if( newDest != null && newConfig != null ) {
			p = new Path(newDest);
			if( !p.isAbsolute())
				p = new Path(origHome).append(newDest);
			if( !p.append(newConfig).toFile().exists()) 
				valid = true;

		}
		if( !valid ) {
			if( newDest == null || newConfig == null ) {
				setMessage(Messages.JBossRuntimeWizardFragment_AllFieldsRequired, IMessageProvider.ERROR);
			} else {
				setMessage(Messages.JBossRuntimeWizardFragment_OutputFolderExists + p.append(newConfig).toString(), IMessageProvider.ERROR);
			}
		} else {
			setCleanMessage();
		}
		if( getButton(Dialog.OK) != null ) 
			getButton(Dialog.OK).setEnabled(valid);
	}
	
	protected void setCleanMessage() {
		setMessage(NLS.bind(Messages.rwf_CopyConfigLabel, origConfig, origDest));
	}
	// Only to be used in initializing dialog
	protected String findNewest(String suggested) {
		IPath p = new Path(origDest);
		if( !p.isAbsolute())
			p = new Path(origHome).append(origDest);
		if( p.append(suggested).toFile().exists()) {
			int i = 1;
			while(p.append(suggested + i).toFile().exists())
				i++;
			return suggested + i;
		}
		return suggested;
	}
	
	protected Point getInitialSize() {
		return new Point(500, super.getInitialSize().y);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.JBossRuntimeWizardFragment_CopyAConfigShellText);
	}

	public String getNewDest() {
		return newDest;
	}
	
	public String getNewConfig() {
		return newConfig;
	}
}