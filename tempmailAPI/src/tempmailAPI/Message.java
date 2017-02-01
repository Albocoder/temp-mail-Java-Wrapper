package tempmailAPI;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Message {
	
	private String msgSource;//source data like header and all that fun stuff
	private String source;//api.temp-mail.ru
	private String msgHash;
	private String emailHash;
	private String from;
	private String subject;
	private String bodyTextOnly;
	private String body;
	private String bodyHtml;
	private String email;
	//timestamp
	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private int second;
	
	public Message(	String email,String src,String msgID,String mailID,
						String from,String subj,String txtOnly,String txt,
							String html,String epoch){
		this.email = email;
		source = src;
		msgSource = null;
		msgHash = msgID;
		emailHash = mailID;
		this.from = from;
		subject = subj;
		bodyTextOnly = txtOnly;
		body = txt;
		bodyHtml = html;
		epochToTime(epoch);
	}
	
	
	//TODO DO these
	public void parseMsgSource(){
		
	}
	
	//string manipulation for these
	public String getFromName(){
		return from.substring(0,from.indexOf('<')).trim();
	}
	public String getFromEmail(){
		return from.substring(from.indexOf('<')+1,from.indexOf('>'));
	}
	
	private void epochToTime(String epochTime){
		epochTime = epochTime.replace(".", "");
		//System.out.println(BigInteger.valueOf(Long.parseLong(epochTime)));
		Date date = new Date(Long.parseLong(epochTime));
		DateFormat format = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
		format.setTimeZone(TimeZone.getDefault());
		String formatted = format.format(date);
		int start = 0,end = formatted.indexOf("-",start);
		day = Integer.parseInt(formatted.substring(start,end));
		start = end + 1;end = formatted.indexOf("-",start);
		month = Integer.parseInt(formatted.substring(start,end));
		start = end + 1;end = formatted.indexOf("-",start);
		year = Integer.parseInt(formatted.substring(start,end));
		start = end + 1;end = formatted.indexOf("-",start);
		hour = Integer.parseInt(formatted.substring(start,end));
		start = end + 1;end = formatted.indexOf("-",start);
		minute = Integer.parseInt(formatted.substring(start,end));
		start = end + 1;
		second = Integer.parseInt(formatted.substring(start));
		System.out.println("\n"+getDay()+"/"+getMonth()+"/"+getYear()+"\t"+getHour()+":"+getMinute()+":"+getSecond());
	}
	
	//Getters DONE!!!
	public String getMsgSource(){return msgSource;}
	public String getEmail(){return email;}
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
	public int getMinute() {return minute;}
	public int getSecond() {return second;}
	
}
