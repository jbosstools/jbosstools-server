/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util;

import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;



public interface IEventCodes {
	public static final int ISTATUS_CODE_OK = 0 << 29;
	public static final int ISTATUS_CODE_INFO = 1 << 29;
	public static final int ISTATUS_CODE_WARN = 2 << 29;
	public static final int ISTATUS_CODE_ERROR = 3 << 29;

	public static final int ISTATUS_MASK = 0xE0 << 24; // slot 31 and 32
	public static final int MAJOR_TYPE_MASK = 0x1F << 24; 
	public static final int PUBLISHING_CODE = 2 << 24;
	public static final int POLLING_CODE = IServerStatePoller.POLLING_CODE;
	public static final int POLLER_MASK = IServerStatePoller.POLLER_MASK;
	public static final int PUBLISHER_MASK = 0xFF << 16;
	public static final int FULL_POLLER_MASK = MAJOR_TYPE_MASK | POLLER_MASK;
	public static final int FULL_PUBLISHER_MASK = MAJOR_TYPE_MASK | PUBLISHER_MASK;

	/* Polling */
	public static final int POLLING_ROOT_CODE = POLLING_CODE | (1 << 16);
	public static final int JMXPOLLER_CODE = POLLING_CODE | (2 << 16); 
	public static final int BEHAVIOR_STATE_CODE = POLLING_CODE | (3 << 16);
	
	
	public static final int STATE_STARTED = 1;
	public static final int STATE_STOPPED = 0;
	public static final int STATE_TRANSITION = 2;

	
	// Polling.Behavior Codes
	public static final int BEHAVIOR_PROCESS_TERMINATED = BEHAVIOR_STATE_CODE | 1;
	public static final int BEHAVIOR_FORCE_STOP = BEHAVIOR_STATE_CODE | 2;
	public static final int BEHAVIOR_FORCE_STOP_FAILED = BEHAVIOR_STATE_CODE | 3;
	
	// Publishing
	public static final int PUBLISHING_ROOT_CODE = PUBLISHING_CODE | (1 << 16);
	public static final int SSH_PUBLISHING_ROOT_CODE = PUBLISHING_CODE | (1 << 15);
	public static final int JST_PUBLISHER_CODE = PUBLISHING_CODE | (2 << 16); 
	public static final int SINGLE_FILE_PUBLISHER_CODE = PUBLISHING_CODE | (3 << 16);
	public static final int ADD_DEPLOYMENT_FOLDER = PUBLISHING_CODE | (4<<16);
	public static final int SUSPEND_DEPLOYMENT_SCANNER = PUBLISHING_CODE | ISTATUS_CODE_WARN | (5<<16);
	public static final int RESUME_DEPLOYMENT_SCANNER = PUBLISHING_CODE | ISTATUS_CODE_WARN | (6<<16);
	public static final int DEPLOYMENT_SCANNER_TRANSITION_CANCELED = PUBLISHING_CODE | ISTATUS_CODE_WARN | (7<<16);
	public static final int NO_PUBLISHER_ROOT_CODE = PUBLISHING_CODE | ISTATUS_CODE_WARN | (8<<16);
	public static final int DEPLOYMENT_SCANNER_TRANSITION_FAILED = PUBLISHING_CODE | ISTATUS_CODE_ERROR | (9<<16);
	
	// Publishing.JST
	public static final int JST_PUB_FULL_SUCCESS = JST_PUBLISHER_CODE | 1;
	public static final int JST_PUB_FULL_FAIL = JST_PUBLISHER_CODE | ISTATUS_CODE_ERROR | 2;
	public static final int JST_PUB_INC_SUCCESS = JST_PUBLISHER_CODE | 3;
	public static final int JST_PUB_INC_FAIL = JST_PUBLISHER_CODE | ISTATUS_CODE_ERROR | 4;
	public static final int JST_PUB_REMOVE_SUCCESS = JST_PUBLISHER_CODE | 5;
	public static final int JST_PUB_REMOVE_FAIL = JST_PUBLISHER_CODE | ISTATUS_CODE_ERROR | 6;
	public static final int JST_PUB_COPY_BINARY_FAIL = JST_PUBLISHER_CODE | ISTATUS_CODE_ERROR | 7;
	public static final int JST_PUB_FILE_DELETE_FAIL = JST_PUBLISHER_CODE | ISTATUS_CODE_ERROR | 8;
	public static final int JST_PUB_ASSEMBLE_FAIL = JST_PUBLISHER_CODE | ISTATUS_CODE_ERROR | 9;
	
	//newer status codes
	public static final int JST_PUB_SUCCESS = JST_PUBLISHER_CODE | 10;
	public static final int JST_PUB_FAIL = JST_PUBLISHER_CODE | ISTATUS_CODE_ERROR | 11;
	
	
	
	// Publishing.single
	public static final int SINGLE_FILE_SUCCESS_MASK = 0x1;
	public static final int SINGLE_FILE_TYPE_MASK = 0x2;
	public static final int SINGLE_FILE_PUBLISH_FAIL = SINGLE_FILE_PUBLISHER_CODE | 0;
	public static final int SINGLE_FILE_PUBLISH_SUCCESS = SINGLE_FILE_PUBLISHER_CODE | 1;
	public static final int SINGLE_FILE_UNPUBLISH_FAIL = SINGLE_FILE_PUBLISHER_CODE | 2;
	public static final int SINGLE_FILE_UNPUBLISH_SUCCESS = SINGLE_FILE_PUBLISHER_CODE | 3;
	public static final int SINGLE_FILE_PUBLISH_MNF = SINGLE_FILE_PUBLISH_FAIL | 16;
	public static final int SINGLE_FILE_UNPUBLISH_MNF = SINGLE_FILE_UNPUBLISH_FAIL | 16;
	
	// Add deployment folder
	public static final int ADD_DEPLOYMENT_FOLDER_FAIL = ADD_DEPLOYMENT_FOLDER | 1;
}
