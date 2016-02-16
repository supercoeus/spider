package me.biezhi.splider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import blade.kit.TaskKit;
import blade.kit.http.HttpRequest;

/**
 * 爬取 http://joke.setin.cn/ 所有笑话
 * @author renqi
 *
 */
public class Joke {

	private static final String UA = "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT)";
    
	private static final Logger LOGGER = LoggerFactory.getLogger(Joke.class);
	
	private String base_url = "http://joke.setin.cn/p/%d.html";
	
	private int start_page = 601;
	
	private int end_page = 1000;
	
	private Sql2o sql2o = DB.getSql2o();
	
	final String sql = "insert into t_joke(content) values (:value)";
	
	private String getBody(){
		return HttpRequest.get(String.format(base_url, start_page)).userAgent(UA).body();
	}
	
	public void execute(){
		
		TaskKit.scheduleAtFixedRate(new Runnable() {
			
			public void run() {
				
				String content = getBody();
				Pattern pattern = Pattern.compile("<div.*?body\">([\\s\\S]*?)<p>([\\s\\S]*?)</p>([\\s\\S]*?)</div>");
		        Matcher matcher = pattern.matcher(content);
		        Connection con = null;
		        try {
		        	
		        	con = sql2o.beginTransaction();
		        	
		        	Query query = con.createQuery(sql);
			        //遍历正则表达式匹配的信息
			        while (matcher.find()) {
			        	String joke = matcher.group(2);
			        	if(joke.indexOf("<script>") == -1){
			        		LOGGER.info(joke);
			        		query.addParameter("value", joke)
	                        .addToBatch();
			        	}
			        }
			        query.executeBatch();
		            con.commit();
		            LOGGER.info("page = " + start_page);
		            if(start_page == end_page){
			        	System.exit(0);
			        }
		            start_page ++;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if(null != con){
						con.close();
					}
				}
			}
		}, 3);
		
	}
	
	public static void main(String[] args) {
		Joke joke = new Joke();
		joke.execute();
	}
}
