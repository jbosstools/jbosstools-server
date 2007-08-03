package org.jboss.ide.eclipse.as.core.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.server.core.EJBBean;
import org.eclipse.jst.server.core.JndiLaunchable;
import org.eclipse.jst.server.core.JndiObject;
import org.eclipse.jst.server.core.Servlet;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.eclipse.wst.server.core.model.LaunchableAdapterDelegate;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.eclipse.wst.server.core.util.HttpLaunchable;
import org.eclipse.wst.server.core.util.WebResource;
import org.jboss.ide.eclipse.as.core.server.JBossServer;

public class JBossLaunchAdapter extends LaunchableAdapterDelegate {

	private static final String JAVA_NAMING_PROVIDER_URL_PROPKEY = "java.naming.provider.url"; //$NON-NLS-1$
	private static final String JAVA_NAMING_FACTORY_INITIAL_PROPKEY = "java.naming.factory.initial"; //$NON-NLS-1$
	public JBossLaunchAdapter() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * @see ILaunchableAdapterDelegate#getLaunchable(IServer, IModuleObject)
	 */
	public Object getLaunchable(IServer server, IModuleArtifact moduleObject) {
		ServerDelegate delegate = (ServerDelegate)server.loadAdapter(ServerDelegate.class,null);
		if (!(delegate instanceof JBossServer ))
			return null;
		if ((moduleObject instanceof Servlet) ||(moduleObject instanceof WebResource))
            return prepareHttpLaunchable(moduleObject, delegate);
		
        if((moduleObject instanceof EJBBean) || (moduleObject instanceof JndiObject))
            return prepareJndiLaunchable(moduleObject,delegate);
		return null;
	}

    private Object prepareJndiLaunchable(IModuleArtifact moduleObject, ServerDelegate delegate) {
        JndiLaunchable launchable = null;
        JBossServer server = (JBossServer)delegate;
        IPath p = new Path(server.getConfigDirectory()).append("jndi.properties");
        Properties props = new Properties();
        try  {
	        props.load(new FileInputStream(p.toFile()));
        } catch( IOException ioe ) {
            props.put(JAVA_NAMING_FACTORY_INITIAL_PROPKEY, "org.jnp.interfaces.NamingContextFactory");
            props.put(JAVA_NAMING_PROVIDER_URL_PROPKEY,"org.jboss.naming:org.jnp.interfaces");
        }
	
        if(moduleObject instanceof EJBBean) {
            EJBBean bean = (EJBBean)moduleObject;
            launchable = new JndiLaunchable(props,bean.getJndiName());
        }
        if(moduleObject instanceof JndiObject)  {
            JndiObject jndi = (JndiObject)moduleObject;
            launchable = new JndiLaunchable(props,jndi.getJndiName());
        }
        return launchable;
    }

    /**
     * @param moduleObject
     * @param delegate
     * @return object
     */
    private Object prepareHttpLaunchable(IModuleArtifact moduleObject, ServerDelegate delegate) {
        try {
			URL url = ((IURLProvider) delegate).getModuleRootURL(moduleObject.getModule());

			if (moduleObject instanceof Servlet) {
				Servlet servlet = (Servlet) moduleObject;
				if (servlet.getAlias() != null) {
					String path = servlet.getAlias();
					if (path.startsWith("/")) //$NON-NLS-1$
						path = path.substring(1);
					url = new URL(url, path);
				} else
					url = new URL(url, servlet.getName()); //$NON-NLS-1$
			} else if (moduleObject instanceof WebResource) {
				WebResource resource = (WebResource) moduleObject;
				String path = resource.getPath().toString();
				if (path != null && path.startsWith("/") && path.length() > 0) //$NON-NLS-1$
					path = path.substring(1);
				if (path != null && path.length() > 0)
					url = new URL(url, path);
			} 
			return new HttpLaunchable(url);
		} catch (Exception e) {
			return null;
		}
    }

}
