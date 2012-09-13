package org.jboss.ide.eclipse.as.core.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.DownloadRuntime;

public class DownloadRuntimeToWTPRuntime implements IJBossToolingConstants {
	private static HashMap<String, String[]> map;
	static {
		map = new HashMap<String, String[]>();
		map.put(AS_32, new String[]{DOWNLOAD_RT_328});
		map.put(AS_40, new String[]{DOWNLOAD_RT_405});
		map.put(AS_42, new String[]{DOWNLOAD_RT_423});
		map.put(AS_50, new String[]{DOWNLOAD_RT_501});
		map.put(AS_51, new String[]{DOWNLOAD_RT_510});
		map.put(AS_60, new String[]{DOWNLOAD_RT_610});
		map.put(AS_70, new String[]{DOWNLOAD_RT_701, DOWNLOAD_RT_702});
		map.put(AS_71, new String[]{DOWNLOAD_RT_710, DOWNLOAD_RT_711});
		// NEW_SERVER_ADAPTER
	}
	
	
	public static IRuntimeType getWTPRuntime(DownloadRuntime rt) {
		Iterator<String> keyIt = map.keySet().iterator();
		while(keyIt.hasNext()) {
			String k = keyIt.next();
			String[] val = map.get(k);
			if( Arrays.asList(val).contains(rt.getId())) {
				return ServerCore.findRuntimeType(k);
			}
		}
		return null;
	}
	
	public static DownloadRuntime[] getDownloadRuntimes(IRuntimeType type) {
		Map<String, DownloadRuntime> dlRuntimes = RuntimeCoreActivator.getDefault().getDownloadRuntimes();
		return getDownloadRuntimes(type, dlRuntimes);
	}
	
	public static DownloadRuntime[] getDownloadRuntimes(IRuntimeType type, DownloadRuntime[] dlRuntimes) {
		HashMap<String, DownloadRuntime> map = new HashMap<String, DownloadRuntime>();
		for( int i = 0; i < dlRuntimes.length; i++ ) {
			map.put(dlRuntimes[i].getId(), dlRuntimes[i]);
		}
		return getDownloadRuntimes(type, map);
	}
	
	public static DownloadRuntime[] getDownloadRuntimes(IRuntimeType type, Map<String, DownloadRuntime> dlRuntimes) {
		String[] all = map.get(type.getId());
		ArrayList<DownloadRuntime> ret = new ArrayList<DownloadRuntime>();
		for( int i = 0; i < all.length;i++ ) {
			DownloadRuntime r = dlRuntimes.get(all[i]);
			if( r != null )
				ret.add(r);
		}
		return (DownloadRuntime[]) ret.toArray(new DownloadRuntime[ret.size()]);
		
	}
}
