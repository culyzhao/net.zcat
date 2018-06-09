package net.zcat.tools.fetchweb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
	
	public Properties appProps = new Properties();

	private static Logger logger = Logger.getLogger("App");
	
   	private ArrayList<Contents> contents = new ArrayList<Contents>();

   	private String document_root;
   	private String document_list_file;
   	private String document_content_file;
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
   	
   	private final static String TEMPLATE_TITLE_LOOP_START = "<!-- title_loop_start -->";
   	private final static String TEMPLATE_TITLE_LOOP_END = "<!-- title_loop_end -->";

   	private final static String TEMPLATE_COMMENTS_LOOP_START = "<!-- comments_loop_start -->";
   	private final static String TEMPLATE_COMMENTS_LOOP_END = "<!-- comments_loop_end -->";

   	private final static String TEMPLATE_SITE = "%site%";
   	private final static String TEMPLATE_PATH = "%path%";
	private final static String TEMPLATE_TITLE = "%title%";
	private final static String TEMPLATE_AUTHER = "%auther%";
	private final static String TEMPLATE_TIME = "%time%";
	private final static String TEMPLATE_ARTICLE = "%article%";
	private final static String TEMPLATE_COMMENT_AUTHER = "%comment.auther%";
	private final static String TEMPLATE_COMMENT_TIME = "%comment.time%";	
	private final static String TEMPLATE_COMMENT = "%comment%";
	
   	private final static String DEFAULT_LIST_TEMPLATE = "./list.html";
   	private final static String DEFAULT_CONTENT_TEMPLATE = "./content.html";

   	
	public App(String propsPath) throws FileNotFoundException, IOException {
				
		String appConfigPath = propsPath;
		logger.info("Load properties from " + appConfigPath);

		appProps.load(new FileInputStream(appConfigPath));
		
		document_root = appProps.getProperty(Settings.DOCUMENT_ROOT);
		document_list_file = appProps.getProperty(Settings.DOCUMENT_LIST_FILE);
		document_content_file = appProps.getProperty(Settings.DOCUMENT_CONTENT_FILE);
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
		
		if (article_list_template.isEmpty()) article_list_template = App.DEFAULT_LIST_TEMPLATE;
		if (article_content_template.isEmpty()) article_content_template = App.DEFAULT_CONTENT_TEMPLATE;
		
		current_path = document_root + site_name + "/";
		logger.info(appProps.toString());
		logger.info("article_list_template="+this.article_list_template);
		logger.info("article_content_template="+this.article_content_template);
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
		createHtml();
		clearOld();
		
	}

	public void clearOld() throws IOException {
		
		logger.info("clear history.");
		File[] files = Files.list(Paths.get(current_path)).filter(Files::isDirectory).toArray(File[]::new);
		Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
		
		if (files.length <= this.article_list_history)
			return;
		
		for (int i = this.article_list_history; i < files.length; i++) {
			Files.walk(files[i].toPath(), FileVisitOption.FOLLOW_LINKS)
		       .sorted(Comparator.reverseOrder())
		       .map(Path::toFile)
		       .peek(System.out::println)
		       .forEach(File::delete);
		}
		
	}
	
	public void createHtml() throws IOException {
		String listHtml = new String(Files.readAllBytes(Paths.get(this.article_list_template)));
		String contentHtml = new String(Files.readAllBytes(Paths.get(this.article_content_template)));
		
		listHtml = listHtml.replaceAll(App.TEMPLATE_SITE, site_name);
		
		String titleLine = listHtml.substring(listHtml.indexOf(App.TEMPLATE_TITLE_LOOP_START) + App.TEMPLATE_TITLE_LOOP_START.length(), 
				listHtml.indexOf(App.TEMPLATE_TITLE_LOOP_END));
		
		StringBuffer listBuf = new StringBuffer();
		StringBuffer contBuf = new StringBuffer();
		
		for (Contents ct : contents) {
			
			listBuf.append(
				titleLine.replaceAll(App.TEMPLATE_TIME, ct.getTime()).
					replaceAll(App.TEMPLATE_TITLE, ct.getTitle()).
					replaceAll(App.TEMPLATE_AUTHER, ct.getAuther()).
					replaceAll(App.TEMPLATE_PATH, ct.getId() + "/")
			).append("\n");
			
			String newContentHtml = contentHtml.replaceAll(App.TEMPLATE_TITLE, ct.getTitle()).
					replaceAll(App.TEMPLATE_AUTHER, ct.getAuther()).
					replaceAll(App.TEMPLATE_TIME, ct.getTime()).
					replaceAll(App.TEMPLATE_ARTICLE, ct.getText());
			
			String commentLine = newContentHtml.substring(newContentHtml.indexOf(App.TEMPLATE_COMMENTS_LOOP_START) + App.TEMPLATE_COMMENTS_LOOP_START.length(), 
					listHtml.indexOf(App.TEMPLATE_COMMENTS_LOOP_END));
			
			for (Comment cmt : ct.getComments()) {
				contBuf.append(
						commentLine.replaceAll(App.TEMPLATE_COMMENT_AUTHER, cmt.getAuther()).
						replaceAll(App.TEMPLATE_COMMENT_TIME, cmt.getTime()).
						replaceAll(App.TEMPLATE_COMMENT, cmt.getText())
				).append("\n");
			}

			newContentHtml = newContentHtml.substring(0, newContentHtml.indexOf(TEMPLATE_COMMENTS_LOOP_START)) + 
					(contBuf.length() == 0 ? "" : contBuf.toString()) +
					newContentHtml.substring(newContentHtml.indexOf(App.TEMPLATE_COMMENTS_LOOP_END) + App.TEMPLATE_COMMENTS_LOOP_END.length());				
			String out = current_path + ct.getId() + "/" + this.document_content_file;
			logger.info("write: " + out);
			BufferedWriter contFile = Files.newBufferedWriter(Paths.get(out), 
	                StandardCharsets.UTF_8);
			contFile.write(newContentHtml);
			contFile.close();
		
		}
		
		listHtml = listHtml.substring(0, listHtml.indexOf(App.TEMPLATE_TITLE_LOOP_START)) + listBuf.toString() + 
				listHtml.substring(listHtml.indexOf(App.TEMPLATE_TITLE_LOOP_END) + App.TEMPLATE_TITLE_LOOP_END.length());
		
		logger.info("write: " + current_path + this.document_list_file);
		BufferedWriter listFile = Files.newBufferedWriter(Paths.get(current_path + this.document_list_file), 
                StandardCharsets.UTF_8);
		listFile.write(listHtml);
		listFile.close();
		
		
	}
	
	
	public void DownloadImage(String url, String local) throws IOException {
    	//Open a URL Stream
    	Response resultImageResponse = Jsoup.connect(url).ignoreContentType(true).execute();
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

			Elements imgs = detail.select(this.article_image);
	   		
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
	   		if (isTest) 
	   			logger.info(ct.toString());
	   		contents.add(ct);
	   	}
	   	logger.info("fetch end.");
	}
	
    public static void main( String[] args ) throws Exception {
    	
    	if (args.length != 2 || args[1].isEmpty()) {
    		System.out.println("Usage: mode property");
    		System.out.println("mode - f: fetch, t: test");
    		System.out.println("property - property file path");
    		return;
    	}
    	
    	String mode = args[0];
    	
    	App app = new App(args[1]);
    	
    	if (mode.equals("t")) {
    		app.test();
    	} else {
    		app.execute();
    	}
    	 
    }
}
