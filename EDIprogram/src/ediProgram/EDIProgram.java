package ediProgram;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;

public class EDIProgram {
	final static String PATH = "C:/Users/rolan.schiro/Desktop/EDI Files/";
	final static File MATRIX = new File(PATH + "matrix.csv");
	final static File LOAD_MANAGERS = new File(PATH + "loadmanagers.csv");
	final static ArrayList <EDI> ediRepository = new ArrayList <EDI>();
	final static String FSC = "0";

	public static void main(String[] args) throws IOException {
		System.setProperty("webdriver.chrome.driver", PATH + "/chromedriver.exe/");
		ChromeDriver driver = new ChromeDriver();
		ALCWebManager ediScraper = new ALCWebManager(driver);
		ArrayList<String> cancelledLoads = new ArrayList<String>();
		ArrayList<String> revisedLoads = new ArrayList<String>();
		ArrayList<String> originalLoads = new ArrayList<String>();
		ArrayList<String> incompleteEDIs = new ArrayList<String>();
		
		File folder =  new File(PATH + "/bin");
		
		System.out.println("Start Date:");
//		String startDate = input.nextLine();
		String startDate = "06/30/17";
		
		System.out.println("End Date:");
//		String endDate = input.nextLine();
		String endDate = "07/06/17";
		
		ediScraper.EDIlogIn("rolan.schiro", "rolan.schiro");
		ediScraper.setEDISearchSettings("JDA", "N", startDate, endDate, "5000");
		ediScraper.search();
		
		for(int i = 0; true; i++){
			try{
				ediScraper.scrapeTo(PATH + "bin", i);
			} catch(NoSuchElementException e){
				System.out.println("File Creation Complete!");
				break;
			}
		}

		for(final File e : folder.listFiles()){
			EDI edi = new EDI();
			//file to object conversion, delete file
			try {
				edi = convertFileToEDI(e);
				System.out.println("Successfully converted file "+ e.getName()+ " to EDI object.");
				e.delete();
			} catch (Exception e1) {
				System.out.println("Error opening file "+ e.getName()+ " - continuing to next file.");
				incompleteEDIs.add(e.getName());
				e1.printStackTrace();
				continue;
				}

			//parsing information from EDI's table variable
			try {
				edi.parseAsJDA();
			} catch (Exception e1) {
				System.out.println("Error parsing EDI "+ e.getName()+ "- continuing to next EDI.");
				incompleteEDIs.add(edi.getShipID());
				e1.printStackTrace();
				continue;
			}
			
			if(edi.getStatus().equals("CANCELLED")){
				cancelledLoads.add(edi.getShipID());
			}
			if(edi.getStatus().equals("REVISED")){
				revisedLoads.add(edi.getShipID());
			}
			if(edi.getStatus().equals("ORIGINAL")){
				originalLoads.add(edi.getShipID());
			}

			//checks EDI information is valid on ALX, if true, completes the EDI file clientside by retrieving matrix information
				
			if(ediScraper.checkEDIdata(edi)){
				try {
					completeEDIfile(edi);
					ediRepository.add(edi);
					System.out.println("COMPLETE: Gathered rate, office, and manager for shipment " + e.getName() + ".");
				} catch (Exception e1) {
					System.out.println("ERROR: Cannot complete file " + e.getName() + ".");
					incompleteEDIs.add(edi.getShipID());
					e1.printStackTrace();
					continue;
				}
			}
			else{
				System.out.println("ERROR: Cannot complete file " + e.getName() + ".");
				incompleteEDIs.add(edi.getShipID());
				continue;
			}
					
		}
		
		ediScraper.setEDISearchSettings("JDA", "N", startDate, endDate, "5000");
		ediScraper.search();
		
		ediScraper.outputToALX(ediRepository);
		ediScraper.findLoadIDs(ediRepository, "JDA");
		
		Path results = Paths.get(PATH, "results.csv");
		ArrayList<String> ediResults = new ArrayList<String>();
		ediResults.add("PO,EFJ,OFFICE,REVISED LOAD,SHIP ID");
		
		for(EDI edi : ediRepository){
			ediResults.add(edi.getPONumber() + "," + edi.getLoadNumber() + "," + edi.getOfficeName() + "," + edi.getStatus() + "," + edi.getShipID());
			System.out.println(edi.toString() + "\n");
		}
		Files.write(results, ediResults, StandardCharsets.UTF_8);

		System.out.println("ORIGINAL LOADS:");
		for(String str : originalLoads){
			System.out.println("	" + (originalLoads.indexOf(str) + 1) + ". "+ str);
		}
		
		System.out.println("REVISED LOADS:");
		for(String str : revisedLoads){
			System.out.println("	" + (revisedLoads.indexOf(str) + 1) + ". "+ str);
		}

		System.out.println("CANCELLED LOADS:");
		for(String str : cancelledLoads){
			System.out.println("	" + (cancelledLoads.indexOf(str) + 1) + ". "+ str);
		}
		
		System.out.println("COMPLETED EDIS:");
		for(EDI edi : ediRepository){
			System.out.println("	" + (ediRepository.indexOf(edi) + 1) + ". "+ edi.getShipID());
		}
		
		System.out.println("INCOMPLETE EDIS:");
		for(String str : incompleteEDIs){
			System.out.println("	" + (incompleteEDIs.indexOf(str) + 1) + ". "+ str);
		}
		
		
	}

	public static EDI convertFileToEDI(File edi) throws IOException{
		ArrayList<String> table = new ArrayList<String>();
		List <String> rows = Files.readAllLines(Paths.get(edi.getPath()));
		for(String line : rows){
			table.add(line);
		}
		EDI e = new EDI(table);
		return e;
	}

	public static void completeEDIfile(EDI e) throws IOException{
		
		boolean foo = false;
		//checks for 5 digit zipcodes first
		for(String line : Files.readAllLines(MATRIX.toPath())){
			if(line.contains(e.getOriginZip())){
				String[] l = line.split(",");
				if(l[2].equals(e.getOriginZip()))
					if(l[5].equals(e.getDepot()))
						if(l[7].equals(e.getEquipment())){
							e.setOffice(l[8]);
							e.setRate(l[9]);
							foo = true;
							break;
						}
			}
		}
		//if it cannot find exact 5 digit zip, trims to 3 digit zip
		if(foo == false){
			for(String line : Files.readAllLines(MATRIX.toPath())){
				if(line.contains(e.trimZip(e.getOriginZip()))){
					String[] l = line.split(",");
					if(l[2].equals(e.trimZip(e.getOriginZip())))
						if(l[5].equals(e.getDepot()))
							if(l[7].equals(e.getEquipment())){
								e.setOffice(l[8]);
								e.setRate(l[9]);
								foo = true;
								break;
							}
				}
			}
		}

		//gather load manager and office name
		for(String line : Files.readAllLines(LOAD_MANAGERS.toPath())){
			if (line.contains(e.getOffice())){
				String[] l = line.split(",");
				if(l[0].equals(e.getOffice())){
					e.setOffice(l[1]);
					e.setOfficeName(l[2]);
					e.setLoadManager(l[3]);
					break;
				}
			}
		}
	}
}
