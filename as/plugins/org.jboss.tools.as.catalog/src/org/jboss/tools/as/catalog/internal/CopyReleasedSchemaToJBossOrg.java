package org.jboss.tools.as.catalog.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CopyReleasedSchemaToJBossOrg {

	private static File dumpingGround;
	public static void main(String[] args) {
		if( args.length < 2) {
			System.err.println("Usage:  java CopyReleasedSchemaToJBossOrg /home/user/apps/wildfly10/docs/schema /home/user/code/jboss.org.schema [relativePathForUnMatched]");
			return;
		}
		
		File releaseSchemaHome = new File(args[0]);
		File jbossOrgHome = new File(args[1]);
		
		if( args.length == 3 ) {
			dumpingGround = new File(jbossOrgHome,args[2]);
		}

		
		File[] releasedSchemas = releaseSchemaHome.listFiles();
		for( int i = 0; i < releasedSchemas.length; i++ ) {
			File f = releasedSchemas[i];
			File[] matching = findMatchingFile(f, jbossOrgHome);
			if( matching != null && matching.length > 0) {
				for( int j = 0; j < matching.length; j++ ) {
					System.out.println("File " + f.getAbsolutePath() + " matches " + matching[j].getAbsolutePath());
					copy(f, matching[j]);
				}
			} else {
				// There's no matching file, so let's try to look for similar named ones?
				System.out.println("No matching file for " + f.getAbsolutePath());
				String fName = f.getName();
				Pattern r = Pattern.compile("(.*)[-_](\\d+[-_]\\d+).xsd");
				Matcher m = r.matcher(fName);
				if( m.find()) {
					String prefix =  m.group(1);//System.out.println("Found value: " + m.group(1));
					File[] possibleFolders = findFolderContaining(jbossOrgHome, prefix);
					System.out.println(possibleFolders.length);
					if( possibleFolders.length > 0 ) {
						for( int k = 0; k < possibleFolders.length; k++ ) {
							copy(f, new File(possibleFolders[k], f.getName()));
						}
					} else {
						// No corrolary exists even with pattern matching
						if( dumpingGround != null ) {
							copy(f, new File(dumpingGround, f.getName()));
						}
					}
				}
				
			}
		}
		
	}
	
	
	private static File[] findFolderContaining(File root, String prefix) {
		return findPossibleMatchingFolder(root, prefix);
	}
	
	public static void copy(File source, File dest) {
		if( !dest.getParentFile().exists()) {
			dest.getParentFile().mkdirs();
		}
		
		
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

	
	
	public static File[] findPossibleMatchingFolder(File jbossOrgHome, String prefix) {
		ArrayList<File> collector = new ArrayList<File>();
		findPossibleMatchingFolder(jbossOrgHome, prefix, collector);
		return (File[]) collector.toArray(new File[collector.size()]);
	}
	public static void findPossibleMatchingFolder(File folder, String prefix, ArrayList<File> collector) {
		String[] list = folder.list();
		for( int i = 0; i < list.length; i++ ) {
			if( list[i].startsWith(prefix)) {
				if( !collector.contains(folder)) {
					collector.add(folder);
				}
			}
			if( new File(folder, list[i]).isDirectory()) {
				findPossibleMatchingFolder(new File(folder, list[i]), prefix, collector);
			}
		}
	}

	
}
