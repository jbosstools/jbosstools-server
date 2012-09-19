/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.server.IJBoss6Server;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.util.ExpressionResolverUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.dialogs.ChangePortDialog;
import org.jboss.ide.eclipse.as.ui.dialogs.ChangePortDialog.ChangePortDialogInfo;

/**
 *
 * @author rob Stryker (rob.stryker@redhat.com)
 *
 */
public class PortSection extends ServerEditorSection {

	private ArrayList<IPortEditorExtension> sectionList = new ArrayList<IPortEditorExtension>();
	protected ServerAttributeHelper helper;
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		helper = new ServerAttributeHelper(server.getOriginal(), server);
		String serverTypeId = server.getServerType().getId();
		if (sectionList.size() <= 0) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] cf = registry.getConfigurationElementsFor(
					JBossServerUIPlugin.PLUGIN_ID, "ServerEditorPortSection"); //$NON-NLS-1$
			for (int i = 0; i < cf.length; i++) {
				try {
					String approvedTypes = cf[i].getAttribute("serverIds"); //$NON-NLS-1$
					if( serverTypeMatches(serverTypeId, approvedTypes)) {
						Object o = cf[i].createExecutableExtension("class"); //$NON-NLS-1$
						if (o != null && o instanceof IPortEditorExtension)
							sectionList.add((IPortEditorExtension) o);
					}
				} catch (CoreException ce) { 
					/* silently ignore */
				}
			}
		}
	}

	protected boolean serverTypeMatches(String serverType, String approvedTypes) {
		if( approvedTypes == null || approvedTypes.equals("")) //$NON-NLS-1$
			return true;
		String[] split = approvedTypes.split(","); //$NON-NLS-1$
		for( int i = 0; i < split.length; i++ )
			if( split[i].equals(serverType))
				return true;
		return false;
	}
	
	public void createSection(Composite parent) {
		super.createSection(parent);
		createUI(parent);
	}

	public static interface IPortEditorExtension {
		public void setServerAttributeHelper(ServerAttributeHelper helper);
		public void setSection(PortSection section);
		public void createControl(Composite parent);
		public String getValue();
	}

	public static class JNDIPortEditorExtension extends PortEditorExtension {
		public JNDIPortEditorExtension() {
			super(Messages.EditorJNDIPort, 
					IJBossServerConstants.JNDI_PORT_DETECT_XPATH,
					IJBossServerConstants.JNDI_PORT_DETECT,
					IJBossServerConstants.JNDI_PORT,
					IJBossServerConstants.JNDI_PORT_DEFAULT_XPATH,
					IJBossToolingConstants.JNDI_DEFAULT_PORT,
					Messages.EditorChangeJNDICommandName);
		}
	}

	public static class WebPortEditorExtension extends PortEditorExtension {
		public WebPortEditorExtension() {
			super(Messages.EditorWebPort, 
					IJBossServerConstants.WEB_PORT_DETECT_XPATH,
					IJBossServerConstants.WEB_PORT_DETECT,
					IJBossServerConstants.WEB_PORT,
					IJBossServerConstants.WEB_PORT_DEFAULT_XPATH, 
					IJBossToolingConstants.JBOSS_WEB_DEFAULT_PORT,
					Messages.EditorChangeWebCommandName);
		}
	}

	public static class JBoss6JMXRMIPortEditorExtension extends PortEditorExtension {
		public JBoss6JMXRMIPortEditorExtension() {
			super(Messages.EditorJMXRMIPort, 
					IJBoss6Server.JMX_RMI_PORT_DETECT_XPATH,
					IJBoss6Server.JMX_RMI_PORT_DETECT,
					IJBoss6Server.JMX_RMI_PORT,
					IJBoss6Server.JMX_RMI_PORT_DEFAULT_XPATH,
					IJBoss6Server.JMX_RMI_DEFAULT_PORT,
					Messages.EditorChangeJMXRMICommandName);
		}
	}

	public static class JBoss7ManagementPortEditorExtension extends PortEditorExtension {
		public JBoss7ManagementPortEditorExtension() {
			super(Messages.EditorAS7ManagementPort, 
					IJBossToolingConstants.AS7_MANAGEMENT_PORT_DETECT_XPATH,
					IJBossToolingConstants.AS7_MANAGEMENT_PORT_DETECT,
					IJBossToolingConstants.AS7_MANAGEMENT_PORT,
					IJBossToolingConstants.AS7_MANAGEMENT_PORT_DEFAULT_XPATH,
					IJBossToolingConstants.AS7_MANAGEMENT_PORT_DEFAULT_PORT,
					Messages.EditorChangeAS7ManagementCommandName);
		}
	}


	public static abstract class PortEditorExtension implements IPortEditorExtension {
		protected Button detect;
		protected Text text;
		protected Label label;
		protected Link link;
		protected String labelText, currentXPathKey, detectXPathKey, overrideValueKey, defaultXPath;
		protected String currentXPath, changeValueCommandName;
		protected ServerAttributeHelper helper;
		protected Listener listener;
		protected PortSection section;
		protected int defaultValue;
		private ControlDecoration decoration;
		
		public PortEditorExtension(String labelText, String currentXPathKey, 
				String detectXPathKey, String overrideValueKey, String defaultXPath,
				int defaultValue,
				String changeValueCommandName) {
			this.labelText = labelText;
			this.currentXPathKey = currentXPathKey;
			this.detectXPathKey = detectXPathKey;
			this.overrideValueKey = overrideValueKey;
			this.defaultXPath = defaultXPath;
			this.changeValueCommandName = changeValueCommandName;
			this.defaultValue = defaultValue;
		}
		public void setServerAttributeHelper(ServerAttributeHelper helper) {
			this.helper = helper;
		}
		public void setSection(PortSection section) {
			this.section = section;
		}
		public void createControl(Composite parent) {
			createUI(parent);
			initialize();
			addListeners();
			
			decoration = new ControlDecoration(text,
					SWT.LEFT | SWT.TOP);
			FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
					.getFieldDecoration(FieldDecorationRegistry.DEC_WARNING);
			decoration.setImage(fieldDecoration.getImage());
			validate();
		}

		protected void createUI(Composite parent) {
			label = new Label(parent, SWT.NONE);
			text = new Text(parent, SWT.SINGLE | SWT.BORDER);
			detect = new Button(parent, SWT.CHECK);
			link = new Link(parent, SWT.NONE);
			
			GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(label);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).minSize(80, 10).grab(true, false).applyTo(text);
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(detect);
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(link);
			
			label.setText(labelText);
			detect.setText(Messages.EditorAutomaticallyDetectPort);
			link.setText("<a href=\"\">" + Messages.Configure + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
			
			text.addVerifyListener(new VerifyListener() {
				public void verifyText(VerifyEvent e) {
					if( e.text == null || e.text.equals(""))
						return;
					try {
						Integer i = Integer.parseInt(e.text);
					} catch( NumberFormatException nfe ) {
						e.doit = false;
					}
				}
			});
			
			
		}
		protected void initialize() {
			boolean shouldDetect = helper.getAttribute(detectXPathKey, true);
			detect.setSelection(shouldDetect);
			detect.setEnabled(defaultXPath != null);
			link.setEnabled(shouldDetect);
			text.setEnabled(!shouldDetect);
			text.setEditable(!shouldDetect);
			currentXPath = helper.getAttribute(currentXPathKey, defaultXPath);
			if( shouldDetect ) {
				text.setText(findPortWithDefault(helper.getServer(), new Path(currentXPath), defaultValue));
			} else
				text.setText(helper.getAttribute(overrideValueKey, "")); //$NON-NLS-1$
		}
		protected void addListeners() {
			listener = new Listener() {
				public void handleEvent(Event event) {
					section.execute(getCommand());
				}
			};
			text.addListener(SWT.Modify, listener);
			detect.addListener(SWT.Selection, listener);
			link.addListener(SWT.Selection, createLinkListener());
		}

		protected Listener createLinkListener() {
			return new Listener() {
				public void handleEvent(Event event) {
					ChangePortDialog dialog = getDialog();
					int result = dialog.open();
					if( result == Dialog.OK) {
						currentXPath = dialog.getSelection();
						section.execute(getCommand());
					}
					if( dialog.isModified() ) {
						initialize();
						validate();
					}
					text.setFocus();
				}
			};
		}
		public ChangePortDialog getDialog() {
			return new ChangePortDialog(section.getShell(), getDialogInfo());
		}
		public ServerCommand getCommand() {
			return new SetPortCommand(helper.getWorkingCopy(), helper, changeValueCommandName,
					overrideValueKey, detectXPathKey,currentXPathKey, defaultXPath, this);
		}
		protected ChangePortDialogInfo getDialogInfo() {
			ChangePortDialogInfo info = new ChangePortDialogInfo();
			info.port = labelText;
			info.defaultValue = defaultXPath;
			info.server = helper.getWorkingCopy().getOriginal();
			info.currentXPath = currentXPath;
			return info;
		}
		public String getValue() {
			return text.getText();
		}
		public void validate() {
			decoration.hide();
			String v = null;
			String errorText;
			if( detect.getSelection()) {
				v = findPort(helper.getServer(), new Path(defaultXPath));
				errorText = "This port cannot be automatically located. A default value is being displayed";
			} else {
				v = text.getText();
				errorText = "The empty string is not a valid port.";
			}
			if( "".equals(v)) {
				decoration.setDescriptionText(errorText);
				decoration.show();
			}
		}
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
		FormData data = new FormData();
		data.top = new FormAttachment(0,5);
		data.left = new FormAttachment(0,5);
		description.setLayoutData(data);

		addUIAdditions(composite, description);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
	}

	private void addUIAdditions(Composite parent, Control top) {
		IPortEditorExtension[] extensions = (IPortEditorExtension[]) sectionList.toArray(new IPortEditorExtension[sectionList.size()]);

		FormData data;
		Composite wrapper = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(6, 10).numColumns(4).applyTo(wrapper);
		data = new FormData();
		data.top = new FormAttachment(top,0);
		data.left = new FormAttachment(0,0);
		data.right = new FormAttachment(100,-5);
		wrapper.setLayoutData(data);
		top = null;
		for( int i = 0; i < extensions.length; i++ ) {
			extensions[i].setServerAttributeHelper(helper);
			extensions[i].setSection(this);
			extensions[i].createControl(wrapper);
		}
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

	public static class SetPortCommand extends ServerCommand {
		ServerAttributeHelper helper;
		String textAttribute, overrideAttribute, overridePathAttribute;
		String preText, prePath, defaultPath;
		boolean preOverride;
		Text text;
		Button button;
		Listener listener;
		String xpath;
		Link link;
		PortSection pSection;
		int defVal;
		PortEditorExtension ext;
		public SetPortCommand(IServerWorkingCopy server, ServerAttributeHelper helper, String name,
				String textAttribute, String overrideAttribute, String overridePathAttribute,
				String pathDefault, PortEditorExtension ext) { //Text text, Button button, String xpath, Listener listener) {
			super(server, name);
			this.helper = helper;
			this.textAttribute = textAttribute;
			this.overrideAttribute = overrideAttribute;
			this.overridePathAttribute = overridePathAttribute;
			this.defaultPath = pathDefault;
			this.text = ext.text;
			this.button = ext.detect;
			this.listener = ext.listener;
			this.xpath = ext.currentXPath;
			this.link = ext.link;
			this.pSection = ext.section;
			this.defVal = ext.defaultValue;
			this.ext = ext;
		}

		public void execute() {
			preText = helper.getAttribute(textAttribute, (String)null);
			if( preText == null )
				preText = text.getText();
			prePath = helper.getAttribute(overridePathAttribute, (String)defaultPath);
			preOverride = helper.getAttribute(overrideAttribute, true);
			helper.setAttribute(textAttribute, text.getText());
			helper.setAttribute(overrideAttribute, button.getSelection());
			link.setEnabled(button.getSelection());
			helper.setAttribute(overridePathAttribute, xpath);

			text.setEnabled(!button.getSelection());
			text.setEditable(!button.getSelection());
			if( button.getSelection() ) {
				text.removeListener(SWT.Modify, listener);
				text.setText(findPortWithDefault(helper.getServer(), new Path(xpath), this.defVal));
				text.addListener(SWT.Modify, listener);
			}
			validate();
		}

		public void undo() {
			// set new values
			helper.setAttribute(textAttribute, preText);
			helper.setAttribute(overrideAttribute, preOverride);
			link.setEnabled(preOverride);
			helper.setAttribute(overridePathAttribute, prePath);
			
			// update ui
			text.removeListener(SWT.Modify, listener);
			button.removeListener(SWT.Selection, listener);

			button.setSelection(preOverride);
			text.setText(preText == null ? "" : preText); //$NON-NLS-1$
			text.setEnabled(!preOverride);
			text.setEditable(!preOverride);
			button.addListener(SWT.Selection, listener);
			text.addListener(SWT.Modify, listener);
			validate();
		}
		
		private void validate() {
			ext.validate();
		}
	}

	protected static String findPortWithDefault(IServer server, IPath path, int defaultValue) {
		String s = findPort(server, path);
		if( s.equals("")) { //$NON-NLS-1$
			s = new Integer(defaultValue).toString();
		} 
		return s;
	}
	
	protected static String findPort(IServer server, IPath path) {
		XPathQuery query = XPathModel.getDefault().getQuery(server, path);
		String result = ""; //$NON-NLS-1$
		if(query!=null) {
			try {
				query.refresh();
				result = query.getFirstResult();
				result = result == null ? "" : result; //$NON-NLS-1$
				result = ExpressionResolverUtil.safeReplaceProperties(result);
				return new Integer(Integer.parseInt(result)).toString();
			} catch(NumberFormatException nfe) {
				/* Intentionally fall through, return non-replaced string */
			} catch( IllegalStateException ise ) {
				/* This will occur of the xpath is malformed. 
				 * Fall through and return the empty string
				 */
			}
		}
		return result;
	}
}
