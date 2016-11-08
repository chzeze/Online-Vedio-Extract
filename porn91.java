package step3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import bean.Porn;
import util.DBConnection;

/**
 * 
 * @ClassName: 91porn
 * @Description: 91porn地址解析
 * @author zeze
 * @date 2016年06月30日 下午7:55:31
 *
 */
public class porn91 {
	
	private static String cookie = "incap_ses_434_649914=b6wiYWlZCwyvOk0txuAFBpw0klcAAAAABCNLfzLz0yYsdc/ZwCAMew==; incap_ses_199_649914=HMiecuja1EF/zwrg4f3CAp40klcAAAAAxstF0rtRgLC7l8GwLgddKg==; _gat=1; visid_incap_649914=eSwhoweRTuyfzZcnQLWQTJw0klcAAAAAQUIPAAAAAACu1mAvclWw5XFVzr7y3Hu6; incap_ses_401_649914=36h4WlhcjEzW4AYrA6SQBWo1klcAAAAAhWaF7GkLdpvrgBgY6Kgonw==; _ga=GA1.2.2130541701.1469199521; session=eyJfZnJlc2giOmZhbHNlLCJjc3JmX3Rva2VuIjp7IiBiIjoiTURSbU16VmhOekppWkRVd01tSm1ZVEl4TlRRd1ltVTRNakptTURVM016Tm1ZbUZsTmpOak5BPT0ifX0.CnPHAQ.ZC00C8p69e3CXePhWAIAnQj6bMM";
	private static String Token = "1469203344##2709a3bff2e04495d88ee0372e37b6a474221e17";
	private static String cookie2 = "incap_ses_434_649914=b6wiYWlZCwyvOk0txuAFBpw0klcAAAAABCNLfzLz0yYsdc/ZwCAMew==; incap_ses_199_649914=HMiecuja1EF/zwrg4f3CAp40klcAAAAAxstF0rtRgLC7l8GwLgddKg==; _gat=1; visid_incap_649914=eSwhoweRTuyfzZcnQLWQTJw0klcAAAAAQUIPAAAAAACu1mAvclWw5XFVzr7y3Hu6; incap_ses_401_649914=36h4WlhcjEzW4AYrA6SQBWo1klcAAAAAhWaF7GkLdpvrgBgY6Kgonw==; _ga=GA1.2.2130541701.1469199521; session=eyJfZnJlc2giOmZhbHNlLCJjc3JmX3Rva2VuIjp7IiBiIjoiTURSbU16VmhOekppWkRVd01tSm1ZVEl4TlRRd1ltVTRNakptTURVM016Tm1ZbUZsTmpOak5BPT0ifX0.CnPHBA.JZpgvatB2fzsd5esaZeu7q0Scqw";
	private static int jumpNum=0;
	
	private static String Url = "http://freeget.co/video/extraction";
	private static String url001 = null;

	private static int cnt0 = 0;

	private static String num = null;
	private static String title = null;
	private static String time = null;
	private static String longtime = null;
	private static String viewnum = null;
	private static String Parurl = null;// "http://www.91porn.com/view_video.php?viewkey=c5ec60d0da8c8fbdb180&page=4&viewtype=basic&category=mr";


	public static void main(String[] args) throws InterruptedException {

		List<Porn> pornList = new ArrayList<Porn>();
		pornList = QueryRemainUrl();
		Iterator<Porn> pornsList2 = pornList.iterator();

		Porn porn = new Porn();
		int cnt = 0;
		while (pornsList2.hasNext()) {
			porn = (Porn) pornsList2.next();
			cnt++;
			if(cnt<=jumpNum)continue;//跳过的数目
			num = porn.getNum();
			title = porn.getTitle();
			time = porn.getTime();
			longtime = porn.getLongtime();
			viewnum = porn.getViewnum();
			Parurl = porn.getParurl();
			System.out.println(cnt+":"+num + "," + title + "," + time + "," + Parurl);
			func_step1();
		}
		System.out.println("采集结束,总共：" + cnt + "条,成功写入" + cnt0 + "条");
	}

	private static void func_step1() {
		HttpClient httpClient = new HttpClient();
		try {
			PostMethod postMethod = new PostMethod(Url);
			postMethod.getParams().setContentCharset("utf-8");
			// 每次访问需授权的网址时需 cookie 作为通行证
			postMethod.setRequestHeader("cookie", cookie);
			postMethod.setRequestHeader("X-CSRFToken", Token);
			postMethod.setRequestHeader("Accept-Language", "zh-CN,zh;q=0.8");
			postMethod.setRequestHeader("Host", "freeget.co");
			postMethod.setRequestHeader("Referer", "http://freeget.co/");
			postMethod.setRequestHeader("User-Agent",
					"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0) QQBrowser/9.2.5063.400");
			postMethod.setParameter("url", Parurl);
			int statusCode = httpClient.executeMethod(postMethod);// 返回状态码200为成功，500为服务器端发生运行错误
			System.out.println("返回状态码：" + statusCode);
			// 打印出返回数据，检验一下是否成功
			if (statusCode == 404) {
				System.err.println("未找到对应视频信息，可能该视频尚未收录或已被删除");
			}
			if (statusCode == 400) {
				System.err.println("Bad Request,请更新cookies");
			}
			String result = postMethod.getResponseBodyAsString();
			if (statusCode == 200) {
				// 解析成功，取得token和view_key
				JSONObject a = new JSONObject(result);
				url001 = "http://freeget.co/video/" + a.get("view_key") + "/" + a.get("token");
				System.out.println("视频解析地址：" + url001);
				func_step2(url001);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void func_step2(String url) {
		HttpClient httpClient = new HttpClient();
		try {
			GetMethod getMethod = new GetMethod(url);
			getMethod.getParams().setContentCharset("utf-8");
			getMethod.setRequestHeader("cookie", cookie2);
			getMethod.setRequestHeader("Accept-Language", "zh-cn");
			getMethod.setRequestHeader("User-Agent",
					"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0) QQBrowser/9.2.5063.400");
			int statusCode = httpClient.executeMethod(getMethod);// 返回状态码200为成功，500为服务器端发生运行错误
			// System.out.println("返回状态码：" + statusCode);
			// 打印出返回数据，检验一下是否成功
			InputStream inputStream = getMethod.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			StringBuffer stringBuffer = new StringBuffer();
			String str = "";
			while ((str = br.readLine()) != null) {
				stringBuffer.append(str);
			}
			if (statusCode == 200) {
				Document doc = Jsoup.parse(stringBuffer.toString());
				Elements name = doc.select("a");
				String playurl = null;
				try {
					playurl = name.get(4).text();
				} catch (Exception e) {
					// TODO: handle exception
					System.err.println("获取播放地址失败！,请更新cookies！");
				}
				if (playurl.indexOf("VID") != -1) {
					System.out.println("在线播放地址：" + playurl);
					UpdataUrl(playurl);
					cnt0++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 查询数据库
	public static void UpdataUrl(String playurl) {
		java.sql.Connection connection = DBConnection.getConnection();
		String sql = null;
		sql = "update porn set playurl='" + playurl + "',status=1 where id=" + num;
		// System.out.println(sql);
		java.sql.PreparedStatement pstmt = DBConnection.getPreparedStatement(connection, sql);
		try {
			pstmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.close(connection, pstmt, null);
		}

		return;
	}

	public static List<Porn> QueryRemainUrl() {
		java.sql.Connection connection = DBConnection.getConnection();
		String sql = "select * from porn where status=0";
		java.sql.PreparedStatement pstmt = DBConnection.getPreparedStatement(connection, sql);
		List<Porn> pornlist = new ArrayList<Porn>();
		System.out.println(sql);
		try {
			Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			java.sql.ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Porn porn = new Porn();
				porn.setNum(rs.getString(1));
				porn.setTitle(rs.getString(2));
				porn.setTime(rs.getString(3));
				porn.setViewkey(rs.getString(4));
				porn.setLongtime(rs.getString(5));
				porn.setViewnum(rs.getString(6));
				porn.setParurl(rs.getString(7));
				pornlist.add(porn);
			}
			rs.last();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.close(connection, pstmt, null);
		}
		return pornlist;
	}
}
