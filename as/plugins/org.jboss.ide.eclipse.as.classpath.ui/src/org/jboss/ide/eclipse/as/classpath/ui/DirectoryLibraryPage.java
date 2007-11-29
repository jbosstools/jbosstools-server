package org.jboss.ide.eclipse.as.classpath.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jboss.ide.eclipse.as.classpath.core.DirectoryLibraryContainerInitializer;

public class DirectoryLibraryPage extends NewElementWizardPage implements
		IClasspathContainerPage, IClasspathContainerPageExtension {
	private IProject ownerProject;
	private String libsProjectName;
	private Combo projectsCombo;

	public DirectoryLibraryPage() {
		super("DirectoryLibrariesContainerPage"); //$NON-NLS-1$

		setTitle("Directory library");
		setDescription("A classpath container that automatically adds all .jar and .zip files found in a directory to the classpath.");
	}

	public IClasspathEntry getSelection() {
		IPath path = new Path(DirectoryLibraryContainerInitializer.CONTAINER_ID);

		final int index = this.projectsCombo.getSelectionIndex();
		final String selectedProjectName = this.projectsCombo.getItem(index);

		if (this.ownerProject == null
				|| !selectedProjectName.equals(this.ownerProject.getName())) {
			path = path.append(selectedProjectName);
		}

		return JavaCore.newContainerEntry(path);
	}

	public void setSelection(final IClasspathEntry cpentry) {
		final IPath path = cpentry == null ? null : cpentry.getPath();

		if (path == null || path.segmentCount() == 1) {
			if (this.ownerProject != null) {
				this.libsProjectName = this.ownerProject.getName();
			}
		} else {
			this.libsProjectName = path.segment(1);
		}
	}

	public void createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		final Label label = new Label(composite, SWT.NONE);
		label.setText("Project:");

		final String[] webProjects = getWebProjects();

		this.projectsCombo = new Combo(composite, SWT.READ_ONLY);
		this.projectsCombo.setItems(webProjects);

		final int index;

		if (this.ownerProject != null) {
			index = indexOf(webProjects, this.libsProjectName);
		} else {
			if (this.projectsCombo.getItemCount() > 0) {
				index = 0;
			} else {
				index = -1;
			}
		}

		if (index != -1) {
			this.projectsCombo.select(index);
		}

		final GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.minimumWidth = 100;

		this.projectsCombo.setLayoutData(gd);

		setControl(composite);
	}

	public boolean finish() {
		return true;
	}

	public void initialize(final IJavaProject project,
			final IClasspathEntry[] currentEntries) {
		this.ownerProject = (project == null ? null : project.getProject());
	}

	private static String[] getWebProjects() {
		return new String[] { "test" };
	}

	private static int indexOf(final String[] array, final String str) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(str)) {
				return i;
			}
		}

		return -1;
	}

}