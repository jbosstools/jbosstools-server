/******************************************************************************* 
 * Copyright (c) 2008 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.ui.wizards.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.DataModelPropertyDescriptor;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonMessages;
import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.jboss.ide.eclipse.as.wtp.core.util.VCFUtil;
import org.jboss.ide.eclipse.as.wtp.core.vcf.ModuleExportOperation;
import org.jboss.ide.eclipse.as.wtp.ui.Messages;
import org.jboss.ide.eclipse.as.wtp.ui.WTPOveridePlugin;

public class ProjectModuleExportDataModelProvider extends
		AbstractDataModelProvider implements IJ2EEComponentExportDataModelProperties {

	public IDataModelOperation getDefaultOperation() {
		return new ModuleExportOperation(model);
	}
    public HashMap<String, IVirtualComponent> componentMap;

	public ProjectModuleExportDataModelProvider() {
		super();
	}

	public Set getPropertyNames() {
		Set<String> propertyNames = super.getPropertyNames();
		propertyNames.add(PROJECT_NAME);
		propertyNames.add(ARCHIVE_DESTINATION);
		propertyNames.add(OVERWRITE_EXISTING);
		propertyNames.add(RUN_BUILD);
		propertyNames.add(COMPONENT);
		return propertyNames;
	}

	public Object getDefaultProperty(String propertyName) {
		if (propertyName.equals(ARCHIVE_DESTINATION)) {
			return ""; //$NON-NLS-1$
		} else if (propertyName.equals(OVERWRITE_EXISTING)) {
			return Boolean.FALSE;
		} else if( propertyName.equals(RUN_BUILD))
			return Boolean.TRUE;
		return super.getDefaultProperty(propertyName);
	}
	
	public boolean isPropertyEnabled( final String propertyName ) {
		return true;
	}
	
	public boolean propertySet(String propertyName, Object propertyValue)  {
		boolean set = super.propertySet(propertyName, propertyValue);
		final IDataModel dm = getDataModel();
		
		if (propertyName.equals(PROJECT_NAME)) {
			if (getComponentMap().isEmpty())
				intializeComponentMap();
			IVirtualComponent component = (IVirtualComponent) getComponentMap().get(propertyValue);
			if (null != component && component.getName().equals(propertyValue)) {
				setProperty(COMPONENT, component);
			} else {
				setProperty(COMPONENT, null);
			}

            dm.notifyPropertyChange( RUNTIME, IDataModel.VALID_VALUES_CHG );
            IFacetedProject fproj = null;
            if( component != null ) {
                try {
                    fproj = ProjectFacetsManager.create( component.getProject() );
                } catch( CoreException e ) {
                    WTPOveridePlugin.logError(e );
                }
            }
		}
		return set;
	}

	public HashMap<String, IVirtualComponent> getComponentMap() {
		if (componentMap == null)
			componentMap = new HashMap<String, IVirtualComponent>();
		return componentMap;
	}

	public void intializeComponentMap() {
		IVirtualComponent[] comps = ComponentUtilities.getAllWorkbenchComponents();
		for (int i = 0; i < comps.length; i++) {
			getComponentMap().put(comps[i].getName(), comps[i]);
		}
	}

	/**
	 * Populate the resource name combo with projects that are not encrypted.
	 */
	public DataModelPropertyDescriptor[] getValidPropertyDescriptors(String propertyName) {
		if (propertyName.equals(PROJECT_NAME)) {
			List<String> componentNames = new ArrayList<String>();
			IVirtualComponent[] wbComps = ComponentUtilities.getAllWorkbenchComponents();
			List<IVirtualComponent> relevantComponents = new ArrayList<IVirtualComponent>();
			for (int i = 0; i < wbComps.length; i++) {
				relevantComponents.add(wbComps[i]);
				getComponentMap().put(wbComps[i].getName(), wbComps[i]);
			}

			if (relevantComponents == null || relevantComponents.size() == 0)
				return null;

			for (int j = 0; j < relevantComponents.size(); j++) {
				componentNames.add(((IVirtualComponent) relevantComponents.get(j)).getName());
			}
			String[] names = (String[]) componentNames.toArray(new String[componentNames.size()]);

			return DataModelPropertyDescriptor.createDescriptors(names);
		}

		return super.getValidPropertyDescriptors(propertyName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperationDataModel#doValidateProperty(java.lang.String)
	 */
	public IStatus validate(String propertyName) {
		if (PROJECT_NAME.equals(propertyName)) {
			String projectName = (String) model.getProperty(PROJECT_NAME);
			if (projectName == null || projectName.equals("")) //$NON-NLS-1$
				return WTPOveridePlugin.createErrorStatus(Messages.MODULE_EXISTS_ERROR);
			IVirtualComponent component = (IVirtualComponent) componentMap.get(projectName);
			if (component == null) {
				return WTPOveridePlugin.createErrorStatus(Messages.MODULE_EXISTS_ERROR);
			}
		}
		if (ARCHIVE_DESTINATION.equals(propertyName)) {
			String archiveLocation = (String) model.getProperty(ARCHIVE_DESTINATION);
			if (!model.isPropertySet(ARCHIVE_DESTINATION) || archiveLocation.equals("")) { //$NON-NLS-1$
				return WTPOveridePlugin.createErrorStatus(Messages.DESTINATION_INVALID);
			} else if (model.isPropertySet(ARCHIVE_DESTINATION)) {
				IStatus tempStatus = validateLocation(archiveLocation);
				if (tempStatus != OK_STATUS)
					return tempStatus;
			} else if (model.isPropertySet(ARCHIVE_DESTINATION) && !validateModuleType(archiveLocation)) {
				String assumedExtension = VCFUtil.getModuleFacetExtension(getProject());
				if( assumedExtension != null ) 
					return WTPOveridePlugin.createWarningStatus(NLS.bind(Messages.DESTINATION_ARCHIVE_SHOULD_END_WITH, assumedExtension));
			}
		}
		if (ARCHIVE_DESTINATION.equals(propertyName) || OVERWRITE_EXISTING.equals(propertyName)) {
			String location = (String) getProperty(ARCHIVE_DESTINATION);
			if (checkForExistingFileResource(location)) {
				return WTPOveridePlugin.createErrorStatus(NLS.bind(Messages.RESOURCE_EXISTS_ERROR, location));
			}
		}
		return OK_STATUS;
	}

	private IStatus validateLocation(String archiveLocation) {
		IPath path = null;
		try {
			path = new Path(archiveLocation);
		} catch (IllegalArgumentException ex) {
			return WTPOveridePlugin.createErrorStatus(Messages.DESTINATION_INVALID);
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus status = workspace.validateName(path.lastSegment(), IResource.FILE);
		if (!status.isOK()) {
			return status;
		}
		String device = path.getDevice();
		if (device == null)
			return OK_STATUS;
		if (path == null || device.length() == 1 && device.charAt(0) == IPath.DEVICE_SEPARATOR)
			return WTPOveridePlugin.createErrorStatus(Messages.DESTINATION_INVALID);

		if (!path.toFile().canWrite()) {
			if (path.toFile().exists()) {
				return WTPOveridePlugin.createErrorStatus(Messages.IS_READ_ONLY);
			}
			boolean OK = false;
			path = path.removeLastSegments(1);
			for (int i = 1; !OK && i < 20 && path.segmentCount() > 0; i++) {
				if (path.toFile().exists()) {
					OK = true;
				}
				status = workspace.validateName(path.lastSegment(), IResource.FOLDER);
				if (!status.isOK()) {
					return WTPOveridePlugin.createErrorStatus(Messages.DESTINATION_INVALID);
				}
				path = path.removeLastSegments(1);
			}
		}

		return OK_STATUS;
	}

	private boolean checkForExistingFileResource(String fileName) {
		if (!model.getBooleanProperty(OVERWRITE_EXISTING)) {
			java.io.File externalFile = new java.io.File(fileName);
			if (externalFile != null && externalFile.exists())
				return true;
		}
		return false;
	}

	private boolean validateModuleType(String archive) {
		String assumedExtension = VCFUtil.getModuleFacetExtension(getProject());
		if( archive.length() < 4)
			return false;
		String lastFour = archive.substring(archive.length() - 4, archive.length());
		if (!lastFour.equalsIgnoreCase(assumedExtension)) {
			return false;
		}
		return true;
	}
	
	private IProject getProject() {
	    final IVirtualComponent component = (IVirtualComponent) getProperty( COMPONENT );
	    return component == null ? null : component.getProject();
	}

}
