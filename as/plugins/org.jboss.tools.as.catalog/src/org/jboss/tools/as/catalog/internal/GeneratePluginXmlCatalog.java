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
	
	private static final String PLUGIN_ROOT_DIR = "plugin.root.dir";
	private static final String MODE = "output.mode";
	private static final String MODE_DEBUG = "showErrors";
	private static final String MODE_GENERATE = "generateCatalog";
	
	
	
	public static void main(String[] args) {
		String mode = System.getProperty(MODE);
		if( mode == null )
			mode = MODE_GENERATE;
		
		if( mode.equals(MODE_GENERATE)) {
			System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			System.out.println("<?eclipse version=\"3.2\"?>");
			System.out.println("<plugin>");
			System.out.println("   <extension");
			System.out.println("         point=\"org.eclipse.wst.xml.core.catalogContributions\">");
			System.out.println("       <catalogContribution>");
			
			System.out.println("<!-- DTDs -->");
			runDTDs(true);
			
			System.out.println("\n\n<!-- XSD -->");
			runXSDs(true);
			
			System.out.println("       </catalogContribution>");
			System.out.println("   </extension>");
			System.out.println("</plugin>");
		} else if( mode.equals(MODE_DEBUG)){
			runDTDs(false);
			runXSDs(false);
			runXSDErrors();
		} else {
			System.out.println("Usage: java -Dplugin.root.dir=/path/to/jbosstools-server/as/plugins/org.jboss.tools.as.catalog org.\\");
			System.out.println("       -Doutput.mode=[generateCatalog | showErrors]");
			System.out.println("       jboss.tools.as.catalog.internal.GeneratePluginXmlCatalog ");
		}
	}
	
	private static void runXSDErrors() {
		// A list of namespaces that are common and are not an error. 
		List<String> commonNamespaces = new ArrayList<String>();
		commonNamespaces.add("http://xmlns.jcp.org/xml/ns/javaee");
		commonNamespaces.add("http://java.sun.com/xml/ns/javaee");
		commonNamespaces.add("http://java.sun.com/xml/ns/j2ee");
		commonNamespaces.add("http://java.sun.com/xml/ns/j2ee");
		commonNamespaces.add("http://java.sun.com/xml/ns/persistence");
		
		
		HashMap<String, XSDObject> duplicateUriMap = new HashMap<String, XSDObject>();
		
		Iterator<XSDObject> xsdIt = xsdObjs.iterator();
		while(xsdIt.hasNext()) {
			XSDObject o = xsdIt.next();
			if( duplicateUriMap.containsKey(o.getName())) {
				String f1Name = duplicateUriMap.get(o.getName()).file.getName();
				String f2Name = o.file.getName();
				if( !f1Name.equals(f2Name) && !commonNamespaces.contains(o.getName()))
					System.err.println( f1Name + " is a duplicate with " + f2Name + " and has name " + o.getName());
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
			rootdir = "";
		}
		
		File root = new File(new File(rootdir).getAbsolutePath());
		File schemas = new File(root, "schema");
		File xsd = new File(schemas, "xsd");
		List<File> all = Arrays.asList(xsd.listFiles());
		all.sort(new Comparator<File>() {
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for( File f : all) {
			if( !f.getName().equalsIgnoreCase(".gitignore")) {
				XSDObject o = new XSDObject(f);
				if( o.valid ) {
					xsdObjs.add(o);
				} else {
					errors.add(f + " is invalid: " + (o.validException == null ? "null" : o.validException.getMessage()));
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
			sb.append("\t\t<uri name=\"");
			sb.append(getName());
			sb.append("\" uri=\"");
			sb.append(getUri());
			sb.append("\"/>");
			return sb.toString();
		}
		
		String getUri() {
			String uri = "platform:/plugin/org.jboss.tools.as.catalog/schema/xsd/" + file.getName();
			return uri;
		}
		
		String getName() {
			if( uri != null ) {
				return uri;
			}
			int start = contents.indexOf("targetNamespace=\"");
			if( start == -1 ) {
				start = contents.indexOf("xmlns=\"");
			}
			if( start == -1 ) {
				valid = false;
				return "";
			}
			int subStart = contents.indexOf("\"", start);
			int subEnd = contents.indexOf("\"", subStart+1);
			uri = contents.substring(subStart+1, subEnd);
			
			if( "http://www.jboss.com/xml/ns/javaee".equals(uri)) {
				uri = "http://www.jboss.org/j2ee/schema/" + file.getName();
			}
			
			return uri;
		}
	}
	
	private static void runDTDs(boolean printEntry) {
		String rootdir = System.getProperty(PLUGIN_ROOT_DIR);
		if( rootdir == null ) {
			rootdir = "";
		}

		File root = new File(new File(rootdir).getAbsolutePath());
		File schemas = new File(root, "schema");
		File dtd = new File(schemas, "dtd");
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
			int start = contents.indexOf("PUBLIC");
			if( start == -1 ) {
				String err = "Improper format: " + f.getAbsolutePath();
				error = err;
				isValid = false;
				return;
			}
			
			
			int end = contents.indexOf(">", start+1);
			String substr = contents.substring(start, end);
			//System.out.println(substr);
			
			int publicOpenQuote = contents.indexOf("\"", start);
			int publicCloseQuote = contents.indexOf("\"", publicOpenQuote+1);
			publicString = contents.substring(publicOpenQuote+1, publicCloseQuote);
			
			int webURLOpenQuote = contents.indexOf("\"", publicCloseQuote+1);
			int webURLCloseQuote = contents.indexOf("\"", webURLOpenQuote+1);
			webURL = contents.substring(webURLOpenQuote+1, webURLCloseQuote);
			
			

			/*
			 
			 
		<public publicId="-//JBoss//DTD JBOSS JCA Config 5.0//EN"
				uri="dtd/jboss-ds_5_0.dtd"/>
		<system systemId="http://www.jboss.org/j2ee/dtd/jboss-ds_5_0.dtd"
  				uri="dtd/jboss-ds_5_0.dtd" />    
  				
			 
			 */
			
			String uri = "platform:/plugin/org.jboss.tools.as.catalog/schema/dtd/" + f.getName();
			StringBuffer sb = new StringBuffer();
			sb.append("\t\t\t<public publicId=\"");
			sb.append(publicString);
			sb.append("\"\n\t\t\t\turi=\"");
			sb.append(uri);
			sb.append("\"/>\n");
			sb.append("\t\t\t<system systemId=\"");
			sb.append(webURL);
			sb.append("\"\n\t\t\t\turi=\"");
			sb.append(uri);
			sb.append("\"/>\n");
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
