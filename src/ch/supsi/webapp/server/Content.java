package ch.supsi.webapp.server;

public class Content
{
	int length = 0;
	String returnCode;
	byte[] content;
	
	public Content(byte[] content, String returnCode){
		this.content = content;
		this.length = content.length;
		this.returnCode = returnCode;
	}
}