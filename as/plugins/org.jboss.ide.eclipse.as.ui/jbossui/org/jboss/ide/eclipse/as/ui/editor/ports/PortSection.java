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
package org.jboss.ide.eclipse.as.ui.editor.ports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.tools.as.core.server.controllable.subsystems.internal.XPathsPortsController;
import org.jboss.tools.foundation.core.expressions.ExpressionResolver;

/**
 *
 * @author rob Stryker (rob.stryker@redhat.com)
 *
 */
public class PortSection extends ServerEditorSection {

	private ArrayList<IPortEditorExtension> sectionList = new ArrayList<IPortEditorExtension>();
	protected ServerAttributeHelper helper;
	private IPortOffsetProvider offsetProvider;
	
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		helper = new ServerAttributeHelper(server.getOriginal(), server);
		String serverTypeId = server.getServerType().getId();
		if (sectionList.size() <= 0) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] cf = registry.getConfigurationElementsFor(
					JBossServerUIPlugin.PLUGIN_ID, "ServerEditorPortSection"); //$NON-NLS-1$
			
			// get a list of approved
			ArrayList<IConfigurationElement> approved = new ArrayList<IConfigurationElement>();
			for (int i = 0; i < cf.length; i++) {
				String approvedTypes = cf[i].getAttribute("serverIds"); //$NON-NLS-1$
				if( serverTypeMatches(serverTypeId, approvedTypes)) {
					approved.add(cf[i]);
				}
			}
			
			// sort them based on weight
			Collections.sort(approved, new Comparator<IConfigurationElement>(){
				public int compare(IConfigurationElement o1, IConfigurationElement o2) {
					String weight1 = o1.getAttribute("weight");
					String weight2 = o2.getAttribute("weight");
					weight1 = (weight1 == null ? "100" : weight1);
					weight2 = (weight2 == null ? "100" : weight2);
					int w1, w2;
					try {
						w1 = Integer.parseInt(weight1);
					} catch( NumberFormatException nfe) {
						w1 = 100;
					}
					try {
						w2 = Integer.parseInt(weight2);
					} catch( NumberFormatException nfe) {
						w2 = 100;
					}
					return w1-w2;
				}
			});
			
			// Create the sections
			for (int i = 0; i < approved.size(); i++) {
				try {
						Object o = approved.get(i).createExecutableExtension("class"); //$NON-NLS-1$
						if (o != null && o instanceof IPortEditorExtension)
							sectionList.add((IPortEditorExtension) o);
						if( o != null && o instanceof IPortOffsetProvider) 
							offsetProvider = (IPortOffsetProvider)o;
				} catch (CoreException ce) { 
					/* silently ignore */
					ce.printStackTrace();
				}
			}
		}
	}
	
	public int getPortOffset() {
		return offsetProvider == null ? 0 : offsetProvider.getOffset();
	}
	
	public void offsetChanged() {
		Iterator<IPortEditorExtension> i = sectionList.iterator();
		IPortEditorExtension ext;
		while(i.hasNext()) {
			ext = i.next();
			if( !(ext instanceof IPortOffsetProvider)) {
				ext.refresh();
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

	
	public static class JNDIPortEditorExtension extends PortEditorXPathExtension {
		public JNDIPortEditorExtension() {
			super(Messages.EditorJNDIPort, 
					XPathsPortsController.JNDI_PORT_DETECT_XPATH,
					XPathsPortsController.JNDI_PORT_DETECT,
					XPathsPortsController.JNDI_PORT,
					XPathsPortsController.JNDI_PORT_DEFAULT_XPATH,
					XPathsPortsController.JNDI_DEFAULT_PORT,
					Messages.EditorChangeJNDICommandName);
		}
	}

	public static class WebPortEditorExtension extends PortEditorXPathExtension {
		public WebPortEditorExtension() {
			super(Messages.EditorWebPort, 
					XPathsPortsController.WEB_PORT_DETECT_XPATH,
					XPathsPortsController.WEB_PORT_DETECT,
					XPathsPortsController.WEB_PORT,
					XPathsPortsController.WEB_PORT_DEFAULT_XPATH, 
					XPathsPortsController.JBOSS_WEB_DEFAULT_PORT,
					Messages.EditorChangeWebCommandName);
		}
	}

	public static class WebPortSettingEditorExtension extends PortEditorExtension {
		public WebPortSettingEditorExtension() {
			super(Messages.EditorWebPort, 
					XPathsPortsController.WEB_PORT,
					XPathsPortsController.JBOSS_WEB_DEFAULT_PORT,
					Messages.EditorChangeWebCommandName);
		}
	}

	
	public static class JBoss6JMXRMIPortEditorExtension extends PortEditorXPathExtension {
		public JBoss6JMXRMIPortEditorExtension() {
			super(Messages.EditorJMXRMIPort, 
					XPathsPortsController.JMX_RMI_PORT_DETECT_XPATH,
					XPathsPortsController.JMX_RMI_PORT_DETECT,
					XPathsPortsController.JMX_RMI_PORT,
					XPathsPortsController.JMX_RMI_PORT_DEFAULT_XPATH,
					XPathsPortsController.JMX_RMI_DEFAULT_PORT,
					Messages.EditorChangeJMXRMICommandName);
		}
	}
	

	public static class JBoss7ManagementPortEditorExtension extends PortEditorXPathExtension {
		public JBoss7ManagementPortEditorExtension() {
			super(Messages.EditorAS7ManagementPort, 
					XPathsPortsController.AS7_MANAGEMENT_PORT_DETECT_XPATH,
					XPathsPortsController.AS7_MANAGEMENT_PORT_DETECT,
					XPathsPortsController.AS7_MANAGEMENT_PORT,
					XPathsPortsController.AS7_MANAGEMENT_PORT_DEFAULT_XPATH,
					XPathsPortsController.AS7_MANAGEMENT_PORT_DEFAULT_PORT,
					Messages.EditorChangeAS7ManagementCommandName);
		}
	}
	public static class JBoss7PortOffsetEditorExtension extends PortEditorXPathExtension implements IPortOffsetProvider {
		public JBoss7PortOffsetEditorExtension() {
			super(Messages.EditorAS7PortOffset, 
					XPathsPortsController.PORT_OFFSET_DETECT_XPATH,
					XPathsPortsController.PORT_OFFSET_DETECT,
					XPathsPortsController.PORT_OFFSET_KEY,
					XPathsPortsController.PORT_OFFSET_DEFAULT_XPATH,
					XPathsPortsController.PORT_OFFSET_DEFAULT_PORT,
					Messages.EditorChangeAS7ManagementCommandName);
		}
		protected void listenerEvent(Event event) {
			section.execute(getCommand());
			section.offsetChanged();
		}
		public int getOffset() {
			String v = text == null ? "" : text.getText();
			int i = "".equals(v) ? 0 : Integer.parseInt(v);
			return i;
		}
		@Override
		/* Do not let the offset apply to itself... that'd be crazy! */
		protected int discoverOffset() {
			return 0;
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

	protected static String findPortWithDefault(IServer server, IPath path, int defaultValue) {
		return findPortWithDefault(server, path, defaultValue, 0);
	}
	protected static String findPortWithDefault(IServer server, IPath path, int defaultValue, int offset) {
		String s = findPort(server, path, offset);
		if( s.equals("")) { //$NON-NLS-1$
			s = new Integer(defaultValue+offset).toString();
		} 
		return s;
	}
	
	protected static String findPort(IServer server, IPath path) {
		return findPort(server, path, 0);
	}
	protected static String findPort(IServer server, IPath path, int offset) {
		XPathQuery query = XPathModel.getDefault().getQuery(server, path);
		String result = ""; //$NON-NLS-1$
		if(query!=null) {
			try {
				query.refresh();
				result = query.getFirstResult();
				result = result == null ? "" : result; //$NON-NLS-1$
	    		result = new ExpressionResolver().resolveIgnoreErrors(result);
				return new Integer(Integer.parseInt(result)+offset).toString();
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
