package ediProgram;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;


public class ALCWebManager {

	ChromeDriver driver;
	ArrayList <String> tempLog = new ArrayList<String>();
	ArrayList <String> diagLog = new ArrayList<String>();
	ArrayList <String> errLog = new ArrayList<String>();
	
	public ALCWebManager(ChromeDriver d)
	{
		driver = d;
	}

	public ChromeDriver getDriver(){
		return driver;
	}

//--------------------------------------------------------
// EDI Methods
//--------------------------------------------------------
	public void scrapeTo(String folderPath, int index){

		ArrayList <String> str = new ArrayList<String>();
		
		//opens EDI file
		driver.findElementByXPath("(//table/tbody/tr/td/a[@target='_blank'])[" + (index + 1) + "]").click();
		//focuses WebScraper on current tab
		while(driver.getWindowHandles().size() == 1)
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ArrayList<String> tabs = new ArrayList<String> (driver.getWindowHandles());
		driver.switchTo().window(tabs.get(1));
		
		WebElement shipIDwait = (new WebDriverWait(driver, 10))
				  .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#R21019224307929393_body > tr:nth-child(2) > td > table:nth-child(5) > tbody > tr:nth-child(1) > td:nth-child(2)")));
		String shipID = shipIDwait.getText();

		//finds latest update to EDI Shipment Received
		List <WebElement> greenWait = (new WebDriverWait(driver, 10))
				  .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("img[src*='green']")));
		ArrayList <WebElement> ediUpdates = new ArrayList<WebElement> (greenWait);
		ediUpdates.get(ediUpdates.size() - 1).click();

		//switch to iframe in page, adds information to array
		driver.switchTo().frame(0);
		str.add(shipID);
		WebElement ediTextWait = (new WebDriverWait(driver, 10))
				  .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a/font[@color='#FFFFFF']")));
		str.add(ediTextWait.getText());

		//checks for existing file, creates new file and populates with array
		Path file = Paths.get(folderPath, str.get(0));
		
		try {
			Files.write(file, str, StandardCharsets.UTF_8);
		} catch (IOException e) {
			log("[ERROR]: Could not write file to bin.");
		}
		driver.close();
		driver.switchTo().window(tabs.get(0));
	}
	
	//searches from, to date
	public void search(String id){
		driver.findElementByXPath("//input[@id='P281_SEARCH']").sendKeys(id);
		driver.findElementByLinkText("Search").click();
	}
	
	public void search(){
		driver.findElementByLinkText("Search").click();
	}
	
	//applies search settings
	public void setEDISearchSettings(String custCode, String status, String startDate, String endDate, String displayRows){
		for(String cust : custCode.split(";")){
			try {
				driver.findElementByXPath("//input[@name='P281_EDI_CUSTOMER_CODE2'][@value='"+ cust + "']").click();
			} catch (Exception e) {
			}
		}
		driver.findElementByXPath("//select[@id='P281_EDI_STATUS']/option[@value='" + status + "']").click();
		driver.findElementByXPath("//select[@id='P281_ROWS']/option[@value='"+ displayRows +"']").click();
		driver.findElementByXPath("//input[@id='P281_FROM_DATE']").clear();
		driver.findElementByXPath("//input[@id='P281_FROM_DATE']").sendKeys(startDate);
		driver.findElementByXPath("//input[@id='P281_TO_DATE']").clear();
		driver.findElementByXPath("//input[@id='P281_TO_DATE']").sendKeys(endDate);
	}
	
	public void setEDISearchSettings(String custCode, String status, String displayRows){
		for(String cust : custCode.split(";")){
			try {
				driver.findElementByXPath("//input[@name='P281_EDI_CUSTOMER_CODE2'][@value='"+ cust + "']").click();
			} catch (Exception e) {
			}
		}
		driver.findElementByXPath("//select[@id='P281_EDI_STATUS']/option[@value='" + status + "']").click();
		driver.findElementByXPath("//select[@id='P281_ROWS']/option[@value='"+ displayRows +"']").click();
		driver.findElementByXPath("//input[@id='P281_FROM_DATE']").clear();
		driver.findElementByXPath("//input[@id='P281_FROM_DATE']").sendKeys("01/01/00");
		driver.findElementByXPath("//input[@id='P281_TO_DATE']").clear();
		driver.findElementByXPath("//input[@id='P281_TO_DATE']").sendKeys("01/01/50");
	}
	
	//logs in to ALX EDI interface
	public void EDIlogIn(String user, String pass){
		driver.get("http://alx-prod.allenlund.com:7777/pls/apex/f?p=45290:281:4120368540374876::NO:RP:P0_AVAIL_LOAD,P0_EDI_LOAD,P0_ACTIVE_LOAD,P0_WATCH_LOAD,P0_REV_LOAD:%2CEDI%2C%2C%2C");
		
		WebElement userID = driver.findElement(By.id("P101_USERNAME"));
		WebElement passID = driver.findElement(By.id("P101_PASSWORD"));
		
		userID.sendKeys(user);
		passID.sendKeys(pass);
		
		passID.submit();
	}
	
	public boolean checkEDIdata(EDI e){
		boolean check = false;
		driver.findElementByXPath("//input[@id='P281_SEARCH']").clear();
		setEDISearchSettings("", "NULL", "5000");
		search(e.getShipID());
		List<WebElement> webelems = driver.findElementsByXPath("//div[contains(text(), '+')]");
		
		//if revised, finds revised load #, checks zipcodes
		if(e.getStatus().equals("REVISED")){
			for(WebElement w : webelems){
				driver.switchTo().window(driver.getWindowHandle());
				w.click();
				driver.switchTo().frame(0);
				try {
					e.setStatus(driver.findElementByXPath("//input[@id='P316_EFJ_ERROR']").getAttribute("value").split(" ")[5]);
					try {
						WebElement rc = (new WebDriverWait(driver, 3))
								  .until(ExpectedConditions.visibilityOfElementLocated(By.linkText("ReCreate EFJ")));
						rc.click();
						log("Shipment " + e.getShipID() + " was REVISED and recreated.");
					} catch (Exception e1) {
						log("Shipment " + e.getShipID() + " was REVISED. EFJ was already recreated.");
					}
					
					check = checkZipcodes(e);
					break;
				} catch (Exception e1) {
					log("[ERROR]: Could not find associated load number with shipment.");
					e.addToErrorLog(getErrLog());
					continue;
				}
			}
		}
		
		//if cancelled, find cancelled load #
		else if(e.getStatus().equals("CANCELLED"))
		{
			for(WebElement w : webelems){
				driver.switchTo().window(driver.getWindowHandle());
				w.click();
				driver.switchTo().frame(0);
				try {
					WebElement rc = (new WebDriverWait(driver, 3))
							  .until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Update EFJ Manually")));
					e.setLoadNumber(driver.findElementByXPath("//input[@id='P316_EFJ_ERROR']").getAttribute("value").split(" ")[5]);
					try {
						rc.click();
						log("Shipment " + e.getShipID() + " was CANCELLED. EFJ recreated.");
					} catch (Exception e1) {
						log("Shipment " + e.getShipID() + " was CANCELLED. EFJ was already recreated.");
					}
					check = true;
					break;
				} catch (Exception e1) {
					log("[ERROR]: Could not find associated load number with shipment.");
					e.addToErrorLog(getErrLog());
					continue;
				}
			}

		}
		else{
			driver.switchTo().window(driver.getWindowHandle());
			webelems.get(0).click();
			driver.switchTo().frame(0);
			check = checkZipcodes(e);
		}
		driver.switchTo().window(driver.getWindowHandle());
		driver.findElementByXPath("//input[@id='P281_SEARCH']").clear();

		return check;
	}
	
	public boolean safeCheckEDIdata(EDI e){
		boolean check = false;
		driver.findElementByXPath("//input[@id='P281_SEARCH']").clear();
		setEDISearchSettings("", "NULL", "5000");
		search(e.getShipID());
		List<WebElement> webelems = driver.findElementsByXPath("//div[contains(text(), '+')]");
		
		//if revised, finds revised load #, checks zipcodes
		if(e.getStatus().equals("REVISED")){
			for(WebElement w : webelems){
				driver.switchTo().window(driver.getWindowHandle());
				w.click();
				driver.switchTo().frame(0);
				try {
					e.setStatus(driver.findElementByXPath("//input[@id='P316_EFJ_ERROR']").getAttribute("value").split(" ")[5]);
					try {
						WebElement rc = driver.findElementByLinkText("ReCreate EFJ");
						log("Shipment " + e.getShipID() + " was REVISED.");
					} catch (Exception e1) {
						log("EFJ was already recreated.");
					}
					
					check = checkZipcodes(e);
					break;
				} catch (Exception e1) {
					continue;
				}
			}
		}
		
		//if cancelled, find cancelled load #
		else if(e.getStatus().equals("CANCELLED"))
		{
			for(WebElement w : webelems){
				driver.switchTo().window(driver.getWindowHandle());
				w.click();
				driver.switchTo().frame(0);
				try {
					e.setLoadNumber(driver.findElementByXPath("//input[@id='P316_EFJ_ERROR']").getAttribute("value").split(" ")[5]);
					try {
						WebElement rc = driver.findElementByLinkText("Update EFJ Manually");
						log("Shipment " + e.getShipID() + " was CANCELLED.");
					} catch (Exception e1) {
						log("EFJ was already recreated.");
					}
					check = true;
					break;
				} catch (Exception e1) {
					continue;
				}
			}

		}
		else{
			driver.switchTo().window(driver.getWindowHandle());
			webelems.get(0).click();
			driver.switchTo().frame(0);
			check = checkZipcodes(e);
		}
		driver.switchTo().window(driver.getWindowHandle());
		driver.findElementByXPath("//input[@id='P281_SEARCH']").clear();

		return check;
	}

	public boolean checkZipcodes(EDI e)
	{
		boolean c = true;

		//check origin zip codes EXACT MATCH

		driver.findElementByXPath("//span[text()='Pick Up Info']").click();

		if(!(e.getOriginZip().equals(driver.findElementByXPath("//td[@headers='Zip']/input").getAttribute("value")))){
			log("[ERROR]: Shipment " + e.getShipID() + " PICK UP zipcodes do not match." +
					"\n	+ CORRECT ZIP = " + e.getOriginZip() +
					"\n	- LISTED ZIP = " + driver.findElementByXPath("//td[@headers='Zip']/input").getAttribute("value"));
			e.addToErrorLog(getErrLog());
			c = false;
		}
		else
			log("	~ ORIGIN ZIP CODES MATCH! ~");
		
		//check destination zip codes PARTIAL MATCH

		driver.findElementByXPath("//span[text()='Delivery Info']").click();
		if(!(driver.findElementByXPath("//td[@headers='Zip']/input").getAttribute("value").contains(e.getDestZip()))){
			log("[ERROR]: Shipment " + e.getShipID() + " DESTINATION zipcodes do not match." +
					"\n	+ CORRECT ZIP = " + e.getDestZip() + 
					"\n	- LISTED ZIP = " + driver.findElementByXPath("//td[@headers='Zip']/input").getAttribute("value"));
			e.addToErrorLog(getErrLog());
			c = false;
		}
		else
			log("	~ DESTINATION ZIP CODES MATCH! ~");
		return c;
	}
	
	public void outputToALX(ArrayList<EDI> edis){
		List<WebElement> webElemsWait = (new WebDriverWait(driver, 30))
				  .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//div[contains(text(), '+')]")));

		List<WebElement> shipments = driver.findElementsByXPath("//a[@target='_blank']");
		List<String> shipIDs = new ArrayList<String>();

		for(WebElement w : shipments){
			shipIDs.add(w.getText());
		}

		for(EDI e : edis){
			int index = 0;
			index = shipIDs.indexOf(e.getShipID());
			if(index == -1){
				log("[ERROR]: Shipment " + e.getShipID() + " could not be found on page.");
				e.addToErrorLog(getErrLog());
				continue;
			}
			try {
				webElemsWait.get(index).click();
				log("Output path established for EDI " + e.getShipID());

			} catch (Exception e1) {
				log("[ERROR]: Could not find associated frame with shipment.");
				e.addToErrorLog(getErrLog());
				continue;
			}
			driver.switchTo().frame(0);
			WebElement shipmentHeaderWait = (new WebDriverWait(driver, 10))
					  .until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Shipment Header']")));
			shipmentHeaderWait.click();
			
			WebElement freightRateWait = (new WebDriverWait(driver, 10))
			  .until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='P316_FREIGHT_RATE']")));
			
			freightRateWait.clear();
			freightRateWait.sendKeys(e.getRate());
			
			driver.findElementByLinkText("Apply Changes").click();
			
			driver.switchTo().window(driver.getWindowHandle());
			
			if(index >= 9){
				WebElement office = driver.findElementByXPath("//select[@id='f02_00" + (index + 1) + "']");
				checkSelectionOutput(office, e, e.getOffice(), 3);
			}
			else{
				WebElement office = driver.findElementByXPath("//select[@id='f02_000" + (index + 1) + "']");
				checkSelectionOutput(office, e, e.getOffice(), 3);
			}

			WebElement loadManager = driver.findElementByXPath("//select[@id='f35_0000" + (index + 1) + "']" );;
			checkSelectionOutput(loadManager, e, e.getLoadManager(), 3);
			
			if(driver.findElementByXPath("//*[@id='APP00" + (index + 1) + "']").isEnabled()){
				
				try {
					driver.findElementByXPath("//*[@id='APP00" + (index + 1) + "']").click();
					log("Shipment APPROVED for EFJ creation.");
				} catch (Exception e1) {
					log("[ERROR]: Unable to approve shipment " + e.getShipID() + ".");
					e.addToErrorLog(getErrLog());
					e1.printStackTrace();
				}
			}
			else{
				log("[ERROR]: Unable to approve shipment " + e.getShipID() + ".");
				e.addToErrorLog(getErrLog());
			}
		}
		driver.findElementByXPath("//input[@id='CREATE_EFJS']").click();
	}
	
	//checks output is correct to ALX
	public void checkSelectionOutput(WebElement outputElem, EDI e, String correctVal, int attempts){
		Select selection = new Select(outputElem);
		for (int i = 1; true; i++){
			outputElem.sendKeys(correctVal);
			if(selection.getFirstSelectedOption().getText().equalsIgnoreCase(correctVal)){
				break;
			}
			log("Value:	[" + correctVal + "]	(ATTEMPT #" + i + ")." +
				"\n		OUTPUT: " + selection.getFirstSelectedOption().getText().toUpperCase() + 
				"\n		CORRECT: " + correctVal);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(i == attempts){
				log("[ERROR]: Could not set correct value (3 attempts) for shipment " + e.getShipID() + ".");
				e.addToErrorLog(getErrLog());
				break;
			}
		}
	}
	
	public void findLoadIDs(ArrayList<EDI> edis, String custCode){
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yy");
		String date = (dateTime.format(format));

		setEDISearchSettings("", "L", date, date, "5000");
		search();
		
		List<WebElement> webElemsWait = (new WebDriverWait(driver, 10))
				  .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//td[@headers='EFJLoad ID']/a")));

		List<WebElement> shipments = driver.findElementsByXPath("//a[@target='_blank']");
		List<String> shipIDs = new ArrayList<String>();

		for(WebElement w : shipments){
			shipIDs.add(w.getText());
		}
		
		for(EDI e : edis){
			int index = 0;
			index = shipIDs.indexOf(e.getShipID());
			if(e.getStatus().equals("CANCELLED"))
				continue;
			if(index == -1){
				log("[ERROR]: Could not retieve LOAD# for shipment " + e.getShipID() + " (not found on page).");
				e.addToErrorLog(getErrLog());
				continue;
			}
			try {
				e.setLoadNumber(webElemsWait.get(index).getText());
				log("Load number retrieved for shipment " + e.getShipID());
			} catch (Exception e1) {
				log("[ERROR]: Could not find associated frame with shipment.");
				e.addToErrorLog(getErrLog());
				continue;
			}

		}
	}

//--------------------------------------------------------
// Accounting Methods
//--------------------------------------------------------
	public void accountingLogIn(String user, String pass){
		driver.get("http://alx-prod.allenlund.com:7777/pls/apex/f?p=45290:281:4120368540374876::NO:RP:P0_AVAIL_LOAD,P0_EDI_LOAD,P0_ACTIVE_LOAD,P0_WATCH_LOAD,P0_REV_LOAD:%2CEDI%2C%2C%2C");
		
		WebElement userID = driver.findElement(By.id("P101_USERNAME"));
		WebElement passID = driver.findElement(By.id("P101_PASSWORD"));
		
		userID.sendKeys(user);
		passID.sendKeys(pass);
		
		passID.submit();
	}

	public void log(String str){
		tempLog.add(str);
		diagLog.add(str);
		if(str.contains("[ERROR]")){
			errLog.add(str);
		}
	}
	
	public String getTempLog(){
		String str = String.join("\n", tempLog);
		tempLog.clear();
		return str;
	}
	
	public String getDiagLog(){
		String str = String.join("\n", diagLog);
		return str;
	}
	
	public String getErrLog(){
		String str = String.join("\n", errLog);
		errLog.clear();
		return str;
	}
	
}

