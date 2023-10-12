package doer.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import doer.pv.Configuration;

/* singleton class for DB handling */
public class Database {
	// local variables
	private static Database db = null;
	private static Connection conn = null;
	private static Statement stmt = null;
	private static ResultSet res = null;
	
	private Database () {
		// DB initialization
		try {
			conn = DriverManager.getConnection(Configuration.DB_URL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(new JDialog(), "DB Error:" + e.getMessage());
		}
	}
	
	public static Database getInstance () {
		if (db == null) {
			db = new Database();
		}
		return db;
	}
	
	public void closeInstance () {
		try {
			conn.close();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(new JDialog(), "DB Error:" + e.getMessage());
		}
	}
	
	public MyResultSet executeQuery(String sql) throws SQLException {
		// get the result
		res = stmt.executeQuery(sql);
		// convert to list and respond back
	    ResultSetMetaData md = res.getMetaData();
	    int columns = md.getColumnCount();
	    List<Map<String, Object>> list = new ArrayList<>();
	    while (res.next()) {
	        Map<String, Object> row = new HashMap<>(columns);
	        for (int i = 1; i <= columns; ++i) {
	            row.put(md.getColumnName(i), res.getObject(i));
	        }
	        list.add(row);
	    }
	    res.close();
	    return new MyResultSet(list);
	}
	
	public int executeUpdate(String sql) throws SQLException {
		return stmt.executeUpdate(sql);
	}
}
