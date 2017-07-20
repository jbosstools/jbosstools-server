package org.jboss.tools.as.catalog.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GeneratePluginXmlCatalog {
	
	private static ArrayList<String> errors = new ArrayList<String>();
	private static ArrayList<XSDObject> xsdObjs  = new ArrayList<XSDObject>();
	private static ArrayList<DTDObject> dtObjs = new ArrayList<DTDObject>();
	
	private static final String PLUGIN_ROOT_DIR = "plugin.root.dir"; //$NON-NLS-1$
	private static final String MODE = "output.mode"; //$NON-NLS-1$
	private static final String MODE_DEBUG = "showErrors"; //$NON-NLS-1$
	private static final String MODE_GENERATE = "generateCatalog"; //$NON-NLS-1$
	
	
	
	public static void main(String[] args) {
		String mode = System.getProperty(MODE);
		if( mode == null )
			mode = MODE_GENERATE;
		
		if( mode.equals(MODE_GENERATE)) {
			System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
			System.out.println("<?eclipse version=\"3.2\"?>"); //$NON-NLS-1$
			System.out.println("<plugin>"); //$NON-NLS-1$
			System.out.println("   <extension"); //$NON-NLS-1$ 
			System.out.println("         point=\"org.eclipse.wst.xml.core.catalogContributions\">"); //$NON-NLS-1$
			System.out.println("       <catalogContribution>"); //$NON-NLS-1$
			
			System.out.println("<!-- DTDs -->"); //$NON-NLS-1$
			runDTDs(true);
			
			System.out.println("\n\n<!-- XSD -->"); //$NON-NLS-1$
			runXSDs(true);
			
			System.out.println("       </catalogContribution>"); //$NON-NLS-1$
			System.out.println("   </extension>"); //$NON-NLS-1$
			System.out.println("</plugin>"); //$NON-NLS-1$
		} else if( mode.equals(MODE_DEBUG)){
			runDTDs(false);
			runXSDs(false);
			runXSDErrors();
		} else {
			System.out.println("Usage: java -Dplugin.root.dir=/path/to/jbosstools-server/as/plugins/org.jboss.tools.as.catalog org.\\"); //$NON-NLS-1$
			System.out.println("       -Doutput.mode=[generateCatalog | showErrors]"); //$NON-NLS-1$
			System.out.println("       jboss.tools.as.catalog.internal.GeneratePluginXmlCatalog "); //$NON-NLS-1$
		}
	}
	
	private static void runXSDErrors() {
		// A list of namespaces that are common and are not an error. 
		List<String> commonNamespaces = new ArrayList<String>();
		commonNamespaces.add("http://xmlns.jcp.org/xml/ns/javaee"); //$NON-NLS-1$
		commonNamespaces.add("http://java.sun.com/xml/ns/javaee"); //$NON-NLS-1$
		commonNamespaces.add("http://java.sun.com/xml/ns/j2ee"); //$NON-NLS-1$
		commonNamespaces.add("http://java.sun.com/xml/ns/j2ee"); //$NON-NLS-1$
		commonNamespaces.add("http://java.sun.com/xml/ns/persistence"); //$NON-NLS-1$
		
		
		HashMap<String, XSDObject> duplicateUriMap = new HashMap<String, XSDObject>();
		
		Iterator<XSDObject> xsdIt = xsdObjs.iterator();
		while(xsdIt.hasNext()) {
			XSDObject o = xsdIt.next();
			if( duplicateUriMap.containsKey(o.getName())) {
				String f1Name = duplicateUriMap.get(o.getName()).file.getName();
				String f2Name = o.file.getName();
				if( !f1Name.equals(f2Name) && !commonNamespaces.contains(o.getName()))
					System.err.println( f1Name + " is a duplicate with " + f2Name + " and has name " + o.getName()); //$NON-NLS-1$  //$NON-NLS-2$
			}
			duplicateUriMap.put(o.getName(), o);
		}
		
		Iterator<String> errIt = errors.iterator();
		while(errIt.hasNext()) {
			System.err.println(errIt.next());
		}
	}
	
	private static void runXSDs(boolean printEntries) {
		String rootdir = System.getProperty(PLUGIN_ROOT_DIR);
		if( rootdir == null ) {
			rootdir = ""; //$NON-NLS-1$
		}
		
		File root = new File(new File(rootdir).getAbsolutePath());
		File schemas = new File(root, "schema"); //$NON-NLS-1$
		File xsd = new File(schemas, "xsd"); //$NON-NLS-1$
		List<File> all = Arrays.asList(xsd.listFiles());
		all.sort(new Comparator<File>() {
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for( File f : all) {
			if( !f.getName().equalsIgnoreCase(".gitignore")) { //$NON-NLS-1$
				XSDObject o = new XSDObject(f);
				if( o.valid ) {
					xsdObjs.add(o);
				} else {
					errors.add(f + " is invalid: " + (o.validException == null ? "null" : o.validException.getMessage())); //$NON-NLS-1$  //$NON-NLS-2$
				}
			}
		}
		

		// Now iterate through 
		Collections.sort(xsdObjs, new Comparator<XSDObject>() {
			public int compare(XSDObject o1, XSDObject o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		if( printEntries ) {
			Iterator<XSDObject> dtdIt = xsdObjs.iterator();
			while(dtdIt.hasNext()) {
				XSDObject o = dtdIt.next();
				if( o.valid ) {
					System.out.println(o.toString());
				}
			}
		}
	}
	
	private static class XSDObject {
		String contents = null;
		File file = null;
		boolean valid = true;
		Exception validException = null;
		String uri = null;
		public XSDObject(File f) {
			try {
				file = f;
				contents = getFileContents(f);
				toString();
			} catch(IOException ioe) {
				valid = false;
				validException = ioe;
			}
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("\t\t<uri name=\""); //$NON-NLS-1$
			sb.append(getName());
			sb.append("\" uri=\""); //$NON-NLS-1$
			sb.append(getUri());
			sb.append("\"/>"); //$NON-NLS-1$
			return sb.toString();
		}
		
		String getUri() {
			return "platform:/plugin/org.jboss.tools.as.catalog/schema/xsd/" + file.getName();  //$NON-NLS-1$
		}
		
		String getName() {
			if( uri != null ) {
				return uri;
			}
			int start = contents.indexOf("targetNamespace=\""); //$NON-NLS-1$
			if( start == -1 ) {
				start = contents.indexOf("xmlns=\""); //$NON-NLS-1$
			}
			if( start == -1 ) {
				valid = false;
				return ""; //$NON-NLS-1$
			}
			int subStart = contents.indexOf("\"", start); //$NON-NLS-1$
			int subEnd = contents.indexOf("\"", subStart+1); //$NON-NLS-1$
			uri = contents.substring(subStart+1, subEnd);
			
			if( "http://www.jboss.com/xml/ns/javaee".equals(uri)) { //$NON-NLS-1$
				uri = "http://www.jboss.org/j2ee/schema/" + file.getName(); //$NON-NLS-1$
			}
			
			return uri;
		}
	}
	
	private static void runDTDs(boolean printEntry) {
		String rootdir = System.getProperty(PLUGIN_ROOT_DIR);
		if( rootdir == null ) {
			rootdir = ""; //$NON-NLS-1$
		}

		File root = new File(new File(rootdir).getAbsolutePath());
		File schemas = new File(root, "schema"); //$NON-NLS-1$
		File dtd = new File(schemas, "dtd"); //$NON-NLS-1$
		File[] dtdFiles = dtd.listFiles();
		List<File> sorted = Arrays.asList(dtdFiles);
		sorted.sort(new Comparator<File>() {
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for( File f : sorted ) {
			try {
				handleDTD(f);
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		
		if( printEntry ) {
			// Now iterate through 
			Iterator<DTDObject> dtdIt = dtObjs.iterator();
			while(dtdIt.hasNext()) {
				DTDObject o = dtdIt.next();
				if( o.isValid ) {
					System.out.println(o.toString());
				}
			}
		}
	}
	
	private static class DTDObject {
		public String contents, toString;
		public boolean isValid = true;
		public String error = null;
		public File f;
		public DTDObject(File f) {
			try {
				this.f = f;
				this.contents = getFileContents(f);
				checkValid();
			} catch(IOException ioe) {
				isValid = false;
			}
		}
		
		public boolean isValid() {
			return isValid;
		}
		
		public String getError() {
			return error;
		}
		
		private void checkValid() {
			String publicString, webURL;
			int start = contents.indexOf("PUBLIC"); //$NON-NLS-1$
			if( start == -1 ) {
				String err = "Improper format: " + f.getAbsolutePath(); //$NON-NLS-1$
				error = err;
				isValid = false;
				return;
			}
			
			int publicOpenQuote = contents.indexOf("\"", start); //$NON-NLS-1$
			int publicCloseQuote = contents.indexOf("\"", publicOpenQuote+1); //$NON-NLS-1$
			publicString = contents.substring(publicOpenQuote+1, publicCloseQuote);
			
			int webURLOpenQuote = contents.indexOf("\"", publicCloseQuote+1); //$NON-NLS-1$
			int webURLCloseQuote = contents.indexOf("\"", webURLOpenQuote+1); //$NON-NLS-1$
			webURL = contents.substring(webURLOpenQuote+1, webURLCloseQuote);
			
			

			/*
			 
			 
		<public publicId="-//JBoss//DTD JBOSS JCA Config 5.0//EN"
				uri="dtd/jboss-ds_5_0.dtd"/>
		<system systemId="http://www.jboss.org/j2ee/dtd/jboss-ds_5_0.dtd"
  				uri="dtd/jboss-ds_5_0.dtd" />    
  				
			 
			 */
			
			String uri = "platform:/plugin/org.jboss.tools.as.catalog/schema/dtd/" + f.getName(); //$NON-NLS-1$
			StringBuffer sb = new StringBuffer();
			sb.append("\t\t\t<public publicId=\""); //$NON-NLS-1$
			sb.append(publicString);
			sb.append("\"\n\t\t\t\turi=\""); //$NON-NLS-1$
			sb.append(uri);
			sb.append("\"/>\n"); //$NON-NLS-1$
			sb.append("\t\t\t<system systemId=\""); //$NON-NLS-1$
			sb.append(webURL);
			sb.append("\"\n\t\t\t\turi=\""); //$NON-NLS-1$
			sb.append(uri);
			sb.append("\"/>\n"); //$NON-NLS-1$
			toString = sb.toString();
		}
		public String toString() {
			return toString;
		}
	}
	
	private static void handleDTD(File f) throws IOException {
		DTDObject o = new DTDObject(f);
		if( o.isValid()) {
			dtObjs.add(o);
		}
	}
	
	private static String getFileContents(File file) throws IOException {
		byte[] contents = getBytesFromFile(file);
		return new String(contents);
	}
	
	private static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        byte[] bytes = new byte[(int)file.length()];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        is.close();
        return bytes;
    }
}
