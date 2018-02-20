package ediProgram;

public class Payment {

	private String LOAD_ID = "";
	private double PAID_AMOUNT = 0;

	private double SCHEDULED_PAYMENT = 0;
	private String SCHEDULED_DATE = "";

	
	public Payment(String dataString){
		String [] data = dataString.split(",");
		if(data.length >= 8)
			if(!(data[7].isEmpty()))
				try {
					LOAD_ID = data[0].replaceAll("'","").replaceAll("[^\\d.]", "");
					PAID_AMOUNT = Double.parseDouble(data[4]);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			else{
				LOAD_ID = data[0].replaceAll("'","").replaceAll("[^\\d.]", "");
				SCHEDULED_PAYMENT = Double.parseDouble(data[4]);
				SCHEDULED_DATE = data[5];
			}
	}
	
	public String getLOAD_ID() {
		return LOAD_ID;
	}

	public void setLOAD_ID(String lOAD_ID) {
		LOAD_ID = lOAD_ID;
	}

	public double getPAID_AMOUNT() {
		return PAID_AMOUNT;
	}

	public void setPAID_AMOUNT(double pAID_AMOUNT) {
		PAID_AMOUNT = pAID_AMOUNT;
	}
	public double getSCHEDULED_PAYMENT() {
		return SCHEDULED_PAYMENT;
	}

	public void setSCHEDULED_PAYMENT(double sCHEDULED_PAYMENT) {
		SCHEDULED_PAYMENT = sCHEDULED_PAYMENT;
	}

	public String getSCHEDULED_DATE() {
		return SCHEDULED_DATE;
	}

	public void setSCHEDULED_DATE(String sCHEDULED_DATE) {
		SCHEDULED_DATE = sCHEDULED_DATE;
	}

}
