package net.zcat.tools.fetchweb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Hello world!
 *
 */
public class App {
	private static Logger logger = Logger.getLogger("App");
	
	public Properties appProps = new Properties();
	
	public App(String propsPath) throws FileNotFoundException, IOException {
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String appConfigPath = rootPath + "app.properties";
		if (propsPath != null && !propsPath.isEmpty()) {
			appConfigPath = propsPath;
		}
		logger.info("Load properties from " + appConfigPath);

		appProps.load(new FileInputStream(appConfigPath));
		appProps.list(System.out);
	}
	
	
	
	public static void init()  throws FileNotFoundException, IOException {
    	String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    	String appConfigPath = rootPath + "app.properties";
    	String catalogConfigPath = rootPath + "catalog";
    	 
    	Properties appProps = new Properties();
		appProps.load(new FileInputStream(appConfigPath));
    	 
    	Properties catalogProps = new Properties();
		catalogProps.load(new FileInputStream(catalogConfigPath));
    	
		
		String appVersion = appProps.getProperty("version");
		String appName = appProps.getProperty("name", "defaultName");
		String appGroup = appProps.getProperty("group", "baeldung");
		String appDownloadAddr = appProps.getProperty("downloadAddr");
		System.out.println(appVersion);
		System.out.println(appName);
		System.out.println(appGroup);
		System.out.println(appDownloadAddr);
		
		appProps.list(System.out); // list all key-value pairs
		 
		Enumeration<Object> valueEnumeration = appProps.elements();
		while (valueEnumeration.hasMoreElements()) {
		    System.out.println(valueEnumeration.nextElement());
		}
		 
		Enumeration<Object> keyEnumeration = appProps.keys();
		while (keyEnumeration.hasMoreElements()) {
		    System.out.println(keyEnumeration.nextElement());
		}
		 
		int size = appProps.size();
		System.out.println(size);		
		
    	System.out.println( "Hello World!" );
    	
		
	}
	
	/*
		docroot
		    wenxuecity
		        index.html
		        xxxxxx
		    boxun
		        index.html
		        xxxxxx
		        
	
	*/
	
	public static void DownloadImage(String url, String local) throws IOException {
    	//Open a URL Stream
    	Response resultImageResponse = Jsoup.connect(url).cookies(null)
    	                                        .ignoreContentType(true).execute();
    	// output here
    	FileOutputStream out = (new FileOutputStream(new java.io.File(local)));
    	out.write(resultImageResponse.bodyAsBytes());  // resultImageResponse.body() is where the image's contents are.
    	out.close();
		
	}
	
	public static void fetchList() throws IOException {
		Document doclist = Jsoup.connect("http://alm.jqdev.saic-gm.com/http%20_www.wenxuecity.com_news_morenews_.html").get();
	   	Elements list = doclist.select("div.list a[href]");
	   	System.out.println(list.size());
	   	ArrayList<Contents> contents = new ArrayList<Contents>();
	   	
	   	
	   	for (Element link : list) {
	   		String linkHref = link.attr("abs:href");
	   		String linkText = link.text();
	   		String htmlFile = linkHref.substring(linkHref.lastIndexOf("/")+1);
	   		System.out.println(linkHref + "," + linkText + "," + DigestUtils.sha1Hex(linkHref));
	   		Contents ct = new Contents();
	   		ct.setId(DigestUtils.sha1Hex(linkHref));
	   		ct.setTitle(linkText);
	   		
			Document detail = Jsoup.connect("http://alm.jqdev.saic-gm.com/http%20_www.wenxuecity.com_news_2018_06_05_7313078.html").get();
		   	Element auther = detail.select("span[itemprop=author]").first();
		   	Element time = detail.select("time[itemprop=datePublished]").first();
	   		Elements pics = detail.select("div#articleContent img");
	   		
	   		for (Element pic : pics) {
	   			String imgSrc = pic.attr("abs:src");
	   			String imgExt = imgSrc.substring(imgSrc.lastIndexOf("."));
	   			String localName = ct.getId() + "/" + DigestUtils.sha1Hex(imgSrc)+imgExt;
	   			//DownloadImage(imgSrc, docroot + "/" + DigestUtils.sha1Hex(imgSrc));
	   			pic.attr("src",localName);
		   		//System.out.println(imgSrc+","+localName);
		   		
	   		}
	   		Element article = detail.select("div#articleContent").first();
	   		ct.setAuther(auther.text());
	   		ct.setTime(time.text());
	   		ct.setText(article.html());
	   		System.out.println(ct);
	   		contents.add(ct);
	   	}
	   	
	   	
	   	
	   	
	   	
	}
	
	
    public static void main( String[] args ) throws Exception {
    	String props = "";
    	if (args.length == 1) {
    		props = args[0];
    	}
    	App app = new App(props);
    	 
    }
}
