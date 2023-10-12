package doer.pv;

import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.jfree.data.xy.XYSeries;

import doer.io.Database;
import doer.io.MyResultSet;

// class to hold all performance calculations
public class Calculate {
	// various params involved/derived for calcualting pump performance
	
	// perf params
	private ArrayList<Double> current = new ArrayList<Double>(); // just captured value as it is
	private ArrayList<Double> ratedDischarge = new ArrayList<Double>();
	private ArrayList<Double> ratedTotHead = new ArrayList<Double>();
	private ArrayList<Double> ratedMotorIP = new ArrayList<Double>();
	private ArrayList<Double> ratedPumpOP = new ArrayList<Double>();
	private ArrayList<Double> ratedOE = new ArrayList<Double>();
	private ArrayList<Double> ratedPE = new ArrayList<Double>();
	private ArrayList<Double> VHC = new ArrayList<Double>();

	// support params
	private Double totHead = 0.0;
	private Double velocityHeadCor = 0.0;
	
	private Double pipeConst = 0.0;
	private Double rtDis = 0.0D;
	private Double rtHead = 0.0D;
	private Double rtMI = 0.0D;
	private Double rtPO = 0.0D;
	private Double rtOE = 0.0D;
	private Double rtPE = 0.0D;
	
	// others
	Database db = null;
	
	Double ratedVal = 0D;
	Double corHead = 0D;
	Double dFact = 1.0D;
	Double hFact = 1.0D;
	Double motEff = 0D;
	Double curRd = 0D;
	Boolean isMotEffDataPoints = false;
	
	// getters
	public Double getCurrent(int i) {
		return current.get(i);
	}
	public Double getRatedDischarge(int i) {
		return ratedDischarge.get(i);
	}
	public Double getRatedTotHead(int i) {
		return ratedTotHead.get(i);
	}
	public Double getRatedMotorIP(int i) {
		return ratedMotorIP.get(i);
	}
	public Double getRatedPumpOP(int i) {
		return ratedPumpOP.get(i);
	}
	public Double getRatedOE(int i) {
		return ratedOE.get(i);
	}
	public Double getRatedPE(int i) {
		return ratedPE.get(i);
	}
	
	public Double getVHC(int i) {
		return VHC.get(i);
	}
	
	// performance calculation
	
	public void doCalculate(String pumpNo, String pumpTypeId, boolean isRatedSpeed, String testedVolt) throws Exception {
		doCalculate(pumpNo, pumpTypeId, isRatedSpeed, testedVolt, "", "");
	}
	
	public void doCalculate(String pumpNo, String pumpTypeId, boolean isRatedSpeed, String testedVolt, String testType, String testName) throws Exception {
		db = Database.getInstance();
		
		// clear the lists
		current.clear();
		ratedDischarge.clear();
		ratedTotHead.clear();
		ratedMotorIP.clear();
		ratedPumpOP.clear();
		ratedOE.clear();
		ratedPE.clear();
		VHC.clear();
		
		String singleTestFilter = "";
		if (!testType.isEmpty()) {
			singleTestFilter = " and test_type='" + testType + "' and test_name_code='" + testName + "'";
		}
		
		MyResultSet res = db.executeQuery("select * from " + Configuration.READING_DETAIL + " where pump_type_id='" +  pumpTypeId + 
				"' and pump_slno = '" + pumpNo + "' and rated_volt = '" + testedVolt +"'" + singleTestFilter + " order by test_name_code");
		if (res.isRowsAvail()) {
			MyResultSet res2 = db.executeQuery("select * from " + Configuration.PUMPTYPE + " where pump_type_id = '" + pumpTypeId + "'");
			if (res2.isRowsAvail()) {
				if (isRatedSpeed) {
					ratedVal = res2.getDouble("speed");
				} else {
					ratedVal = res2.getDouble("freq");
				}
				
				dFact = 1.0D;
				if (res2.getString("discharge_unit").equals("lph")) {
					dFact = 3600.0D;
				} else if (res2.getString("discharge_unit").equals("lpm")) {
					dFact = 60.0D;
				} else if (res2.getString("discharge_unit").equals("gph")) {
					dFact = 0.26417D * 3600.0D;
				} else if (res2.getString("discharge_unit").equals("gpm")) {
					dFact = 0.26417D * 60.0D;
				} else if (res2.getString("discharge_unit").equals("gps")) {
					dFact = 0.26417D; 
				} else if (res2.getString("discharge_unit").contains("sup")) {
					dFact = 0.001D * 3600.0D;
				} else {
					dFact = 1.0D; // lps
				}
				
				hFact = 1.0D;
				if (res2.getString("head_unit").equals("ft")) {
					hFact = 3.28084D;
				} else if (res2.getString("head_unit").equals("bar")) {
					hFact = 0.0980638D;
				} else {
					hFact = 1.0D; // m
				}
				
				corHead = res2.getDouble("gauge_distance");
				
				motEff = res2.getDouble("mot_eff");
				curRd = 0D;
				
				// ISI handling
				if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
					pipeConst = (16/(2*Math.pow(Math.PI,2)*9.81)) * ( (1/Math.pow((res2.getFloat("delivery_size")/1000), 4))) * 1/1000000; // calc with no declared vacuum
				} else {
					pipeConst = (16/(2*Math.pow(Math.PI,2)*9.81)) * ( (1/Math.pow((res2.getFloat("suction_size")/1000), 4)) - (1/Math.pow((res2.getFloat("delivery_size")/1000), 4)) ) * 1/1000000;
				}
				Configuration.CUR_PIPE_CONST = pipeConst;
				
				// pre-load mot eff data points for later use
				XYSeries srMotEff = null;
				try {
					MyResultSet res3 = db.executeQuery("select * from " + Configuration.MOT_EFF_DATA_POINTS + " where pump_type_id = '" + pumpTypeId + "' order by ip_kw");
					if (res3.isRowsAvail()) {
						isMotEffDataPoints = true;
						// create data set with available data points (mi vs eff)
						srMotEff = new XYSeries("ME");
						while (res3.next()) {
							srMotEff.add(res3.getFloat("ip_kw"), res3.getFloat("eff"));
						}
					} else {
						isMotEffDataPoints = false;
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(new JDialog(), e.getMessage());
				}
				
				while(res.next()) {
					// do support calc
					//if (!res.getString("flow").isEmpty()) {
						// ISI handling
						if (Configuration.LAST_USED_ISSTD.startsWith("IS 8034:") || Configuration.LAST_USED_ISSTD.startsWith("IS 14220:")) {
							velocityHeadCor = Math.abs(Math.pow((res.getFloat("flow")/dFact), 2) * pipeConst);
							totHead = res.getFloat("dhead") + corHead + velocityHeadCor;
						} else {
							velocityHeadCor = Math.abs(Math.pow((res.getFloat("flow")/dFact), 2) * pipeConst);
							totHead = res.getFloat("shead") + res.getFloat("dhead") + corHead + velocityHeadCor;
						}
						
						VHC.add(velocityHeadCor);
						// perform calc
						if (isRatedSpeed) {
							curRd = res.getDouble("speed");
						} else {
							curRd = res.getDouble("freq");
						}
						rtDis = curRd == 0 ? 0 : ratedVal / curRd * res.getFloat("flow");
						rtHead = curRd == 0 ? 0 : (Math.pow(ratedVal / curRd,2) * totHead);
						rtMI = curRd == 0 ? 0 : (Math.pow(ratedVal / curRd,3) * res.getFloat("power"));
						rtPO = ((rtDis/dFact) * (rtHead/hFact)) / 102.0F;
						rtOE = rtMI == 0 ? 0 : (rtPO / rtMI) * 100.0F;
						// calculate pump efficiency either based on data points if available or fixed declared mot eff
						if (Configuration.REP_SHOW_PUMP_EFF.equals("1")) {
							if (isMotEffDataPoints) {
								// find motor efficiency at current motor input reading by finding intersection of actual input vs eff
								CustLine il = null;
								CustLine ipVsEffLine = new CustLine(rtMI,0.0D,rtMI,150.0D); // 100% + 50 just in case
						        for(int i=0; i<srMotEff.getItemCount()-1; i++) {
						        	il = findIntersection(ipVsEffLine, new CustLine(srMotEff.getX(i).doubleValue(),srMotEff.getY(i).doubleValue(),srMotEff.getX(i+1).doubleValue(),srMotEff.getY(i+1).doubleValue()),null, null);
						        	if (il != null) {
						        		break;
						        	}
						        }
						        if (il != null) {
						        	motEff = il.getY2();
						        } else {
						        	motEff = 0.0D;
						        }
							}
							rtPE = motEff == 0 ? 0 : (rtOE / motEff) * 100.0F;
						} else {
							rtPE = 0.0;
						}
						
						current.add(res.getDouble("current")); // rated current is just the captured current as it is
						ratedDischarge.add(rtDis);
						ratedTotHead.add(rtHead);
						ratedMotorIP.add(rtMI);
						ratedPumpOP.add(rtPO);
						ratedOE.add(rtOE);
						ratedPE.add(rtPE);
						
					/*} else {
						VHC.add(0.0D);
						current.add(0.0D);
						ratedDischarge.add(0.0D);
						ratedTotHead.add(0.0D);
						ratedMotorIP.add(0.0D);
						ratedPumpOP.add(0.0D);
						ratedOE.add(0.0D);
						ratedPE.add(0.0D);
					}*/
				}
			} else {
				throw new Exception("Error calculating performance: Unable to find pump type with id:" + pumpTypeId);
			}
		} else {
			throw new Exception("Error calculating performance: Unable to find pump slno:" + pumpNo);
		}
		return;
	}
	
	// function to find and return the intersection of two lines
	private CustLine findIntersection(CustLine line1, CustLine line2, Double m1, Double m2)
	{
		Double dx;
		Double dy;
		Double ix;
		Double iy;
		Double c1 = 0.0D;
		Double c2 = 0.0D;
		Boolean l1_vertical = false;
		
		dx = line1.getX2() - line1.getX1();
		dy = line1.getY2() - line1.getY1();
		
		// formula of line => y = mx + c
		if (m1 == null) {
			if (dx == 0) { // vertical line 1
				m1 = 0.0D;
				c1 = 0.0D;
				l1_vertical = true;
			} else {
				m1 = dy/dx;
				c1 = line1.getY1() - m1 * line1.getX1();
			}
		}
		
		
		dx = line2.getX2() - line2.getX1();
		dy = line2.getY2() - line2.getY1();
		if(m2 == null) {
			m2 = dy/dx;
		}
		c2 = line2.getY1() - m2 * line2.getX1(); 
		
		
		if((m1 - m2) == 0)
			return null;
		else
		{
			if (!l1_vertical) {
				ix = (c2 - c1) / (m1 - m2);
				iy = m1 * ix + c1;
			} else { // vertical line => x is constant, y is derived using the m & c of other line 
				ix = line1.getX1();
				iy = m2 * ix + c2;
			}
		}
		
		
		// bound check
		if(isPointInBox(line1.getX1(),line1.getY1(),line1.getX2(),line1.getY2(),ix,iy) && isPointInBox(line2.getX1(),line2.getY1(),line2.getX2(), line2.getY2(),ix,iy))
		{
			CustLine il = new CustLine(0.0D,0.0D,ix,iy);
			return il;
		} else {
			return null;
		}
	}

	private boolean isPointInBox(Double x1, Double y1, Double x2, Double y2, Double ix, Double iy)
	{
		Double left, top, right, bottom;
		
		if(x1 < x2) {
			left = x1;
			right = x2;
		} else {
			left = x2;
			right = x1;
		}
	
		if(y1 < y2) {
			top = y1;
			bottom = y2;
		} else {
			top = y2;
			bottom = y1;
		}
	
		if((ix+0.01) >= left && (ix-0.01) <= right && (iy+0.01) >= top && (iy-0.01) <= bottom ) {
			return true; 
		} else { 
			return false;
		}
	}
}

class CustLine {
	private Double x1=0.0D;
	private Double y1=0.0D;
	private Double x2=0.0D;
	private Double y2=0.0D;
	
	CustLine() {
		
	}
	CustLine(Double x1, Double y1, Double x2, Double y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	public Double getX1() {
		return x1;
	}
	public Double getY1() {
		return y1;
	}
	public Double getX2() {
		return x2;
	}
	public Double getY2() {
		return y2;
	}
}

