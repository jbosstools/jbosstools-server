/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.model.impl;

import java.util.concurrent.CountDownLatch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.rsp.api.dao.JobHandle;
import org.jboss.tools.rsp.api.dao.JobProgress;
import org.jboss.tools.rsp.api.dao.JobRemoved;

/**
 * A wrapper representing a job executing on an RSP that this extension receives
 * status updates for.
 */
public class RspProgressJob extends Job {
	private IRsp rsp;
	private JobHandle jobHandle;

	private JobProgress progress;
	private JobRemoved jobRemoved;
	private int previousProgress = 0;

	private CountDownLatch latch;

	public RspProgressJob(IRsp rsp, JobHandle jobHandle) {
		super(jobHandle.getName());
		this.rsp = rsp;
		this.jobHandle = jobHandle;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		latch = newLatch();
		monitor.beginTask(jobHandle.getName(), 1000);
		while (!isDone() && !monitor.isCanceled()) {
			try {
				latch.await();
			} catch (InterruptedException ie) {
			}
			updateIndicator(monitor);
			if (monitor.isCanceled()) {
				onCancel();
			}
		}
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	public void onCancel() {
		rsp.getModel().getClient(rsp).getServerProxy().cancelJob(jobHandle);
	}

	private synchronized void updateIndicator(IProgressMonitor monitor) {
		if (this.progress != null) {
			double d = this.progress.getPercent();
			int newTotal = (int) Math.round(d * 10); // TODO verify?
			int newWorked = newTotal - this.previousProgress;
			monitor.worked(newWorked);
			newLatch();
		}
	}

	private synchronized CountDownLatch getLatch() {
		return latch;
	}

	private synchronized CountDownLatch newLatch() {
		latch = new CountDownLatch(1);
		return latch;
	}

	private synchronized void countdown() {
		if (latch != null)
			latch.countDown();
	}

	private synchronized boolean isDone() {
		return jobRemoved != null;
	}

	public synchronized void setJobProgress(JobProgress jp) {
		this.progress = jp;
		countdown();
	}

	public synchronized void setJobRemoved(JobRemoved jr) {
		this.jobRemoved = jr;
		countdown();
	}
}
