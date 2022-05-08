import java.net.*;
import java.io.*;

public class HTTPEcho {

	public static void main(String[] args) throws IOException {

		int serverPort = Integer.parseInt(args[0]);
		ServerSocket serverSocket = new ServerSocket(serverPort);

		while (true) {

			try {

				Socket connectionSocket = serverSocket.accept();
				connectionSocket.setSoTimeout(10000);

				BufferedReader fromClientToServer = new BufferedReader(
					new InputStreamReader(connectionSocket.getInputStream()));
				DataOutputStream fromServerToClient = new DataOutputStream(
					connectionSocket.getOutputStream());

				String line = "HTTP/1.1 200 OK\r\n\r\n";
				StringBuilder sb = new StringBuilder();
				sb.append(line);

				while ((line = fromClientToServer.readLine()) != null && line.length() != 0) {
					sb.append(line + "\r\n");
				}

				fromServerToClient.writeBytes(sb.toString() + "\n");
				connectionSocket.close();
				fromServerToClient.close();

			} catch (java.io.IOException ex) {
				ex.fillInStackTrace();
				System.out.println("Exception");
			}
		}
	}
}