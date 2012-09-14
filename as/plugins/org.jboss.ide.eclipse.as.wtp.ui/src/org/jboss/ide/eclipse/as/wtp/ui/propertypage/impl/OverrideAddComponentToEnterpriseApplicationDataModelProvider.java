package org.jboss.ide.eclipse.as.wtp.ui.propertypage.impl;

import java.util.Map;

import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationOp;
import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;

public class OverrideAddComponentToEnterpriseApplicationDataModelProvider 
 extends AddComponentToEnterpriseApplicationDataModelProvider {

	public OverrideAddComponentToEnterpriseApplicationDataModelProvider() {
		super();
	}
	
	public IDataModelOperation getDefaultOperation() {
		return new OverrideAddComponentToEnterpriseApplicationOp(model);
	}
	
	public static class OverrideAddComponentToEnterpriseApplicationOp 
	  extends AddComponentToEnterpriseApplicationOp {
		public OverrideAddComponentToEnterpriseApplicationOp(IDataModel model) {
			super(model);
		}
		protected String getArchiveName(IVirtualComponent comp) {
			Map map = (Map) model.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP);
			String uri = (String) map.get(comp);
			return uri == null ? "" : uri; //$NON-NLS-1$
		}
	}
}
