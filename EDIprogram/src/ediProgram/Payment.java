package ediProgram;

public class Payment {

	private String LOAD_ID = "";
	private double PAID_AMOUNT = 0;

	
	public Payment(String dataString){
		String [] data = dataString.split(",");
		if(data.length >= 5)
			try {
				LOAD_ID = data[0].replaceAll("'","").replaceAll("E", "");
				PAID_AMOUNT = Double.parseDouble(data[4]);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			LOAD_ID = data[0].replaceAll("'","").replaceAll("E", "");
			PAID_AMOUNT = 0;
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

}
