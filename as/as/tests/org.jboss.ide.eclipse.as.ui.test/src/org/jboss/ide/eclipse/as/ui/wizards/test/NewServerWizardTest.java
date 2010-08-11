package org.jboss.ide.eclipse.as.ui.wizards.test;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.jboss.ide.eclipse.as.ui.wizards.JBossServerWizardFragment;
import org.jboss.tools.test.util.JobUtils;
import org.jboss.tools.test.util.SwtUtils;
import org.jboss.tools.test.util.WorkbenchUtils;

public class NewServerWizardTest extends TestCase implements ILogListener {

	public static final String JBOSS_AS_4_2 = "JBoss AS 4.2";
	public static final String JBOSS_COMMUNITY = "JBoss Community";
	public static final String NEW_SERVER_WIZARD_ID = "org.eclipse.wst.server.ui.new.server";
	public static final String JBOSS_AS_CORE_RUNTIME_CONFIGURATION_NAME = "org.jboss.ide.eclipse.as.core.runtime.configurationName";
	public static final String PROPERTY_VM_TYPE_ID = "PROPERTY_VM_TYPE_ID";
	public static final String PROPERTY_VM_ID = "PROPERTY_VM_ID";
	public static final String JBOSS_PATH_PROP_NAME = "jbosstools.test.jboss.home.4.2";
	public static final String JBOSS_AS_RUNTIME_ID = "org.jboss.ide.eclipse.as.runtime.42";

	public NewServerWizardTest(String name) {
		super(name);
		 Platform.addLogListener(this);
	}

	public void testJbide3824() throws CoreException {
		
		String jbossPath = System.getProperty(JBOSS_PATH_PROP_NAME);
		if(jbossPath==null) {
			throw new IllegalArgumentException("Yo have to define " + JBOSS_PATH_PROP_NAME
					+ "system property which points to JBoss AS 4.2 home folder");
		}
		
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null, null, JBOSS_AS_RUNTIME_ID
				);
		if (runtimeTypes.length > 0) {
			IRuntimeWorkingCopy runtime = runtimeTypes[0].createRuntime(null, null);
			runtime.setLocation(new Path(JBOSS_PATH_PROP_NAME));
			runtime.setName("test");
			IVMInstall defaultVM = JavaRuntime.getDefaultVMInstall();
			((RuntimeWorkingCopy) runtime).setAttribute(PROPERTY_VM_ID,
					defaultVM.getId());
			((RuntimeWorkingCopy) runtime).setAttribute(PROPERTY_VM_TYPE_ID,
					defaultVM.getVMInstallType().getId());
			((RuntimeWorkingCopy) runtime).setAttribute(
					JBOSS_AS_CORE_RUNTIME_CONFIGURATION_NAME,
					"default");
				runtime.save(false, null);
		}
		
		IWizard newServerWizard = WorkbenchUtils.findWizardByDefId(NEW_SERVER_WIZARD_ID);
		
		WizardDialog dialog = new WizardDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				newServerWizard);
		dialog.setBlockOnOpen(false);
		dialog.open();
		Composite pageControl = (Composite)newServerWizard.getStartingPage().getControl();
		
		Tree tree = (Tree)SwtUtils.findControlByClass(pageControl, Tree.class);
		TreeItem[] items = tree.getItems();
		for (TreeItem treeItem : items) {
			if(treeItem.getText().equals(JBOSS_COMMUNITY)) {
				treeItem.clearAll(true);
				TreeItem jBossAS42 = new TreeItem(treeItem,SWT.NONE);
				jBossAS42.setText(JBOSS_AS_4_2);
				tree.setSelection(jBossAS42);
				JobUtils.delay(1000);
				dialog.showPage(newServerWizard.getStartingPage().getNextPage());
				JobUtils.delay(1000);
			}
		}
		dialog.close();
		newServerWizard = WorkbenchUtils.findWizardByDefId(NEW_SERVER_WIZARD_ID);
		dialog = new WizardDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				newServerWizard);
		dialog.setBlockOnOpen(false);
		dialog.open();
		pageControl = (Composite)newServerWizard.getStartingPage().getControl();
		tree = (Tree)SwtUtils.findControlByClass(pageControl, Tree.class);
		items = tree.getItems();
		for (TreeItem treeItem : items) {
			if(treeItem.getText().equals(JBOSS_COMMUNITY)) {
				treeItem.clearAll(true);
				TreeItem jBossAS42 = new TreeItem(treeItem,SWT.NONE);
				jBossAS42.setText(JBOSS_AS_4_2);
				tree.setSelection(jBossAS42);
				JobUtils.delay(1000);
				newServerWizard.performFinish();
				JobUtils.delay(1000);
			}
		}
		dialog.close();
		
	}

	public void logging(IStatus status, String plugin) {
		StringWriter out = new StringWriter();
		out.append(status.getMessage()).append('\n');
		if(status.getException()!=null) {
			status.getException().printStackTrace(new PrintWriter(out));
			if(out.toString().contains(JBossServerWizardFragment.class.getName())) {
				fail(out.toString());
			}
		}
	}
}
