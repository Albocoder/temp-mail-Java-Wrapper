package tempmailAPI;

public class Message {
	
	private String msgSource;
	private String source;//api.temp-mail.ru
	private String msgHash;
	private String emailHash;
	private String from;
	private String subject;
	private String bodyTextOnly;
	private String body;
	private String bodyHtml;
	//timestamp
	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private int second;
	//https://api.temp-mail.ru/request/source/id/75740c1865965424b79a4787b60a9a5f/
	
	
	
	
	//TODO DO these
	public String parseSubject(){
		return null;
	}
	public void parseMsgSource(){
		
	}
	//string manipulation for these
	public String getFromName(){
		return null;
	}
	public String getFromEmail(){
		return null;
	}
	
	
	
	//Getters DONE!!!
	public String getMsgSource(){return msgSource;}
	public String getSource() {return source;}
	public String getMsgHash(){return msgHash;}
	public String getEmailHash(){return emailHash;}
	public String getBodyTextOnly() {return bodyTextOnly;}
	public String getFrom() {return from;}
	public String getBody() {return body;}
	public String getBodyHtml() {return bodyHtml;}
	public String getSubject() {return subject;}
	public int getYear() {return year;}
	public int getMonth() {return month;}
	public int getDay() {return day;}
	public int getHour() {return hour;}
	public int getMinutes() {return minute;}
	public int getSeconds() {return second;}
	
}
