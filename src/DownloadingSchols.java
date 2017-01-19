import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DownloadingSchols {
	
	public static final String SCHOLS_YEAR_OF_COURSE = "2";
	public static final int MAX_SEARCH_VALUE = 541;				//541 is the max searchValue value at https://www.tcd.ie/Local/Exam_Papers/schols.html

    public static void main(String[] args) throws IOException {
        
        //Download the 1998-2012 Schols papers from the archive.
        //Sort the past papers.
        //Download the 2013-2016 Schols papers and set searchValues from old versions of the same module from 1998-2012
        //		Printing out each module that doesn't have an old version and 'assuming' its values.
        
        List<PastPaper> pastPapers = new ArrayList<PastPaper>();

        pastPapers = addOldScholsPapers(pastPapers);
        pastPapers = addNewScholsPapers(pastPapers);

        writePapersToCSV(pastPapers,"/Users/GeoffreyNatin/Documents/GithubRepositories/examinating/past-papers/ScholsPapers.csv");
        
    }
    
    private static List<PastPaper> addOldScholsPapers(List<PastPaper> pastPapers) throws IOException{

        //Cycle through every searchValue
		for(int searchValue=0;searchValue<MAX_SEARCH_VALUE;searchValue++){	
			
			//Add appropriate amount of leading zeros to the searchValue for the url
			String svWithZeros = searchValue<10? "00"+searchValue : (searchValue<100? "0"+searchValue : ""+searchValue);
			
			//If valid searchValue
			if(!getCourseNameSchols(svWithZeros).equals("Course unknown")){
				
				//Add past papers from every year for this searchValue
	    		for(int year=2012;year>1998;year--){
	    			
	    			
	    			String url = "https://www.tcd.ie/Local/Exam_Papers/schol_search.cgi?Course="+svWithZeros+"&acyear="+year;
	    			
	    			//Download the web page
        	        Document doc = Jsoup.connect(url).get();
        	        
        	        //If the web page has past papers, then add them.
        	        if(hasPapers(doc)){
        	        	
				        Elements ps = doc.select("p");
				        Elements entireBody = doc.select("body");
				        
				      //The first past paper on each page needs to be extracted differently.
				        String firstModuleID = doc.select("a").get(0).text();
		    			firstModuleID = "\""+firstModuleID+"\"";
				        String firstModuleYear = "\""+ year+"\"";
				        String firstModuleName = entireBody.get(0).ownText();
		        		if(firstModuleName.contains("\"")){										//Escape all commas and quotes
		        			firstModuleName = firstModuleName.replace('\"',' ');
		        		}
		        		firstModuleName = "\""+firstModuleName+"\"";
				        String firstModuleLink = "https://www.tcd.ie"+doc.select("a").attr("href");
				        String firstModuleAcademicYear = ""+ SCHOLS_YEAR_OF_COURSE;
				        String firstModuleSearchValue = "\""+ svWithZeros+"\"";	
				        
				        //Add first past paper on the page.
		    			if(!firstModuleID.equals("\"\"")){
					        PastPaper f = new PastPaper(firstModuleID,firstModuleYear,firstModuleName,firstModuleLink,firstModuleAcademicYear,firstModuleSearchValue);
			        		pastPapers.add(f);
			        	}
				        
		    			//Add every past paper on the page after the first
				        for (Element p : ps) {		
				        	
				        	//Ignore the <p> that indicates amount of past papers on the page.
				        	if(p.children().size()!=0 && p.text().startsWith("Your")){
				        		
				        	}
				        	//If the <p> has a past paper in it
				        	else if(p.children().size()!=0 && !p.child(0).html().startsWith("/Local") && !p.child(0).attr("href").startsWith("http://")){
				    			
				        		String moduleID = p.child(0).html();
				    			moduleID = "\""+moduleID+"\"";								//Escape all commas
				        		String yearString = "\""+year+"\"";
				        		String searchValueString = "\""+svWithZeros+"\"";
				        		String moduleName = p.text();
				        		
				        		moduleName = moduleName.substring(moduleName.indexOf(' ')+1,moduleName.length());	//Get rid of moduleID from moduleName
				        		if(moduleName.contains("\"")){														//Escape all commas and quotes
				        			moduleName = "\""+moduleName.replace('\"',' ')+"\"";
				        		}
				        		else{
				        			moduleName = "\""+moduleName+"\"";
				        		}
				        		String link = "https://www.tcd.ie"+p.child(0).attr("href");
				        		
				        		//Add the past paper
				        		PastPaper n = new PastPaper(moduleID,yearString,moduleName,link,SCHOLS_YEAR_OF_COURSE,searchValueString);
				        		pastPapers.add(n);
				    		}
				        }
		    		}
				}
	    	}
		}
		//Sort the list having added all the past papers.
        Collections.sort(pastPapers);
        return pastPapers;
    }

    private static List<PastPaper> addNewScholsPapers(List<PastPaper> pastPapers) throws IOException{

        List<PastPaper> newPastPapers = new ArrayList<PastPaper>();
        
        //For every year, add all past papers
        for(int year=2013;year<2017;year++){
        	
        	//Download the web page
	        String url = "https://www.tcd.ie/academicregistry/exams/past-papers/scholarship-"+(year-1)%100+""+(year)%100+"/";
	        //Document doc = Jsoup.connect(url).get();
	        
	        File f = new File("/Users/GeoffreyNatin/Desktop/Exam_sites/annual-"+(year-1)%100+""+(year)%100+"/");
	        Document doc = Jsoup.parse(f,null);
	        
	        Elements rows = doc.select("tr");
	        
	        Element currentCourse = rows.get(0);
	        
	        //Go through the rows of the web page, adding the past papers.
	        for (Element row : rows) {
	        	
	        	//If the row has one element, set the current course to that.
	        	if(row.children().size()==1){
	        		currentCourse = row.child(0);
	        		
	        		//Remove any wrapping html
	        		while(currentCourse.children().size()>0){
	        			currentCourse = currentCourse.child(0);
	        		}
	        	}
	        	//If the row is a past paper
	        	else if(row.children().size()==2){
	        		
	        		//Extract the name and code of the paper
	        		Element name = row.child(0);
	        		Element code = row.child(1);
	        		
	        		//Remove wrapping html
	        		while(code.children().size()>0){
	        			code = code.child(0);
	        		}
	        		while(name.children().size()>0){
	        			name = name.child(0);
	        		}
	        		
	        		//If the row is a valid past paper row
	        		if(!code.html().equals("") && !name.html().equals("") && !currentCourse.equals("") && !row.child(1).html().equals("&nbsp;") && code.hasAttr("href")){
		        		
	        			
	        			String currentCourseString = "\""+currentCourse.html();
		        		if(currentCourseString.contains(" ")){
		        			currentCourseString = currentCourseString.substring(0,currentCourse.html().lastIndexOf(' ')+1)+"\"";
		        		}
		        		else{
		        			currentCourseString += "\"";
		        		}
		        		
		        		//Try to match the course of the past paper to a course from the past papers
		        		boolean matched = false;
		        		for(int t=0;t<pastPapers.size();t++){
		        			if(pastPapers.get(t).getModuleID().equals("\""+code.html()+"\"") || getCourseNameSchols(pastPapers.get(t).getSearchValue().substring(1,pastPapers.get(t).getSearchValue().length()-1)).equals(currentCourseString.substring(1,currentCourseString.length()-1))){
		        				currentCourseString = pastPapers.get(t).getSearchValue();
		        				matched = true;
		        			}
		        		}
		        		
		        		//Print out if a past paper was not added to a course
		        		if(!matched){
		        			System.out.println("Couldn't match "+currentCourseString);
		        		}
		        		
		        		//Add the past paper to the list.
		        		PastPaper n = new PastPaper("\""+code.html()+"\"","\""+year+"\"","\""+name.html()+"\"",url+code.attr("href"),SCHOLS_YEAR_OF_COURSE,currentCourseString);
		        		newPastPapers.add(n);
	        		}
	    		}
	        }
        }
        
        //Add all 
        pastPapers.addAll(newPastPapers);
        Collections.sort(pastPapers);
        return pastPapers;
    }
     
    private static boolean hasPapers(Document doc){
        Elements entireBody = doc.select("body");
        boolean noPapersFound = false;
		 for (Element elem : entireBody) {
			 if(elem.text().contains("No papers found")){			
	        	noPapersFound = true;
	         }
	     }
		 if(noPapersFound){
			 return false;
		 }
    	 return true;
    }
    
    //Writes an ArrayList of past papers to a csv file
    private static void writePapersToCSV(List<PastPaper> pastPapers,String csv){
    	
    	
    	final String FILE_HEADER = "moduleID,year,moduleName,link,academicYear,searchValue";
    	final String NEW_LINE_SEPARATOR = "\n";
    	
    	 FileWriter fileWriter = null;
         try{
         	fileWriter = new FileWriter(csv);
         	fileWriter.append(FILE_HEADER.toString());
         	fileWriter.append(NEW_LINE_SEPARATOR);
         	
         	for (PastPaper p : pastPapers) {
         		fileWriter.append(p.CSVFormat());
         	}
         }
         catch(Exception e){
         	System.out.println("Error while writing to the CSV.");
         	e.printStackTrace();
         }
         finally{
         	try{
         		fileWriter.flush();
         		fileWriter.close();
         	}
             catch(IOException e){
             	System.out.println("Error while flushing/closing fileWriter.");
             	e.printStackTrace();
             }
         }
    }

    //Returns the course name that corresponds to the search value argument for schols exams
    private static String getCourseNameSchols(String sv){
    	String courseName;
    	switch(sv){
	    	case "482": courseName = "Ancient and Medieval History and Culture"; break;
	    	case "481": courseName = "Business and Computing"; break;
	    	case "405": courseName = "Business Studies and a Language"; break;
	    	case "046": courseName = "Business, Economic and Social Studies"; break;
	    	case "456": courseName = "Chemistry with Molecular Modelling"; break;
	    	case "005": courseName = "Classics"; break;
	    	case "037": courseName = "Clinical Speech and Language Studies"; break;
	    	case "019": courseName = "Computer Science (BA)"; break;
	    	case "073": courseName = "Computer Science (B.Sc.)"; break;
	    	case "412": courseName = "Computer Science (B.Sc.) - Honors"; break;
	    	case "057": courseName = "Computer Science, Linguistics and a Language"; break;
	    	case "503": courseName = "Deaf Studies"; break;
	    	case "067": courseName = "Dental Science"; break;
	    	case "027": courseName = "Drama and Theatre Studies"; break;
	    	case "031": courseName = "Early and Modern Irish"; break;
	    	case "477": courseName = "Earth Sciences"; break;
	    	case "070": courseName = "Engineering"; break;
	    	case "464": courseName = "Engineering with Management"; break;
	    	case "048": courseName = "English Studies"; break;
	    	case "053": courseName = "European Studies"; break;
	    	case "049": courseName = "Germanic Languages"; break;
	    	case "040": courseName = "History"; break;
	    	case "026": courseName = "History and Political Science"; break;
	    	case "420": courseName = "Human Genetics"; break;
	    	case "476": courseName = "Human Health and Disease"; break;
	    	case "069": courseName = "Human Nutrition and Dietetics"; break;
	    	case "541": courseName = "Human Nutrition and Dietetics (B.Sc.)"; break;
	    	case "430": courseName = "Information and Communications Technology"; break;
	    	case "454": courseName = "Information Systems (B.Sc.)"; break;
	    	case "463": courseName = "Irish Studies"; break;
	    	case "051": courseName = "Law"; break;
	    	case "478": courseName = "Law and Business"; break;
	    	case "408": courseName = "Law and French"; break;
	    	case "409": courseName = "Law and German"; break;
	    	case "479": courseName = "Law and Political Science"; break;
	    	case "018": courseName = "Management Science and Information Systems Science"; break;
	    	case "089": courseName = "Mathematics"; break;
	    	case "436": courseName = "Medicinal Chemistry"; break;
	    	case "066": courseName = "Medicine"; break;
	    	case "455": courseName = "Medicine (5-year)"; break;
	    	case "458": courseName = "Midwifery (B.Sc.)"; break;
	    	case "041": courseName = "Music"; break;
	    	case "055": courseName = "Music Education (B.Ed.)"; break;
	    	case "446": courseName = "Nursing Studies"; break;
	    	case "457": courseName = "Nursing: Children's and General"; break;
	    	case "054": courseName = "Occupational Therapy"; break;
	    	case "016": courseName = "Pharmacy"; break;
	    	case "472": courseName = "Philosophy"; break;
	    	case "414": courseName = "Philosophy and Political Science"; break;
	    	case "470": courseName = "Philosophy, Political Science, Economics and Sociology"; break;
	    	case "440": courseName = "Physics and Chemistry of Advanced Materials"; break;
	    	case "017": courseName = "Physiotherapy"; break;
	    	case "480": courseName = "Policital Science &amp; Geography"; break;
	    	case "012": courseName = "Psychology"; break;
	    	case "451": courseName = "Radiation Therapy"; break;
	    	case "462": courseName = "Religions and Theology"; break;
	    	case "015": courseName = "Science"; break;
	    	case "085": courseName = "Social Studies"; break;
	    	case "413": courseName = "Sociology and Social Policy"; break;
	    	case "404": courseName = "Theoretical Physics"; break;
	    	case "082": courseName = "Two Subject Moderatorship"; break;
	    	case "084": courseName = "World Religions and Theology"; break;
	    	default: 	courseName = "Course unknown";
    	}
    	return courseName;
    }    

    
}
