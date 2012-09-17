package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

public class ProgressMonitorUtil {

	public static class CustomSubProgress extends SubProgressMonitor {
		public CustomSubProgress(IProgressMonitor monitor, int ticks, int style) {
			super(monitor, ticks, style);
		}
		public void beginTask(String name, int totalWork) {
			super.beginTask(null, totalWork);
			setTaskName(name);
		}
	}
	
	public static IProgressMonitor getSubMon(IProgressMonitor parent, int ticks) {
		IProgressMonitor subMon = new CustomSubProgress(parent, ticks, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		return subMon;
	}
	
	public static IProgressMonitor getMonitorFor(IProgressMonitor monitor) {
		if (monitor == null)
			return new NullProgressMonitor();
		return monitor;
	}
	
	   
    public static IProgressMonitor submon( final IProgressMonitor parent,
                                           final int ticks ) {
        return submon( parent, ticks, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL );
    }

    public static IProgressMonitor submon( final IProgressMonitor parent,
                                           final int ticks, final int style ) {
        return ( parent == null ? new NullProgressMonitor() : new SubProgressMonitor( parent, ticks, style ) );
    }
    
    public static void subTask( final IProgressMonitor monitor,
                                final String taskName ) {
        if( monitor != null ) {
            monitor.subTask( taskName );
        }
    }
    
    public static void checkIfCanceled( final IProgressMonitor monitor )
        throws InterruptedException  {
        if( monitor != null && monitor.isCanceled() ) {
            throw new InterruptedException();
        }
    }
}