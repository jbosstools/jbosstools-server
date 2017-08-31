/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.reddeer.server.family;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum JBossFamily {
	
	@JsonProperty("AS")
	AS("JBoss Community", "JBoss AS"),
	@JsonProperty("WILDFLY")
	WILDFLY("JBoss Community", "WildFly"),
	@JsonProperty("EAP")
	EAP("Red Hat JBoss Middleware", "Red Hat JBoss Enterprise Application Platform");
	
	private final String category;
	private final String label;
	
	private JBossFamily(String category, String label) {
		this.category = category;
		this.label = label;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getLabel() {
		return label;
	}

}
