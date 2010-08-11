package org.jboss.ide.eclipse.as.ui.views;

import java.io.PrintWriter;


public class Group extends AbstractEntry {

	private String name;

	public Group(String name) {
		this.name = name;
	}

	public void write(PrintWriter writer) {
		Object[] children = getChildren(null);
		for (int i = 0; i < children.length; i++) {
			AbstractEntry entry = (AbstractEntry) children[i];
			entry.write(writer);
			writer.println();
		}
	}

	public String toString() {
		return name;
	}

}
