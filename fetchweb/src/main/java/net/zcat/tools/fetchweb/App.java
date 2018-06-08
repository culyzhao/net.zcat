package net.zcat.tools.fetchweb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.filechooser.FileFilter;

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
	
	public Properties appProps = new Properties();

	private static Logger logger = Logger.getLogger("App");
	
   	private ArrayList<Contents> contents = new ArrayList<Contents>();

   	private String document_root;
   	private String site_name;
   	private String site_start_url;
   	private String article_list_template;
   	private String article_list_select;
   	private String article_content_template;
   	private int article_list_history;
   	private String article_auther;
   	private String article_time;
   	private String article_image;
   	private String article_content;
   	private String comments_auther;
   	private String comments_time;
   	private String comments_content;
   	
   	private String current_path;
   	
	public App(String propsPath) throws FileNotFoundException, IOException {
		
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String appConfigPath = rootPath + "app.properties";
		if (propsPath != null && !propsPath.isEmpty()) {
			appConfigPath = propsPath;
		}
		logger.info("Load properties from " + appConfigPath);

		appProps.load(new FileInputStream(appConfigPath));
		
		document_root = appProps.getProperty(Settings.DOCUMENT_ROOT);
		site_name = appProps.getProperty(Settings.SITE_NAME);
		site_start_url = appProps.getProperty(Settings.SITE_START_URL);
		article_list_template = appProps.getProperty(Settings.ARTICLE_LIST_TEMPLATE);
		article_content_template = appProps.getProperty(Settings.ARTICLE_CONTENT_TEMPLATE);
		article_list_select = appProps.getProperty(Settings.ARTICLE_LIST_SELECT);
		article_list_history = Integer.valueOf(appProps.getProperty(Settings.ARTICLE_LIST_HISTORY));
		article_auther = appProps.getProperty(Settings.ARTICLE_AUTHER);
		article_time = appProps.getProperty(Settings.ARTICLE_TIME);
		article_image = appProps.getProperty(Settings.ARTICLE_IMAGE);
		article_content = appProps.getProperty(Settings.ARTICLE_CONTENT);
		comments_auther = appProps.getProperty(Settings.COMMENTS_AUTHER);
		comments_time = appProps.getProperty(Settings.COMMENTS_TIME);
		comments_content = appProps.getProperty(Settings.COMMENTS_CONTENT);

		if (document_root.charAt(document_root.length() - 1) != '/') {
			document_root += "/";
		}
		
		current_path = document_root + site_name + "/";
		
	}

	public void test() throws IOException {
		fetch(true);
	}
	
	public void execute() throws FileNotFoundException, IOException {
		
		File fdoc = new File(document_root);
		if (!fdoc.exists()) 
			throw new FileNotFoundException(document_root + " not found.");
		File fsite = new File(current_path);
		if (!fsite.exists()) {
			fsite.mkdir();
		}
		
		fetch(false);
		
//		File[] files = new File(current_path).listFiles(FileFilter);
//		Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
//		
//		Path p = Paths.get(arg0, arg1)
		
	}

	public void clearOld() {
		
//		Files.walk(Path, FileVisitOption.FOLLOW_LINKS)
//	       .sorted(Comparator.reverseOrder())
//	       .map(Path::toFile)
//	       .peek(System.out::println)
//	       .forEach(File::delete);
	}
	
	
	
	public void DownloadImage(String url, String local) throws IOException {
    	//Open a URL Stream
    	Response resultImageResponse = Jsoup.connect(url).cookies(null)
    	                                        .ignoreContentType(true).execute();
    	// output here
    	FileOutputStream out = (new FileOutputStream(new java.io.File(local)));

    	// resultImageResponse.body() is where the image's contents are.
    	out.write(resultImageResponse.bodyAsBytes());  
    	out.close();
	}
	
	public void fetch(boolean isTest) throws IOException {
		
		logger.info("fetch article list");
		Document doclist = Jsoup.connect(this.site_start_url).get();
	   	Elements list = doclist.select(this.article_list_select);
	   	logger.info("list size=" + list.size());
	   	
	   	for (Element link : list) {
	   		String linkHref = link.attr("abs:href");
	   		String linkText = link.text();
	   		
	   		Contents ct = new Contents();
	   		ct.setId(DigestUtils.sha1Hex(linkHref));
	   		ct.setTitle(linkText);
	   		
	   		if (!isTest) {
	   			File f = new File(current_path + ct.getId());
	   			if (f.exists()) continue;
	   		}
	   		
			Document detail = Jsoup.connect(linkHref).get();
			
			Element auther = detail.selectFirst(this.article_auther);
			ct.setAuther( auther == null ? this.site_name : auther.text());
			
			Element time = detail.selectFirst(this.article_time);
			ct.setTime( time == null ? Instant.now().toString() : time.text());

			Elements imgs = detail.select(appProps.getProperty(Settings.ARTICLE_IMAGE));
	   		
	   		for (Element img : imgs) {
	   			String imgSrc = img.attr("abs:src");
	   			String localSrc = ct.getId() + "/" + DigestUtils.sha1Hex(imgSrc);
	   			String localPath = this.current_path + localSrc;
	   			if (!isTest) {
	   				DownloadImage(imgSrc, localPath);
	   			}
	   			img.attr("src",localSrc);
	   		}
	   		Element article = detail.selectFirst(this.article_content);
	   		ct.setText(article.html());
	   		
	   		if (!this.comments_content.isEmpty()) {
	   			Elements comment_auther = detail.select(this.comments_auther);
	   			Elements comment_time = detail.select(this.comments_time);
	   			Elements comment_text = detail.select(this.comments_content);
	   			for (int i = 0; i < comment_text.size(); i++) {
	   				Comment cmt = new Comment(); 
   					cmt.setAuther(comment_auther == null ? site_name : comment_auther.get(i).text());
	   				cmt.setTime(comment_time == null ? Instant.now().toString() : comment_time.get(i).text());
	   				cmt.setText(comment_text.get(i).text());
	   				ct.getComments().add(cmt);
	   			}
	   		}
	   		logger.info(ct.toString());
	   		contents.add(ct);
	   	}
	   	logger.info("fetch end.");
	}
	
    public static void main( String[] args ) throws Exception {
    	
    	if (args.length == 0) {
    		System.out.println("Usage: mode [property]");
    		System.out.println("mode - f: fetch, t: test");
    		System.out.println("[property] - property file path");
    		return;
    	}
    	
    	String mode = args[0];
    	
    	App app = new App(args.length == 2 ? args[1] : "");
    	
    	
    	 
    }
}
