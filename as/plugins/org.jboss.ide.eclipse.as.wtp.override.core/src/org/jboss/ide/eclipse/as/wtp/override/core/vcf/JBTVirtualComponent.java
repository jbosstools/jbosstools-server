package org.jboss.ide.eclipse.as.wtp.override.core.vcf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jst.j2ee.componentcore.util.EARVirtualRootFolder;
import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.jst.jee.application.ICommonModule;
import org.eclipse.wst.common.componentcore.internal.ComponentcoreFactory;
import org.eclipse.wst.common.componentcore.internal.ComponentcorePackage;
import org.eclipse.wst.common.componentcore.internal.ReferencedComponent;
import org.eclipse.wst.common.componentcore.internal.StructureEdit;
import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
import org.eclipse.wst.common.componentcore.internal.builder.DependencyGraphManager;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualComponent;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualFolder;
import org.eclipse.wst.common.componentcore.internal.util.IComponentImplFactory;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;

public class JBTVirtualComponent 
	extends VirtualComponent implements IJBTComponent, IComponentImplFactory {

	private IVirtualReference[] cachedReferences;
	private long depGraphModStamp;

	public JBTVirtualComponent() {
		super();
	}
	
	public JBTVirtualComponent(IProject aProject, IPath aRuntimePath) {
		super(aProject, aRuntimePath);
	}

	/*
	 * These methods allow this component to make new elements
	 */
	public IVirtualComponent createComponent(IProject aProject) {
		return new JBTVirtualComponent(aProject, new Path("/")); //$NON-NLS-1$
	}

	public IVirtualComponent createArchiveComponent(IProject aProject, String archiveLocation, IPath aRuntimePath) {
		return new JBTVirtualArchiveComponent(aProject, archiveLocation, aRuntimePath);
	}
	
	public IVirtualFolder createFolder(IProject aProject, IPath aRuntimePath) {
		return new VirtualFolder(aProject, aRuntimePath);
	}
	

	/*
	 * The following group of methods was 
	 * stolen / adapted from EarVirtualComponent. 
	 */
	
	public IVirtualReference[] getReferences() {
		
		IVirtualReference[] cached = getCachedReferences();
		if (cached != null)
			return cached;
		List<IVirtualReference> hardReferences = getHardReferences(this);
		if( shouldExposeLooseReferences()) {
			List dynamicReferences = getLooseArchiveReferences(this, hardReferences);
			if (dynamicReferences != null) {
				hardReferences.addAll(dynamicReferences);
			}
		}
		cachedReferences = (IVirtualReference[]) hardReferences.toArray(new IVirtualReference[hardReferences.size()]);
		return cachedReferences;
	}

	protected boolean shouldExposeLooseReferences() {
		return false;
	}
	
	// Returns cache if still valid or null
	public IVirtualReference[] getCachedReferences() {
		if (cachedReferences != null && checkIfStillValid())
			return cachedReferences;
		else
			depGraphModStamp = DependencyGraphManager.getInstance().getModStamp();
		return null;
	}

	private boolean checkIfStillValid() {
		return DependencyGraphManager.getInstance().checkIfStillValid(depGraphModStamp);
	}
	
	private static List<IVirtualReference> getHardReferences(IVirtualComponent earComponent) {
		StructureEdit core = null;
		List hardReferences = new ArrayList();
		try {
			core = StructureEdit.getStructureEditForRead(earComponent.getProject());
			if (core != null && core.getComponent() != null) {
				WorkbenchComponent component = core.getComponent();
				if (component != null) {
					List referencedComponents = component.getReferencedComponents();
					for (Iterator iter = referencedComponents.iterator(); iter.hasNext();) {
						ReferencedComponent referencedComponent = (ReferencedComponent) iter.next();
						if (referencedComponent == null)
							continue;
						IVirtualReference vReference = createVirtualReference(earComponent, referencedComponent);
						if (vReference != null) {
							IVirtualComponent referencedIVirtualComponent = vReference.getReferencedComponent();
							if (referencedIVirtualComponent != null && referencedIVirtualComponent.exists()) {
								String archiveName = getArchiveName(referencedIVirtualComponent, referencedComponent);
								vReference.setArchiveName(archiveName);
								hardReferences.add(vReference);
							}
						}
					}
				}
			}
		} finally {
			if (core != null)
				core.dispose();
		}
		return hardReferences;
	}
	
	protected static String getArchiveName(IVirtualComponent referencedIVirtualComponent, ReferencedComponent referencedComponent) {
		String archiveName = null;
		if (referencedComponent.getDependentObject() != null) {
			/*
			 * The getDependentObject part of this code needs explanation.
			 * The IVirtualComponent allows you to set some "dependent object" 
			 * if you wish. WTP projects often do in the case that there's a 
			 * deployment descriptor of some sort. In those cases, WTP's 
			 * "dependent object" is of type ICommonModule and is an EObject.
			 * This allows virtual component to override the default archive 
			 * name with one from a deployment descriptor. 
			 */
			archiveName = ((ICommonModule) referencedComponent.getDependentObject()).getUri();
		} else {
			if( referencedIVirtualComponent instanceof IJBTComponent ) {
				archiveName = getJBTComponentArchiveName(referencedIVirtualComponent, referencedComponent);
			} else {
				archiveName = legacy_getWTPComponentArchiveName(referencedIVirtualComponent, referencedComponent);
			}
			
		}
		return archiveName;
	}
	
	protected static String getJBTComponentArchiveName(IVirtualComponent moduleComp, ReferencedComponent ref) {
		return ref.getArchiveName();
	}

	/*
	 * Legacy WTP implementation
	 */
	
	protected static String legacy_getWTPComponentArchiveName(IVirtualComponent moduleComp, ReferencedComponent ref) {
		String archiveName = null;
		if (moduleComp.isBinary()) {
			String uri = legacy_getJarURI(ref, moduleComp);
		} else if(ref.getArchiveName() != null){
			archiveName = ref.getArchiveName();
		} else {
			IProject referencedProject = moduleComp.getProject();
			if (JavaEEProjectUtilities.isDynamicWebProject(referencedProject) || J2EEProjectUtilities.isStaticWebProject(referencedProject)) {
				archiveName = moduleComp.getName() + IJ2EEModuleConstants.WAR_EXT;
			} else if (JavaEEProjectUtilities.isJCAProject(referencedProject)) {
				archiveName = moduleComp.getName() + IJ2EEModuleConstants.RAR_EXT;
			} else if (JavaEEProjectUtilities.isUtilityProject(referencedProject)) {
				archiveName = legacy_getJarURI(ref, moduleComp);
			} else {
				archiveName = moduleComp.getName() + IJ2EEModuleConstants.JAR_EXT;
			}
		}
		return archiveName;
	}
	
	protected static String legacy_getJarURI(ReferencedComponent ref, IVirtualComponent moduleComp) {
		String uri = null;
		if (uri == null || uri.length() < 0) {
			if(moduleComp.isBinary()){
				uri = new Path(moduleComp.getName()).lastSegment();
			} else {
				uri = moduleComp.getName() + IJ2EEModuleConstants.JAR_EXT;
			}
		} else {
			String prefix = ref.getRuntimePath().makeRelative().toString();
			if (prefix.length() > 0) {
				uri = prefix + "/" + uri; //$NON-NLS-1$
			}
		}
		return uri;
	}
	
	private static List getLooseArchiveReferences(JBTVirtualComponent component, List hardReferences) {
		return  getLooseArchiveReferences(component, hardReferences, null, (EARVirtualRootFolder)component.getRootFolder());
	}
		
	private static List getLooseArchiveReferences(JBTVirtualComponent component, List hardReferences, List dynamicReferences, EARVirtualRootFolder folder) {
		return null;
	}

	
	// Potentially to be overridden (awesome?)
	protected static IVirtualReference createVirtualReference(IVirtualComponent context, ReferencedComponent referencedComponent) {
		IReferenceResolver res = ReferenceResolverUtil.getDefault().getResolver(context, referencedComponent);
		return res.resolve(context, referencedComponent);
	}
	
	protected static ReferencedComponent createReferencedComponent(IVirtualReference reference) {
		IReferenceResolver res = ReferenceResolverUtil.getDefault().getResolver(reference);
		return res.resolve(reference);
	}
	
	/* *******************************************
	 * Overrides from VirtualComponent class
	 * These are mostly here because of the extrapolation 
	 * of createVirtualReference into its own method.
	 * *******************************************/
	@Override
	public IVirtualReference[] getAllReferences() { 
		StructureEdit core = null;
		List references = new ArrayList();
		try {
			core = StructureEdit.getStructureEditForRead(getProject());
			if (core!=null && core.getComponent()!=null) {
				WorkbenchComponent component = core.getComponent();
				if (component!=null) {
					List referencedComponents = component.getReferencedComponents();
					for (Iterator iter = referencedComponents.iterator(); iter.hasNext();) {
						ReferencedComponent referencedComponent = (ReferencedComponent) iter.next();
						if (referencedComponent==null) 
							continue;
						IVirtualReference vReference = createVirtualReference(this, referencedComponent);
						if( vReference != null ){
							vReference.setArchiveName( referencedComponent.getArchiveName() );
						}
						if (vReference != null && vReference.getReferencedComponent() != null)
							references.add(vReference); 
					}
				}
			}
			return (IVirtualReference[]) references.toArray(new IVirtualReference[references.size()]);
		} finally {
			if(core != null)
				core.dispose();
		}		
	}
	
	@Override
	protected ReferencedComponent getWorkbenchReferencedComponent(IVirtualReference aReference, WorkbenchComponent component) {
		if (aReference == null || aReference.getReferencedComponent() == null || component == null)
			return null;
		List referencedComponents = component.getReferencedComponents();
		URI uri = createReferencedComponent(aReference).getHandle(); 
		for (int i=0; i<referencedComponents.size(); i++) {
			ReferencedComponent ref = (ReferencedComponent) referencedComponents.get(i);
			if( ref.getHandle().equals(uri))
				return ref;
		}
		return null;
	}
	
	@Override
	public void addReferences(IVirtualReference[] references) {
		if (references==null || references.length==0)
			return;
		StructureEdit core = null;
		try {
			core = StructureEdit.getStructureEditForWrite(getProject());
			if (core == null)
				return;
			WorkbenchComponent component = core.getComponent();
			ReferencedComponent referencedComponent = null;
			ComponentcoreFactory factory = ComponentcorePackage.eINSTANCE.getComponentcoreFactory();
			for (int i=0; i<references.length; i++) {
				if (references[i] == null)
					continue;
				referencedComponent = createReferencedComponent(references[i]);
				if( referencedComponent != null ) 
					component.getReferencedComponents().add(referencedComponent);
			}
			//clean up any old obsolete references
			if (component != null){
				cleanUpReferences(component);
			}
		} finally {
			if(core != null) {
				core.saveIfNecessary(null);
				core.dispose();
			}
		}	
	}
	
	/* @Override */
	private void cleanUpReferences(WorkbenchComponent component) {
		List referencedComponents = component.getReferencedComponents();
		for (Iterator iter = referencedComponents.iterator(); iter.hasNext();) {
			ReferencedComponent referencedComponent = (ReferencedComponent) iter.next();
			if (referencedComponent==null) 
				continue;
			IVirtualReference vReference = createVirtualReference(this, referencedComponent);
			if (vReference == null || vReference.getReferencedComponent() == null || !vReference.getReferencedComponent().exists()){
				iter.remove();
			}
		}
	}
	
	@Override
	public void setReferences(IVirtualReference[] references) { 
		StructureEdit core = null;
		try {
			core = StructureEdit.getStructureEditForWrite(getProject());
			WorkbenchComponent component = core.getComponent();
			ReferencedComponent referencedComponent = null;
			  
			component.getReferencedComponents().clear();
			ComponentcoreFactory factory = ComponentcorePackage.eINSTANCE.getComponentcoreFactory();
			for (int i=0; i<references.length; i++) {
				referencedComponent = createReferencedComponent(references[i]);
				if( referencedComponent != null ) 
					component.getReferencedComponents().add(referencedComponent);
			}
		} finally {
			if(core != null) {
				core.saveIfNecessary(null);
				core.dispose();
			}
		}	
	}

}
