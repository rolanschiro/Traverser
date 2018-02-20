package ediProgram;

public class Invoice {
	
	private String OFFICE = "";
	private String FILE_NUMBER = "";
	private String LOAD_ID = "";
	private double BILLED_AMOUNT = 0;
	private String ADJ = "";
	private double PAID_AMOUNT = 0;
	private double SCHEDULED_PAYMENT = 0;
	private double BALANCE_DUE = 0;
	private String DAYS_OLD = "";
	private String SHIPPER_NUMBER = "";
	private String SCHEDULED_PAYMENT_DATE = "";
	
	public Invoice(String dataString){
		String [] data = dataString.split(",");
		if(data.length == 8)
		try {
			FILE_NUMBER = data[0];
			OFFICE = FILE_NUMBER.substring(0, 2);
			LOAD_ID = data[1];
			BILLED_AMOUNT = Double.parseDouble(data[2]);
			ADJ = data[3];
			PAID_AMOUNT = Double.parseDouble(data[4]);
			BALANCE_DUE = Double.parseDouble(data[5]);
			DAYS_OLD = data[6];
			SHIPPER_NUMBER = data[7];
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Invoice(){
		
	}
	
	public String getFILE_NUMBER() {
		return FILE_NUMBER;
	}

	public  void setFILE_NUMBER(String fILE_NUMBER) {
		FILE_NUMBER = fILE_NUMBER;
	}

	public String getLOAD_ID() {
		return LOAD_ID;
	}

	public void setLOAD_ID(String lOAD_ID) {
		LOAD_ID = lOAD_ID;
	}

	public double getBILLED_AMOUNT() {
		return BILLED_AMOUNT;
	}

	public void setBILLED_AMOUNT(double bILLED_AMOUNT) {
		BILLED_AMOUNT = bILLED_AMOUNT;
	}

	public String getADJ() {
		return ADJ;
	}

	public void setADJ(String aDJ) {
		ADJ = aDJ;
	}

	public double getPAID_AMOUNT() {
		return PAID_AMOUNT;
	}

	public void setPAID_AMOUNT(double pAID_AMOUNT) {
		PAID_AMOUNT = pAID_AMOUNT;
	}

	public double getBALANCE_DUE() {
		return BALANCE_DUE;
	}

	public void setBALANCE_DUE(double bALANCE_DUE) {
		BALANCE_DUE = bALANCE_DUE;
	}

	public String getDAYS_OLD() {
		return DAYS_OLD;
	}

	public void setDAYS_OLD(String dAYS_OLD) {
		DAYS_OLD = dAYS_OLD;
	}

	public String getSHIPPER_NUMBER() {
		return SHIPPER_NUMBER;
	}

	public void setSHIPPER_NUMBER(String sHIPPER_NUMBER) {
		SHIPPER_NUMBER = sHIPPER_NUMBER;
	}

	public String getSCHEDULED_PAYMENT_DATE() {
		return SCHEDULED_PAYMENT_DATE;
	}

	public void setSCHEDULED_PAYMENT_DATE(String payment_date) {
		SCHEDULED_PAYMENT_DATE = payment_date;
	}
	public double getSCHEDULED_PAYMENT() {
		return SCHEDULED_PAYMENT;
	}

	public void setSCHEDULED_PAYMENT(double sCHEDULED_PAYMENT) {
		SCHEDULED_PAYMENT = sCHEDULED_PAYMENT;
	}

	public String toString(){
		String str = OFFICE + "," + FILE_NUMBER + "," + SHIPPER_NUMBER + "," + LOAD_ID + "," + BILLED_AMOUNT + "," + ADJ + "," + PAID_AMOUNT + "," + DAYS_OLD + "," + SCHEDULED_PAYMENT + "," + SCHEDULED_PAYMENT_DATE + "," + BALANCE_DUE + ",";
		return str;
	}
}
