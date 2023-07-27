/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.types;

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.IRspCore;

/**
 * Provides the id, name, ports, icons, and download information for
 * the latest version of the Community Server Connector.
 */
public class CommunityServerConnector extends AbstractServerConnector {
    public CommunityServerConnector() {
        super("redhat-community-server-connector", "Community Server Connector by Red Hat",
                9000, 9500, "community-24x24.png",
                "https://download.jboss.org/jbosstools/adapters/snapshots/rsp-server-community/distributions/LATEST",
                "org.jboss.tools.rsp.community.distribution.latest.version",
                "org.jboss.tools.rsp.community.distribution.latest.url");
    }

    @Override
    protected IRsp createFallbackRsp(IRspCore core) {
        return getType(core).createRsp("0.23.5.Final", "https://download.jboss.org/jbosstools/adapters/snapshots/rsp-server-community/distributions/0.23.5.Final/org.jboss.tools.rsp.server.community.distribution-0.23.5.Final.zip");
    };

    @Override
    protected Image findIconForServerType(String serverType) {
        String fname = getIconFileForServerType(serverType);
        if( fname == null )
            return null;
        return loadIcon(fname);
    }

    private String getIconFileForServerType(String serverType) {
        if( serverType == null ) {
            return "server-light-24x24.png";
        }

        if( serverType.toLowerCase().indexOf("karaf") != -1) {
            return "karaf-24x24.png";
        }
        if( serverType.toLowerCase().indexOf("tomcat") != -1) {
            return "tomcat-24x24.png";
        }
        if( serverType.toLowerCase().indexOf("felix") != -1 ) {
            return "felix-24x24.png";
        }
        if( serverType.toLowerCase().indexOf("jetty") != -1 ) {
            return "jetty-24x24.png";
        }
        if( serverType.toLowerCase().indexOf("glassfish") != -1 ) {
            return "glassfish-24x24.png";
        }
        return "server-dark-24x24.png";
    }

}
