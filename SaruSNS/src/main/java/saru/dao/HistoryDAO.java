package saru.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import saru.model.History;

public class HistoryDAO {
	
	public List<History>  findByKeyword(int user_id,int post_id,int postTyp) {//後でかきなおして
			List<History> list = new ArrayList<>();

			DataSource ds;
			try {
				InitialContext ctx = new InitialContext();
				ds = (DataSource) ctx.lookup("java:comp/env/jdbc/saru_sns");
			} catch (NamingException e) {
				e.printStackTrace();
				return null;
			}
			//閲覧=1
			//いいね=2
			//ブックマーク=3
			String SQL = "後で書く";
			int Key=0;
			
			if(postTyp ==1) {		
				 SQL = "後で書く";
				 Key=7;
			}else if(postTyp ==2) {
				 SQL = "後で書く";
				 Key=7;
			}else if(postTyp ==3){
				 SQL = "後で書く";
				 Key=7;
			}
				
			try (Connection conn = ds.getConnection();
					PreparedStatement pStmt = conn.prepareStatement(SQL)) {
				pStmt.setInt(1,Key);
				
				try (ResultSet rs = pStmt.executeQuery()) {
					while (rs.next()) {
						
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return null;	
			}
			return list;
	}
}
