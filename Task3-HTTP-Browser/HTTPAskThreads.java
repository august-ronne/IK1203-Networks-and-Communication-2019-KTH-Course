import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@SuppressWarnings("ALL")
public class HTTPAskThreads {
	private ServerSocket serverSocket;
	public static void main(String[] args) throws IOException {
		HTTPAskThreads server = new HTTPAskThreads(Integer.parseInt(args[0]));
		server.startServer();
	}

	private HTTPAskThreads(int port) throws IOException {
		serverSocket = new ServerSocket(port);
	}

	private void startServer() throws IOException {
		System.out.println("[SERVER STARTED...]");
		while (true) {
			Socket socket = this.serverSocket.accept();
			System.out.println("New request found, dispatching worker...");
			Worker connection = new Worker(socket, "\r\n");

			Thread request = new Thread(connection);
			request.start();
		}
	}

	class Worker implements Runnable {
		private Socket socket;
		private String lineEnding;
		private String requestAction;
		private String requestHost;
		private String requestPort;
		private String requestParams;

		Worker(Socket socket, String lineEnding) {
			this.socket = socket;
			this.lineEnding = lineEnding;
			this.requestAction = null;
			this.requestHost = null;
			this.requestPort = null;
			this.requestParams = null;
		}

		@Override
		public void run() {
			System.out.println(">>Worker<< started working with request on socket: "
				+ this.socket.getPort());
			try {
				InputStream input = this.socket.getInputStream();
				DataOutputStream output = new DataOutputStream(
					this.socket.getOutputStream());
				handleRequest(input, output);
				input.close();
				output.close();
			} catch (IOException e) {
				System.out.println(">>Worker<< encountered an error: ");
				e.printStackTrace();
			}
		}

		private void handleRequest(InputStream in, DataOutputStream out) throws IOException {
			BufferedReader bf = new BufferedReader(
				new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String httpRequest = bf.readLine();

			if (!httpRequest.startsWith("GET") || httpRequest.split(" ").length < 3) {
				out.writeBytes(createResponse("400 Bad Request",
					"This server only supports GET requests with a path."));
			} else {
				extractRequestParams(httpRequest);
				doAction(out);
			}
		}
	}
}