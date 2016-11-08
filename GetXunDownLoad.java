package step3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import bean.Porn;
import util.DBConnection;

/**
 * 
 * @ClassName: 91porn
 * @Description: 91porn获取迅雷下载地址，接口：http://91.9p91.com/getfile_jw.php?VID=
 * @author zeze
 * @date 2016年07月01日 21:41:31
 *
 */
public class GetXunDownLoad {

	private static int cnt0 = 0;

	private static String num = null;
	private static String title = null;
	private static String Playurl = null;
	private static String viewkey=null;
	private static String downUrl = null;
	private static String VID = null;
	private static String destDir = "F:/91porn/download/";
	private static String destFile = null;
	private static int flag = 0;
	private static String playid = null;
	private static int currentRow = 0;
	private static int pagesize = 200;

	public static void main(String[] args) throws InterruptedException {

		if (args.length == 1) {
			currentRow = Integer.parseInt(args[0]);
			System.out.println("获取第 " + currentRow + " 开始,pagesize=" + pagesize);
		}
		if (args.length == 2) {
			currentRow = Integer.parseInt(args[0]);
			pagesize = Integer.parseInt(args[1]);
			System.out.println("获取第 " + currentRow + " 开始,pagesize=" + pagesize);
		}
		destFile = destDir + "url" + currentRow + "_" + pagesize + ".txt";
		File file = new File(destFile);// 删除原文件
		if (file.exists())
			deleteDir(file);

		List<Porn> pornList = new ArrayList<Porn>();
		pornList = QueryRemainUrl();
		Iterator<Porn> pornsList2 = pornList.iterator();

		Porn porn = new Porn();
		int cnt = 0;
		while (pornsList2.hasNext()) {
			porn = (Porn) pornsList2.next();
			num = porn.getNum();
			viewkey=porn.getViewkey();
			title = porn.getTitle();
			Playurl = porn.getPlayurl();
			int index = Playurl.indexOf("VID") + 4;
			if (index == -1)
				continue;
			VID = Playurl.substring(index);
			String RequestUrl = "http://91.9p91.com/getfile_jw.php?VID=" + VID;
			cnt++;
			System.out.println(cnt0 + " : " + num + "," + title);
			func_step2(RequestUrl);
		}
		System.out.println("采集结束,总共：" + cnt + "条,成功写入" + cnt0 + "条");
	}

	private static void func_step2(String url) {
		HttpClient httpClient = new HttpClient();
		try {
			GetMethod getMethod = new GetMethod(url);
			getMethod.getParams().setContentCharset("utf-8");
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
			String result = null;
			if (statusCode == 200) {
				result = stringBuffer.toString();
				result = result.substring(result.indexOf("file") + 5);
				playid = result.substring(result.indexOf("mp43/") + 5, result.indexOf(".mp4") + 4);
				// System.out.println("playid:"+playid);
				downUrl = result.substring(0, result.indexOf(".mp4") + 4) + "?";
				downUrl += result.substring(result.indexOf(".mp4") + 5)
						+ "&start=0&id=91&client=FLASH%20WIN%2021,0,0,182&version=4.1.60";
				System.out.println(downUrl);
				cnt0++;
				writefile(downUrl);
				System.out.println("写入文件成功！");
				sqllist();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<Porn> QueryRemainUrl() {
		java.sql.Connection connection = DBConnection.getConnection();
		String sql = "select * from porn where status=1 order by time desc limit " + currentRow + "," + pagesize;
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
				porn.setPlayurl(rs.getString(8));
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

	private static void writefile(String url) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter pw = null;
		try {
			fw = new FileWriter(new File(destFile), true);
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			pw.write(url + "\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// 查询数据库
	public static String sqllist() {
		java.sql.Connection connection = DBConnection.getConnection();
		String sql = null;
		sql = "select * from vedio where viewkey='" + viewkey+"'";
		java.sql.PreparedStatement pstmt = DBConnection.getPreparedStatement(connection, sql);
		List<Porn> userMoodsList = new ArrayList<Porn>();
		String url = null;
		int id = -1;
		try {
			java.sql.ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				id = 1;
			}
			rs.last();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.close(connection, pstmt, null);
		}

		if (id == -1) {//不存在，插入
			sql = "insert into vedio values " + "('" + viewkey + "','" + playid + "')";
			System.out.println("插入数据库成功！");
			java.sql.Connection connection2 = DBConnection.getConnection();
			java.sql.PreparedStatement pstmt2 = DBConnection.getPreparedStatement(connection2, sql);
			try {
				pstmt2.executeUpdate(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBConnection.close(connection2, pstmt2, null);
			}
		}
		else{
			System.out.println("数据库中已存在！");
		}
		return url;
	}

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();// 递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}
}
