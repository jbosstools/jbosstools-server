package org.jboss.ide.eclipse.as.ui.editor;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author rob Stryker (rob.stryker@redhat.com)
 *
 */
public class PortSection extends ServerEditorSection {
	protected ServerAttributeHelper helper;
	protected Label jndiLabel, webLabel;
	protected Text jndiText, webText;
	protected Button jndiDetect, webDetect;
	protected Combo jndiDetectCombo, webDetectCombo;

	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		helper = new ServerAttributeHelper(server.getOriginal(), server);
	}
	
	public void createSection(Composite parent) {
		super.createSection(parent);
		createUI(parent);
		initializeState();
		addListeners();
	}
	
	protected void initializeState() {
		boolean detectJNDI = helper.getAttribute(IJBossServerConstants.JNDI_PORT_DETECT, true);
		jndiDetect.setSelection(detectJNDI);
		jndiDetectCombo.setEnabled(detectJNDI);
		jndiText.setEnabled(!detectJNDI);
		jndiText.setEditable(!detectJNDI);
		String jndiXPath = helper.getAttribute(IJBossServerConstants.JNDI_PORT_DETECT_XPATH, IJBossServerConstants.JNDI_PORT_DEFAULT_XPATH.toString());
		int index = jndiDetectCombo.indexOf(jndiXPath);
		if( index != -1 ) {
			jndiDetectCombo.select(index);
			if( detectJNDI )
				jndiText.setText(findPort(new Path(jndiXPath)));
			else
				jndiText.setText(helper.getAttribute(IJBossServerConstants.JNDI_PORT, ""));
		}
		
		boolean detectWeb = helper.getAttribute(IJBossServerConstants.WEB_PORT_DETECT, true);
		webDetect.setSelection(detectWeb);
		webDetectCombo.setEnabled(detectWeb);
		webText.setEnabled(!detectWeb);
		webText.setEditable(!detectWeb);
		String webXPath = helper.getAttribute(IJBossServerConstants.WEB_PORT_DETECT_XPATH, IJBossServerConstants.WEB_PORT_DEFAULT_XPATH.toString());
		int index2 = webDetectCombo.indexOf(webXPath);
		if( index2 != -1 ) {
			webDetectCombo.select(index2);
			if( detectWeb ) 
				webText.setText(findPort(new Path(webXPath)));
			else
				webText.setText(helper.getAttribute(IJBossServerConstants.WEB_PORT, ""));
		}
	}
	
	protected String findPort(IPath path) {
		XPathQuery query = XPathModel.getDefault().getQuery(server.getOriginal(), path);
		if(query!=null) {
			String result = query.getFirstResult();
			if( result != null ) {
				return result;
			}
		}
	return "-1";
}

	
	protected void createUI(Composite parent) {
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED|ExpandableComposite.TITLE_BAR);
		section.setText(Messages.EditorServerPorts);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		
		Composite composite = toolkit.createComposite(section);
		composite.setLayout(new FormLayout());
		Label description = new Label(composite, SWT.NONE);
		description.setText(Messages.EditorServerPortsDescription);

		Composite jndiChild = createJNDIUI(composite);
		Composite webChild = createWebUI(composite);
		
		FormData data = new FormData();
		data.top = new FormAttachment(0,5);
		description.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(description, 5);
		jndiChild.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(jndiChild, 5);
		webChild.setLayoutData(data);

		toolkit.paintBordersFor(composite);
		section.setClient(composite);
	}
	
	protected Composite createJNDIUI(Composite composite) {
		Composite child = new Composite(composite, SWT.NONE);
		child.setLayout(new FormLayout());
		jndiLabel = new Label(child, SWT.NONE);
		jndiText = new Text(child, SWT.DEFAULT);
		jndiDetect = new Button(child, SWT.CHECK);
		jndiDetectCombo = new Combo(child, SWT.DEFAULT);
		
		FormData data = new FormData();
		data.left = new FormAttachment(0,5);
		data.top = new FormAttachment(0,5);
		jndiLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(jndiLabel,5);
		data.right = new FormAttachment(jndiLabel, 150);
		data.top = new FormAttachment(0,5);
		jndiText.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(jndiText,5);
		data.top = new FormAttachment(0,5);
		jndiDetect.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(jndiDetect,5);
		data.top = new FormAttachment(0,5);
		jndiDetectCombo.setLayoutData(data);
		
		jndiLabel.setText(Messages.EditorJNDIPort);
		jndiDetect.setText(Messages.EditorAutomaticallyDetectPort);
		jndiDetectCombo.setItems(getXPathStrings());
		return child;
	}
	
	protected Composite createWebUI(Composite composite) {
		Composite child = new Composite(composite, SWT.NONE);
		child.setLayout(new FormLayout());
		webLabel = new Label(child, SWT.NONE);
		webText = new Text(child, SWT.DEFAULT);
		webDetect = new Button(child, SWT.CHECK);
		webDetectCombo = new Combo(child, SWT.DEFAULT);
		
		FormData data = new FormData();
		data.left = new FormAttachment(0,5);
		data.top = new FormAttachment(0,5);
		webLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(webLabel,5);
		data.right = new FormAttachment(webLabel, 150);
		data.top = new FormAttachment(0,5);
		webText.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(webText,5);
		data.top = new FormAttachment(0,5);
		webDetect.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(webDetect,5);
		data.top = new FormAttachment(0,5);
		webDetectCombo.setLayoutData(data);
		
		webLabel.setText(Messages.EditorWebPort);
		webDetect.setText(Messages.EditorAutomaticallyDetectPort);
		webDetectCombo.setItems(getXPathStrings());
		return child;
	}
	
	protected String[] getXPathStrings() {
		ArrayList<String> list = new ArrayList<String>();
		XPathCategory[] categories = XPathModel.getDefault().getCategories(server.getOriginal());
		for( int i = 0; i < categories.length; i++ ) {
			XPathQuery[] queries = categories[i].getQueries();
			for( int j = 0; j < queries.length; j++ ) {
				list.add(categories[i].getName() + IPath.SEPARATOR + queries[j].getName());
			}
		}
		return (String[]) list.toArray(new String[list.size()]);
	}
	
	protected Listener jndiListener, webListener;
	
	protected void addListeners() {
		jndiListener = new Listener() {
			public void handleEvent(Event event) {
				execute(new ChangeJNDICommand(server));
			}
		};
		jndiText.addListener(SWT.Modify, jndiListener);
		jndiDetect.addListener(SWT.Selection, jndiListener);
		jndiDetectCombo.addListener(SWT.Modify, jndiListener);
		
		webListener = new Listener() {
			public void handleEvent(Event event) {
				execute(new ChangeWebCommand(server));
			}
		};
		webText.addListener(SWT.Modify, webListener);
		webDetect.addListener(SWT.Selection, webListener);
		webDetectCombo.addListener(SWT.Modify, webListener);
		
	}

	public class ChangeJNDICommand extends SetPortCommand {
		public ChangeJNDICommand(IServerWorkingCopy server) {
			super(server, Messages.EditorChangeJNDICommandName,  
					IJBossServerConstants.JNDI_PORT, IJBossServerConstants.JNDI_PORT_DETECT,
					IJBossServerConstants.JNDI_PORT_DETECT_XPATH, IJBossServerConstants.JNDI_PORT_DEFAULT_XPATH,
					jndiText, jndiDetect, jndiDetectCombo, jndiListener);
		}
	}

	public class ChangeWebCommand extends SetPortCommand {
		public ChangeWebCommand(IServerWorkingCopy server) {
			super(server, Messages.EditorChangeWebCommandName,  
					IJBossServerConstants.WEB_PORT, IJBossServerConstants.WEB_PORT_DETECT,
					IJBossServerConstants.WEB_PORT_DETECT_XPATH, IJBossServerConstants.WEB_PORT_DEFAULT_XPATH,
					webText, webDetect, webDetectCombo, webListener);
		}
	}

	
	public class SetPortCommand extends ServerCommand {
		String textAttribute, overrideAttribute, overridePathAttribute;
		String preText, prePath, defaultPath;
		boolean preOverride;
		Text text;
		Button button;
		Combo combo;
		Listener listener;
		public SetPortCommand(IServerWorkingCopy server, String name, 
				String textAttribute, String overrideAttribute, String overridePathAttribute,
				String pathDefault, Text text, Button button, Combo xpath, Listener listener) {
			super(server, name);
			this.textAttribute = textAttribute;
			this.overrideAttribute = overrideAttribute;
			this.overridePathAttribute = overridePathAttribute;
			this.defaultPath = pathDefault;
			this.text = text;
			this.button = button;
			this.combo = xpath;
			this.listener = listener;
		}

		public void execute() {
			preText = helper.getAttribute(textAttribute, (String)null);
			prePath = helper.getAttribute(overridePathAttribute, (String)defaultPath);
			preOverride = helper.getAttribute(overrideAttribute, false);
			helper.setAttribute(textAttribute, text.getText());
			helper.setAttribute(overrideAttribute, button.getSelection());
			helper.setAttribute(overridePathAttribute, combo.getText());
			
			text.setEnabled(!button.getSelection());
			text.setEditable(!button.getSelection());
			combo.setEnabled(button.getSelection());
			if( button.getSelection() ) {
				text.removeListener(SWT.Modify, listener);
				text.setText(findPort(new Path(combo.getText())));
				text.addListener(SWT.Modify, listener);
			}
		}
		
		public void undo() {
			// set new values
			helper.setAttribute(textAttribute, preText);
			helper.setAttribute(overrideAttribute, preOverride);
			helper.setAttribute(overridePathAttribute, prePath);

			// update ui
			combo.removeListener(SWT.Modify, listener);
			text.removeListener(SWT.Modify, listener);
			button.removeListener(SWT.Selection, listener);

			button.setSelection(preOverride);
			text.setText(preText == null ? "" : preText);
			int ind = combo.indexOf(prePath);
			if( ind == -1 )
				combo.clearSelection();
			else
				combo.select(ind);

			text.setEnabled(!preOverride);
			text.setEditable(!preOverride);
			combo.setEnabled(preOverride);

			combo.addListener(SWT.Modify, listener);
			button.addListener(SWT.Selection, listener);
			text.addListener(SWT.Modify, listener);		
		}
	}

}
