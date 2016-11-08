package step3;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bean.Porn;
import util.DBConnection;
/*****
 * 
* @ClassName: DelRepeatData 
* @Description: 删除数据库重复数据
* @author zeze
* @date 2016年7月2日 下午4:11:06 
*
 */

public class DelRepeatData {
	
	private static String num = null;
	private static String title = null;
	private static String viewKey = null;

	
	public static void main(String[] args) throws InterruptedException {
	
		List<Porn> pornList = new ArrayList<Porn>();
		pornList = QueryAllData();
		Iterator<Porn> pornsList2 = pornList.iterator();

		Porn porn = new Porn();
		int cnt = 0;
		while (pornsList2.hasNext()) {		
			porn = (Porn) pornsList2.next();
			cnt++;
			if(cnt<26000)continue;
			num = porn.getNum();
			title = porn.getTitle();
			viewKey = porn.getViewkey();
			QueryViewNum(viewKey);
			System.out.println(cnt+":"+num + "," + title + "," + viewKey);
		}
		System.out.println("查重结束,总共:" + cnt + "条");
	}
	
	public static List<Porn> QueryViewNum(String viewKey) {
		java.sql.Connection connection = DBConnection.getConnection();
		String sql = "select * from porn where viewKey='"+viewKey+"'";
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
				pornlist.add(porn);
			}
			rs.last();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.close(connection, pstmt, null);
		}
		
		if(pornlist.size()>1){
			System.err.println("重复数据条数 ："+pornlist.size()+" 条");
			Iterator<Porn> pornsList2 = pornlist.iterator();
			Porn porn = new Porn();
			int cnt = 0;
			while (pornsList2.hasNext()) {
				porn = (Porn) pornsList2.next();
				cnt++;
				if(cnt==1)continue;//保留一条
				num = porn.getNum();
				viewKey = porn.getViewkey();
				DeleteInfo(num,viewKey);
				System.out.println("删除第  "+(cnt-1)+" 条重复记录,Id="+num);
			}
		}
		else{
			System.out.println("没有重复条数！");
		}
		return pornlist;
	}
	
	public static void DeleteInfo(String num,String viewkey) {
		java.sql.Connection connection = DBConnection.getConnection();
		String sql = "delete from porn where Id='"+num+"' and viewkey='" +viewkey+"'";
		java.sql.PreparedStatement pstmt = DBConnection.getPreparedStatement(connection, sql);
		System.out.println(sql);
		try {
			Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			stmt.executeUpdate(sql);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			DBConnection.close(connection, pstmt, null);
		}
	}
	
	public static List<Porn> QueryAllData() {
		java.sql.Connection connection = DBConnection.getConnection();
		String sql = "select * from porn";
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
