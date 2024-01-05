package doer.lic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import doer.io.Encrypt;

public class LicenseFile {
	private String fileName = "";
	
	private String custName = "";
	private String custAdr1 = "";
	private String custAdr2 = "";
	private String ISRefNo = "";
	private String licCount = "";
	private String licISIs = "";
	private String trialInfo = ""; // "YES:days:date" or "NO"
	private String regCode = "";
	private String dbName = "";
	private String isMultiDb = "";
	private String dbNameList = "";
	
	public LicenseFile() {
		
	}
	
	public LicenseFile(String licFile) throws Exception {
		try {
			setFileName(licFile);
			
			FileInputStream licFileIPStream = new FileInputStream(licFile);
			DataInputStream dis = new DataInputStream(licFileIPStream);;
			
			setCustName(Encrypt.decrypt(dis.readUTF()));
			setCustAdr1(Encrypt.decrypt(dis.readUTF()));
			setCustAdr2(Encrypt.decrypt(dis.readUTF()));
			setISRefNo(Encrypt.decrypt(dis.readUTF()));
			setLicCount(Encrypt.decrypt(dis.readUTF())); 
			setLicISIs(Encrypt.decrypt(dis.readUTF()));
			setTrialInfo(Encrypt.decrypt(dis.readUTF()));
			try {
				setRegCode(dis.readUTF()); // no need decrypt reg code as it was already MD5 hashed
			} catch (EOFException e) {
				// ignore as reg code is not created in .lic file by default
			}
			try {
				setDbName(Encrypt.decrypt(dis.readUTF()));
			} catch (EOFException e) {
				// ignore as db name is not created in .lic file by default
			}
			
			try {
				setIsMultiDb(Encrypt.decrypt(dis.readUTF()));
			} catch (EOFException e) {
				// ignore as is multi db is not created in .lic file by default
			}
			
			try {
				setDbNameList(Encrypt.decrypt(dis.readUTF()));
			} catch (EOFException e) {
				// ignore as db name list is not created in .lic file by default
			}
			
			dis.close();
			licFileIPStream.close();
		} catch (FileNotFoundException e) {
			throw new Exception("Software license file is missing\nPlease contact Doer for support. For contact details, please visit www.doerautomation.in");
		} catch (IOException e) {
			throw new Exception("Software license file is corrupted\nPlease contact Doer for software support. For contact details, please visit www.doerautomation.in");
		} catch (Exception e) {
			throw new Exception("Error reading license file:" + e.getMessage());
		}
	}

	public void rewriteFile() throws Exception {
		FileOutputStream licFileOPStream = new FileOutputStream(getFileName());
		DataOutputStream objLicOPStream = new DataOutputStream(licFileOPStream);
		
		objLicOPStream.writeUTF(Encrypt.encrypt(custName));
		objLicOPStream.writeUTF(Encrypt.encrypt(custAdr1));
		objLicOPStream.writeUTF(Encrypt.encrypt(custAdr2));
		objLicOPStream.writeUTF(Encrypt.encrypt(ISRefNo));
		objLicOPStream.writeUTF(Encrypt.encrypt(licCount));
		objLicOPStream.writeUTF(Encrypt.encrypt(licISIs));
		objLicOPStream.writeUTF(Encrypt.encrypt(trialInfo));
		objLicOPStream.writeUTF(regCode); // no need encrypt reg code as it was already MD5 hashed
		objLicOPStream.writeUTF(Encrypt.encrypt(dbName)); 
		objLicOPStream.writeUTF(Encrypt.encrypt(isMultiDb)); 
		objLicOPStream.writeUTF(Encrypt.encrypt(dbNameList)); 
		
		objLicOPStream.close();
		licFileOPStream.close();
	}
	
	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getCustAdr2() {
		return custAdr2;
	}

	public void setCustAdr2(String custAdr2) {
		this.custAdr2 = custAdr2;
	}

	public String getCustAdr1() {
		return custAdr1;
	}

	public void setCustAdr1(String custAdr1) {
		this.custAdr1 = custAdr1;
	}

	public String getISRefNo() {
		return ISRefNo;
	}

	public void setISRefNo(String iSRefNo) {
		ISRefNo = iSRefNo;
	}

	public String getLicCount() {
		return licCount;
	}

	public void setLicCount(String licCount) {
		this.licCount = licCount;
	}

	public String getRegCode() {
		return regCode;
	}

	public void setRegCode(String regCode) {
		this.regCode = regCode;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	public String getIsMultiDb() {
		return this.isMultiDb;
	}
	
	public void setIsMultiDb(String isMultiDb) {
		this.isMultiDb = isMultiDb;
	}
	
	public String getDbNameList() {
		return dbNameList;
	}
	
	public void setDbNameList(String dbNameList) {
		this.dbNameList = dbNameList;
	}

	public String getLicISIs() {
		return licISIs;
	}

	public void setLicISIs(String licISIs) {
		this.licISIs = licISIs;
	}
	
	public String getTrialInfo() {
		return trialInfo;
	}

	public void setTrialInfo(String trialInfo) {
		this.trialInfo = trialInfo;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
