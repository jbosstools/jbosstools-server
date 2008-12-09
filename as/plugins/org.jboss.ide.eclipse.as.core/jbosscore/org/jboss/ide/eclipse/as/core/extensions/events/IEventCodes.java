package org.jboss.ide.eclipse.as.core.extensions.events;


public interface IEventCodes {
	public static final int MAJOR_TYPE_MASK = 0xFF << 24;
	public static final int POLLING_CODE = 1 << 24;
	public static final int PUBLISHING_CODE = 2 << 24;
	public static final int POLLER_MASK = 0xFF << 16;
	public static final int PUBLISHER_MASK = 0xFF << 16;
	public static final int FULL_POLLER_MASK = MAJOR_TYPE_MASK | POLLER_MASK;
	public static final int FULL_PUBLISHER_MASK = MAJOR_TYPE_MASK | PUBLISHER_MASK;

	/* Polling */
	public static final int POLLING_ROOT_CODE = POLLING_CODE | (1 << 16);
	public static final int JMXPOLLER_CODE = POLLING_CODE | (2 << 16); 
	public static final int BEHAVIOR_STATE_CODE = POLLING_CODE | (3 << 16);
	
	// Polling.Behavior Codes
	public static final int BEHAVIOR_PROCESS_TERMINATED = BEHAVIOR_STATE_CODE | 1;
	public static final int BEHAVIOR_FORCE_STOP = BEHAVIOR_STATE_CODE | 2;
	public static final int BEHAVIOR_FORCE_STOP_FAILED = BEHAVIOR_STATE_CODE | 3;
	
	// Publishing
	public static final int PUBLISHING_ROOT_CODE = PUBLISHING_CODE | (1 << 16);
	public static final int JST_PUBLISHER_CODE = PUBLISHING_CODE | (2 << 16); 
	public static final int SINGLE_FILE_PUBLISHER_CODE = PUBLISHING_CODE | (3 << 16);
	public static final int ADD_DEPLOYMENT_FOLDER = PUBLISHING_CODE | (4<<16);

	// Publishing.JST
	public static final int JST_PUB_FULL_SUCCESS = JST_PUBLISHER_CODE | 1;
	public static final int JST_PUB_FULL_FAIL = JST_PUBLISHER_CODE | 2;
	public static final int JST_PUB_INC_SUCCESS = JST_PUBLISHER_CODE | 3;
	public static final int JST_PUB_INC_FAIL = JST_PUBLISHER_CODE | 4;
	public static final int JST_PUB_REMOVE_SUCCESS = JST_PUBLISHER_CODE | 5;
	public static final int JST_PUB_REMOVE_FAIL = JST_PUBLISHER_CODE | 6;
	public static final int JST_PUB_COPY_BINARY_FAIL = JST_PUBLISHER_CODE | 7;
	public static final int JST_PUB_FILE_DELETE_FAIL = JST_PUBLISHER_CODE | 8;
	public static final int JST_PUB_ASSEMBLE_FAIL = JST_PUBLISHER_CODE | 9;
	
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
