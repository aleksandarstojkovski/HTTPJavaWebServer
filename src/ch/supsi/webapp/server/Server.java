package ch.supsi.webapp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Server {

	private static ServerSocket serverSocket;
	private final static int PORT = 8080;
	private final static String CONTENT_LENGTH_HEADER = "Content-Length";
	private final static String LINEBREAK = "\r\n";

	public static void main(String[] args) throws Exception {
		serverSocket = new ServerSocket(PORT);
		System.out.println("Server avviato sulla porta : " + PORT);
		System.out.println("-------------------------------------");

		while (true) {
			Socket clientSocket = serverSocket.accept();
			clientSocket.setSoTimeout(200);
			handleRequest(clientSocket);
			clientSocket.close();
		}
	}

	public static void handleRequest(Socket socket) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			OutputStream out = socket.getOutputStream();

			Request request = readRequest(in);
			System.out.println(request.allRequest);
			
			Content responseBody = handleResponseContent(request);
			produceResponse(out, responseBody);

			out.flush();
			out.close();
			in.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	private static Request readRequest(BufferedReader input) throws IOException {
		String firstline = input.readLine();
		if (firstline != null) {
			System.out.println("----------------- " + new Date() + " --------------");
			boolean isPost = firstline.startsWith("POST");
			return getRequest(input, firstline, isPost);
		}
		return null;
	}

	private static Request getRequest(BufferedReader input, String line, boolean isPost) throws NumberFormatException, IOException {
		StringBuilder rawRequest = new StringBuilder();
		rawRequest.append(line);
		String resource = line.substring(line.indexOf(' ')+1, line.lastIndexOf(' '));
		int contentLength = 0;
		while (!(line = input.readLine()).equals("")) {
			rawRequest.append('\n' + line);
			if (line.startsWith(CONTENT_LENGTH_HEADER))
				contentLength = Integer.parseInt(line.substring(CONTENT_LENGTH_HEADER.length()+2));
		}
		String body = "";
		if (isPost) {
			rawRequest.append("\n\n" + getBody(input, contentLength));
		}
		return new Request(rawRequest.toString(), resource, body, isPost);
	}

	private static String getBody(BufferedReader bf, int length) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++)
			sb.append((char) bf.read());
		return sb.toString();
	}

	/*
	 * Usare questo metodo per gestire la richiesta ricevuta e produrre un 
	 * contenuto (txt, html, ...) da dare come corpo nella risposta
	 * 
	 */
	private static Content handleResponseContent(Request request) {
		return new Content("Il mio primo documento HTML5".getBytes());
	}

	/*
	 * Usare questo metodo per scrivere l'intera risposta HTTP (prima linea+headers+body)
	 * 
	 */
	private static void produceResponse(OutputStream output, Content responseContent) throws IOException {
		String body =
				"HTTP/1.1 200 OK" + LINEBREAK +
				"Date: Sun, 10 Oct 2010 23:26:07 GMT" + LINEBREAK +
				"Server: Apache/2.2.8 (Ubuntu) mod_ssl/2.2.8 OpenSSL/0.9.8g" + LINEBREAK +
				"Last-Modified: Sun, 26 Sep 2010 22:04:35 GMT" + LINEBREAK +
				"ETag: \"45b6-834-49130cc1182c0\"" + LINEBREAK +
				"Accept-Ranges: bytes" + LINEBREAK +
				"Content-Length: " +responseContent.length+ LINEBREAK +
				"Connection: close" + LINEBREAK +
				"Content-Type: text/html" + LINEBREAK
				+ LINEBREAK +
				new String(responseContent.content);
		// usare la variabile LINEBREAK per andare a capo
		output.write(body.getBytes());
	}

}