package step3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/*****
 * 
* @ClassName: KKRefresh 
* @Description: KK刷阅读量
* @author zeze
* @date 2016年7月6日 上午10:16:14 
*
 */

public class KKRefresh {
	
	private static String url = "http://www.0597kk.com/forum.php?mod=viewthread&tid=2240811&extra=page%3D1";	
	private static int i=1;
	private static int RefreshNum=100000000;//刷新次数
	private static int RefreshTime=0;//刷新间隔
	private static SimpleDateFormat dayformt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	public static void main(String[] args) throws InterruptedException{
		
		if(args.length==1){
			RefreshNum=Integer.parseInt(args[0]);
		}
		System.out.println("刷新网址："+url+",刷新次数："+RefreshNum);
		
		for(i=1;i<=RefreshNum;i++){
			func_httpGet(url);
			Thread.sleep(RefreshTime);
		}
	}
	
	private static void func_httpGet(String url) {
		HttpClient httpClient = new HttpClient();
		try {
			GetMethod getMethod = new GetMethod(url);
			//getMethod.getParams().setContentCharset("uft-8");
			//getMethod.setRequestHeader("Accept-Encoding", "uft-8,deflate, sdch");
			//getMethod.setRequestHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			//getMethod.setRequestHeader("Referer","http://www.0597kk.com/thread.php?fid=2");
			//getMethod.setRequestHeader("Accept-Language", "zh-CN,zh;q=0.8");
			//getMethod.setRequestHeader("User-Agent","Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0) QQBrowser/9.2.5063.400");
			int statusCode = httpClient.executeMethod(getMethod);// 返回状态码200为成功，500为服务器端发生运行错误
			//System.out.println("返回状态码：" + statusCode);
			// 打印出返回数据，检验一下是否成功
			//String result = getMethod.getResponseBodyAsString();
			InputStream inputStream = getMethod.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			StringBuffer stringBuffer = new StringBuffer();
			String str = "";
			while ((str = br.readLine()) != null) {
				stringBuffer.append(str);
			}
			if (statusCode == 200) {
				Document doc = Jsoup.parse(stringBuffer.toString());
				Elements num = doc.select("span[class=xi1]");
				try {
					System.out.println(dayformt.format(new Date())+" Num:"+num.get(0).text());
				} catch (Exception e) {
					// TODO: handle exception
					System.err.println("Get Failed");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
