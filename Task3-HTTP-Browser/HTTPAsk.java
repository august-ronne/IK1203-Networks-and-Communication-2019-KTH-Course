import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HTTPAsk {

	private ServerSocket serverSocket;

	public static void main(String[] args) throws IOException {
		int serverPort = Integer.parseInt(args[0]);
		HTTPAsk server = new HTTPAsk(serverPort);
		server.runServer();
	}

	private HTTPAsk(int serverPort) throws IOException {
		serverSocket = new ServerSocket(serverPort);
	}

	private void runServer() throws IOException {
		System.out.println("\n[SERVER IS RUNNING ON PORT: " + serverSocket.getLocalPort() + "...]");
		while (true) {
			Socket socket = serverSocket.accept();
			System.out.println("\n  ~ New <client> has connected to <server> on: " + socket.toString());
			System.out.println("  ~ Connection status of " + socket.toString() + " = " + socket.isConnected());
			Worker clientServerConnection = new Worker(socket);
			Thread session = new Thread(clientServerConnection);
			session.start();
		}
	}

	class Worker implements Runnable {
		private Socket socket;
		private String clientRequestMethod;
		private String clientRequestHost;
		private Integer clientRequestPort;
		private String clientRequestString;

		Worker(Socket socket) {
			this.socket = socket;
			this.clientRequestMethod = null;
			this.clientRequestHost = null;
			this.clientRequestPort = null;
			this.clientRequestString = null;
		}

		@Override
		public void run() {
			System.out.println("\n  <worker> is now processing a client request on port: " + this.socket.getPort());
			try {
				BufferedReader inFromClient = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
				DataOutputStream outFromServer = new DataOutputStream(
					socket.getOutputStream());
				processRequest(inFromClient, outFromServer);
				inFromClient.close();
				outFromServer.close();
			} catch (Exception ex) {
				System.out.println("\n  Something when wrong for <worker> processing client request on port: " + this.socket.getPort());
				ex.printStackTrace();
			}
		}

		private void processRequest(BufferedReader inFromClient, DataOutputStream outFromServer) throws IOException {
			String clientRequest = inFromClient.readLine();
			System.out.println(clientRequest.split(" ")[2].startsWith("HTTP"));
			System.out.println("\n  Client has supplied server with the following URL: " + clientRequest);
			if (!clientRequest.startsWith("GET") || 
			clientRequest.split(" ").length < 3 || 
			!clientRequest.split(" ")[2].startsWith("HTTP") ||
			!clientRequest.split(" ")[1].startsWith("/ask?")) {
				outFromServer.writeBytes(httpResponse("400 Bad Request", "(1) Something is wrong with the URL supplied by the client."));
			} else {
				Map<String, String> clientRequestParameters = getHttpRequestParameters(clientRequest, outFromServer);
				if (clientRequestParameters.get("hostname") == null || clientRequestParameters.get("port") == null) {
					outFromServer.writeBytes(httpResponse("400 Bad Request", "(2) Something is wrong with the URL supplied by the client."));
				} else {
					executeRequest(clientRequestParameters, outFromServer);
				}
			}
		}

		private Map<String, String> getHttpRequestParameters(String clientRequest, DataOutputStream outFromServer) throws IOException {
			String[] requestArray = clientRequest.split("[ =&?/]");
			for (String s : requestArray) {
				System.out.println(s);
			}
			Map<String, String> requestParameters = new HashMap<String, String>();
			for (int i = 0; i < requestArray.length; i++) {
				if (requestArray[i].equals("hostname")) {
					requestParameters.put(requestArray[i], requestArray[++i]);
				} else if (requestArray[i].equals("port")) {
					requestParameters.put(requestArray[i], requestArray[++i]);
				} else if (requestArray[i].equals("string")) {
					requestParameters.put(requestArray[i], requestArray[i + 1].replace('+', ' '));
					i++;
				}
			}

			System.out.println("\n  The following URL parameters have been extracted:");
			for (String key : requestParameters.keySet()) {
				System.out.println("    - " + key + ": " + requestParameters.get(key));
			}
			return requestParameters;
		}

		private void executeRequest(Map<String, String> requestParameters, DataOutputStream outFromServer) throws IOException {
			String serverResponse;
			try {
				serverResponse = TCPClient.askServer(
					requestParameters.get("hostname"),
					requestParameters.get("port"),
					requestParameters.get("string"));
			} catch (Exception ex) {
				serverResponse = "";
			}
			if (serverResponse == null || serverResponse.length() == 0) {
				outFromServer.writeBytes(httpResponse("404 Not Found", "We couldn't find what the client was looking for."));
			} else {
				outFromServer.writeBytes(httpResponse("200 OK", serverResponse));
			}
		}

		private String httpResponse(String statusCode, String data) {
			return "HTTP/1.1 " + statusCode + "\r\n\r\n" + data + "\r\n";
		}
	}
}

class TCPClient {

	public static String askServer(String hostname, String port, String ToServer) throws IOException {

		if (ToServer == null) return askServer(hostname, port);

		Socket clientSocket = new Socket(hostname, Integer.parseInt(port));
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

	public static String askServer(String hostname, String port) throws IOException {
		Socket clientSocket = new Socket(hostname, Integer.parseInt(port));
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