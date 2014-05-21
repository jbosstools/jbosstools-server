package org.jboss.tools.as.test.core.classpath.modules;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.as.test.core.internal.utils.IOUtil;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;

public class MockJBossModulesUtil {
	public static final String OVERLAY = ".overlays";
	
	/* Creates a mock file structure first, then creates an IServer out of it */
	public static IServer createMockServerWithRuntime(String serverType, String name) {
		IServer s = ServerCreationTestUtils.createMockServerWithRuntime(serverType, name);
		return s;
	}
	
	public static IPath addLayer(IPath modulesRoot, String layerName, boolean addToLayersConf) throws IOException {
		IPath layerPath = modulesRoot.append("system").append("layers").append(layerName);
		layerPath.toFile().mkdirs();
		if( addToLayersConf) {
			IPath layersConf = modulesRoot.append("layers.conf");
			if( layersConf.toFile().exists()) {
				String contents = IOUtil.getContents(layersConf.toFile());
				if( contents.endsWith(",")) {
					contents = contents + layerName;
				} else {
					contents = contents + "," + layerName;
				}
				IOUtil.setContents(layersConf.toFile(), contents);
			} else {
				String contents = "layers=" + layerName;
				IOUtil.setContents(layersConf.toFile(), contents);
			}
		}
		return layerPath;
	}
	
	
	public static IPath addOverlay(IPath layerRoot, String overlayName) {
		IPath overlaysFolder = layerRoot.append(OVERLAY);
		overlaysFolder.toFile().mkdirs();
		IPath newOverlay = overlaysFolder.append(overlayName);
		newOverlay.toFile().mkdirs();
		return newOverlay;
	}
	
	public static void setActiveOverlays(IPath layerRoot, String[] overlayNames) throws IOException {
		IPath overlaysFolder = layerRoot.append(OVERLAY);
		overlaysFolder.toFile().mkdirs();
		IPath overlaysFile = overlaysFolder.append(OVERLAY);
		String imploded = implode("\n", overlayNames) + "\n";
		IOUtil.setContents(overlaysFile.toFile(), imploded);
	}
	
	public static void cloneModule(IPath layerRoot, String moduleId, IPath newBase) throws IOException {
		String asPath = moduleId.replaceAll("\\.", "/");
		IPath srcFolder = layerRoot.append(asPath);
		IPath destFolder = newBase.append(asPath);
		IOUtil.copyFolder(srcFolder.toFile(), destFolder.toFile());
	}

	public static void cloneModule(IPath layerRoot, String oldModuleId, IPath newBase, String moduleId) throws IOException {
		String oldModPath = oldModuleId.replaceAll("\\.", "/");
		IPath srcPath = layerRoot.append(oldModPath);
		
		String newModPath = moduleId.replaceAll("\\.", "/");
		IPath destFolder = newBase.append(newModPath);
		
		IOUtil.copyFolder(srcPath.toFile(), destFolder.toFile());
	}

	
	public static void duplicateToSlot(IPath layerRoot, String moduleId, String newSlot) throws IOException {
		duplicateToSlot(layerRoot, moduleId, "main", layerRoot, moduleId, newSlot);
	}
	
	
	public static void duplicateToSlot(IPath layerRoot, String oldModuleId, String oldSlot, IPath newLayerRoot, String moduleId, String newSlot) throws IOException {
		String oldModPath = oldModuleId.replaceAll("\\.", "/");
		IPath srcPath = layerRoot.append(oldModPath).append(oldSlot);
		
		String newModPath = moduleId.replaceAll("\\.", "/");
		IPath destFolder = newLayerRoot.append(newModPath).append(newSlot);
		
		IOUtil.copyFolder(srcPath.toFile(), destFolder.toFile());
	}

	
	public static String implode(String glue, String[] strArray) {
	    String ret = "";
	    for(int i=0;i<strArray.length;i++) {
	        ret += (i == strArray.length - 1) ? strArray[i] : strArray[i] + glue;
	    }
	    return ret;
	}
}
