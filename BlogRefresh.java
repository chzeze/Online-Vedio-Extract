/**   
* @Title: BlogRefreshFun.java 
* @Package step3 
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2016年8月10日 上午10:54:44 
* @version V1.0   
*/
package step3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.james.mime4j.message.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BlogRefresh {
	private static String url = "https://www.amazon.com/gp/offer-listing/B01LQ6UEYS?m=A29M1BYRALDYMF&mv_color_name=all&mv_size_name=all";
	private static int i = 1;
	private static int RefreshNum = 1;// 刷新次数
	private static int RefreshTime = 10000;// 刷新间隔
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private static String outputpath = "f:/testvpn/";

	public static void main(String[] args) throws InterruptedException {

		if (args.length == 1) {
			RefreshNum = Integer.parseInt(args[0]);
		}
		System.out.println(dateFormat.format(new Date()) + " 次数：" + RefreshNum + ",刷新网址：" + url);

		for (i = 1; i <= RefreshNum; i++) {
			func_httpGet(url);
			Thread.sleep(RefreshTime);
		}
	}

	/**
	  * 抽取页面的商品数量，并返回pid的值，用作之后的查重操作
	  */
	public static Set<StringBuffer> AmazonExtractGoodsList(StringBuffer html) {
		Set<StringBuffer> goodsList = new HashSet<StringBuffer>();
		Document doc = Jsoup.parse(html.toString());
		Elements Rlist = doc.select("div[id=variationRow]");
		//System.out.println("goods list size:"+Rlist.size());
		int index=-1;
		if (Rlist.size() >= 0) {
			for (Element result : Rlist) {
				String goodUrl=result.select("a[class=a-link-normal]").get(0).attr("href");
				index=goodUrl.indexOf("/offer-listing/");
				if(index!=-1){
					System.out.println("Good Pid:"+goodUrl.substring(index+15, index+25));
					goodsList.add(new StringBuffer(goodUrl.substring(index+15, index+25)));
				}
			}
		}else{
			System.out.println("This product No Goods");
		}
		System.out.println("goods list size:"+goodsList.size());
		return goodsList;
	}

	/***
	 * 返回拼接好的Lowest 的url,带有m参数,mv_size_name=all&mv_color_name=all
	 */
	public static String AmazonExtractLowestUrl(StringBuffer html) {
		StringBuffer lowestUrl = new StringBuffer();
		Document doc = Jsoup.parse(html.toString());
		Elements Rlist = doc.select("a[class=a-button-text]");
		StringBuffer pid = new StringBuffer();
		int indexPid = -1, indexM = -1;
		if (Rlist.size() >= 0) {
			for (Element result : Rlist) {
				if (result.text().equals("Lowest offer for each")) {
					String urlStr = result.attr("href");
					indexPid = urlStr.indexOf("/offer-listing/");
					indexM = urlStr.indexOf("m=");
					if (indexPid != -1) {// 找到pid
						pid.append("https://www.amazon.com/gp/offer-listing/");
						pid.append(urlStr.substring(indexPid + 15, urlStr.indexOf("/ref=")));
						if (indexM != -1) {// 找到m值
							pid.append("?");
							pid.append(urlStr.substring(indexM, indexM + 16));
							pid.append("&mv_size_name=all&mv_color_name=all");
						} else {
							pid.append("?mv_size_name=all&mv_color_name=all");
						}
						System.out.println("Lowest Url:" + pid);
					}
					return pid.toString();
				}
			}
		}
		System.out.println("No Lowest Url");
		return null;
	}

	public static Set<StringBuffer> AmazonExtract4Index(StringBuffer html) {
		Document doc = Jsoup.parse(html.toString());
		Elements RList = doc.select("div[class=s-item-container]");
		// logger.info("商品数目:" + RList.size());
		System.out.println(Thread.currentThread().getName() + " " + "商品数目:" + RList.size());
		if (RList.size() < 1)
			return (new HashSet<StringBuffer>());
		Set<StringBuffer> links = new HashSet<StringBuffer>();
		for (Element result : RList) {
			StringBuffer pid = new StringBuffer();
			StringBuffer title = new StringBuffer();
			StringBuffer price = new StringBuffer();
			StringBuffer url = new StringBuffer();
			int index = -1;
			title.append(result.select("h2").text());
			price.append(result.select("span[class=a-size-base a-color-price s-price a-text-bold]").text());
			if (price.length() == 0) {
				price.append(result.select("span[class=a-size-base a-color-price a-text-bold]").text());
			}

			Elements urlList = result.select("a[class=a-link-normal s-access-detail-page  a-text-normal]");
			if (urlList.size() < 1) {
				urlList = result.select("a[class=a-link-normal s-access-detail-page  visited-title a-text-normal]");
			}
			if (urlList.size() < 1)
				System.out.println("urlList empty:" + urlList.size());
			System.out.println(urlList.attr("href"));
			url.append(urlList.attr("href"));
			index = url.indexOf("/dp/");
			if (index != -1) {
				pid.append(url.substring(index + 4, url.indexOf("/ref=")));
				links.add(pid);
			}

		}
		// -------------------------------------------------------
		System.out.println(Thread.currentThread().getName() + " " + "返回的pid数目:" + links.size());
		return links;
	}

	private static void func_httpGet(String url) {
		HttpClient httpClient = new HttpClient();
		try {
			GetMethod getMethod = new GetMethod(url);
			getMethod.getParams().setContentCharset("uft-8");
			getMethod.setRequestHeader("Accept-Encoding", "uft-8,deflate,sdch");
			getMethod.setRequestHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			getMethod.setRequestHeader("Host", "www.amazon.com");
			getMethod.setRequestHeader("Accept-Language", "zh-CN,zh;q=0.8");
			getMethod.setRequestHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36");
			int statusCode = httpClient.executeMethod(getMethod);// 返回状态码200为成功，500为服务器端发生运行错误
			System.out.println("返回状态码：" + statusCode);
			// 打印出返回数据，检验一下是否成功
			// String result = getMethod.getResponseBodyAsString();
			// System.out.println(result);
			InputStream inputStream = getMethod.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			StringBuffer stringBuffer = new StringBuffer();
			String str = "";
			while ((str = br.readLine()) != null) {
				stringBuffer.append(str);
			}
			if (statusCode == 200) {
				AmazonExtractGoodsList(stringBuffer);
				// 链接入口分层
				/*
				 * String Fisturl = getBrandUrl(stringBuffer.toString()); if
				 * (Fisturl == null) {// 没有see more List<String> UrlList = new
				 * ArrayList<String>(); UrlList =
				 * getBrandList(stringBuffer.toString()); if (UrlList.isEmpty())
				 * {// 没有brand列表 System.out.println("depth=2 " + url); } else {
				 * int cnt = 0; for (String str_url : UrlList) {
				 * System.out.println((cnt++) + ":depth=2 " + str_url); }
				 * 
				 * } } else { System.out.println("depth=1 " + Fisturl); }
				 */

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<String> getBrandList(String page) {
		List<String> UrlList = new ArrayList<String>();
		Document doc = Jsoup.parse(page);
		// brand没有see more,判断页面是否有brand
		Elements results = doc.select("h2");
		for (Element result : results) {
			Element tempNode = result.nextElementSibling();
			if (tempNode == null)
				continue;
			Elements tempNodes = tempNode.select("ul[id=ref_0]");
			if (result.text().equals("Brand") && tempNodes.size() > 0) {
				Elements results1 = tempNode.select("a");
				// System.out.println("brand Num:" + results1.size());
				int cnt = 0;
				for (Element result1 : results1) {
					String url = "https://www.amazon.com" + result1.attr("href");
					UrlList.add(url);
					// System.out.println((cnt++) + ":depth=2 " + url);
				}
			}
		}

		if (UrlList.isEmpty()) {
			// 2016年9月28日09:47:32 brand没有see
			// more,但是有categoryRefinementsSection，更改解析列表
			results = doc.select("div[class=categoryRefinementsSection]");
			results = results.select("span[class=refinementLink]");
			for (Element result : results) {
				Element tempNode = result.nextElementSibling();
				if (tempNode != null) {
					Elements tempNodes = tempNode.select("span[class=narrowValue]");
					if (tempNodes.size() > 0) {
						// System.out.println(result.text()+"
						// "+tempNode.text());
						String url = "https://www.amazon.com" + result.parentNode().attr("href");
						UrlList.add(url);
					}
				}
			}
		}
		return UrlList;
	}

	/**
	 * 
	 * 获取品牌的url
	 * 
	 * @Title: getBrandUrl
	 * @param @param
	 *            page
	 * @param @return
	 *            设定文件
	 * @return String 返回类型
	 */
	private static String getBrandUrl(String page) {
		String url = null;
		Document doc = Jsoup.parse(page);
		Elements count = doc.select("h2[id=s-result-count]");
		// System.out.println("总数:" + count.get(0).text());

		Elements results1 = doc.select("span[class=expander]");
		int cnt = 0;
		// System.out.println("size:"+results1.size());
		for (Element result : results1) {
			String url1 = result.parentNode().parentNode().attr("href").toString();
			if (url1.indexOf("pickerToList=lbr_brands_browse-bin") != -1
					|| url1.indexOf("pickerToList=brandtextbin") != -1) {
				url = "https://www.amazon.com" + url1;
				// System.out.println("depth=1:" + url);
			}
		}
		return url;
	}

	private static void savePage(String page) {

		long start = System.currentTimeMillis();

		String path = null;
		File file2 = null;
		path = new String(outputpath + dateFormat.format(new Date()) + ".html");
		file2 = new File(outputpath);
		if (!file2.exists())
			file2.mkdirs();

		file2 = new File(path);

		FileOutputStream outputStream;

		// System.out.println(path);
		System.out.println(page);

		try {
			outputStream = new FileOutputStream(file2);
			outputStream.write(page.getBytes());
			start = System.currentTimeMillis();
			outputStream.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}

}
