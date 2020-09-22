package ch.supsi.webapp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
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

		String fileName = request.resource.replace("/","");
		File file = new File(fileName);
		boolean fileExists = file.exists();
		String mimetype = null;
		String contentType;
		String returnCode;
		byte[] bytesToReturn = new byte[0];

		try {
			mimetype = Files.probeContentType(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!fileExists){
			System.out.println("DEBUG: File "+fileName+" does not exist.");
			fileName="404.html";
			returnCode="404 Not Found";
		} else {
			System.out.println("DEBUG: File "+fileName+" exist.");
			returnCode="200 OK";
		}

		if (mimetype != null && mimetype.split("/")[0].equals("image")) {
			contentType="image/jpeg";
			//returnCode="304 Not Modified";
			try {
				bytesToReturn = Files.readAllBytes(file.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			contentType="text/html";
			try {
				bytesToReturn = new String(Files.readAllBytes(Paths.get(fileName))).getBytes();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

//		String body = "<!DOCTYPE html>\n" +
//				"<html>\n" +
//				"<head>\n" +
//				"<meta charset=\"UTF-8\">\n" +
//				"<title>Prova</title>\n" +
//				"</head>\n" +
//				"<body>\n" +
//				"Il mio primo documento HTML5 <br>\n" +
//				"Data attuale: " + new Date() +"\n" +
//				"<p>La domanda</p>\n" +
//				"<form method=\"GET\">\n" +
//				"<input name=\"name\" type=\"text\" />\n" +
//				"<input type=\"submit\"/>\n" +
//				" </form>\n"+
//				"</body>\n" +
//				"</html>";

		return new Content(bytesToReturn, returnCode, contentType);
	}

	/*
	 * Usare questo metodo per scrivere l'intera risposta HTTP (prima linea+headers+body)
	 * 
	 */
	private static void produceResponse(OutputStream output, Content responseContent) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
		String headers =
				"HTTP/1.1 " + responseContent.returnCode + LINEBREAK +
				"Content-Type: " + responseContent.contentType + LINEBREAK +
		        "Content-Length: " +responseContent.length + LINEBREAK +
				LINEBREAK +
				new String(responseContent.content);
		// usare la variabile LINEBREAK per andare a capo

		output.write(headers.getBytes());
	}

}