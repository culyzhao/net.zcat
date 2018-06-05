package net.zcat.tools.fetchweb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws FileNotFoundException, IOException
    {
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
    	
    	//Open a URL Stream
    	Response resultImageResponse = Jsoup.connect("").cookies(null)
    	                                        .ignoreContentType(true).execute();

    	// output here
    	FileOutputStream out = (new FileOutputStream(new java.io.File("")));
    	out.write(resultImageResponse.bodyAsBytes());  // resultImageResponse.body() is where the image's contents are.
    	out.close();
    }
}
