package ch.supsi.webapp.server;

public class Content
{
	int length = 0;
	String returnCode;
	byte[] content;
	String contentType;
	
	public Content(byte[] content, String returnCode, String contentType){
		this.content = content;
		this.length = content.length;
		this.returnCode = returnCode;
		this.contentType = contentType;
	}
}