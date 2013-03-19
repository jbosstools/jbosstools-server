/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.views.log.TailInputStream;

public class LogReader {
	private static final int SESSION_STATE = 10;
	public static final long MAX_FILE_LENGTH = 1024 * 1024;
	private static final int ENTRY_STATE = 20;
	private static final int SUBENTRY_STATE = 30;
	private static final int MESSAGE_STATE = 40;
	private static final int STACK_STATE = 50;
	private static final int TEXT_STATE = 60;
	private static final int UNKNOWN_STATE = 70;

	public static LogSession parseLogFile(File file, List<AbstractEntry> entries, IMemento memento) {
		if (file == null || !file.exists())
			return null;

//		if (memento.getString(LogView.P_USE_LIMIT).equals("true") //$NON-NLS-1$
//				&& memento.getInteger(LogView.P_LOG_LIMIT).intValue() == 0)
//			return null;

		List<AbstractEntry> parents = new ArrayList<AbstractEntry>();
		LogEntry current = null;
		LogSession session = null;
		int writerState = UNKNOWN_STATE;
		StringWriter swriter = null;
		PrintWriter writer = null;
		int state = UNKNOWN_STATE;
		LogSession currentSession = null;
		BufferedReader reader = null;
		try {

			reader = new BufferedReader(new InputStreamReader(new TailInputStream(file, MAX_FILE_LENGTH), "UTF-8")); //$NON-NLS-1$
			for (;;) {
				String line = reader.readLine();
				if (line == null)
					break;
				line = line.trim();

				if (line.startsWith("!SESSION")) { //$NON-NLS-1$
					state = SESSION_STATE;
				} else if (line.startsWith("!ENTRY")) { //$NON-NLS-1$
					state = ENTRY_STATE;
				} else if (line.startsWith("!SUBENTRY")) { //$NON-NLS-1$
					state = SUBENTRY_STATE;
				} else if (line.startsWith("!MESSAGE")) { //$NON-NLS-1$
					state = MESSAGE_STATE;
				} else if (line.startsWith("!STACK")) { //$NON-NLS-1$
					state = STACK_STATE;
				} else
					state = TEXT_STATE;

				if (state == TEXT_STATE) {
					if (writer != null)
						writer.println(line);
					continue;
				}

				if (writer != null) {
					setData(current, session, writerState, swriter);
					writerState = UNKNOWN_STATE;
					swriter = null;
					writer.close();
					writer = null;
				}

				if (state == STACK_STATE) {
					swriter = new StringWriter();
					writer = new PrintWriter(swriter, true);
					writerState = STACK_STATE;
				} else if (state == SESSION_STATE) {
					session = new LogSession();
					session.processLogLine(line);
					swriter = new StringWriter();
					writer = new PrintWriter(swriter, true);
					writerState = SESSION_STATE;
					currentSession = updateCurrentSession(currentSession, session);
					// if current session is most recent and not showing all sessions
//					if (currentSession.equals(session) && !memento.getString(LogView.P_SHOW_ALL_SESSIONS).equals("true")) //$NON-NLS-1$
//						entries.clear();
				} else if (state == ENTRY_STATE) {
					if (currentSession == null) { // create fake session if there was no any
						currentSession = new LogSession();
					}
					LogEntry entry = new LogEntry();
					entry.setSession(currentSession);
					entry.processEntry(line);
					setNewParent(parents, entry, 0);
					current = entry;
					addEntry(current, entries, memento);
				} else if (state == SUBENTRY_STATE) {
					if (parents.size() > 0) {
						LogEntry entry = new LogEntry();
						entry.setSession(session);
						int depth = entry.processSubEntry(line);
						setNewParent(parents, entry, depth);
						current = entry;
						LogEntry parent = (LogEntry) parents.get(depth - 1);
						parent.addChild(entry);
					}
				} else if (state == MESSAGE_STATE) {
					swriter = new StringWriter();
					writer = new PrintWriter(swriter, true);
					String message = ""; //$NON-NLS-1$
					if (line.length() > 8)
						message = line.substring(9).trim();
					message = message.trim();
					if (current != null)
						current.setMessage(message);
					writerState = MESSAGE_STATE;
				}
			}

			if (swriter != null && current != null && writerState == STACK_STATE) {
				writerState = UNKNOWN_STATE;
				current.setStack(swriter.toString());
			}
		} catch (FileNotFoundException e) { // do nothing
		} catch (IOException e) { // do nothing
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e1) { // do nothing
			}
			if (writer != null) {
				setData(current, session, writerState, swriter);
				writer.close();
			}
		}

		return currentSession;
	}

	/**
	 * Assigns data from writer to appropriate field of current Log Entry or Session,
	 * depending on writer state.
	 */
	private static void setData(LogEntry current, LogSession session, int writerState, StringWriter swriter) {
		if (writerState == STACK_STATE && current != null) {
			current.setStack(swriter.toString());
		} else if (writerState == SESSION_STATE && session != null) {
			session.setSessionData(swriter.toString());
		} else if (writerState == MESSAGE_STATE && current != null) {
			StringBuffer sb = new StringBuffer(current.getMessage());
			sb.append(swriter.toString());
			current.setMessage(sb.toString().trim());
		}
	}

	/**
	 * Updates the {@link currentSession} to be the one that is not null or has most recent date.
	 * @param session
	 */
	private static LogSession updateCurrentSession(LogSession currentSession, LogSession session) {
		if (currentSession == null) {
			return session;
		}
		Date currentDate = currentSession.getDate();
		Date sessionDate = session.getDate();
		if (currentDate == null && sessionDate != null)
			return session;
		else if (currentDate != null && sessionDate == null)
			return session;
		else if (currentDate != null && sessionDate != null && sessionDate.after(currentDate))
			return session;

		return currentSession;
	}

	/**
	 * Adds entry to the list if it's not filtered. Removes entries exceeding the count limit.
	 * 
	 * @param entry
	 * @param entries
	 * @param memento
	 */
	private static void addEntry(LogEntry entry, List<AbstractEntry> entries, IMemento memento) {

		if (isLogged(entry, memento)) {
			entries.add(entry);

//			if (memento.getString(LogView.P_USE_LIMIT).equals("true")) {//$NON-NLS-1$
//				int limit = memento.getInteger(LogView.P_LOG_LIMIT).intValue();
//				if (entries.size() > limit) {
//					entries.remove(0);
//				}
//			}
		}
	}

	/**
	 * Returns whether given entry is logged (true) or filtered (false).
	 * 
	 * @param entry
	 * @param memento
	 * @return is entry logged or filtered
	 */
	public static boolean isLogged(LogEntry entry, IMemento memento) {
		return true;
//		int severity = entry.getSeverity();
//		switch (severity) {
//			case IStatus.INFO :
//				return memento.getString(LogView.P_LOG_INFO).equals("true"); //$NON-NLS-1$
//			case IStatus.WARNING :
//				return memento.getString(LogView.P_LOG_WARNING).equals("true"); //$NON-NLS-1$
//			case IStatus.ERROR :
//				return memento.getString(LogView.P_LOG_ERROR).equals("true"); //$NON-NLS-1$
//			case IStatus.OK :
//				return memento.getString(LogView.P_LOG_OK).equals("true"); //$NON-NLS-1$
//		}
//
//		return false;
	}

	private static void setNewParent(List<AbstractEntry> parents, LogEntry entry, int depth) {
		if (depth + 1 > parents.size())
			parents.add(entry);
		else
			parents.set(depth, entry);
	}
}
