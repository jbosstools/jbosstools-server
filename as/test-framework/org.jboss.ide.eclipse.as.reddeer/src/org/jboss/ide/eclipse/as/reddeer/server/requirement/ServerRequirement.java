/*******************************************************************************
 * Copyright (c) 2020-2022 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.ide.eclipse.as.reddeer.server.requirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.reddeer.common.exception.RedDeerException;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.eclipse.rse.ui.view.SystemViewPart;
import org.eclipse.reddeer.eclipse.rse.ui.wizards.newconnection.RSEDefaultNewConnectionWizardMainPage;
import org.eclipse.reddeer.eclipse.rse.ui.wizards.newconnection.RSEMainNewConnectionWizard;
import org.eclipse.reddeer.eclipse.rse.ui.wizards.newconnection.RSENewConnectionWizardSelectionPage;
import org.eclipse.reddeer.eclipse.rse.ui.wizards.newconnection.RSENewConnectionWizardSelectionPage.SystemType;
import org.eclipse.reddeer.eclipse.wst.server.ui.wizard.NewServerWizard;
import org.eclipse.reddeer.eclipse.wst.server.ui.wizard.NewServerWizardPage;
import org.eclipse.reddeer.junit.requirement.ConfigurableRequirement;
import org.eclipse.reddeer.requirements.server.AbstractServerRequirement;
import org.jboss.ide.eclipse.as.reddeer.server.family.JBossFamily;
import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerRequirement.JBossServer;
import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.JBossRuntimeWizardPage;
import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.NewServerAdapterPage;
import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.NewServerAdapterPage.Profile;
import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.NewServerRSIWizardPage;
import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.NewServerWizardPageWithErrorCheck;
import org.eclipse.reddeer.requirements.server.ServerRequirementState;
import org.eclipse.reddeer.eclipse.exception.EclipseLayerException;
import org.eclipse.reddeer.eclipse.rse.ui.view.System;

import static org.junit.Assert.assertTrue;


/**
 * 
 * @author psrna, Radoslav Rabara, Ondrej Dockal
 *
 */

public class ServerRequirement extends AbstractServerRequirement implements ConfigurableRequirement<ServerRequirementConfig, JBossServer> {

	private static final Logger LOGGER = Logger.getLogger(ServerRequirement.class);
	
	private ServerRequirementConfig config;
	private JBossServer server;
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface JBossServer {
		ServerRequirementState state() default ServerRequirementState.RUNNING;
		boolean cleanup() default true;
	}

	@Override
	public void fulfill() {
		if (!isLastConfiguredServerPresent()) {
			LOGGER.info("Setup server");
			if(config.getRemote() != null) {
				setupRemoteServerAdapter();
			} else {
				setupLocalServerAdapter();
			}	
		}
		setupServerState(server.state());
	}
	
	protected void setupLocalServerAdapter() {
		NewServerWizard serverW = new NewServerWizard();
		try {
			serverW.open();

			NewServerWizardPage sp = new NewServerWizardPage(serverW);
			
			String serverTypeLabelText = config.getFamily().getLabel()+" "+ config.getVersion();
			
			//workaround for JBIDE-20548
			if(JBossFamily.WILDFLY == config.getFamily()) {
				String label = config.getFamily().getLabel();
				String version = config.getVersion();
				if(version.equals("8.x")){
					serverTypeLabelText = label+"  "+version;
				}
				if(version.equals("9.x")){
					serverTypeLabelText = label+"  "+version;
				}
				if(version.equals("10.x")){
					serverTypeLabelText = label+" "+ version;
				}
			}
			
			sp.selectType(config.getFamily().getCategory(), serverTypeLabelText);
			sp.setName(getServerName());

			serverW.next();

			NewServerAdapterPage ap = new NewServerAdapterPage(serverW);
			ap.setRuntime(null);
			ap.checkErrors();

			serverW.next();

			setupRuntime(serverW);

			serverW.finish();
		} catch(RuntimeException e) {
			try{
				serverW.cancel();
			} catch (RedDeerException ex){
				throw e;
			}
			throw e;
		} catch(AssertionError e) {
			try{
				serverW.cancel();
			} catch (RedDeerException ex){
				throw e;
			}
			throw e;
		}
	}
	
	protected void setupRuntime(NewServerWizard wizard){
		
		JBossRuntimeWizardPage rp = new JBossRuntimeWizardPage(wizard);
		rp.setRuntimeName(getRuntimeName());
		rp.setRuntimeDir(config.getRuntime());
		if (config.getRuntimeEnvironment() != null && !config.getRuntimeEnvironment().isEmpty()) {
			rp.setExecutionEnvironment(config.getRuntimeEnvironment());
		}
		rp.checkErrors();
	}

	protected void setupRemoteSystem(){
		
		SystemViewPart sview = new SystemViewPart();
		sview.open();
		RSEMainNewConnectionWizard connW = sview.newConnection();
		RSENewConnectionWizardSelectionPage sp = new RSENewConnectionWizardSelectionPage(connW);
		RemoteServerConfiguration remote = config.getRemote();
		sp.selectSystemType(getSystemTypeFromString(remote.getSystemType()));
		connW.next();
		RSEDefaultNewConnectionWizardMainPage mp = new RSEDefaultNewConnectionWizardMainPage(connW);
		mp.setHostName(remote.getServerHost());
		if (remote.getServerHostName() != null) {
			mp.setConnectionName(remote.getServerHostName());
		}
		connW.finish();
		
		if (getSystemTypeFromString(remote.getSystemType()) == SystemType.SSH_ONLY) {
			org.eclipse.reddeer.eclipse.rse.ui.view.System system = sview.getSystem(remote.getServerHost());
			system.connect(remote.getUsername(), remote.getPassword());
					
			assertTrue(system.isConnected());
		}
	}
	
	private SystemType getSystemTypeFromString(String type) {
		for(SystemType value : SystemType.values()) {
			if (value.getLabel().equalsIgnoreCase(type.toLowerCase())) {
				return value;
			}
		}
		throw new RedDeerException("Given type :" + type + " does no correspond to any of SystemType enum value");
	}
	
	protected void setupRemoteServerAdapter() {
		NewServerWizard serverW = new NewServerWizard();
		try {
			//setup remote system first
			setupRemoteSystem();

			//-- Open 'New Server' wizard 
			serverW.open();
			//-- Select the server type and fill in server name, then continue on next page
			NewServerWizardPageWithErrorCheck sp = new NewServerWizardPageWithErrorCheck(serverW);
			sp.selectType(config.getFamily().getCategory(),config.getFamily().getLabel()+" "+ config.getVersion());
			sp.setName(getServerName());
			sp.checkErrors();
			serverW.next();
			
			//-- Select server profile (Remote)
			NewServerAdapterPage ap = new NewServerAdapterPage(serverW);
			ap.setProfile(Profile.REMOTE);
			//Remote server can be configured without local runtime if runtime is not specified
			if(config.getRuntime() == null)
				ap.setAssignRuntime(false);
			serverW.next();
			
			if(config.getRuntime() != null){
				//create new runtime
				setupRuntime(serverW);
				serverW.next();
			}
			
			NewServerRSIWizardPage rsp = new NewServerRSIWizardPage(serverW);
			rsp.setRemoteServerHome(config.getRemote().getServerHome());
			rsp.selectHost(config.getRemote().getServerHost()); //host was configured in setupRemoteSystem 
			serverW.finish();
			
		} catch(RuntimeException e) {
			serverW.cancel();
			throw e;
		} catch(AssertionError e) {
			serverW.cancel();
			throw e;
		}
	}

	@Override
	public void setDeclaration(JBossServer declaration) {
		this.server = declaration;
		
	}

	@Override
	public JBossServer getDeclaration() {
		return server;
	}

	@Override
	public void cleanUp() {
		if (server.cleanup() && config != null){
			removeServerAndRuntime();
		}
		if (server.cleanup() && config.getRemote() != null) {
			removeRemoteConnection();
		}
		
	}
	
	public void removeRemoteConnection() {
		SystemViewPart sview = new SystemViewPart();
		sview.open();
		RemoteServerConfiguration localConfig = config.getRemote();
		String host = localConfig.getServerHostName() != null ? localConfig.getServerHostName() : localConfig.getServerHost();
		try {
			System system = sview.getSystem(host);
			system.delete();
		} catch (EclipseLayerException exc) {
			// no such remote system exists
		}
	}

	@Override
	public Class<ServerRequirementConfig> getConfigurationClass() {
		return ServerRequirementConfig.class;
	}

	@Override
	public void setConfiguration(ServerRequirementConfig configuration) {
		this.config = configuration;
		
	}

	@Override
	public ServerRequirementConfig getConfiguration() {
		return config;
	}

	@Override
	public String getServerName() {
		return config.getFamily().getLabel()+" "+config.getVersion()+" Server"; 
	}

	@Override
	public String getRuntimeName() {
		return config.getFamily().getLabel()+" "+config.getVersion()+" Runtime"; 
	}

}
