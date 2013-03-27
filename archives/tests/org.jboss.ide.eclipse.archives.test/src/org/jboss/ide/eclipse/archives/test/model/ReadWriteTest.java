package org.jboss.ide.eclipse.archives.test.model;

import java.util.List;

import junit.framework.TestCase;

import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbFolder;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackage;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackages;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbProperty;

public class ReadWriteTest extends TestCase {
	/*
	 * We definitely need more tests like this. 
	 * This is not enough. 
	 */
	
	
	public void testReadWritePackageWithProperties() {
		XbPackages packs = new XbPackages();
		XbPackage pack = new XbPackage();
		pack.setName("name1");
		pack.setToDir("toDir1");
		packs.addChild(pack);
		XbProperty innerProp = new XbProperty();
		innerProp.setName("key5");
		innerProp.setValue("val5");
		pack.getProperties().addProperty(innerProp);
		
		String s = XBMarshallTest.writeToString(packs, true);
		XbPackages packs2 = XBUnmarshallTest.parseFromString(s, true, null);
		List packs2List = packs2.getChildren(XbPackage.class);
		assertEquals(1, packs2List.size());
		XbPackage pack2 = (XbPackage)packs2List.get(0);
		assertEquals(pack.getName(), pack2.getName());
		assertEquals(pack.getToDir(), pack2.getToDir());
		assertEquals(pack.getProperties().getProperties().get("key5"), "val5");
		assertEquals(pack2.getProperties().getProperties().get("key5"), "val5");
	}
	
	public void testUnmarshallDefectJBIDE13868ChildArchives() {
		XbPackages packs = new XbPackages();
		XbPackage earPack = new XbPackage();
		earPack.setName("rootEar");
		earPack.setToDir("toDir1");
		packs.addChild(earPack);
		
		XbPackage warPack = new XbPackage();
		warPack.setName("childWar");
		earPack.addChild(warPack);
		
		XbPackage jarPack = new XbPackage();
		jarPack.setName("utilJar");
		earPack.addChild(jarPack);

		String s = XBMarshallTest.writeToString(packs, true);
		XbPackages packs2 = XBUnmarshallTest.parseFromString(s, true, null);
		List packs2List = packs2.getChildren(XbPackage.class);
		assertEquals(1, packs2List.size());
		XbPackage earPack2 = (XbPackage)packs2List.get(0);
		assertEquals(earPack2.getName(), earPack.getName());
		
		List earChildren2 = earPack2.getChildren(XbPackage.class);
		assertNotNull(earChildren2);
		assertEquals(2, earChildren2.size());
		
		XbPackage child1 = (XbPackage)earChildren2.get(0);
		XbPackage child2 = (XbPackage)earChildren2.get(1);
		
		assertNotNull(child1);
		assertNotNull(child2);
		
		String c1Name = child1.getName();
		String c2Name = child2.getName();
		assertTrue(c1Name.equals("childWar") || c2Name.equals("childWar"));
		assertTrue(c1Name.equals("utilJar") || c2Name.equals("utilJar"));
		
	}


	public void testUnmarshallDefectJBIDE13868FolderWithChildArchives() {
		XbPackages packs = new XbPackages();
		XbPackage earPack = new XbPackage();
		earPack.setName("rootEar");
		earPack.setToDir("toDir1");
		packs.addChild(earPack);
		
		XbFolder folder = new XbFolder();
		folder.setName("insideEar");
		earPack.addChild(folder);
		
		XbPackage warPack = new XbPackage();
		warPack.setName("childWar");
		folder.addChild(warPack);
		
		XbPackage jarPack = new XbPackage();
		jarPack.setName("utilJar");
		folder.addChild(jarPack);

		String s = XBMarshallTest.writeToString(packs, true);
		XbPackages packs2 = XBUnmarshallTest.parseFromString(s, true, null);
		List packs2List = packs2.getChildren(XbPackage.class);
		assertEquals(1, packs2List.size());
		XbPackage earPack2 = (XbPackage)packs2List.get(0);
		assertEquals(earPack2.getName(), earPack.getName());
		
		List folderList = earPack2.getChildren(XbFolder.class);
		assertNotNull(folderList);
		assertEquals(1, folderList.size());
		XbFolder xbFolder = (XbFolder)folderList.get(0);
		
		
		List earChildren2 = xbFolder.getChildren(XbPackage.class);
		assertNotNull(earChildren2);
		assertEquals(2, earChildren2.size());
		
		XbPackage child1 = (XbPackage)earChildren2.get(0);
		XbPackage child2 = (XbPackage)earChildren2.get(1);
		
		assertNotNull(child1);
		assertNotNull(child2);
		
		String c1Name = child1.getName();
		String c2Name = child2.getName();
		assertTrue(c1Name.equals("childWar") || c2Name.equals("childWar"));
		assertTrue(c1Name.equals("utilJar") || c2Name.equals("utilJar"));
	}


	public void testUnmarshallDefectJBIDE13868FolderWithChildFolders() {
		XbPackages packs = new XbPackages();
		XbPackage earPack = new XbPackage();
		earPack.setName("rootEar");
		earPack.setToDir("toDir1");
		packs.addChild(earPack);
		
		XbFolder folder = new XbFolder();
		folder.setName("insideEar");
		earPack.addChild(folder);

		XbFolder cf1 = new XbFolder();
		cf1.setName("cf1");
		folder.addChild(cf1);

		XbFolder cf2 = new XbFolder();
		cf2.setName("cf2");
		folder.addChild(cf2);

		String s = XBMarshallTest.writeToString(packs, true);
		XbPackages packs2 = XBUnmarshallTest.parseFromString(s, true, null);
		List packs2List = packs2.getChildren(XbPackage.class);
		assertEquals(1, packs2List.size());
		XbPackage earPack2 = (XbPackage)packs2List.get(0);
		assertEquals(earPack2.getName(), earPack.getName());
		
		List folderList = earPack2.getChildren(XbFolder.class);
		assertNotNull(folderList);
		assertEquals(1, folderList.size());
		XbFolder xbFolder = (XbFolder)folderList.get(0);
		
		
		List nestedFolders = xbFolder.getChildren(XbFolder.class);
		assertNotNull(nestedFolders);
		assertEquals(2, nestedFolders.size());
		
		XbFolder child1 = (XbFolder)nestedFolders.get(0);
		XbFolder child2 = (XbFolder)nestedFolders.get(1);
		
		assertNotNull(child1);
		assertNotNull(child2);
		
		String c1Name = child1.getName();
		String c2Name = child2.getName();
		assertTrue(c1Name.equals("cf1") || c2Name.equals("cf1"));
		assertTrue(c1Name.equals("cf2") || c2Name.equals("cf2"));
	}
}
