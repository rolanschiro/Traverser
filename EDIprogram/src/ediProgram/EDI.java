package ediProgram;

import java.io.IOException;
import java.util.ArrayList;

public class EDI {
	
	String SHIP_ID = null;
	String LOAD_NUMBER = null;
	String PO_NUMBER = null;
	String ORIGIN_ZIP = null;
	String DEST_ZIP = null;
	String DEST_DEPOT = null;
	String EQUIPMENT_TYPE = null;
	String OFFICE = null;
	String OFFICE_NAME = null;
	String RATE = null;
	String LOAD_MANAGER = null; 
	String STATUS = null;
	ArrayList <String> table = null; 
	
	public EDI(String shipID, String origin, String depot, String equipment){
		SHIP_ID = shipID;
		ORIGIN_ZIP = origin;
		DEST_DEPOT = depot;
		EQUIPMENT_TYPE = equipment;
	}
	
	public EDI(ArrayList <String> t){
		table = t;
	}
	
	public EDI(){
		
	}
	
	public String parse(int row, int index){
		String[] line = table.get(row).split("\\*");
		return line[index];
	}
	
	public ArrayList<String[]> parseRowsByID(String id){
		ArrayList<String[]> rows = new ArrayList<String[]>();
		for(String line : table)
		{
			if(line.contains(id))
				if(line.split("\\*")[0].equals(id))
					rows.add(line.split("\\*"));
		}
		return rows;
	}
	
	public void parseAsJDA() throws IOException{
		setShipID(this.parse(0, 0));
		
		//checks if shipment has been cancelled
		if(this.parseRowsByID("B2A").get(0)[1].replaceAll("~", "").equals("01"))
			setStatus("CANCELLED");
		else if(this.parseRowsByID("B2A").get(0)[1].replaceAll("~", "").equals("00"))
			setStatus("ORIGINAL");
		else
			setStatus("REVISED");
		
		//getting and setting origin zipcode
		String zip = this.parseRowsByID("N4").get(1)[3];
		setOriginZip(zip.replaceAll("~", ""));

		//getting and setting destination zipcode		
		zip = this.parseRowsByID("N4").get(this.parseRowsByID("N4").size() - 1)[3];
		setDestZip(zip.substring(0, 5));
		
		String poNum = this.parseRowsByID("OID").get(this.parseRowsByID("OID").size() - 1)[2];
		setPONumber(poNum);
		
		//finding depot and equipment type
		String[] str = this.parseRowsByID("N1").get(this.parseRowsByID("N1").size() - 1);
		String dep = str[4].replaceAll("~", "");
		if(dep.length() > 4)
			dep = dep.substring(dep.length() - 4);
		if(dep.charAt(0) == '0'){
			dep = dep.substring(1);
		}
		setDepot(dep);
		if(str[2].contains("DRY"))
			setEquipment("DRY VAN");
		else
			setEquipment("REFRIGERATED");
	}
	
	public boolean isComplete(){
		return true;
		
	}
	
	//removing excess digits from zip to interface with the matrix
	public String trimZip(String zip){
		String str;
		if(zip.length() > 5)
			str = zip.substring(0, 5);
		else
			str = zip.substring(0, 3);
		return str;
	}
	
	//get methods
	public String getOfficeName(){
		return OFFICE_NAME;
	}
	public String getPONumber(){
		return PO_NUMBER;
	}
	public String getLoadNumber() {
		return LOAD_NUMBER;
	}
	public String getStatus(){
		return STATUS;
	}
	public String getOriginZip(){
		return ORIGIN_ZIP;
	}
	public String getDestZip(){
		return DEST_ZIP;
	}
	public String getDepot(){
		return DEST_DEPOT;
	}
	public String getEquipment(){
		return EQUIPMENT_TYPE;
	}
	public String getOffice(){
		return OFFICE;
	}
	public String getRate(){
		return RATE;
	}
	public String getLoadManager(){
		return LOAD_MANAGER;
	}
	public String getShipID(){
		return SHIP_ID;
	}	
	public ArrayList <String> getTable(){
		return table;
	}
	
	//set methods
	public void setPONumber(String po){
		PO_NUMBER = po;
	}
	public void setShipID(String id){
		SHIP_ID = id;
	}
	
	public void setOffice(String office){
		OFFICE = office;
	}
	
	public void setRate(String rate){
		RATE = rate;
	}
	
	public void setLoadManager(String loadmanager){
		LOAD_MANAGER = loadmanager;
	}

	public void setTable(ArrayList <String> t){
		table = t;
	}
	
	public void setOriginZip(String zip){
		ORIGIN_ZIP = zip;
	}
	public void setDestZip(String zip){
		DEST_ZIP = zip;
	}
	public void setDepot(String depot){
		DEST_DEPOT = depot;
	}
	public void setEquipment(String equip){
		EQUIPMENT_TYPE = equip;
	}
	public void setStatus(String status){
		STATUS = status;
	}
	public void setLoadNumber(String load) {
		LOAD_NUMBER = load;
	}
	public void setOfficeName(String name){
		OFFICE_NAME = name;
	}
	public String toString(){
		String str;
		str = 	"SHIP ID = " + getShipID() +
				"\nLOAD # = " + getLoadNumber() +
				"\nPO # = " + getPONumber() +
				"\nORIGIN ZIP = " + getOriginZip() + 
				"\nDEST ZIP = " + getDestZip() +
				"\nDEPOT # = " + getDepot() +
				"\nEQUIPMENT = " + getEquipment() +
				"\nOFFICE = " + getOffice() +
				"\nRATE = " + getRate() +
				"\nLOAD MANAGER = " + getLoadManager() + 
				"\nSTATUS = " + getStatus();
		
		return str;
	}
	
}