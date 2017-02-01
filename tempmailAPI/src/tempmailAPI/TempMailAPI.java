package tempmailAPI;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.*;
import javax.net.ssl.HttpsURLConnection;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class TempMailAPI {
	
	private ArrayList<String> domains;
	private ArrayList<URL> sources;
	private String randomSet;
	private String email;
	private HashMap<String,String> domainOfSourceRelation;
	private HashMap<String,Message> allMessages;
	private ArrayList<String> orderedMessages;
	
	//Constants 
	private final int MAX_CHAR = 60;
	private final Pattern domainPattern = Pattern.compile("^[0-9a-z!#$%&'*+-/=?^`{|}~]+$", Pattern.CASE_INSENSITIVE);
	
	public TempMailAPI() {
		//initializing data
		domains = new ArrayList<String>();
		sources = new ArrayList<URL>();
		orderedMessages = new ArrayList<String>();
		randomSet = "0123456789abcdefghijklmnopqrstuvwxyz!#$%&'*+-/=?^`{|}~";
		domainOfSourceRelation = new HashMap<String,String>(); 
		allMessages = new HashMap<String,Message>();
		email = null;
		
		// adding sources 
		try {
			sources.add(new URL("https://api.temp-mail.org/request/domains/format/xml/"));
			sources.add(new URL("https://api.temp-mail.ru/request/domains/format/xml/"));
		} catch (MalformedURLException e) {System.err.println("Error Step 1: " + e.getMessage());}
		
		//dumping all email possibilities
		try {
			dumpDomains();
		} catch (IOException | ParserConfigurationException e) {e.printStackTrace();} catch (SAXException e) {
			System.err.println("Error Step 2: " + e.getMessage());
		}
		//stops execution if no domain found
		if (sources.isEmpty()){throw new RuntimeException("No email domain found");}
		//generateEmail("albocoder",0);
		generateEmail();
		//System.out.println(email);
		//while(true){
		//	try {
		//		Thread.sleep(4000);
		//	} catch (InterruptedException e) {e.printStackTrace();}
		//	System.out.println(retrieveNewMessages());
		//}
		//System.out.println(deleteMessageOnline("ecf17fcb4e9274b181866ce610d46052"));
	}
	
	//TODO: Gotta test em' all!!
	public ArrayList<Message> getAllMessages(){return new ArrayList<Message>(allMessages.values());}
	public Message getMessage(String id){return allMessages.get(id);}
	public Message getMessage(int index){return allMessages.get(orderedMessages.get(index));}
	public Message getLastMesasage(){return getMessage(orderedMessages.size()-1);}
	public boolean deleteMessageOnline(int index){return deleteMessageRemotely(orderedMessages.get(index));}
	public boolean deleteMessageLocally(int index){return deleteMessageLocally(orderedMessages.get(index));}
	public boolean deleteMessageTotally(int index){return deleteMessageTotally(orderedMessages.get(index));}
	public boolean deleteMessageRemotely(String id){return deleteMsg(id,true);}
	public boolean deleteMessageTotally(String id){return deleteMsg(id,false);}
	
	public boolean deleteMessageLocally(String id){
		Message tmp = allMessages.remove(id);
		orderedMessages.remove(id);
		if(tmp==null&&allMessages.isEmpty())
			return true;
		else if (tmp!=null)
			return true;
		return false;
	}
	private boolean deleteMsg(String id,boolean onlyOnline){
		Message tmp = allMessages.get(id);
		if(tmp==null)
			return false;
		
		try{
			return tmp.deleteMsg();
		}finally{
			if(!onlyOnline){
				allMessages.remove(id);
				orderedMessages.remove(id);
			}
		}
	}
	//END OF WHAT NEEDS TO BE TESTED
	
	//returns the number of new messages it gets
	public int retrieveNewMessages(){
		//https://api.temp-mail.ru/request/mail/id/75740c1865965424b79a4787b60a9a5f/
		StringBuilder requestURL = new StringBuilder("https://");
		requestURL.append(this.getEmailSource());
		requestURL.append("/request/mail/format/xml/id/");
		requestURL.append(this.getMD5FromEmail());
		//requestURL.append("/");
		try {
			URL destination = new URL(requestURL.toString());
			HttpsURLConnection con = (HttpsURLConnection)destination.openConnection();
			con.addRequestProperty("User-Agent", 
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		    String xmlData,line;
		    StringBuilder dataBuilder = new StringBuilder();
		    while ((line = in.readLine())!=null){
		    	//System.out.println(line);
		    	dataBuilder.append(line+"\n");
		    }
		    xmlData = dataBuilder.toString();
		    try {
		    	//System.out.println(xmlData);
				return parseMsgFromXML(xmlData);
			} catch (ParserConfigurationException | SAXException e) {
				//e.printStackTrace();
				System.err.println("Error in parsing: "+e.getMessage());
				return 0;
			}
		} catch (MalformedURLException e) {
			//e.printStackTrace();
			System.err.println("Error in URL: "+e.getMessage());
			return -1;
		} catch (IOException e) {
			//e.printStackTrace();
			System.err.println("Error in opening connection: "+e.getMessage());
			System.err.println("No Messages found or wrong response!");
			return -1;
		}
	}
	private int parseMsgFromXML(String xmlData) throws ParserConfigurationException, SAXException, IOException{
		int numberOfNewMsgs = 0;
		Document doc = loadXMLFromString(xmlData);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("xml");
		nList = nList.item(0).getChildNodes();
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			Node inner = nNode.getFirstChild().getNextSibling();
			String msgID = inner.getTextContent().trim();
			if(allMessages.containsKey(msgID))
				continue;
			inner = inner.getNextSibling();
			String emailID = inner.getTextContent().trim();
			inner = inner.getNextSibling();
			String emailFrom = inner.getTextContent().trim();
			inner = inner.getNextSibling();
			String emailSubject = inner.getTextContent().trim();
			inner = inner.getNextSibling().getNextSibling();
			String emailTxtOnly = inner.getTextContent().trim();
			inner = inner.getNextSibling();
			String emailTxt = inner.getTextContent().trim();
			inner = inner.getNextSibling();
			String emailHtml = inner.getTextContent().trim();
			inner = inner.getNextSibling();
			String emailTimeStamp = inner.getTextContent().trim();
			
			//put the message in the hashmap
			allMessages.put(msgID, new Message(email,this.getEmailSource(),msgID,emailID,emailFrom
					,emailSubject,emailTxtOnly,emailTxt,emailHtml,emailTimeStamp));
			numberOfNewMsgs++;
			orderedMessages.add(msgID);
		}
		return numberOfNewMsgs;
	}
	public void generateEmail(int nameLength){
		Random randGen = new Random();
		generateEmail(nameLength,randGen.nextInt(domains.size()));
	}
	public void generateEmail(int nameLength,int domainsIndex){
		if(nameLength>MAX_CHAR)
			throw new RuntimeException("Email name longer than 60 characters");
		StringBuilder emailBuilder = new StringBuilder();
		Random randGen = new Random();
		for(int i = 0; i < nameLength; i++){
			int charAt = randGen.nextInt(randomSet.length());
			emailBuilder.append(randomSet.substring(charAt,charAt+1));
		}
		emailBuilder.append(domains.get(domainsIndex));
		email = emailBuilder.toString();
	}
	public void generateEmail(String domain,int domainsIndex){
		//precheck
		if(!checkDomain(domain))
			throw new RuntimeException("Email name not supported. Use this pattern: "+getPattern());
		if(domain.length()>MAX_CHAR)
			throw new RuntimeException("Email name longer than 60 characters");
		String emailGen = "";
		emailGen+=domain;
		emailGen+=domains.get(domainsIndex);
		email = emailGen;
	}
	public void generateEmail(String domain){
		Random randGen = new Random();
		generateEmail(domain,randGen.nextInt(domains.size()));
	}
	public String getMD5FromEmail() {
		if(email == null)
			return null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			//e.printStackTrace();
			return null;
		}
		try {
			md.update(email.getBytes("iso-8859-1"));
			byte byteData[] = md.digest();
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < byteData.length; i++) {
	         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			//e.printStackTrace();
			return null;
		}
	}
	public boolean checkDomain(String domain){
		Matcher m = domainPattern.matcher(domain);
		return m.find();
	}
	public void generateEmail(){
		generateEmail(60);
	}
	public void dumpDomains() throws IOException, ParserConfigurationException, SAXException {
		Iterator<URL> i = sources.iterator();
		int index = 0;
		while(i.hasNext()){
			URL tmp = i.next();
			HttpsURLConnection con = (HttpsURLConnection)tmp.openConnection();
			con.addRequestProperty("User-Agent", 
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
		    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		    String xmlData,line;
		    StringBuilder dataBuilder = new StringBuilder();
		    while ((line = in.readLine())!=null){
		    	//System.out.println(line);
		    	dataBuilder.append(line+"\n");
		    }
		    xmlData = dataBuilder.toString();
		    dumpDomainsFromXml(xmlData,index);
		    index++;
		    in.close();
		}
	}
	private void dumpDomainsFromXml(String xmlData,int index) throws ParserConfigurationException, SAXException, IOException{
		//System.out.println(xmlData);
		Document doc = loadXMLFromString(xmlData);
		doc.getDocumentElement().normalize();
		//System.out.println("Root element: " 
		//		+ doc.getDocumentElement().getNodeName());
		NodeList nList = doc.getElementsByTagName("item");
		//System.out.println("----------------------------");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			//System.out.print("\nCurrent Element: " 
			//		+ nNode.getTextContent());
			String parsedDomain = nNode.getTextContent();
			String src = sources.get(index).toString();
			int start = src.indexOf("://")+3;
			domainOfSourceRelation.putIfAbsent(parsedDomain,src.substring(start,src.indexOf("/",start)));
			if(!domains.contains(parsedDomain))
				domains.add(parsedDomain);
		}
		//System.out.println("\n\n");
	}
	private Document loadXMLFromString(String xmlString) throws ParserConfigurationException, SAXException, IOException {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    return builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
	}
	//getters
	public ArrayList<String> getDomains(){return domains;}
	public String getPattern(){return domainPattern.pattern();}
	public String getEmail(){return email;}
	public String getEmailDomain(){return email.substring(email.indexOf("@"));}
	public String getEmailSource(){return domainOfSourceRelation.get(this.getEmailDomain());}
	//setters will corrupt relation domain of source then I will have to dumpDomains and remake the map
	//public void setSources(ArrayList<URL> src){sources=src;}
}
