package org.jboss.ide.eclipse.archives.webtools.filesets.vcf;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.ComponentcorePackage;
import org.eclipse.wst.common.componentcore.internal.DependencyType;
import org.eclipse.wst.common.componentcore.internal.ReferencedComponent;
import org.eclipse.wst.common.componentcore.internal.impl.PlatformURLModuleConnection;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.jboss.ide.eclipse.as.wtp.core.vcf.IReferenceResolver;
import org.jboss.ide.eclipse.as.wtp.core.vcf.OutputFoldersVirtualComponent;

public class FilesetComponentResolver implements IReferenceResolver {
	public static final String FILESET_SEGMENT = "org.jboss.ide.eclipse.archives.webtools.filesets.vcf.FilesetComponentResolver"; //$NON-NLS-1$
	public static final String FILESET_PROTOCOL = PlatformURLModuleConnection.MODULE_PROTOCOL
								+IPath.SEPARATOR+ FILESET_SEGMENT + IPath.SEPARATOR;
	public boolean canResolve(IVirtualComponent context,
			ReferencedComponent referencedComponent) {
		URI handle = referencedComponent.getHandle();
		String s = handle.toString();
		if(s.startsWith(FILESET_PROTOCOL))
			return true;
		return false;
	}

	public boolean canResolve(IVirtualReference reference) {
		IVirtualComponent vc = reference.getReferencedComponent();
		if( vc instanceof WorkspaceFilesetVirtualComponent)
			return true;
		return false;
	}

	public IVirtualReference resolve(IVirtualComponent context,
			ReferencedComponent referencedComponent) {
		IProject p = context.getProject();
		URI uri = referencedComponent.getHandle();
		String main = uri.segment(1);
		String[] split = main.split("&"); //$NON-NLS-1$
		String path, includes, excludes;
		path = includes = excludes = null;
		try {
			for( int i = 0; i < split.length; i++ ) {
				boolean hasEquals = split[i].contains("="); //$NON-NLS-1$
				String pre = split[i].substring(0, split[i].indexOf("=")); //$NON-NLS-1$
				String post = split[i].substring(split[i].indexOf("=") + 1); //$NON-NLS-1$
				post = URLDecoder.decode(post, "UTF-8"); //$NON-NLS-1$
				if( "path".equals(pre))  //$NON-NLS-1$
					path = post;
				else if( "includes".equals(pre)) //$NON-NLS-1$
					includes = post;
				else if( "excludes".equals(pre)) //$NON-NLS-1$
					excludes = post;
			}
		} catch( UnsupportedEncodingException uee) {}
		WorkspaceFilesetVirtualComponent comp = new WorkspaceFilesetVirtualComponent(p, context, path);
		comp.setIncludes(includes);
		comp.setExcludes(excludes);
		IVirtualReference ref = ComponentCore.createReference(context, comp);
		ref.setArchiveName(referencedComponent.getArchiveName());
		ref.setRuntimePath(referencedComponent.getRuntimePath());
		ref.setDependencyType(referencedComponent.getDependencyType().getValue());
		return ref;
	}

	public ReferencedComponent resolve(IVirtualReference reference) {
		IVirtualComponent vc = reference.getReferencedComponent();
		WorkspaceFilesetVirtualComponent fsvc = (WorkspaceFilesetVirtualComponent)vc;
		String path = fsvc.getRootFolderPath();
		String includes = fsvc.getIncludes();
		String excludes = fsvc.getExcludes();
		try {
			String url = "path=" + URLEncoder.encode(path, "UTF-8");  //$NON-NLS-1$//$NON-NLS-2$
			url += "&includes=" + URLEncoder.encode(includes, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			url += "&excludes=" + URLEncoder.encode(excludes, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			
			ReferencedComponent rc = ComponentcorePackage.eINSTANCE.getComponentcoreFactory().createReferencedComponent();
			rc.setArchiveName(reference.getArchiveName());
			rc.setRuntimePath(reference.getRuntimePath());
			String tmp = fsvc.getId();
			rc.setHandle(URI.createURI(fsvc.getId() + url));
			rc.setDependencyType(DependencyType.CONSUMES_LITERAL);
			return rc;

		} catch( UnsupportedEncodingException uee) {}
		int x = 5;
		return null;
	}

}
