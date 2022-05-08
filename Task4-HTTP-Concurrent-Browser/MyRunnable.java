import java.net.*;
import java.io.*;

public class MyRunnable implements Runnable {
	Socket clientSocket;

	public MyRunnable(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		System.out.println("<run() called>");
		try {

			String host = null;
			String port = null;
			String input = null;
			BufferedReader inFromClient = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(
				clientSocket.getOutputStream());
			String clientRawURL = inFromClient.readLine();
			String[] urlArray = clientRawURL.split("[ =&?/]");

			if (urlArray[2].equals("ask")) {

				for (int i = 0; i < urlArray.length; i++) {
					if (urlArray[i].equals("hostname")) {
						host = urlArray[++i];
					} else if (urlArray[i].equals("port")) {
						port = urlArray[++i];
					} else if (urlArray[i].equals("string")) {
						input = urlArray[++i];
					}
				}
				if (host == null || port == null) {
					throw new IOException();
				}
				String serverAnswer = null;
				try {
					serverAnswer = TCPClient.askServer(host, Integer.parseInt(port), input);
					String header = "HTTP/1.1 200 OK\r\n\r\n";
					outToClient.writeBytes(header + serverAnswer + "\r\n");
				} catch (Exception e) {
					outToClient.writeBytes("HTTP/1.1 404 Not found\r\n");
				}

			} else {
				outToClient.writeBytes("HTTP/1.1 400 Bad request\r\n");
			}
			clientSocket.close();
			inFromClient.close();
			outToClient.close();
			System.out.println("Closed.");

		} catch (Exception e) {
			System.out.println("Error: run");
		}
	}
}