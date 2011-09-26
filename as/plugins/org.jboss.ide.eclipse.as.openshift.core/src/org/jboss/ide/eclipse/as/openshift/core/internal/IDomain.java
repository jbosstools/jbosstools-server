package org.jboss.ide.eclipse.as.openshift.core.internal;

import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;

public interface IDomain {

	public abstract String getNamespace() throws OpenshiftException;

	public abstract String getRhcDomain() throws OpenshiftException;

}