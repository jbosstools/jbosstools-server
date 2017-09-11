package org.jboss.ide.eclipse.as.reddeer.server.editor;

import org.jboss.ide.eclipse.as.reddeer.server.view.JBossServer;
import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.JBossRuntimeWizardPage;
import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.TaskWizard;
import org.eclipse.reddeer.core.matcher.WithLabelMatcher;
import org.eclipse.reddeer.eclipse.wst.server.ui.editor.ServerEditor;
import org.eclipse.reddeer.swt.impl.combo.LabeledCombo;
import org.eclipse.reddeer.swt.impl.spinner.DefaultSpinner;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.uiforms.impl.hyperlink.DefaultHyperlink;
import org.eclipse.reddeer.uiforms.impl.section.DefaultSection;

/**
 * Represents a server editor with entries specific for JBoss servers {@link JBossServer}
 * @author Lucia Jelinkova
 *
 */
public class JBossServerEditor extends ServerEditor {

	public JBossServerEditor(String title) {
		super(title);
	}

	public JBossServerLaunchConfiguration openLaunchConfiguration(){
		new DefaultHyperlink(this, "Open launch configuration").activate();
		return new JBossServerLaunchConfiguration(this);
	}
	
	public JBossRuntimeWizardPage editRuntimeEnvironment(){
		new DefaultHyperlink(this, "Runtime Environment:").activate();
		return new JBossRuntimeWizardPage(new TaskWizard());
	}

	public void setStartTimeout(int timeout){
		openSection("Timeouts");
		new DefaultSpinner(this, new WithLabelMatcher("Start (in seconds):")).setValue(timeout);
	}

	public void setStopTimeout(int timeout){
		openSection("Timeouts");
		new DefaultSpinner(this, new WithLabelMatcher("Stop (in seconds):")).setValue(timeout);
	}

	public String getStartupPoller(){
		return new LabeledCombo(this, "Startup Poller").getText();
	}

	public void setStartupPoller(String text){
		new LabeledCombo(this, "Startup Poller").setText(text);
	}

	public String getShutdownPoller(){
		return new LabeledCombo(this, "Shutdown Poller").getText();
	}

	public void setShutdownPoller(String text){
		new LabeledCombo(this, "Shutdown Poller").setText(text);
	}

	public String getWebPort(){
		return new LabeledText(this, "Web").getText();
	}

	public String getJNDIPort(){
		return new LabeledText(this, "JNDI").getText();
	}

	public String getJMXPort(){
		return new LabeledText(this, "JMX RMI").getText();
	}

	public String getManagementPort(){
		return new LabeledText(this, "Management").getText();
	}
	
	protected void openSection(final String title){
		new DefaultSection(this, title).setExpanded(true);
	}
}
