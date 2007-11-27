/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class PackageTypeSearcher {

	private String packageName;
	private String remainder;
	private String fullString;
	private ResultFilter filter;
	public PackageTypeSearcher(String string) {
		fullString = string;
		int lastDot = string.lastIndexOf(".");
		if( lastDot == -1 ) {
			packageName = string;
			remainder = null;
		} else {
			packageName = string.substring(0, lastDot);
			remainder = string.substring(lastDot+1);
		}
	}
	
	public PackageTypeSearcher(String string, ResultFilter filter) {
		this(string);
		this.filter = filter;
	}
	
	public interface ResultFilter {
		public boolean accept(Object found);
	}
	
	public ArrayList getPackageProposals() {

		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchPattern packagePattern = SearchPattern.createPattern(fullString, IJavaSearchConstants.PACKAGE,
			IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PREFIX_MATCH);

		if (packagePattern == null)
			return new ArrayList();

		SearchEngine searchEngine = new SearchEngine();

		LocalTextfieldSearchRequestor requestor = new LocalTextfieldSearchRequestor();
		try {
			searchEngine.search(packagePattern, new SearchParticipant[]
		           {SearchEngine.getDefaultSearchParticipant()}, scope, requestor, new NullProgressMonitor());

			ArrayList results = requestor.getResults();
			Collections.sort(results, new Comparator() {
				
				public int compare(Object o1, Object o2) {
		              if (!(o1 instanceof IPackageFragment))
		                  return 0;
		               if (!(o2 instanceof IPackageFragment))
		                  return 0;

	             	IPackageFragment o1a = (IPackageFragment) o1;
	            	IPackageFragment o2a = (IPackageFragment) o2;
	            	return o1a.getElementName().compareTo(o2a.getElementName());
				}
			});

        return results;
		}
		catch (CoreException ce) {

		}
		return new ArrayList();
	}
	   
	   private class LocalTextfieldSearchRequestor extends SearchRequestor {
		      private ArrayList results;

		      public LocalTextfieldSearchRequestor() {
		         results = new ArrayList();
		      }

		      public void acceptSearchMatch(SearchMatch match) throws CoreException {
		    	  if( filter == null || filter.accept(match.getElement()))
		    		  results.add(match.getElement());
		      }

		      @Override
		    public void beginReporting() {
		    	// TODO Auto-generated method stub
		    	super.beginReporting();
		    }
		      public void endReporting() {
		      }

		      public ArrayList getResults() {
		         return results;
		      }
		   }
	   
	   public IPackageFragment getPackage() {
	      IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
	      SearchPattern packagePattern = SearchPattern.createPattern(packageName, IJavaSearchConstants.PACKAGE,
	            IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);

	      if (packagePattern == null)
	         return null;

	      SearchEngine searchEngine = new SearchEngine();

	      LocalTextfieldSearchRequestor requestor = new LocalTextfieldSearchRequestor();
	      try {
	         searchEngine.search(packagePattern, new SearchParticipant[]
	         {SearchEngine.getDefaultSearchParticipant()}, scope, requestor, new NullProgressMonitor());

	         ArrayList results = requestor.getResults();
	         if (results.size() != 1) // TODO: there can be multiple packagefragments for the same name in a workspace
	            return null;

	         return (IPackageFragment) results.get(0);
	      }
	      catch (CoreException ce)  {

	      }
	      return null;
	   }
	   
	   
	   public ArrayList getTypeMatches()  {
		  IPackageFragment packageElement = getPackage();
	      if (packageElement != null && remainder != null) {
	         try  {
	            IClassFile[] classFiles = packageElement.getClassFiles();
	            ICompilationUnit[] compUnits = packageElement.getCompilationUnits();
	            ArrayList returnList = new ArrayList();

	            for (int i = 0; i < classFiles.length; i++) {
	               String typeName = classFiles[i].getType().getElementName();
	               if (typeName.equals(""))
	                  continue;
	               if (typeName.toLowerCase().startsWith(remainder.toLowerCase()) && (filter == null || filter.accept(classFiles[i].getType())))
	                  returnList.add(classFiles[i].getType());
	            }

	            for (int i = 0; i < compUnits.length; i++) {
	               IType type = compUnits[i].findPrimaryType();
	               String typeName = type.getElementName();
	               if (typeName.toLowerCase().startsWith(remainder.toLowerCase()) && (filter == null || filter.accept(type))) 
	                  returnList.add(type);
	            }

	            return returnList;
	         }
	         catch (JavaModelException jme)  {
	         }
	      }

	      return new ArrayList();
	   }

	   public ICompletionProposal[] generateProposals(int beginIndex) {
		  return generateProposals(beginIndex, "", "");
	   }

	   public ICompletionProposal[] generateProposals(int beginIndex, 
			   		String proposalPrefix, String proposalSuffix) {
			ArrayList packages = getPackageProposals();
		    ArrayList types = getTypeMatches();

			   JavaUILabelProvider imageDelegate = new JavaUILabelProvider();

	      ArrayList list = new ArrayList();

	      for (Iterator i = types.iterator(); i.hasNext();) {
	         IType type = (IType) i.next();
	         String replaceString = proposalPrefix + type.getFullyQualifiedName() + proposalSuffix;
	         CompletionProposal p = new CompletionProposal(replaceString, beginIndex, fullString.length(), 
	        		 replaceString.length(), imageDelegate.getImage(type), type.getElementName(), null, null);
	         list.add(p);
	      }

	      for (Iterator i = packages.iterator(); i.hasNext();) {
	         IPackageFragment fragment = (IPackageFragment) i.next();
	         String replaceString = proposalPrefix + fragment.getElementName() + proposalSuffix;
	         CompletionProposal p = new CompletionProposal(replaceString, beginIndex, fullString.length(), 
	        		 replaceString.length(), imageDelegate.getImage(fragment), null, null, null);
	         list.add(p);
	      }

	      ICompletionProposal props[] = new ICompletionProposal[list.size()];
	      list.toArray(props);
	      return props;
	   }
}
