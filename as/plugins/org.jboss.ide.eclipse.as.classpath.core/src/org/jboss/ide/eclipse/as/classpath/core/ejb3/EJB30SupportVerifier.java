package org.jboss.ide.eclipse.as.classpath.core.ejb3;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.classpath.core.runtime.WebtoolsProjectJBossClasspathContainerInitializer.WebtoolsProjectJBossClasspathContainer;
import org.jboss.ide.eclipse.as.core.runtime.server.AbstractJBossServerRuntime;

public class EJB30SupportVerifier {
	public static boolean verify(IRuntime rt) {
		AbstractJBossServerRuntime ajbsr = (AbstractJBossServerRuntime)rt.loadAdapter(AbstractJBossServerRuntime.class, null);
		//		 i refuse to verify. if they say they support, believe them
		if( ajbsr == null ) return true;  

		// one of ours. verify
		IPath path = new Path("org.jboss.ide.eclipse.as.core.runtime.ProjectInitializer");
		path = path.append(rt.getId()).append("jst.ejb").append("3.0");

		WebtoolsProjectJBossClasspathContainer container =
			new WebtoolsProjectJBossClasspathContainer(path);
		IClasspathEntry[] entries = container.getClasspathEntries();
		if( entries.length == 0 ) return false;
		IPath p;
		for( int i = 0; i < entries.length; i++ ) {
			p = entries[i].getPath();
			if( !p.toFile().exists())
				return false;
		}
		return true;
	}
}
