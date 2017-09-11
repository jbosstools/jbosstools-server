package org.jboss.tools.as.ui.bot.itests.reddeer.util;

import org.eclipse.reddeer.eclipse.ui.browser.WebBrowserView;
import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.common.wait.AbstractWait;
import org.eclipse.reddeer.common.wait.TimePeriod;

/**
 * Waits until the active browser contains the specified text. 
 * 
 * @author Lucia Jelinkova
 *
 */
public class BrowserContainsTextCondition extends AbstractWaitCondition {

	private String text;

	private boolean refresh;

	private WebBrowserView browserView;

	public BrowserContainsTextCondition(String text) {
		this(text, false);
	}

	public BrowserContainsTextCondition(String text, boolean refresh) {
		this(null, text, refresh);
	}

	public BrowserContainsTextCondition(String url, String text, boolean refresh) {
		this.text = text;
		this.refresh = refresh;

		browserView = new WebBrowserView();
		browserView.open();
		AbstractWait.sleep(TimePeriod.DEFAULT);
		if (url != null){
			browserView.openPageURL(url);
		} 
	}

	@Override
	public boolean test() {
		if (refresh){
			browserView.refreshPage();
		}
		return browserView.getText().contains(text);
	}

	
	public void cleanup() {
		if( browserView != null ) {
			browserView.close();
		}
	}
	
	@Override
	public String description() {
		return "Browser should contain text: " + text + ", but contains: " + browserView.getText();
	}

}
