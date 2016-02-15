package org.jboss.tools.as.catalog.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

public class CopyReleasedSchemaToJBossOrg {

	public static void main(String[] args) {
		if( args.length < 2) {
			System.err.println("Usage:  java CopyReleasedSchemaToJBossOrg /home/user/apps/wildfly10/docs/schema /home/user/code/jboss.org.schema");
			return;
		}
		
		File releaseSchemaHome = new File(args[0]);
		File jbossOrgHome = new File(args[1]);
		
		File[] releasedSchemas = releaseSchemaHome.listFiles();
		for( int i = 0; i < releasedSchemas.length; i++ ) {
			File f = releasedSchemas[i];
			File[] matching = findMatchingFile(f, jbossOrgHome);
			if( matching != null ) {
				for( int j = 0; j < matching.length; j++ ) {
					System.out.println("File " + f.getAbsolutePath() + " matches " + matching[j].getAbsolutePath());
					copy(f, matching[j]);
				}
			} else {
				// There's no matching file, so let's try to look for similar named ones?
				System.out.println("No matching file for " + f.getAbsolutePath());
			}
		}
		
	}
	
	public static void copy(File source, File dest) {
		try {
			Files.copy(Paths.get(source.getAbsolutePath()), Paths.get(dest.getAbsolutePath()), 
					StandardCopyOption.REPLACE_EXISTING);
		} catch(IOException ioe) {
			System.err.println("Copy of " + source.getAbsolutePath() + " to " + dest.getAbsolutePath() + " has failed");
		}
	}
	
	public static File[] findMatchingFile(File f, File jbossOrgHome) {
		ArrayList<File> collector = new ArrayList<File>();
		findMatchingFile(f, jbossOrgHome, collector);
		return (File[]) collector.toArray(new File[collector.size()]);
	}
	public static void findMatchingFile(File f, File folder, ArrayList<File> collector) {
		String[] list = folder.list();
		if( Arrays.asList(list).contains(f.getName())) {
			collector.add(new File(folder, f.getName()));
		}
		for( int i = 0; i < list.length; i++ ) {
			if( new File(folder, list[i]).isDirectory()) {
				findMatchingFile(f, new File(folder, list[i]), collector);
			}
		}
	}

}
