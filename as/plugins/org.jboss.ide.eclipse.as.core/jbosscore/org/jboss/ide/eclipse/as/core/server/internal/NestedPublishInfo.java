package org.jboss.ide.eclipse.as.core.server.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.ModulePublishInfo;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;



public class NestedPublishInfo {
	public static final String PUBLISH_INFO = "publishInfo";
	public static final String INDEX = "index.properties";
	private static NestedPublishInfo instance;
	
	
	public static NestedPublishInfo getDefault() {
		if( instance == null ) {
			instance = new NestedPublishInfo();
		}
		return instance;
	}
	
	HashMap idToSPI;
	protected NestedPublishInfo() {
		idToSPI = new HashMap();
	}
	
	public NestedServerPublishInfo getServerPublishInfo(IServer server) {
		if( idToSPI.get(server.getId()) == null ) {
			idToSPI.put(server.getId(), new NestedServerPublishInfo());
		}
		return (NestedServerPublishInfo)idToSPI.get(server.getId());
	}
	
	public static class NestedServerPublishInfo {
	
		private HashMap<String, OpenedModulePublishInfo> idToModulePublishInfo;
		public NestedServerPublishInfo() {
			idToModulePublishInfo = new HashMap<String, OpenedModulePublishInfo>();
		}
		
		public ModulePublishInfo getPublishInfo(ArrayList ids, IModule module) {
			String id = getId(ids);
			ModulePublishInfo ret = idToModulePublishInfo.get(id);
			if( ret == null ) { 
				ret = new OpenedModulePublishInfo(id, module.getName(), module.getModuleType());
				idToModulePublishInfo.put(id, (OpenedModulePublishInfo) ret);
			}
			return ret;
		}
		
		// Takes a list of the nesting pattern for the modules 
		// followed by the desired module (which can share its current resources)
		public IModuleResourceDelta[] getDelta(ArrayList ids, IModule module) {
			return ((OpenedModulePublishInfo)getPublishInfo(ids, module)).getDelta(new IModule[]{module});
		}
		
		protected String getId(ArrayList ids) {
			String result = "";
			Iterator i = ids.iterator();
			while(i.hasNext()) {
				result += i.next().hashCode() + "" + Path.SEPARATOR;
			}
			return result;
		}
	}

	public static class OpenedModulePublishInfo extends ModulePublishInfo {
		
		public OpenedModulePublishInfo(DataInput in) throws IOException {
			super(in);
		}
		
		public OpenedModulePublishInfo(String moduleId, String name, IModuleType moduleType) {
			super(moduleId, name, moduleType);
		}
		
		public IModuleResourceDelta[] getDelta(IModule[] module) {
			return super.getDelta(module);
		}
		
		public void load(DataInput in) throws IOException {
			super.load(in);
		}
		
		public void save(DataOutput out) {
			super.save(out);
		}
	}
}
