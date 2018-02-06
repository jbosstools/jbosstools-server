package org.jboss.ide.eclipse.as.reddeer.server.editor;

import org.hamcrest.Matcher;
import org.eclipse.core.runtime.Platform;
import org.eclipse.reddeer.swt.impl.browser.InternalBrowser;
import org.eclipse.reddeer.workbench.impl.editor.AbstractEditor;

/**
 * Represents a special editor that contains only browser. 
 * 
 * @author Lucia Jelinkova
 *
 */
public abstract class AbstractEditorWithBrowser extends AbstractEditor {

	private InternalBrowser browser;
	
	public AbstractEditorWithBrowser(Matcher<String> title) {
		super(title);
	}
	
	public String getText(){
		activate();
		String browserText;
		if (Platform.getOS().startsWith(Platform.OS_WIN32)) {
			browserText = getBrowser().getText();
		} else {
			// Workaround for webkit issues with method browser.getText(), e.g.
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=514719
			browserText = (String) getBrowser().evaluate("return document.documentElement.innerHTML;");
		}	
		return browserText;
	}
	
	public void refresh(){
		activate();
		getBrowser().refresh();
	}
	
	private InternalBrowser getBrowser() {
		if (browser == null){
			activate();
			browser = new InternalBrowser(this);
		}
		return browser;
	}
}
