package org.jboss.ide.eclipse.as.ui.views.server;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.runtime.server.IServerPollerTimeoutListener;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class TwiddlePollerListener implements IServerPollerTimeoutListener {
	public static final String key = "org.jboss.ide.eclipse.as.ui.preferencepages.TwiddlePollerListener";
	public void serverTimedOut(IServer server, boolean expectedState) {
		if( expectedState && firstEverTimeout()) {
			IPreferenceStore store = JBossServerUIPlugin.getDefault().getPreferenceStore();
			store.setValue(key, 2);
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageBox box = new MessageBox(new Shell());
					box.setMessage("The Poller has timed out");
					box.setText("Twiddle Poller Timed Out");
					box.open();
				} 
			});
		}
	}
	
	protected boolean firstEverTimeout() {
		return true;
//		IPreferenceStore store = JBossServerUIPlugin.getDefault().getPreferenceStore();
//		int val = store.getInt(key);
//		if( val == 2 ) return false;
//		return true;
	}

}
