package me.biezhi.splider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blade.kit.FileKit;
import blade.kit.PatternKit;
import blade.kit.http.HttpRequest;

/**
 * 抓取Q友网头像
 * @version 
 * @since JDK 1.8
 */
public class QqYou {

	private static final Logger LOGGER = LoggerFactory.getLogger(QqYou.class);
	
	private static final String site = "http://www.qqyou.com";
	
	private static String type = "nansheng";
	
	private static String homeUrl = site + "/touxiang/" + type;
	
	private int imageCount = 0;
	
	public QqYou() {
		if(!FileKit.isDirectory("avatar")){
			FileKit.createDir("avatar");
		}
	}
	
	/**
	 * 加载页面
	 */
	public String loadPage(int page){
		return HttpRequest.get(homeUrl + "list"+ page +".html").body();
	}
	
	/**
	 * 加载页面
	 */
	public List<String> loadItems(String html){
		List<String> items = new ArrayList<String>();
		Pattern pattern = Pattern.compile("<div.*?pics\">([\\s\\S]*?)<a href=\"([\\s\\S]*?)\".*?>([\\s\\S]*?)</a>([\\s\\S]*?)</div>");
		Matcher m = pattern.matcher(html);
		while(m.find()){
			String detailHref = site + m.group(2);
			items.add(detailHref);
		}
		return items;
	}
	
	/**
	 * 抓取图片主逻辑
	 */
	public void start(int page){
		LOGGER.info("load url : " + homeUrl);
		start(1, page);
	}
	
	public QqYou start(int start_page, int end_page){
		if (start_page < 1) {
			start_page = 1;
		}
		if (end_page < 1) {
			end_page = 1;
		}
		
		this.loadItems(start_page, end_page);
		
		return this;
	}
	
	private void loadItems(int start_page, int end_page){
		for(int page=start_page; page <= end_page; page++){
			// 加载一页
			String content = loadPage(page);
			// 一页中的所有项
			List<String> items = loadItems(content);
			process(items);
		}
	}
	
	public void process(List<String> items){
		Pattern pattern = Pattern.compile("<a.*?<img.*?class=img200X200.*?src=\"([\\s\\S]*?)\".*?</a>");
		
		for(String item : items){
			
			List<String> images = new ArrayList<String>();
			String content = HttpRequest.get(item).body();
			
			Matcher m = pattern.matcher(content);
			while(m.find()){
				String img_url = m.group(1);
				images.add(img_url);
			}
			
			download(images);
		}
		
		LOGGER.info(" 下载图片数量：" + imageCount);
	}
	
	private void download(List<String> images) {
		for(String image : images){
			String fileName = FileKit.getName(image);
			File output = new File("avatar/" + fileName);
			HttpRequest.get(image).receive(output);
			LOGGER.info(" 下载完成：" + fileName);
			imageCount+=1;
		}
	}

	@SuppressWarnings("resource")
	private void init() {
		System.out.println("请选择要下载的类型：");
		System.out.println("1. 男生头像");
		System.out.println("2. 女生头像");
		System.out.println("3. 情侣头像");
		System.out.println("4. 动漫头像");
		System.out.print("请输入头像类型：");
		int count = new Scanner(System.in).nextInt();
		
		switch (count) {
			case 1: homeUrl = site + "/touxiang/nansheng/"; break;
			case 2: homeUrl = site + "/touxiang/nvsheng/"; break;
			case 3: homeUrl = site + "/touxiang/qinglv/"; break;
			case 4: homeUrl = site + "/touxiang/katong/"; break;
		}
		System.out.print("请输入要下载多少页：");
		String page = new Scanner(System.in).nextLine();
		if(PatternKit.isNumber(page)){
			// 抓取多少页图片并下载
			this.start(Integer.valueOf(page));
		} else {
			if(page.indexOf(",") != -1){
				String[] pageindex = page.split(",");
				this.start(Integer.valueOf(pageindex[0]), Integer.valueOf(pageindex[1]));
			}
		}
	}
	
	public static void main(String[] args) {
		new QqYou().init();
	}
	
}
