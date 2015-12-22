package me.biezhi.splider;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blade.kit.DateKit;
import blade.kit.StringKit;
import blade.kit.http.HttpRequest;

/**
 * 抓取糗事百科段子
 * @version 
 * @since JDK 1.8
 */
public class QiuBai {

	private int page = 1;
	
	private static final String user_agent = "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT)";
	
	private boolean enable = false;
	
	private List<List<QB>> stories;
	
	class QB{
		private String author;
		private String text;
		private String dateline;
		private String star;
		public QB() {
		}
		
		public QB(String author, String text, String dateline, String star) {
			super();
			this.author = author;
			this.text = text;
			this.dateline = dateline;
			this.star = star;
		}

		public String getAuthor() {
			return author;
		}
		public void setAuthor(String author) {
			this.author = author;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public String getDateline() {
			return dateline;
		}
		public void setDateline(String dateline) {
			this.dateline = dateline;
		}
		public String getStar() {
			return star;
		}
		public void setStar(String star) {
			this.star = star;
		}
		
	}
	
	public QiuBai() {
		stories = new ArrayList<List<QB>>();
	}
	
	public String getPage(int page){
		String myUrl = "http://m.qiushibaike.com/hot/page/" + page;
		//构建请求的request
		String content = HttpRequest.get(myUrl).userAgent(user_agent).body();
		return content;
	}
	
	public List<QB> getPageItems(int page){
		String pageContent = this.getPage(page);
		List<QB> items = new ArrayList<QiuBai.QB>();
		if(StringKit.isNotBlank(pageContent)){
			
			Pattern pattern = Pattern.compile("<div.*?author\">([\\s\\S]*?)<a[\\s\\S]*?<img.*?>([\\s\\S]*?)</a>[\\s\\S]*?<div.*?content\">([\\s\\S]*?)<!--(.*?)-->[\\s\\S]*?</div>([\\s\\S]*?)<div class=\"stats[\\s\\S]*?class=\"number\">(.*?)</i>");
			Matcher matcher = pattern.matcher(pageContent);
			//遍历正则表达式匹配的信息
			while (matcher.find()) {
				String img = matcher.group(1).replaceAll("\n", "");
				String author = matcher.group(2).replaceAll("\n", "");
				String text = matcher.group(3).replaceAll("\n", "");
				String dateline = matcher.group(4).replaceAll("\n", "");
				String star = matcher.group(6).replaceAll("\n", "");
				
				// 是否含有图片
				if(img.indexOf("img") == -1){
					String date = DateKit.formatDateByUnixTime(Long.valueOf(dateline), "yyyy-MM-dd");
					QB qb = new QB(author, text, date, star);
					items.add(qb);
				}
			} 
		}
		return items;
	}
	
	// 加载并提取页面的内容，加入到列表中
	private void loadPage() {
		//如果当前未看的页数少于2页，则加载新一页
        if (this.enable){
            if(stories.size() < 2){
            	//获取新一页
                List<QB> pageStories = this.getPageItems(this.page);
                //将该页的段子存放到全局list中
                if(pageStories.size() > 0){
                	this.stories.add(pageStories);
                    //获取完之后页码索引加一，表示下次读取下一页
                    this.page += 1;
                }
            }
        }
	}
	
	//调用该方法，每次敲回车打印输出一个段子
    @SuppressWarnings("resource")
	public void showStory(List<QB> items, int page){
    	int count = 1;
        //遍历一页的段子
        for(QB item : items){
        	//等待用户输入
			String aString = new Scanner(System.in).nextLine();
            //如果输入Q则程序结束
            if(aString.equalsIgnoreCase("q")){
            	this.enable = false;
            	break;
            }
            //每当输入回车一次，判断一下是否要加载新页面
            this.loadPage();
            System.out.println(String.format("第%d页第%d条\t发布人:%s\t发布时间:%s\t赞:%s\n%s", page, count, item.getAuthor(), 
            		item.getDateline(), item.getStar(), item.getText()));
            count += 1;
        }
    }
    
    public void run() {
		
		System.out.print("正在读取糗事百科,按回车查看新段子,按Q退出：");
		this.enable = true;
		//先加载一页内容
		this.loadPage();
		//局部变量，控制当前读到了第几页
		int nowPage = 0;
		while (this.enable && this.stories.size() > 0) {
			//从全局list中获取一页的段子
			List<QB> items = this.stories.get(0);
			//当前读到的页数加一
			nowPage += 1;
			//将全局list中第一个元素删除，因为已经取出
			this.stories.remove(0);
			//输出该页的段子
			this.showStory(items, nowPage);
		}
	}

	public static void main(String[] args) {
		System.out.println("---------------------------------------\r\n"
				+ "\t程序：糗百爬虫\r\n" 
				+ "\t版本：0.1\r\n" 
				+ "\t作者：王爵\r\n"
				+ "\t日期：2015-08-18\r\n" 
				+ "\t语言：Java\r\n"
				+ "---------------------------------------");
		new QiuBai().run();
	}

}
