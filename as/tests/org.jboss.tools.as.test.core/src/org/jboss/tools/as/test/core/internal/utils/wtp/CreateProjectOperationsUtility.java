/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Oct 27, 2003
 * 
 * To change the template for this generated file go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 * Comments
 */
package org.jboss.tools.as.test.core.internal.utils.wtp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jst.common.project.facet.IJavaFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.applicationclient.internal.creation.AppClientFacetProjectCreationDataModelProvider;
import org.eclipse.jst.j2ee.earcreation.IEarFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.ejb.project.operations.IEjbFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.internal.ejb.project.operations.EjbFacetProjectCreationDataModelProvider;
import org.eclipse.jst.j2ee.internal.project.facet.EARFacetProjectCreationDataModelProvider;
import org.eclipse.jst.j2ee.internal.web.archive.operations.WebFacetProjectCreationDataModelProvider;
import org.eclipse.jst.j2ee.jca.project.facet.ConnectorFacetProjectCreationDataModelProvider;
import org.eclipse.jst.j2ee.jca.project.facet.IConnectorFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.jst.j2ee.project.facet.IAppClientFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetProjectCreationDataModelProperties;
import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.project.facet.IJavaUtilityProjectCreationDataModelProperties;
import org.eclipse.jst.j2ee.project.facet.JavaUtilityProjectCreationDataModelProvider;
import org.eclipse.jst.j2ee.web.project.facet.IWebFacetInstallDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetInstallDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.jboss.ide.eclipse.as.ui.mbeans.project.IJBossSARFacetDataModelProperties;
import org.jboss.ide.eclipse.as.ui.mbeans.project.JBossSARFacetProjectCreationDataModelProvider;


/**
 * @author jsholl
 * @author Ian Tewksbury (ictewksb@us.ibm.com)
 * 
 * To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code
 * Generation&gt;Code and Comments
 */
public class CreateProjectOperationsUtility {
	private static final String APP_CLIENT_PROJ_12 = "myAppClient_12";
	private static final String APP_CLIENT_PROJ_13 = "myAppClient_13";
	private static final String APP_CLIENT_PROJ_14 = "myAppClient_14";
	private static final String APP_CLIENT_PROJ_5 = "myAppClient_5";
	private static final String APP_CLIENT_PROJ_6 = "myAppClient_6";
	
	private static final String EJB_PROJ_11 = "myEJB_11";
	private static final String EJB_PROJ_2 = "myEJB_2";
	private static final String EJB_PROJ_21 = "myEJB_21";
	private static final String EJB_PROJ_3 = "myEJB_3";
	private static final String EJB_PROJ_31 = "myEJB_31";
	
	private static final String WEB_PROJ_22 = "myWeb_22";
	private static final String WEB_PROJ_23 = "myWeb_23";
	private static final String WEB_PROJ_24 = "myWeb_24";
	private static final String WEB_PROJ_25 = "myWeb_25";
	private static final String WEB_PROJ_30 = "myWeb_30";
	
	private static final String CONNECTOR_PROJ_1 = "myConnector_1";
	private static final String CONNECTOR_PROJ_15 = "myConnector_15";

    /**
     * Creates and returns a utility project DM provider with the given name and of the given version.
     * If earName is not null then util project will be added to the EAR with earName
     * 
     * @param projName name of the project to create
     * @param earName name of the ear to add the project too, if NULL then don't add to an EAR
     * @param version version of Application Client to use
     * @return a Utility Project Data Model with the appropriate properties set
     */
    public static IDataModel getUtilityProjectCreationDataModel(String projName, String earName){
    	IDataModel dm = DataModelFactory.createDataModel(new JavaUtilityProjectCreationDataModelProvider());
    	dm.setProperty(IJavaUtilityProjectCreationDataModelProperties.PROJECT_NAME, projName);
    	dm.setProperty(IJavaUtilityProjectCreationDataModelProperties.SOURCE_FOLDER, "src");
    	if(earName != null) {
    		dm.setProperty(IJavaUtilityProjectCreationDataModelProperties.EAR_PROJECT_NAME, earName);
    	} 
    	return dm;
    }

    public void testEAR12_Defaults() throws Exception{
    	IDataModel dm = getEARDataModel("aEAR", null, null, null, JavaEEFacetConstants.EAR_12, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR13_Defaults() throws Exception{
    	IDataModel dm = getEARDataModel("bEAR", null, null, null, JavaEEFacetConstants.EAR_13, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR14_Defaults() throws Exception{
    	IDataModel dm = getEARDataModel("cEAR", null, null, null, JavaEEFacetConstants.EAR_14, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR50_Defaults() throws Exception{
    	IDataModel dm = getEARDataModel("dEAR", null, null, null, JavaEEFacetConstants.EAR_5, false);
    	OperationTestCase.runAndVerify(dm);
    }
    
//    public void testEAR60_Defaults() throws Exception{
//    	IDataModel dm = getEARDataModel("eeEAR", null, null, null, JavaEEFacetConstants.EAR_6, false);
//    	OperationTestCase.runAndVerify(dm);
//    }
    
    
    public void testEAR12_ChangedContentDir() throws Exception{
    	IDataModel dm = getEARDataModel("eEAR", "whosContent", null, null, JavaEEFacetConstants.EAR_12, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR13_ChangedContentDir() throws Exception{
    	IDataModel dm = getEARDataModel("fEAR", "myContent", null, null, JavaEEFacetConstants.EAR_13, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR14_ChangedContentDir() throws Exception{
    	IDataModel dm = getEARDataModel("gEAR", "yourContent", null, null, JavaEEFacetConstants.EAR_14, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR50_ChangedContentDir() throws Exception{
    	IDataModel dm = getEARDataModel("hEAR", "ourContent", null, null, JavaEEFacetConstants.EAR_5, false);
    	OperationTestCase.runAndVerify(dm);
    }
    
//    public void testEAR60_ChangedContentDir() throws Exception{
//    	IDataModel dm = getEARDataModel("iEAR", "herContent", null, null, JavaEEFacetConstants.EAR_6, false);
//    	OperationTestCase.runAndVerify(dm);
//    }
    
    
    public void testEAR12_WithDependencies() throws Exception{
    	IDataModel dm = getEARDataModel("zEAR", null, getJ2EEDependencyList_12(), getJavaDependencyList_12(), JavaEEFacetConstants.EAR_12, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR13_WithDependencies() throws Exception{
    	IDataModel dm = getEARDataModel("yEAR", null, getJ2EEDependencyList_13(), getJavaDependencyList_13(), JavaEEFacetConstants.EAR_13, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR14_WithDependencies() throws Exception{
    	IDataModel dm = getEARDataModel("xEAR", null, getJ2EEDependencyList_14(), getJavaDependencyList_14(), JavaEEFacetConstants.EAR_14, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR50_WithDependencies() throws Exception{
    	IDataModel dm = getEARDataModel("wEAR", null, getJ2EEDependencyList_5(), getJavaDependencyList_5(), JavaEEFacetConstants.EAR_5, false);
    	OperationTestCase.runAndVerify(dm);
    }
    
//    public void testEAR60_WithDependencies() throws Exception{
//    	IDataModel dm = getEARDataModel("whyEAR", null, getJ2EEDependencyList_6(), getJavaDependencyList_6(), JavaEEFacetConstants.EAR_6, false);
//    	OperationTestCase.runAndVerify(dm);
//    }
    
    
    public void testEAR12_ChangedContentDir_WithDependencies() throws Exception{
    	IDataModel dm = getEARDataModel("zEAR", "myContent", getJ2EEDependencyList_12(), getJavaDependencyList_12(), JavaEEFacetConstants.EAR_12, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR13_ChangedContentDir_WithDependencies() throws Exception{
    	IDataModel dm = getEARDataModel("yEAR", "ourContent", getJ2EEDependencyList_13(), getJavaDependencyList_13(), JavaEEFacetConstants.EAR_13, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR14_ChangedContentDir_WithDependencies() throws Exception{
    	IDataModel dm = getEARDataModel("xEAR", "theirContent", getJ2EEDependencyList_14(), getJavaDependencyList_14(), JavaEEFacetConstants.EAR_14, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR50_ChangedContentDir_WithDependencies() throws Exception{
    	IDataModel dm = getEARDataModel("wEAR", "yourContent", getJ2EEDependencyList_5(), getJavaDependencyList_5(), JavaEEFacetConstants.EAR_5, false);
    	OperationTestCase.runAndVerify(dm);
    }
    
//    public void testEAR60_ChangedContentDir_WithDependencies() throws Exception{
//    	IDataModel dm = getEARDataModel("xyzEAR", "hisContent", getJ2EEDependencyList_6(), getJavaDependencyList_6(), JavaEEFacetConstants.EAR_6, false);
//    	OperationTestCase.runAndVerify(dm);
//    }
    
    
    public void testEAR50_Defaults_WithDD() throws Exception{
    	IDataModel dm = getEARDataModel("myEAR", null, null, null, JavaEEFacetConstants.EAR_5, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
//    public void testEAR60_Defaults_WithDD() throws Exception{
//    	IDataModel dm = getEARDataModel("mineEAR", null, null, null, JavaEEFacetConstants.EAR_6, true);
//    	OperationTestCase.runAndVerify(dm);
//    }
    
    
    public void testEAR50_ChangedContentDir_WithDD() throws Exception{
    	IDataModel dm = getEARDataModel("yourEAR", "ourContent", null, null, JavaEEFacetConstants.EAR_5, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
//    public void testEAR60_ChangedContentDir_WithDD() throws Exception{
//    	IDataModel dm = getEARDataModel("thatEAR", "thatContent", null, null, JavaEEFacetConstants.EAR_6, true);
//    	OperationTestCase.runAndVerify(dm);
//    }
    
/*    
    public void testEAR50_WithDependencies_WithDD() throws Exception{
    	IDataModel dm = getEARDataModel("anEAR", null, getJ2EEDependencyList_5(), getJavaDependencyList_5(), JavaEEFacetConstants.EAR_5, true);
    	OperationTestCase.runAndVerify(dm);
    }
    
    public void testEAR50_ChangedContentDir_WithDependencies_WithDD() throws Exception{
    	IDataModel dm = getEARDataModel("theirEAR", "gotContent", getJ2EEDependencyList_5(), getJavaDependencyList_5(), JavaEEFacetConstants.EAR_5, true);
    	OperationTestCase.runAndVerify(dm);
    }
*/
    
    
    
    
    public static List getJ2EEDependencyList_12() throws Exception {
    	List dependencies = new ArrayList();
    	List<IDataModel> models = new ArrayList<IDataModel>();
    	
    	models.add(getAppClientCreationDataModel(APP_CLIENT_PROJ_12, null, JavaEEFacetConstants.APP_CLIENT_12, true, true));
    	
    	models.add(getEJBDataModel(EJB_PROJ_11, null, null, null, JavaEEFacetConstants.EJB_11, true));
    	
    	models.add(getWebDataModel(WEB_PROJ_22, null, null, null, null, JavaEEFacetConstants.WEB_22, true));
    	
    	for(int i = 0; i < models.size(); i++) {
    		OperationTestCase.runDataModel(models.get(i));
    	}
    	
    	dependencies.addAll(Arrays.asList(JavaEEProjectUtilities.getAllProjects()));
    	
    	return dependencies;
    }
    
    public static List getJavaDependencyList_12() {
    	return Collections.emptyList();
    }
    
    
    public static List getJ2EEDependencyList_13() throws Exception {
    	getJ2EEDependencyList_12();
    	List dependencies = new ArrayList();
    	List<IDataModel> models = new ArrayList<IDataModel>();
    	
    	models.add(getAppClientCreationDataModel(APP_CLIENT_PROJ_13, null, JavaEEFacetConstants.APP_CLIENT_13, true, true));
    	
    	models.add(getEJBDataModel(EJB_PROJ_2, null, null, null, JavaEEFacetConstants.EJB_2, true));
    	
    	models.add(getWebDataModel(WEB_PROJ_23, null, null, null, null, JavaEEFacetConstants.WEB_23, true));
    	
    	models.add(getConnectorDataModel(CONNECTOR_PROJ_1, null, null, JavaEEFacetConstants.CONNECTOR_1));
    	
    	for(int i = 0; i < models.size(); i++) {
    		OperationTestCase.runDataModel(models.get(i));
    	}
    	
    	dependencies.addAll(Arrays.asList(JavaEEProjectUtilities.getAllProjects()));
    	
    	return dependencies;
    }
    
    public static List getJavaDependencyList_13() {
    	return Collections.emptyList();
    }
    
    
    public static List getJ2EEDependencyList_14() throws Exception {
    	getJ2EEDependencyList_13();
    	List dependencies = new ArrayList();
    	
    	List<IDataModel> models = new ArrayList<IDataModel>();
    	
    	models.add(getAppClientCreationDataModel(APP_CLIENT_PROJ_14, null, JavaEEFacetConstants.APP_CLIENT_14, true, true));

    	models.add(getEJBDataModel(EJB_PROJ_21, null, null, null, JavaEEFacetConstants.EJB_21, true));

    	models.add(getWebDataModel(WEB_PROJ_24, null, null, null, null, JavaEEFacetConstants.WEB_24, true));

    	for(int i = 0; i < models.size(); i++) {
    		OperationTestCase.runDataModel(models.get(i));
    	}
    	
    	dependencies.addAll(Arrays.asList(JavaEEProjectUtilities.getAllProjects()));
    	
    	return dependencies;
    }
    
    public static List getJavaDependencyList_14() {
    	return Collections.emptyList();
    }
    
    public static List getJ2EEDependencyList_5() throws Exception {
    	getJ2EEDependencyList_14();
    	List dependencies = new ArrayList();
    	
    	List<IDataModel> models = new ArrayList<IDataModel>();
    	
    	models.add(getAppClientCreationDataModel(APP_CLIENT_PROJ_5, null, JavaEEFacetConstants.APP_CLIENT_5, true, false));
    	models.add(getAppClientCreationDataModel(APP_CLIENT_PROJ_5 + "_WithDD", null, JavaEEFacetConstants.APP_CLIENT_5, true, true));
    	
    	models.add(getEJBDataModel(EJB_PROJ_3, null, null, null, JavaEEFacetConstants.EJB_3, false));
    	models.add(getEJBDataModel(EJB_PROJ_3 + "_WithDD", null, null, null, JavaEEFacetConstants.EJB_3, true));
    	
    	models.add(getWebDataModel(WEB_PROJ_25, null, null, null, null, JavaEEFacetConstants.WEB_25, false));
      	models.add(getWebDataModel(WEB_PROJ_25 + "_WithDD", null, null, null, null, JavaEEFacetConstants.WEB_25, true));
    	
    	for(int i = 0; i < models.size(); i++) {
    		OperationTestCase.runDataModel(models.get(i));
    	}
    	
    	dependencies.addAll(Arrays.asList(JavaEEProjectUtilities.getAllProjects()));
    	
    	return dependencies;
    }
    
    /**
     * Creates and returns an EJB Data Model with the given name and of the given version.
     * Can also set the clientName to be different then the default.
     * If earName is not null then AppClient will be added to the EAR with earName, and if appropriate
     * with or without a deployment descriptor.
     * 
     * @param projName name of the project to create
     * @param clientName name of client jar to create, if NULL or earName is NULL then don't create one
     * @param clientSourceFolder source folder for client, use default if value is NULL, ignored if clientName is NULL
     * @param earName name of the EAR to add the project too, if NULL then don't add to an EAR
     * @param version version of EJB to use
     * @param createDD only used if version is JEE5, if true then create DD else don't
     * @return an EJB Model with the appropriate properties set
     */
    public static IDataModel getEJBDataModel(String projName, String clientName, String clientSourceFolder, String earName, IProjectFacetVersion version, boolean createDD) {
    	IDataModel dm = DataModelFactory.createDataModel(new EjbFacetProjectCreationDataModelProvider());
    	dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, projName);

    	FacetDataModelMap facetMap = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
    	IDataModel facetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.EJB);
    	facetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, version);

    	if(earName != null) {
    		dm.setProperty(IJ2EEFacetProjectCreationDataModelProperties.ADD_TO_EAR, true);
    		dm.setProperty(IJ2EEFacetProjectCreationDataModelProperties.EAR_PROJECT_NAME, earName);

    		//only create client if given a client name, and is added to EAR
    		if(clientName != null) {
    			facetModel.setBooleanProperty(IEjbFacetInstallDataModelProperties.CREATE_CLIENT, true);
    			facetModel.setStringProperty(IEjbFacetInstallDataModelProperties.CLIENT_NAME, clientName);

    			//use default source folder unless different name is given
    			if(clientSourceFolder != null) {
    				facetModel.setStringProperty(IEjbFacetInstallDataModelProperties.CLIENT_SOURCE_FOLDER, clientSourceFolder);
    			}
    		}
    	} else {
    		dm.setProperty(IJ2EEFacetProjectCreationDataModelProperties.ADD_TO_EAR, false);
    	}

    	facetModel.setBooleanProperty(IJ2EEFacetInstallDataModelProperties.GENERATE_DD, createDD);

//    	if(version.equals(JavaEEFacetConstants.EJB_31))
//    	{
//    		IDataModel javaFacetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.JAVA);
//	    	javaFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, JavaEEFacetConstants.JAVA_6);
//    	}
//    	else{    
	    	IDataModel javaFacetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.JAVA);
	    	javaFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, JavaEEFacetConstants.JAVA_5);
//    	}

        
    	return dm;
	}
    
    
    /**
     * Creates and returns an EJB Data Model with the given name and of the given version.
     * Can also set the clientName to be different then the default, or choose not to have a client.
     * If earName is not null then AppClient will be added to the EAR with earName, and if appropriate
     * with or without a deployment descriptor.
     * 
     * Created so EJB's could be created without clients.
     * 
     * @param projName name of the project to create
     * @param clientName name of client jar to create, if NULL or earName is NULL then don't create one
     * @param clientSourceFolder source folder for client, use default if value is NULL, ignored if clientName is NULL
     * @param earName name of the EAR to add the project too, if NULL then don't add to an EAR
     * @param version version of EJB to use
     * @param createClient if True and earName not NULL then create with client, else dont
     * @param createDD only used if version is JEE5, if true then create DD else don't
     * @return an EJB Model with the appropriate properties set
     */
    public static IDataModel getEJBDataModel(String projName, String clientName, String clientSourceFolder, String earName, IProjectFacetVersion version, boolean createCleint, boolean createDD) {
    	IDataModel dm = getEJBDataModel(projName, clientName, clientSourceFolder, earName, version, createDD);
    	
    	FacetDataModelMap facetMap = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
    	IDataModel facetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.EJB);
    	facetModel.setBooleanProperty(IEjbFacetInstallDataModelProperties.CREATE_CLIENT, createCleint);
        
    	return dm;
	} 
    public static List getJavaDependencyList_5() {
    	return Collections.emptyList();
    }
    
//    public static List getJ2EEDependencyList_6() throws Exception {
//    	getJ2EEDependencyList_5();
//    	List dependencies = new ArrayList();
//    	
//    	List<IDataModel> models = new ArrayList<IDataModel>();
//    	
//    	models.add(AppClientProjectCreationOperationTest.getAppClientCreationDataModel(APP_CLIENT_PROJ_6, null, JavaEEFacetConstants.APP_CLIENT_6, true, false));
//    	models.add(AppClientProjectCreationOperationTest.getAppClientCreationDataModel(APP_CLIENT_PROJ_6 + "_WithDD", null, JavaEEFacetConstants.APP_CLIENT_6, true, true));
//    	
//    	models.add(EJBProjectCreationOperationTest.getEJBDataModel(EJB_PROJ_31, null, null, null, JavaEEFacetConstants.EJB_31, false));
//    	models.add(EJBProjectCreationOperationTest.getEJBDataModel(EJB_PROJ_31 + "_WithDD", null, null, null, JavaEEFacetConstants.EJB_31, true));
//    	
//    	models.add(WebProjectCreationOperationTest.getWebDataModel(WEB_PROJ_30, null, null, null, null, JavaEEFacetConstants.WEB_30, false));
//      	models.add(WebProjectCreationOperationTest.getWebDataModel(WEB_PROJ_30 + "_WithDD", null, null, null, null, JavaEEFacetConstants.WEB_30, true));
//    	
//    	for(int i = 0; i < models.size(); i++) {
//    		OperationTestCase.runDataModel(models.get(i));
//    	}
//    	
//    	dependencies.addAll(Arrays.asList(JavaEEProjectUtilities.getAllProjects()));
//    	
//    	return dependencies;
//    }
    
    public static List getJavaDependencyList_6() {
    	return Collections.emptyList();
    }
 
    /**
     * Creates and returns an EAR Data Model with the given name and of the given version 
     * 
     * @param projName name of the project to create
     * @param version version of EAR to use
     * @param contentDir directory to store the content in, if NULL use default
     * @param dependenciesJ2EE list of J2EE IProjects that this EAR depends on, ignored if NULL
     * @param dependenciesJava list of Java IProjects that this EAR depends on, ignored if NULL
     * @param createDD only used if version is JEE5, if true then create DD else don't
     * @return an EAR Data Model with the appropriate properties set
     */
    public static IDataModel getEARDataModel(String projName, String contentDir, List dependenciesJ2EE, List dependenciesJava, IProjectFacetVersion version, boolean createDD) {
    	IDataModel dm = DataModelFactory.createDataModel(new EARFacetProjectCreationDataModelProvider());
    	dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, projName);

		FacetDataModelMap factMap = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
		IDataModel facetModel = (IDataModel) factMap.get(IEarFacetInstallDataModelProperties.ENTERPRISE_APPLICATION);
		facetModel.setProperty(IFacetInstallDataModelProperties.FACET_VERSION, version);
		
		
		if(contentDir != null) {
			facetModel.setStringProperty(IEarFacetInstallDataModelProperties.CONTENT_DIR,contentDir); 
		}
		
		if(dependenciesJ2EE != null) {
			facetModel.setProperty(IEarFacetInstallDataModelProperties.J2EE_PROJECTS_LIST, dependenciesJ2EE);
		}
		
		if(dependenciesJava != null) {
			facetModel.setProperty(IEarFacetInstallDataModelProperties.JAVA_PROJECT_LIST, dependenciesJava);
		}
		
        
        facetModel.setBooleanProperty(IJ2EEFacetInstallDataModelProperties.GENERATE_DD, createDD);
        
		
    	return dm;
    }

    
    /**
     * Creates and returns an Application Client Data Model with the given name and of the given version.
     * If earName is not null then AppClient will be added to the EAR with earName, and if appropriate
     * with or without a deployment descriptor.
     * 
     * @param projName name of the project to create
     * @param earName name of the ear to add the project too, if NULL then don't add to an EAR
     * @param version version of Application Client to use
     * @param createDefaultMainClass if true then create default main class, else don't
     * @param createDD only used if version is JEE5, if true then create DD else don't
     * @return an Application Data Model with the appropriate properties set
     */
    public static IDataModel getAppClientCreationDataModel(String projName, String earName, IProjectFacetVersion version, boolean createDefaultMainClass, boolean createDD){
    	IDataModel dm = DataModelFactory.createDataModel(new AppClientFacetProjectCreationDataModelProvider());
    	dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, projName);
    	
    	if(earName != null) {
        	dm.setProperty(IJ2EEFacetProjectCreationDataModelProperties.ADD_TO_EAR, true);
        	dm.setProperty(IJ2EEFacetProjectCreationDataModelProperties.EAR_PROJECT_NAME, earName);
    	} else {
    		dm.setProperty(IJ2EEFacetProjectCreationDataModelProperties.ADD_TO_EAR, false);
    	}
    	
    	FacetDataModelMap facetMap = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
        IDataModel facetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.APPLICATION_CLIENT);
        facetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, version);
        facetModel.setProperty(IAppClientFacetInstallDataModelProperties.CREATE_DEFAULT_MAIN_CLASS, createDefaultMainClass);
        
        
        facetModel.setBooleanProperty(IJ2EEFacetInstallDataModelProperties.GENERATE_DD, createDD);
        
//        if(version.equals(JavaEEFacetConstants.APP_CLIENT_6))
//    	{
//    		IDataModel javaFacetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.JAVA);
//	    	javaFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, JavaEEFacetConstants.JAVA_6);
//    	}
//    	else{ 
    		IDataModel javaFacetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.JAVA);
    		javaFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, JavaEEFacetConstants.JAVA_5);
//    	}
        
    	return dm;
    }
	
    /**
     * Creates and returns a Web Data Model with the given name and of the given version.
     * Can also be used to specify none default context root, content directory, and/or
     * the java source directory.
     * If earName is not null then Web will be added to the EAR with earName, and if appropriate
     * with or without a deployment descriptor.
     * 
     * @param projName name of the project to create
     * @param earName name of the ear to add the project too, if NULL then don't add to an EAR
     * @param contextRoot the context root to use for this  project, use default if NULL
     * @param contentDir the content directory to use for this project, use default if NULL
     * @param javaSrcDir the java source directory to use for this project, use default if NULL
     * @param version version of Web to use
     * @param createDD only used if version is JEE5, if true then create DD else don't
     * @return a Web Data Model with the appropriate properties set
     */
    public static IDataModel getWebDataModel(String projName, String earName, String contextRoot, String contentDir, String javaSrcDir, IProjectFacetVersion version, boolean createDD){
    	IDataModel dm = DataModelFactory.createDataModel(new WebFacetProjectCreationDataModelProvider());
    	dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, projName);
    	
    	if(earName != null) {
    		dm.setProperty(IJ2EEFacetProjectCreationDataModelProperties.ADD_TO_EAR, true);
    		dm.setProperty(IJ2EEFacetProjectCreationDataModelProperties.EAR_PROJECT_NAME, earName);
    	} else {
    		dm.setProperty(IJ2EEFacetProjectCreationDataModelProperties.ADD_TO_EAR, false);
    	}
    	
    	FacetDataModelMap facetMap = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
    	IDataModel facetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.DYNAMIC_WEB);
    	facetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, version);
    	
    	//if no contextRoot provided use default, contextRoot only matters if adding to EAR
    	if(contextRoot != null && earName != null) {
    		facetModel.setStringProperty(IWebFacetInstallDataModelProperties.CONTEXT_ROOT, contextRoot);
    	}
    	
    	//if no contentDir provided use default
    	if(contentDir != null) {
    		facetModel.setStringProperty(IJ2EEModuleFacetInstallDataModelProperties.CONFIG_FOLDER, contentDir);
    	}
    	
    	//if no javaSrcDir provided use default
    	if(javaSrcDir != null) {
    		facetModel.setStringProperty(IWebFacetInstallDataModelProperties.SOURCE_FOLDER, javaSrcDir);
    	}
    	
    	facetModel.setBooleanProperty(IJ2EEFacetInstallDataModelProperties.GENERATE_DD, createDD);
    	
//    	if(version.equals(JavaEEFacetConstants.WEB_30))    	{
//    		IDataModel javaFacetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.JAVA);
//	    	javaFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, JavaEEFacetConstants.JAVA_6);
//    	}
//    	else{    		
            IDataModel javaFacetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.JAVA);
            javaFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, JavaEEFacetConstants.JAVA_5);
//    	}
    	
    	return dm;
    }

    /**
     * Creates and returns an Connector Data Model with the given name and of the given version.
     * If earName is not null then Connector will be added to the EAR with earName.
     * Can also specify none default source folder
     * 
     * @param projName name of the project to create
     * @param earName name of the EAR to add the project too, if NULL then don't add to an EAR
     * @param sourceFolder name of the source folder to use, if NULL then use default
     * @param version version of Application Client to use
     * @return a Connector Model with the appropriate properties set
     */
    public static IDataModel getConnectorDataModel(String projName, String earName, String sourceFolder, IProjectFacetVersion version){
    	IDataModel dm = DataModelFactory.createDataModel(new ConnectorFacetProjectCreationDataModelProvider());
    	dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, projName);
    	
    	if(earName != null) {
        	dm.setProperty(IJ2EEFacetProjectCreationDataModelProperties.ADD_TO_EAR, true);
        	dm.setProperty(IJ2EEFacetProjectCreationDataModelProperties.EAR_PROJECT_NAME, earName);
    	} else {
    		dm.setProperty(IJ2EEFacetProjectCreationDataModelProperties.ADD_TO_EAR, false);
    	}
    	
    	FacetDataModelMap facetMap = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
        IDataModel facetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.JCA);
        facetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, version);
        
        if(sourceFolder != null) {
        	facetModel.setProperty(IConnectorFacetInstallDataModelProperties.CONFIG_FOLDER, sourceFolder);
        }
        
        //be sure to use Java5 with JEE5
        if(version == JavaEEFacetConstants.CONNECTOR_15){
            IDataModel javaFacetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.JAVA);
            javaFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, JavaEEFacetConstants.JAVA_5);
        }
//        else if(version == JavaEEFacetConstants.CONNECTOR_16) {
//            IDataModel javaFacetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.JAVA);
//            javaFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, JavaEEFacetConstants.JAVA_6);
//        }
        
    	return dm;
    }
    
    
    
    
    public static IDataModel getSarDataModel(String projName, String sourceFolder, String contentFolder, 
    		String outputFolder, IProjectFacetVersion version){
    	IDataModel dm = DataModelFactory.createDataModel(new JBossSARFacetProjectCreationDataModelProvider());
    	dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, projName);
    	
    	FacetDataModelMap facetMap = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
    	IDataModel esbFacetModel = facetMap.getFacetDataModel(IJBossSARFacetDataModelProperties.SAR_PROJECT_FACET);
        esbFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, version);
        
        String tmp = JavaEEFacetConstants.JAVA_FACET.getId();
        IDataModel javaFacetModel = facetMap.getFacetDataModel(tmp);
        
        if(sourceFolder != null)
        	javaFacetModel.setProperty(IJavaFacetInstallDataModelProperties.SOURCE_FOLDER_NAME, sourceFolder);
        
        if(outputFolder != null)
        	javaFacetModel.setProperty(IJavaFacetInstallDataModelProperties.DEFAULT_OUTPUT_FOLDER_NAME, outputFolder);

        if(contentFolder != null)
        	esbFacetModel.setProperty(IJBossSARFacetDataModelProperties.SAR_CONTENT_FOLDER, contentFolder);
        	
    	return dm;
    }

}