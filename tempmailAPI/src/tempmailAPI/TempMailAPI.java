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
	private final int MAX_CHAR = 60;
	//private final String ALLOWED_CHARS 
	//	= "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!#$%&'*+-/=?^`{|}~";
	private final Pattern domainPattern = Pattern.compile("^[0-9a-z!#$%&'*+-/=?^`{|}~]+$", Pattern.CASE_INSENSITIVE);
	
	public TempMailAPI() {
		//initializing data
		domains = new ArrayList<String>();
		sources = new ArrayList<URL>();
		randomSet = "0123456789abcdefghijklmnopqr]stuvwxyz!#$%&'*+-/=?^`{|}~";
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
		for(int i = 0; i< 10000; i++){
			generateEmail();
			if(!checkDomain(email.substring(0,email.indexOf("@"))))
				System.out.println(email);
		}
	}
	
	//TODO: TEST THE METHODS BELOW
	public void generateEmail(int nameLength){
		Random randGen = new Random();
		generateEmail(nameLength,randGen.nextInt(domains.size()));
	}
	public void generateEmail(int nameLength,int domainsIndex){
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
	// END OF TESTING TARGETS
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
		System.out.print("\tMD5: ");
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
		    dumpDomainsFromXml(xmlData);
		    in.close();
		}
	}
	private void dumpDomainsFromXml(String xmlData) throws ParserConfigurationException, SAXException, IOException{
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
			domains.add(nNode.getTextContent());
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
	public String getPattern(){return domainPattern.pattern();}
	public String getEmail(){return email;}
}
