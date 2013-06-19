package com.invindible.facetime.database;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;


import oracle.sql.BLOB;

import com.invindible.facetime.model.Imageinfo;
import com.invindible.facetime.model.User;


public class UserDao {
	
	/**
	 * 注册所用 插入数据库
	 * @param u
	 * @param conn
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	
	public static int[]  doInsert(User u,Connection conn,Imageinfo im) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		// TODO Auto-generated method stub
		PreparedStatement pst=conn.prepareStatement("insert into userinfo values(userid.nextval,?,?)");
		pst.setString(1, u.getUsername());
		pst.setString(2, u.getPassword()); 
		pst.executeUpdate();
		pst=conn.prepareStatement("select id from userinfo where username=? and password=?");
		pst.setString(1, u.getUsername());
		pst.setString(2, u.getPassword());
		ResultSet rs=pst.executeQuery();
		rs.next();
		int id=rs.getInt("id");
		InputStream[] is=im.getInputstream();
		for(int i=0;i<5;i++){		
			pst=conn.prepareStatement("insert into imageinfo values("+id+",?)");
			pst.setBinaryStream(1, is[i],is[i].available());
			pst.executeUpdate();
		}
		pst=conn.prepareStatement("insert into project(id) values(?)");
		pst.setInt(1, id);
		pst.executeUpdate();
		pst=conn.prepareStatement("insert into classmean(id) values(?)");
		pst.setInt(1, id); 
		pst.executeUpdate();
		pst=conn.prepareStatement("select id from project",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		rs=pst.executeQuery();
		rs.last();
		int[] allid=new int[rs.getRow()];
		int index=0;
		rs.beforeFirst();
		while(rs.next())
		{
			allid[index++]=rs.getInt("id");
		}
		pst.close();
		rs.close();
		return allid;
	}
	
	/**
	 * 检验
	 * @param conn
	 */
	public static BufferedImage[] doSelectAll (Connection conn) {
		// TODO Auto-generated method stub
		BufferedImage[] bf=null;
		try {
			PreparedStatement pst=conn.prepareStatement("select image from imageinfo",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			ResultSet rs=pst.executeQuery();
			rs.last();
			InputStream[] fos=new InputStream[rs.getRow()];
			bf=new BufferedImage[rs.getRow()];
			rs.beforeFirst();
			int index=0;
			while(rs.next()){
//				this.setUsername(rs.getString("username"));
				fos[index]=rs.getBinaryStream("image");
				bf[index]= ImageIO.read(fos[index++]);
				//im=javax.imageio.ImageIO.read(fos);	
//				System.out.println("finish"+rs.getString("username"));
			}
			pst.close();
			rs.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		return bf;
		
	}
	
	public static boolean registerable(Connection conn,User u) throws SQLException{
		String username=u.getUsername();
		PreparedStatement pst=conn.prepareStatement("select username from userinfo");
		ResultSet rs=pst.executeQuery();
		while(rs.next()){
			if(rs.getString("username").equals(username))
				return false;
		}
		return true;
	}
	
	/**
	 * 一般的输入账户密码登陆
	 * @param u
	 * @param conn
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static boolean doLogin(User u,Connection conn) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		boolean flag;
		PreparedStatement pst=conn.prepareStatement("select username,password from userinfo where username=? and password=?");
		pst.setString(1, u.getUsername());
		pst.setString(2, u.getPassword());
		ResultSet rs=pst.executeQuery();
		if(rs!=null)
			flag=true;
		else
			flag=false;
		pst.close();
		return flag;
	}
	
	/**
	 * 找出相应的人
	 * @param result 判别结果
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static User doLoginByImage(int id,Connection conn) throws SQLException{
		PreparedStatement pst=conn.prepareStatement("select username,password from userinfo where id=?");
		pst.setInt(1, id);
		ResultSet rs=pst.executeQuery();
		rs.next();
		User u=new User();
		u.setUsername(rs.getString("username"));
		u.setPassword(rs.getString("password"));
		return u;		
	}
	
	public static BufferedImage[] selectImageById(int id,Connection conn) throws SQLException, IOException{
		PreparedStatement pst = conn.prepareStatement("select image from imageinfo where id=?");
		pst.setInt(1, id);
		ResultSet rs = pst.executeQuery();
		InputStream[] fos=new InputStream[5];
		BufferedImage[] bf=new BufferedImage[5];
		int index=0;
		while(rs.next()){
//			this.setUsername(rs.getString("username"));
			fos[index]=rs.getBinaryStream("image");
			bf[index]= ImageIO.read(fos[index++]);
			//im=javax.imageio.ImageIO.read(fos);	
//			System.out.println("finish"+rs.getString("username"));
		}
		pst.close();
		rs.close();
		return bf;
	}
}
