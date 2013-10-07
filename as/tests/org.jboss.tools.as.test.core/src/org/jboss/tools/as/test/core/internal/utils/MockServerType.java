package org.jboss.tools.as.test.core.internal.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;

public class MockServerType implements IServerType {

	private String typeId;
	public MockServerType(String typeId) {
		this.typeId = typeId;
	}
	
	
	@Override
	public String getId() {
		return typeId;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRuntimeType getRuntimeType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasRuntime() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsLaunchMode(String launchMode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasServerConfiguration() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsRemoteHosts() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IServerWorkingCopy createServer(String id, IFile file,
			IRuntime runtime, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IServerWorkingCopy createServer(String id, IFile file,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}
	
}