package org.jboss.ide.eclipse.as.ui.viewproviders;

import java.util.Properties;

public interface ISimplePropertiesHolder {
	public String[] getPropertyKeys(Object selected);
	public Properties getProperties(Object selected);
}
