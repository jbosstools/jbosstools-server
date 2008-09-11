/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.extensions.descriptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;

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
		try {
			String[] files = getFilter().getIncludedFiles();
			String fileLoc;
			ArrayList<XPathFileResult> resultList = new ArrayList<XPathFileResult>();
			List<Node> nodeList = null;
			for( int i = 0; i < files.length; i++ ) {
				fileLoc = new Path(baseDir).append(files[i]).toOSString();
				Document d = getRepository().getDocument(fileLoc);
				if( d != null )
					nodeList = d.selectNodes(xpathPattern);
				if( nodeList != null && nodeList.size() > 0 ) 
					resultList.add(new XPathFileResult(this, fileLoc, nodeList));
			}
			results = resultList.toArray(new XPathFileResult[resultList.size()]);
		} catch( IllegalStateException ise ) {
			// cannot load  TODO log?
			results = new XPathFileResult[0];
		}
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
