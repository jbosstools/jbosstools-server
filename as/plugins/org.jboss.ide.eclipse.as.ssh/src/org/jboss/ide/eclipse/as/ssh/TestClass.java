package org.jboss.ide.eclipse.as.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class TestClass {
	public static void main(String[] arg) {
		FileInputStream fis = null;
		try {


			String lfile = "/home/rob/apps/eclipse/workspaces/runtime/run12/.metadata/.plugins/org.jboss.ide.eclipse.as.core/SCP_Remote_Server_at_localhost/deploy/TEST10.jar";
			String rfile = "/home/rob/scptest999.jar";
			String host = "oxbeef.net";
			// username and password will be given via UserInfo interface.
			ServerUserInfo ui = new ServerUserInfo();

			JSch jsch = new JSch();
			Session session = jsch.getSession(ui.getUser(), host, 22);
			jsch.setKnownHosts(ui.getHostsFile());
			session.setUserInfo(ui);
			session.connect();

			// exec 'scp -t rfile' remotely
			String command = "scp -p -t " + rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if (checkAck(in) != 0) {
				System.exit(0);
			}

			// send "C0644 filesize filename", where filename should not include
			// '/'
			long filesize = (new File(lfile)).length();
			command = "C0644 " + filesize + " ";
			if (lfile.lastIndexOf('/') > 0) {
				command += lfile.substring(lfile.lastIndexOf('/') + 1);
			} else {
				command += lfile;
			}
			command += "\n";
			System.out.println(command);
			out.write(command.getBytes());
			out.flush();
			if (checkAck(in) != 0) {
				System.exit(0);
			}

			// send a content of lfile
			fis = new FileInputStream(lfile);
			byte[] buf = new byte[1024];
			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len); // out.flush();
			}
			fis.close();
			fis = null;
			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			if (checkAck(in) != 0) {
				System.exit(0);
			}
			out.close();

			channel.disconnect();
			session.disconnect();

			System.exit(0);
		} catch (Exception e) {
			System.out.println(e);
			try {
				if (fis != null)
					fis.close();
			} catch (Exception ee) {
			}
		}
	}

	static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

	public static class ServerUserInfo implements UserInfo {
		private String user = "rob";
		private String password = "p3nh2ogo";
		private String hostsFile = "/home/rob/.ssh/known_hosts";
		public String getPassword() {
			return password;
		}

		public String getUser() {
			return user;
		}
		
		public String getHostsFile() {
			return hostsFile;
		}
		
		public String getPassphrase() {
			return null;
		}

		public boolean promptPassphrase(String message) {
			return true;
		}

		public boolean promptPassword(String message) {
			return true;
		}

		public void showMessage(String message) {
			// TODO eh?
		}

		public boolean promptYesNo(String message) {
			// TODO Auto-generated method stub
			return false;
		}
	}
}
