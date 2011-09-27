package org.jboss.ide.eclipse.as.openshift.core;


public interface IDomain {

	public abstract String getNamespace() throws OpenshiftException;

	public abstract String getRhcDomain() throws OpenshiftException;

}