package doer.io;

import java.util.List;
import java.util.Map;

// class to hold result set got out of executing a SQL query
public class MyResultSet {
	
	List<Map<String, Object>> rs = null;
	int curIdx = -1;
	
	// constructor
	public MyResultSet (List<Map<String, Object>> rsList) {
		this.rs = rsList;
	}
	
	
	// getters
	public int getLength () {
		return this.rs.size();
	}
	
	public boolean isRowsAvail () {
		return this.rs.size() > 0;
	}
	
	public boolean next () {
		if (curIdx < rs.size() - 1) {
			++curIdx;
			return true;
		} else {
			return false;
		}
	}
	
	public String getString (String colName) {
		try {
			return this.rs.get(curIdx < 0 ? 0 : curIdx).get(colName).toString();
		} catch (NullPointerException ne) {
			return "";
		}
	}
	public Integer getInt (String colName) {
		try {
			return Integer.valueOf(this.rs.get(curIdx < 0 ? 0 : curIdx).get(colName).toString());
		} catch (Exception e) {
			return 0;
		}
	}
	public Long getLong (String colName) {
		
		try {
			return Long.valueOf(this.rs.get(curIdx < 0 ? 0 : curIdx).get(colName).toString());
		} catch (Exception e) {
			return 0L;
		}
	}
	public Double getDouble (String colName) {
		try {
			return Double.valueOf(this.rs.get(curIdx < 0 ? 0 : curIdx).get(colName).toString());
		} catch (Exception e) {
			return 0D;
		}
	}
	public Float getFloat (String colName) {
		try {
			return Float.valueOf(this.rs.get(curIdx < 0 ? 0 : curIdx).get(colName).toString());
		} catch (Exception e) {
			return 0F;
		}
	}
	public Boolean getBoolean (String colName) {
		try {
			return Boolean.valueOf(this.rs.get(curIdx < 0 ? 0 : curIdx).get(colName).toString().equals("true") ? "true" : "false");
		} catch (NullPointerException ne) {
			return false;
		}
	}
	public Object getObject(String colName) {
		return this.rs.get(curIdx < 0 ? 0 : curIdx).get(colName);
	}
}
