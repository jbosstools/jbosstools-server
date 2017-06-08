package org.jboss.tools.jmx.core;

public interface IAsyncRefreshable {
	public interface ICallback {
		public void refreshComplete();
	}
	public void refresh(ICallback cb);
}
