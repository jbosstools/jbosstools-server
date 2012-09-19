package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.IServerModule;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;

public class ModulePublishDecorator implements ILightweightLabelDecorator {

	public void addListener(ILabelProviderListener listener) {
	}
	public void dispose() {
	}
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}
	public void removeListener(ILabelProviderListener listener) {
	}
	public void decorate(Object element, IDecoration decoration) {
		if( recentPublishFailed(element)) {
			ImageDescriptor id = JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.ERROR_MARKER);
			decoration.addOverlay(id, IDecoration.BOTTOM_LEFT);
		}
	}
	
	protected boolean recentPublishFailed(Object element) {
		if( element instanceof IServerModule ) {
			IServer s = ((IServerModule)element).getServer();
			IModule[] m = ((IServerModule)element).getModule();
			String name = m[m.length-1].getName();
			if( s == null )
				return false;
			IStatus[] all = ServerLogger.getDefault().getLog(s);
			
			// This really isn't the best. But I need string comparisons to know it's the same module
			// I can compare on status code like IEventCodes.JST_PUB_FULL_FAIL but this won't tell me what module
			// For now I will stick with this method. 
			for( int i = all.length-1; i >= 0; i-- ) {
				String success = NLS.bind(Messages.ModulePublished, name);
				if(all[i].getMessage().equals(success))
					return false;
				String fail = NLS.bind(Messages.FullPublishFail, name);
				if( all[i].getMessage().equals(fail))
					return true;
			}
		}
		return false;
	}
}
