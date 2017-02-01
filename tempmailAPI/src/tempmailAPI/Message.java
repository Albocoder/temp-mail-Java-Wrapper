package tempmailAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

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
	
	
	//TODO DO this
	public void parseMsgSource(){
		
	}
	public boolean deleteMsg(){
		//https://api.temp-mail.org/request/delete/format/xml/id/78529cc6cde904b97aaae7fad8158773/
		StringBuilder urlBuilder = new StringBuilder("https://");
		urlBuilder.append(getSource()).append("/request/delete/format/xml/id/").append(getMsgHash());
		try {
			URL destination = new URL(urlBuilder.toString());
			HttpsURLConnection con = (HttpsURLConnection)destination.openConnection();
			con.addRequestProperty("User-Agent", 
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		    String xmlData = null,line;
		    StringBuilder dataBuilder = new StringBuilder();
		    while ((line = in.readLine())!=null){
		    	//System.out.println(line);
		    	dataBuilder.append(line+"\n");
		    }
		    xmlData = dataBuilder.toString();
		    if(xmlData.contains("<result>success</result>")){
		    	return true;
		    }
		    return false;
		} catch (MalformedURLException e) {
			//e.printStackTrace();
			System.err.println("Error: "+e.getMessage());
			return false;
		} catch (IOException e) {
			//e.printStackTrace();
			System.err.println("Error in opening connection: "+e.getMessage());
			return false;
		}
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
	public String toString(){
		return "MessageID:\t"+msgHash+"\nE-MailID:\t"+emailHash+"\nFrom:\t"+from+"\nSubject:\t"+subject+
		"MailTextOnly:\t"+bodyTextOnly+"\nMailText:\t"+body+"\nMailHtml:\t"
				+bodyHtml+"\nTime(dd/mm/yyyy hh:mm:ss):\t"+day+"/"+month+"/"+"/"+year+"\t"+hour+":"+minute+":"+second+"\n";
	}
}
