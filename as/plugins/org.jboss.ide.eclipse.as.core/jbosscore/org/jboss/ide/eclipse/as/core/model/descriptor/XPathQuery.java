package org.jboss.ide.eclipse.as.core.model.descriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.core.model.descriptor.XPathFileResult.XPathResultNode;

/**
 * A simple value object to hold the XPath query data
 * @author rstryker@redhat.com
 *
 */
public class XPathQuery implements Serializable {
	private static final long serialVersionUID = 1L;

	/*
	 * XPath-important fields
	 */
	protected String name;
	protected String baseDir;
	protected String filePattern;
	protected String xpathPattern;
	protected String attribute;
	
	/*
	 * The filter, need not be saved on serialize
	 */
	protected transient AntFileFilter filter;
	
	/*
	 * The file results, need not be saved on serialize
	 */
	protected transient XPathFileResult[] results;
	protected transient XPathCategory category;
	protected transient XMLDocumentRepository repository = null;
	
	public XPathQuery(String name, List list) {
		this.name = name;
		this.baseDir = list.get(0).equals(XPathModel.EMPTY_STRING) ? null : (String)list.get(0);
		this.filePattern = list.get(1).equals(XPathModel.EMPTY_STRING) ? null : (String)list.get(1);
		this.xpathPattern = list.get(2).equals(XPathModel.EMPTY_STRING) ? null : (String)list.get(2);
		this.attribute = list.size() < 3 || list.get(3).equals(XPathModel.EMPTY_STRING) ? null : (String)list.get(3);			
	}
	public XPathQuery(String name, String baseDir, String filePattern, String xpathPattern, String attribute) {
		this.name = name;
		this.baseDir = baseDir;
		this.filePattern = filePattern;
		this.xpathPattern = xpathPattern;
		this.attribute = attribute;
		this.results = null;
	}
	protected AntFileFilter getFilter() {
		if( filter == null ) 
			filter = new AntFileFilter(baseDir, filePattern);
		return filter;
	}
	public void refresh() {
		String[] files = getFilter().getIncludedFiles();
		boolean changed = false;
		for( int i = 0; i < files.length; i++ ) {
			changed = changed || getRepository().refresh(new Path(baseDir).append(files[i]).toOSString());
		}
		if( changed ) {
			results = null;
		}
	}
	
	public XPathFileResult[] getResults() {
		if( results == null ) 
			loadResults();
		return results;
	}
	
	public boolean resultsLoaded() {
		return results == null ? false : true;
	}

	protected void loadResults() {
		String[] files = getFilter().getIncludedFiles();
		String fileLoc;
		ArrayList resultList = new ArrayList();
		List nodeList;
		for( int i = 0; i < files.length; i++ ) {
			fileLoc = new Path(baseDir).append(files[i]).toOSString();
			nodeList = getRepository().getDocument(fileLoc).selectNodes(xpathPattern);
			if( nodeList.size() > 0 ) 
				resultList.add(new XPathFileResult(this, fileLoc, nodeList));
		}
		results = (XPathFileResult[]) resultList.toArray(new XPathFileResult[resultList.size()]);
	}
	
	public String getFirstResult() {
		XPathFileResult[] fileResults = getResults();
		if( fileResults.length > 0 ) {
			XPathResultNode[] nodes = fileResults[0].getChildren();
			if( nodes.length > 0 ) {
				return nodes[0].getText();
			}
		}
		return null;
	}
	/*
	 * Field Getters and setters
	 */
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFilePattern() {
		return filePattern;
	}
	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}
	public String getXpathPattern() {
		return xpathPattern;
	}
	public void setXpathPattern(String xpathPattern) {
		this.xpathPattern = xpathPattern;
	}
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public String getBaseDir() {
		return baseDir;
	}
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}
	public XPathCategory getCategory() {
		return category;
	}
	public void setCategory(XPathCategory category) {
		this.category = category;
	}
	public void setRepository(XMLDocumentRepository repo) {
		this.repository = repo;
	}
	protected XMLDocumentRepository getRepository() {
		return repository == null ? XMLDocumentRepository.getDefault() : repository;
	}
}
