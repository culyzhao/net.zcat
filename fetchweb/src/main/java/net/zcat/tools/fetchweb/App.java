package net.zcat.tools.fetchweb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
   	private ArrayList<String> idList = new ArrayList<String>();

   	private String document_root;
   	private String document_list_file;
   	private String document_content_file;
   	private String document_content_root;
   	private String site_name;
   	private String site_start_url;
   	private String article_list_template;
   	private String article_list_select;
   	private String article_content_template;
   	private int article_list_history;
   	private String article_author;
   	private String article_time;
   	private String article_image;
   	private String article_youtube;
   	private String article_youtube_server;
   	private String article_content;
   	private String comments_author;
   	private String comments_time;
   	private String comments_content;
   	private int comments_reversed;
   	
   	private String current_path;
   	
   	private final static String TEMPLATE_TITLE_LOOP_START = "<!-- title_loop_start -->";
   	private final static String TEMPLATE_TITLE_LOOP_END = "<!-- title_loop_end -->";

   	private final static String TEMPLATE_COMMENTS_LOOP_START = "<!-- comments_loop_start -->";
   	private final static String TEMPLATE_COMMENTS_LOOP_END = "<!-- comments_loop_end -->";

   	private final static String TEMPLATE_SITE = "%site%";
   	private final static String TEMPLATE_PATH = "%path%";
	private final static String TEMPLATE_TITLE = "%title%";
	private final static String TEMPLATE_AUTHOR = "%author%";
	private final static String TEMPLATE_TIME = "%time%";
	private final static String TEMPLATE_ARTICLE = "%article%";
	private final static String TEMPLATE_COMMENT_AUTHOR = "%comment.author%";
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
		document_content_root = appProps.getProperty(Settings.DOCUMENT_CONTENT_ROOT);
		site_name = appProps.getProperty(Settings.SITE_NAME);
		site_start_url = appProps.getProperty(Settings.SITE_START_URL);
		article_list_template = appProps.getProperty(Settings.ARTICLE_LIST_TEMPLATE);
		article_content_template = appProps.getProperty(Settings.ARTICLE_CONTENT_TEMPLATE);
		article_list_select = appProps.getProperty(Settings.ARTICLE_LIST_SELECT);
		article_list_history = Integer.valueOf(appProps.getProperty(Settings.ARTICLE_LIST_HISTORY));
		article_author = appProps.getProperty(Settings.ARTICLE_AUTHOR);
		article_time = appProps.getProperty(Settings.ARTICLE_TIME);
		article_image = appProps.getProperty(Settings.ARTICLE_IMAGE);
		article_youtube = appProps.getProperty(Settings.ARTICLE_YOUTUBE);
		article_youtube_server = appProps.getProperty(Settings.ARTICLE_YOUTUBE_SERVER);
		article_content = appProps.getProperty(Settings.ARTICLE_CONTENT);
		comments_author = appProps.getProperty(Settings.COMMENTS_AUTHOR);
		comments_time = appProps.getProperty(Settings.COMMENTS_TIME);
		comments_content = appProps.getProperty(Settings.COMMENTS_CONTENT);
		comments_reversed = Integer.valueOf(appProps.getProperty(Settings.COMMENTS_REVERSED));
		
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

	public void test() throws IOException, KeyManagementException, NoSuchAlgorithmException {
		fetch(true);
	}
	
	public void execute() throws FileNotFoundException, IOException, KeyManagementException, NoSuchAlgorithmException {
		
		File fdoc = new File(document_root);
		if (!fdoc.exists()) 
			throw new FileNotFoundException(document_root + " not found.");
		File fsite = new File(current_path);
		if (!fsite.exists()) {
			fsite.mkdir();
		}
		
		fetch(false);
		clearOld();
		createHtml();
	}

	public void clearOld() throws IOException {
		
		logger.info("clear history.");
         
		File[] files = new File(current_path).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
               return pathname.isDirectory();
            };
         });
		
		if (files.length <= this.article_list_history)
			return;
		
		for (File f : files) {
			if (!idList.contains(f.getName())) {
				Files.walk(f.toPath(), FileVisitOption.FOLLOW_LINKS)
			       .sorted(Comparator.reverseOrder())
			       .map(Path::toFile)
			       .peek(System.out::println)
			       .forEach(File::delete);
			}
		}

		//Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
	}
	
	public void createHtml() throws IOException {
		
		String listHtml = new String(Files.readAllBytes(Paths.get(this.article_list_template)));
		String contentHtml = new String(Files.readAllBytes(Paths.get(this.article_content_template)));
		
		listHtml = listHtml.replace(App.TEMPLATE_SITE, site_name);
		
		String titleLine = listHtml.substring(listHtml.indexOf(App.TEMPLATE_TITLE_LOOP_START) + App.TEMPLATE_TITLE_LOOP_START.length(), 
				listHtml.indexOf(App.TEMPLATE_TITLE_LOOP_END));
		
		StringBuffer listBuf = new StringBuffer();
		
		for (int i = contents.size() - 1 ; i >= 0; i--) {
			Contents ct = contents.get(i);
			if (!Files.exists(Paths.get(current_path + ct.getId()))) continue;
			
			String newContentHtml = null;
			StringBuffer contBuf = new StringBuffer();
			try {
				listBuf.append(
					titleLine.replace(App.TEMPLATE_TIME, ct.getTime()).
						replace(App.TEMPLATE_TITLE, ct.getTitle()).
						replace(App.TEMPLATE_AUTHOR, ct.getAuthor()).
						replace(App.TEMPLATE_PATH, (this.document_content_root + ct.getId()) + "/")
				).append("\n");
				
				newContentHtml = contentHtml.replace(App.TEMPLATE_TITLE, ct.getTitle()).
						replace(App.TEMPLATE_AUTHOR, ct.getAuthor()).
						replace(App.TEMPLATE_TIME, ct.getTime()).
						replace(App.TEMPLATE_ARTICLE, ct.getText());
				
				String commentLine = newContentHtml.substring(newContentHtml.indexOf(App.TEMPLATE_COMMENTS_LOOP_START) + App.TEMPLATE_COMMENTS_LOOP_START.length(), 
						newContentHtml.indexOf(App.TEMPLATE_COMMENTS_LOOP_END));
				
				for (Comment cmt : ct.getComments()) {
					contBuf.append(
							commentLine.replace(App.TEMPLATE_COMMENT_AUTHOR, cmt.getAuthor()).
							replace(App.TEMPLATE_COMMENT_TIME, cmt.getTime()).
							replace(App.TEMPLATE_COMMENT, cmt.getText())
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
			} catch (Exception e) {
				logger.info("Error: ");
				logger.info("contentHtml=" + contentHtml);
				logger.info("newContentHtml=" + newContentHtml);
				e.printStackTrace();
				continue;
			}
		
		}
		
		listHtml = listHtml.substring(0, listHtml.indexOf(App.TEMPLATE_TITLE_LOOP_START)) + listBuf.toString() + 
				listHtml.substring(listHtml.indexOf(App.TEMPLATE_TITLE_LOOP_END) + App.TEMPLATE_TITLE_LOOP_END.length());
		
		logger.info("write: " + current_path + this.document_list_file);
		BufferedWriter listFile = Files.newBufferedWriter(Paths.get(current_path + this.document_list_file), 
                StandardCharsets.UTF_8);
		listFile.write(listHtml);
		listFile.close();
		
		
	}
	
	public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {  
	    SSLContext sc = SSLContext.getInstance("SSLv3");  
	  
	    // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法  
	    X509TrustManager trustManager = new X509TrustManager() {  
	        @Override  
	        public void checkClientTrusted(  
	                java.security.cert.X509Certificate[] paramArrayOfX509Certificate,  
	                String paramString) throws CertificateException {  
	        }  
	  
	        @Override  
	        public void checkServerTrusted(  
	                java.security.cert.X509Certificate[] paramArrayOfX509Certificate,  
	                String paramString) throws CertificateException {  
	        }  
	  
	        @Override  
	        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
	            return null;  
	        }  
	    };  
	  
	    sc.init(null, new TrustManager[] { trustManager }, null);  
	    return sc;  
	}  
	
	public void DownloadImage(String url, String local) throws IOException, KeyManagementException, NoSuchAlgorithmException {
    	//Open a URL Stream
		logger.info("download image: " + url);
    	
		Response resultImageResponse = Jsoup.connect(url).sslSocketFactory(createIgnoreVerifySSL().getSocketFactory()).ignoreContentType(true).timeout(60000).execute();
    	// output here
    	FileOutputStream out = (new FileOutputStream(new java.io.File(local)));

    	// resultImageResponse.body() is where the image's contents are.
    	out.write(resultImageResponse.bodyAsBytes());  
    	out.close();
	}
	
	public void fetch(boolean isTest) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		
		logger.info("fetch article list");
		
		Document doclist = Jsoup.connect(this.site_start_url).sslSocketFactory(createIgnoreVerifySSL().getSocketFactory()).get();
	   	Elements list = doclist.select(this.article_list_select);
	   	logger.info("list size=" + list.size());
	   	
	   	for (int n = (list.size() > this.article_list_history ? this.article_list_history : list.size()) - 1; n >= 0 ; n--) {
	   		Element link = list.get(n);

	   		String linkHref = null;
	   		String linkText = null;
	   		
	   		Contents ct = new Contents();
	   		try {
		   		linkHref = link.attr("abs:href");
		   		linkText = link.text();
		   				   		
		   		ct.setId(DigestUtils.sha1Hex(linkHref));
		   		idList.add(ct.getId());
		   		ct.setTitle(linkText);
		   		logger.info("found: " + linkText);
		   		if (!isTest) {
		   			String contentPath = current_path + ct.getId() + "/" + this.document_content_file; 
		   			File fi = new File(contentPath);
		   			if (fi.exists()) {
		   				ct.setFetched(true);
			   			logger.info("found: " + contentPath);
		   			} else {
		   				logger.info("not found: " + contentPath);
		   				File fd = new File(current_path + ct.getId());
		   				if (!fd.exists()) fd.mkdir();
		   			}
		   		}
		   		
				Document detail = Jsoup.connect(linkHref).sslSocketFactory(createIgnoreVerifySSL().getSocketFactory()).get();;
				
				Element author = detail.selectFirst(this.article_author);
				ct.setAuthor( author == null ? this.site_name : author.text());
				
				Element time = detail.selectFirst(this.article_time);
				ct.setTime( time == null ? Instant.now().toString() : time.text());
	
				Elements imgs = detail.select(this.article_image);
		   		logger.info("found img size=" + imgs.size());

		   		for (Element img : imgs) {
		   			String imgSrc = img.attr("abs:src");
		   			String imgId = DigestUtils.sha1Hex(imgSrc);
		   			String localPath = this.current_path + ct.getId() + "/" + imgId;
		   			if (!isTest && !ct.isFetched()) {
		   				DownloadImage(imgSrc, localPath);
		   			}
		   			img.attr("src",imgId);
		   		}
		   		
		   		//fix iframe to auto fit panel
		   		Elements iframe = detail.select("iframe");
		   		iframe.forEach( frame -> { 
		   			frame.attr("width", "100%");
		   			frame.attr("height", "100%");
		   			frame.wrap("<div class=\"auto-resizable-iframe\"><div></div></div>");
		   		});
		   		
		   		if (!this.article_youtube_server.isEmpty()) {
 			
			   		Elements videos = detail.select(this.article_youtube);
			   		logger.info("found video size=" + videos.size());
			   		for (Element video : videos) {
			   			String videoSrc = video.attr("src");
			   			if (videoSrc.startsWith("https")) {
			   				videoSrc = videoSrc.replace("https://www.youtube.com", this.article_youtube_server);
			   			} else {
			   				videoSrc = videoSrc.replace("http://www.youtube.com", this.article_youtube_server);
			   			}
			   			logger.info("convert youtube video to " + videoSrc);
			   			video.attr("src", videoSrc);
			   		}
			   		
		   		}
		   		Element article = detail.selectFirst(this.article_content);
		   		ct.setText(article.html());
		   		
		   		if (!this.comments_content.isEmpty()) {
		   			Elements comment_author = detail.select(this.comments_author);
		   			Elements comment_time = detail.select(this.comments_time);
		   			Elements comment_text = detail.select(this.comments_content);
			   		logger.info("found comment size=" + comment_text.size());
		   			for (int i = 0; i < comment_text.size(); i++) {
		   				Comment cmt = new Comment(); 
	   					cmt.setAuthor(comment_author == null ? site_name : comment_author.get(i).text());
		   				cmt.setTime(comment_time == null ? Instant.now().toString() : comment_time.get(i).text());
		   				cmt.setText(comment_text.get(i).text());
		   				if (this.comments_reversed == 1) {
			   				ct.getComments().add(0, cmt);
		   				} else {
		   					ct.getComments().add(cmt);
		   				}
		   			}
		   		}
		   		if (isTest) logger.info(ct.toString());
		   		
	   		} catch (Exception e) {
	   			logger.info("Error: ");
	   			logger.info("linkHref=" + linkHref);
	   			logger.info("linkText=" + linkText);
	   			e.printStackTrace();
	   			continue;
	   		}
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
    	
    	System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    	
    	String mode = args[0];
    	
    	App app = new App(args[1]);
    	
    	if (mode.equals("t")) {
    		app.test();
    	} else {
    		app.execute();
    	}
    	 
    }
}
