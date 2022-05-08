package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {

	public static String askServer(String hostname, int port, String ToServer) throws IOException {

		if (ToServer == null) return askServer(hostname, port);

		Socket clientSocket = new Socket(hostname, port);
		clientSocket.setSoTimeout(5000);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		outToServer.writeBytes(ToServer + '\n');

		StringBuilder sb = new StringBuilder();
		String serverLine;
		try {
			while (((serverLine = inFromServer.readLine()) != "\n") && (serverLine != null)) {
				sb.append(serverLine + '\n');
			}
			clientSocket.close();
			return sb.toString();
		} catch (IOException ex) {
			clientSocket.close();
			return sb.toString();
		}
	}

	public static String askServer(String hostname, int port) throws IOException {
		Socket clientSocket = new Socket(hostname, port);
		clientSocket.setSoTimeout(5000);
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		StringBuilder sb = new StringBuilder();
		String serverLine;
		final int MAX_LINES = 1024;
		int counter = 0;
		try {
			while ((serverLine = inFromServer.readLine()) != "\n" && serverLine != null) {
				sb.append(serverLine + '\n');
				counter++;
				if (counter >= MAX_LINES) {
					return sb.toString();
				}
			}
			clientSocket.close();
			return sb.toString();

		} catch (IOException ex) {
			clientSocket.close();
			return sb.toString();
		}
	}
}