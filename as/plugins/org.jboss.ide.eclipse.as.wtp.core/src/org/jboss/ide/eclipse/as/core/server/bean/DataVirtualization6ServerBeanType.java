package org.jboss.ide.eclipse.as.core.server.bean;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.jboss.ide.eclipse.as.core.server.bean.AbstractCondition;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanTypeUnknownAS72Product.UnknownAS72ProductServerTypeCondition;

public class DataVirtualization6ServerBeanType extends JBossServerType {
	public DataVirtualization6ServerBeanType() {
		super(
				"DV",//$NON-NLS-1$
				"JBoss Data Virtualization",//$NON-NLS-1$
				asPath("modules", "system", "layers", "base", "org", "jboss", "as", "server", "main"),
				new String[]{}, new DV6Condition());
	}	
	
	@Override
	public String getServerBeanName(File root) {
		// TODO bug in upstream; ICondition is not public (??) 
		return "JBoss Data Virtualization " + ((AbstractCondition)condition).getFullVersion(root, null);
	}
	
	public static class DV6Condition extends UnknownAS72ProductServerTypeCondition {
		public String getFullVersion(File location, File systemJarFile) {
			String productSlot = getSlot(location);
			boolean hasDV = "dv".equalsIgnoreCase(productSlot);
			if( hasDV ) {
				List<String> layers = Arrays.asList(getLayers(location));
				if( layers.contains("dv") ) {
					String dvProductDir = "org.jboss.as.product.dv.dir";
					File[] modules = new File[]{new File(location, "modules")};
					String vers = ServerBeanType.getManifestPropFromJBossModulesFolder(modules, dvProductDir, 
							"META-INF", "JBoss-Product-Release-Version");
					if( vers.startsWith("6."))
						return vers;
				}
			}
			return null;
		}
		
		public String getUnderlyingTypeId(File location, File systemFile) {
			if( getFullVersion(location, systemFile) != null ) 
				return "DV";
			return null;
		}
	}
}
