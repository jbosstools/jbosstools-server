package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.ide.eclipse.as.core.publishers.AbstractPublishMethod;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.IPublishCopyCallbackHandler;

public class MockPublishMethod extends AbstractPublishMethod {

	public static final String PUBLISH_METHOD_ID = "mock";
	public static final MockCopyCallbackHandler HANDLER = new MockCopyCallbackHandler();
	
	public IPublishCopyCallbackHandler getCallbackHandler(IPath path,
			IServer server) {
		return HANDLER;
	}

	public String getPublishDefaultRootFolder(IServer server) {
		return "/";
	}

	public String getPublishMethodId() {
		return PUBLISH_METHOD_ID;
	}

	public static class MockCopyCallbackHandler implements IPublishCopyCallbackHandler {
		public ArrayList<IPath> changed = new ArrayList<IPath>();
		public ArrayList<IPath> removed = new ArrayList<IPath>();
		
		public void reset() {
			changed.clear();
			removed.clear();
		}
		
		public IPath[] getRemoved() {
			return (IPath[]) removed.toArray(new IPath[removed.size()]);
		}

		public IPath[] getChanged() {
			return (IPath[]) changed.toArray(new IPath[changed.size()]);
		}

		public IStatus[] deleteResource(IPath path, IProgressMonitor monitor)
				throws CoreException {
			if( !removed.contains(path.makeRelative()))
				removed.add(path.makeRelative());
			return new IStatus[]{};
		}

		public IStatus[] makeDirectoryIfRequired(IPath dir,
				IProgressMonitor monitor) throws CoreException {
			if( !changed.contains(dir.makeRelative()))
				changed.add(dir.makeRelative());
			return new IStatus[]{};
		}

		private boolean shouldRestartModule = false;
		public boolean shouldRestartModule() {
			return shouldRestartModule;
		}
		public IStatus[] copyFile(IModuleFile mf, IPath path,
				IProgressMonitor monitor) throws CoreException {
			File file = PublishUtil.getFile(mf);
			shouldRestartModule |= PublishCopyUtil.checkRestartModule(file);
			if( !changed.contains(path.makeRelative()))
				changed.add(path.makeRelative());
			return new IStatus[]{};
		}
		
		public IStatus[] touchResource(IPath path) {
			if( !changed.contains(path.makeRelative()))
				changed.add(path.makeRelative());
			return new IStatus[]{};
		}
	}
}
