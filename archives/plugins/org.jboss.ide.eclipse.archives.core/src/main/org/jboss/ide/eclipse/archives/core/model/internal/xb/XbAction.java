package org.jboss.ide.eclipse.archives.core.model.internal.xb;

public class XbAction extends XbPackageNodeWithProperties {
	private String time, type;
	
	public XbAction() {
		super();
	}

	public XbAction(XbAction action) {
		super(action);
		copyFrom(action);
	}
	
	public void copyFrom (XbAction node) {
		super.copyFrom(node);
		this.time = node.time == null ? null : new String(node.time);
		this.type = node.type == null ? null : new String(node.type);
	}
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
