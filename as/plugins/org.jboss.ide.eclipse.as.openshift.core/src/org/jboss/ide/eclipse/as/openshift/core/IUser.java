package org.jboss.ide.eclipse.as.openshift.core;

import java.util.Collection;

public interface IUser {

	public abstract String getRhlogin();

	public abstract String getPassword();

	public abstract Domain getDomain() throws OpenshiftException;

	public abstract ISSHPublicKey getSshKey() throws OpenshiftException;

	public abstract Collection<Cartridge> getCartridges() throws OpenshiftException;

	public abstract Collection<Application> getApplications() throws OpenshiftException;

}