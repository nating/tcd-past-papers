import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.io.FileNotFoundException;
import java.io.FileReader;

//For CSV Writing
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//For JSON writing
import org.json.*;

//import com.fasterxml.jackson.core.JsonGenerationException;
//import com.fasterxml.jackson.core.JsonMappingException;
//import com.fasterxml.jackson.core.ObjectMapper;

public class TCDPastPapers {
	
	private static final String FILE_HEADER = "moduleID,year,moduleName,link,academicYear,searchValue";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final int MAX_SEARCH_VAL_ANNUAL = 807;			//807 is the max searchValue value at http://www.tcd.ie/Local/Exam_Papers/summer_nonTSM.html
	private static final int MAX_YEAR_OF_COURSE = 6;			//6 is the max yearOfCourse value at http://www.tcd.ie/Local/Exam_Papers/summer_nonTSM.html
	
	private static final String SCHOLS_YEAR_OF_COURSE = "2";
	private static final int MAX_SEARCH_VALUE_SCHOLS = 541;		//541 is the max searchValue value at https://www.tcd.ie/Local/Exam_Papers/schols.html

	
	
	/* 
	 * *Create an array of annual past paper links from before 2013
	 * *Add annual past papers from post - 2013 to the array
	 * *Add the foundation scholarship papers to the array
	 * 
	 * *Use the array of links to create a JSON to be used as the websites database.
	 * 
	 */
    public static void main(String[] args) throws IOException {
        
        //Download the 1998-2012 papers from the archive.
        //Sort the past papers.
        //Download the 2013-2016 papers and set searchValues from old versions of the same module from 1998-2012

        //List<PastPaper> annualPastPapers = new ArrayList<>();
        //List<PastPaper> scholsPastPapers = new ArrayList<>();
        
        //annualPastPapers = addOldAnnualPapers(annualPastPapers);
        //annualPastPapers = addNewAnnualPapers(annualPastPapers);

        //scholsPastPapers = addOldScholsPapers(scholsPastPapers);
        //scholsPastPapers = addNewScholsPapers(scholsPastPapers);


		//writePapersToCSV(annualPastPapers,"/Users/GeoffreyNatin/Documents/Things/AnnualPapers.csv");
		//writePapersToCSV(scholsPastPapers,"/Users/GeoffreyNatin/Documents/Things/ScholsPapers.csv");
		//writePapersToJSON(annualPastPapers,"/Users/GeoffreyNatin/Documents/Things/AnnualPapers.json");
		//writePapersToJSON(scholsPastPapers,"/Users/GeoffreyNatin/Documents/Things/ScholsPapers.json");

		List<PastPaper> ps = extractPapersFromCSV("/Users/GeoffreyNatin/Documents/Things/AnnualPapers.csv");
		createDatabase(ps,"/Users/GeoffreyNatin/Documents/Things/database.json");
    }
    
    //Adds all the past papers up and including 2012 to an ArrayList
    private static List<PastPaper> addOldAnnualPapers(List<PastPaper> pastPapers) throws IOException{

        //Add all exam papers up to 2012
		for(int searchValue=0;searchValue<MAX_SEARCH_VAL_ANNUAL;searchValue++){	
			if(!getCourseNameAnnual(searchValue).equals("Course unknown")){
	    		for(int yearOfCourse=1;yearOfCourse<=MAX_YEAR_OF_COURSE;yearOfCourse++){
	    			for(int year=2012;year>1998;year--){
	    				
	    				//Get web page
	        	        String url = "http://www.tcd.ie/Local/Exam_Papers/annual_search.cgi?Course="+searchValue+"&Standing="+yearOfCourse+"&acyear="+year+"&annual_search.cgi=Search";
	        	        Document d = Jsoup.connect(url).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
	        	        	     .get();
	        	        
	        			if(!containsPastPapers(d)){ break; }
				        
				    	//The first module past paper link on each page is of a different format to the rest
				        addFirstAnnualModule(d,pastPapers,year,yearOfCourse,searchValue);
				        
				        Elements ps = d.select("p");
				        
		    			//Add each past paper on the web page to the ArrayList.
				        for (Element p : ps) {			  
				        	
				        	//Ignore the <p> explaining number of results.
				        	if(p.children().size()!=0 && p.text().startsWith("Your")){
				        	}
				        	else if(p.children().size()!=0 && !p.child(0).html().startsWith("/Local") && !p.child(0).attr("href").startsWith("http://")){
				    			
				        		String moduleID = p.child(0).html();
				        		
				        		//Get rid of leading 'X'
				        		if(moduleID.startsWith("X")){
				        			moduleID = moduleID.substring(1, moduleID.length());	
				        		}
				        		
				        		//Get rid of trailing Paper Number
				        		if(moduleID.length()>6){
				        			moduleID = moduleID.substring(0, moduleID.length()-1);	
				        		}							
				        		
				        		String yearString = ""+year;
				        		String searchValueString = ""+searchValue;
				        		String moduleName = p.text();
				        		String academicYear = ""+yearOfCourse;
				        		
				        		//Get rid of moduleID from moduleName
				        		moduleName = moduleName.substring(moduleName.indexOf(' ')+1,moduleName.length());	
				        		
				        		//Change all quotes to spaces
				        		if(moduleName.contains("\"")){														
				        			moduleName = moduleName.replace('\"',' ');
				        		}
				        		
				        		
				        		String link = ("http://www.tcd.ie"+p.child(0).attr("href")).replace(' ','%');
				        		
				        		//Add the past paper
				        		PastPaper n = new PastPaper(moduleID,yearString,moduleName,link,academicYear,searchValueString);
				        		assert(academicYear.length()<2);
				        		pastPapers.add(n);
				    		}
				        }
	        		}
	    		}
			}
			System.out.println(searchValue);
    	}
		
		//Sort the past papers
        Collections.sort(pastPapers);
        
		return pastPapers;
    }

    
  //Adds all the past papers since 2013 to an ArrayList
    private static List<PastPaper> addNewAnnualPapers(List<PastPaper> pastPapers) throws IOException{
    	
    	//Make a separate list for new papers that will be added after its creation
        List<PastPaper> newPastPapers = new ArrayList<>();
        
        for(int year=2013;year<2017;year++){
        	
	        String url = "https://www.tcd.ie/academicregistry/exams/past-papers/annual-"+(year-1)%100+""+(year)%100+"/";
        	//Commented out to avoid downloading entire webpage every time the program runs
	        //Document doc = Jsoup.connect(url).get();
        	
        	//Code added to use local files for extraction rather than HTTP requesting them on every run
	        File in = new File("/Users/GeoffreyNatin/Desktop/Exam_sites/annual-"+(year-1)%100+""+(year)%100+"/");
	        Document doc = Jsoup.parse(in,null);
	        
	        
	        Elements rows = doc.select("tr");
	        
	        //Used for a special case of module where it has two past papers and needs to be extracted from the html differently.
	        String lastModuleID = "";
	
	        //Add each past paper on web page
	        for (Element row : rows) {
	        	
	        	//Ignore cases where the last child of the <tr> is empty.
	        	if(row.children().size()==3 && row.child(2).children().size()==0){
	        		
	        	}
	        	else if(row.children().size()==2 && row.child(1).children().size()==0){
	        		
	        	}
	        	
	        	//Normal case:
	        	else if(row.children().size()==3 && !row.child(0).html().equals("Module Code")){
	        		
	        		Element code = row.child(0);
	        		Element name = row.child(1);
	        		
	        		//Remove surrounding html
	        		while(code.children().size()>0){
	        			code = code.child(0);
	        		}
	        		while(name.children().size()>0){
	        			name = name.child(0);
	        		}
	        		
	        		//Create past paper
	        		PastPaper p = new PastPaper(code.html(),"","","","","");
	        		
	        		//Get the index of the moduleID in the old past papers
	        		int index = Collections.binarySearch(pastPapers,p);
        			List<String> coursesWithModule = new ArrayList<>();
        			
        			//If the moduleID was present in the old past papers
	        		if(index>=0){
	        			
	        			//Decrease index to the first occurrence of the moduleID
	        			while(index>0 && pastPapers.get(index-1).getModuleID().equals(row.child(0).html())){
	        				index--;
	        			}
	        			
	        			//For every different course the moduleID is part of, add it to the 'coursesWithModule' list
	        			while(index<pastPapers.size() && pastPapers.get(index).getModuleID().equals(row.child(0).html())){
	        				if(!coursesWithModule.contains(pastPapers.get(index).getSearchValue())){
	        					coursesWithModule.add(pastPapers.get(index).getSearchValue());
	        				}
	        				index++;
	        			}
	        			
	        		}
	        		
	        		//If the moduleID was not present in the past papers, find out what the best course to put this module into is
	        		else{
	        			
	        			//Change index to where it would the moduleID would have been inserted into the past papers list
	        			index = Math.abs(index+1);
	        			
	        			//If the moduleID above the index is closer, then change the index to that
	        			int a = index;
	        			int b = index-1;
	        			if(index!=0){
	        				index = (Math.abs(p.compareTo(pastPapers.get(b))) < Math.abs(p.compareTo(pastPapers.get(a))))? b : a;
	        			}
	        			
	        			
	        			//---------COURSE INFERENCE----------//
	        			
	        			//If a past paper with the same course is found; give it the same searchValue. 
	        			if(Math.abs(p.compareTo(pastPapers.get(index))) < 6){
		        			coursesWithModule.add(pastPapers.get(index).getSearchValue());
		        		}
	        			
	        			//If no past paper is found to be in the same course; then make searchValue unknown.
		        		else{
		        			coursesWithModule.add("unknown");
		        		}
	        		}
	        		
	        		//Add a past paper for each course that the module is in.
	        		for(int t=0;t<coursesWithModule.size();t++){
		    			p = new PastPaper(code.html(),""+year,name.html(),(url+row.child(2).child(0).attr("href")).replace(' ','%'),""+code.html().charAt(2),coursesWithModule.get(t));
		        		newPastPapers.add(p);
	        		}
	        		
	        		//Set the lastModuleID for the special case of <tr> <tr> combo of a single module with two past papers.
	    			lastModuleID = code.html();
	    		}
	        	
	        	//Case of second past paper for a single module:
	    		else if(row.children().size()==2 && !row.child(0).html().equals("Module Code")){
	        		Element name = row.child(0);
	        		
	        		//Remove surrounding html
	        		while(name.children().size()>0){
	        			name = name.child(0);
	        		}
	        		
	        		//Create a past paper
	        		PastPaper p = new PastPaper(lastModuleID,"","","","","");
	        		
	        		//Find all the courses that contain the module
	        		int index = Collections.binarySearch(pastPapers,p);
        			List<String> coursesWithModule = new ArrayList<>();
        			
        			//If the module was present in the old past papers
	        		if(index>=0){
	        			
	        			//Decrease the index to the first occurence of the moduleID
	        			while(index>0 && pastPapers.get(index-1).getModuleID().equals(lastModuleID)){
	        				index--;
	        			}
	        			
	        			//Add each unique course that contains the moduleID to 'coursesWithModule'
	        			while(index<pastPapers.size() && pastPapers.get(index).getModuleID().equals(lastModuleID)){
	        				if(!coursesWithModule.contains(pastPapers.get(index).getSearchValue())){
	        					coursesWithModule.add(pastPapers.get(index).getSearchValue());
	        				}
	        				index++;
	        			}
	        		}
	        		
	        		//If the moduleID was not present in the old past papers
	        		else{
	        			
	        			
	        			//Change index to where it would the moduleID would have been inserted into the past papers list
	        			index = Math.abs(index+1);
	        			
	        			//If the moduleID above the index is closer, then change the index to that
	        			int a = index;
	        			int b = index-1;
	        			if(index!=0){
	        				index = (Math.abs(p.compareTo(pastPapers.get(b))) < Math.abs(p.compareTo(pastPapers.get(a))))? b : a;
	        			}
	        			
		        		//If a past paper with the same course is found; give it the same searchValue.
	        			if(p.compareTo(pastPapers.get(Math.abs(index))) < 6){
		        			coursesWithModule.add(pastPapers.get(index).getSearchValue());
		        		}
	        			
		        		//If no past paper with the same course is found, then set the searchValue to unknown.
		        		else{
	    					coursesWithModule.add("unknown");
		        		}
	        		}
	        		
	        		//Add a past paper for each course the module belongs to.
	        		for(int t=0;t<coursesWithModule.size();t++){
		        		p = new PastPaper(lastModuleID,""+year,name.html(),(url+row.child(1).child(0).attr("href")).replace(' ','%'),""+lastModuleID.charAt(2),coursesWithModule.get(t));
		            	newPastPapers.add(p);
	        		}
	    		}
	        }
        }
        
        //Put all the new past papers in with the old past papers.
        pastPapers.addAll(newPastPapers);
        
        //Re-sort the past papers.
        Collections.sort(pastPapers);
        
        return pastPapers;
    }
  
    
    //Returns false if the text of any element in the document contains the string "No papers found"
    private static boolean containsPastPapers(Document d){
        Elements entireBody = d.select("body");
		for (Element elem : entireBody) {
			if(elem.text().contains("No papers found")){			
				return false;
	        }
	    }
		return true;
    }
    

    //Returns the passed List with the first past papers of the Document added to it.
    private static List<PastPaper> addFirstAnnualModule(Document d, List<PastPaper> p, int year, int yearOfCourse, int searchValue){
    	
        Elements entireBody = d.select("body");
        String mID = d.select("a").get(0).text();
        
        
		if(mID.startsWith("X")){
			mID = mID.substring(1, mID.length());		//Get rid of leading 'X'
		}
		if(mID.length()>6){
			mID = mID.substring(0, mID.length()-1);	//Get rid of trailing Paper Number
		}
        String yearString = ""+year;
        String mName = entireBody.get(0).ownText().replace('\"',' ');
        String mLink = ("http://www.tcd.ie"+d.select("a").attr("href")).replace(' ','%');
        String yearOfCourseString = ""+ yearOfCourse;
        String svString = ""+searchValue;	
		if(!mID.equals("\"\"")){
	        PastPaper f = new PastPaper(mID,yearString,mName,mLink,yearOfCourseString,svString);
	        assert(yearOfCourseString.length()<2);
    		p.add(f);
    	}
		return p;
    }
    
    
    //Writes an ArrayList of past papers to a CSV file
    private static void writePapersToCSV(List<PastPaper> pastPapers,String csv){
    	 FileWriter fileWriter = null;
         try{
         	fileWriter = new FileWriter(csv);
         	fileWriter.append(FILE_HEADER);
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
    
    //Extracts papers from a csv into an ArrayList of past papers
    private static List<PastPaper> extractPapersFromCSV(String csv){
    	ArrayList<PastPaper> ps = new ArrayList<PastPaper>();
    	
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
    	
    	try {

            br = new BufferedReader(new FileReader(csv));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] paper = line.split(cvsSplitBy);
                
                
            	String code = paper[0];
            	String moduleID = paper[1];
            	String moduleName = paper[2];
            	for(int i=3;i<paper.length-3;i++){
            		moduleName+= ","+paper[i];
            	}
            	String academicYear = paper[paper.length-2];
            	if(!academicYear.matches("\\d") || academicYear.equals("7")){
            		academicYear = "1";
            	}
                PastPaper p = new PastPaper(code,moduleID,moduleName,paper[paper.length-3],academicYear,paper[paper.length-1]);
                ps.add(p);

            }

        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    	
    	return ps;
    }
    
    
    //Writes an ArrayList of past papers to a JSON file
    private static void writePapersToJSON(List<PastPaper> pastPapers,String json){
    	
     
    		// try-with-resources statement based on post comment below :)
    		try (FileWriter file = new FileWriter(json)) {
    			file.write("[");
    			for(int i=0;i<pastPapers.size();i++){
    	    		PastPaper p = pastPapers.get(i);
    	    		JSONObject obj = new JSONObject();
    	    		obj.put("moduleID", p.getModuleID());
    	    		obj.put("yearOfCourse", p.getYear());
    	    		obj.put("moduleName", p.getModuleName());
    	    		obj.put("link", p.getLink());
    	    		obj.put("academicYear", p.getAcademicYear());
    	    		obj.put("searchValue", p.getSearchValue());
    	    		
    	    		if(i!=pastPapers.size()-1){
    	    			file.write(obj.toString()+",");
    	    		}
    	    		else{
    	    			file.write(obj.toString());
    	    		}
    			}
    			file.write("]");
    		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
    }

    
    //Returns the course name that corresponds to the search value argument for annual exams
    private static String getCourseNameAnnual(int sv){
    	String courseName;
    	switch(sv){
	    	case 482: courseName = "Ancient and Medieval History and Culture"; break;
	    	case 23: courseName = "Biblical and Theological Studies"; break;
	    	case 481: courseName = "Business and Computing"; break;
	    	case 80: courseName = "Business Studies"; break;
	    	case 405: courseName = "Business Studies and a Language"; break;
	    	case 456: courseName = "Chemistry with Molecular Modelling"; break;
	    	case 457: courseName = "Children's and General Nursing (BSc)"; break;
	    	case 5: courseName = "Classics&nbsp;"; break;
	    	case 37: courseName = "Clinical Speech and Language Studies"; break;
	    	case 19: courseName = "Computer Science (B.A.)&nbsp;"; break;
	    	case 412: courseName = "Computer Science (B.Sc.)"; break;
	    	case 299: courseName = "Computer Science (Integrated)"; break;
	    	case 57: courseName = "Computer Science, Linguistics and a Language"; break;
	    	case 503: courseName = "Deaf Studies (Bachelor)"; break;
	    	case 447: courseName = "Deaf Studies (Diploma)"; break;
	    	case 406: courseName = "Dental Hygiene (Diploma)"; break;
	    	case 461: courseName = "Dental Nursing (Diploma)"; break;
	    	case 67: courseName = "Dental Science"; break;
	    	case 459: courseName = "Dental Technology (Bachelor)"; break;
	    	case 27: courseName = "Drama and Theatre Studies"; break;
	    	case 31: courseName = "Early and Modern Irish"; break;
	    	case 477: courseName = "Earth Sciences"; break;
	    	case 46: courseName = "Economic and Social Studies&nbsp;"; break;
	    	case 98: courseName = "Education (B.Ed.)"; break;
	    	case 48: courseName = "English Studies"; break;
	    	case 70: courseName = "Engineering"; break;
	    	case 280: courseName = "Engineering (Integrated)"; break;
	    	case 450: courseName = "Engineering Double Diplome"; break;
	    	case 464: courseName = "Engineering with Management"; break;
	    	case 287: courseName = "Engineering with Management (Integrated)"; break;
	    	case 53: courseName = "European Studies"; break;
	    	case 445: courseName = "European Studies Double Diploma"; break;
	    	case 432: courseName = "Foundation Course for Higher Education - Mature Students"; break;
	    	case 437: courseName = "Foundation Course for Higher Education - Young Adults"; break;
	    	case 49: courseName = "Germanic Languages"; break;
	    	case 40: courseName = "History"; break;
	    	case 97: courseName = "History of European Painting (Diploma)"; break;
	    	case 26: courseName = "History and Political Science"; break;
	    	case 420: courseName = "Human Genetics"; break;
	    	case 476: courseName = "Human Health and Disease"; break;
	    	case 453: courseName = "Information Systems (Diploma) "; break;
	    	case 454: courseName = "Information Systems (BSc Hons) "; break;
	    	case 463: courseName = "Irish Studies"; break;
	    	case 51: courseName = "Law"; break;
	    	case 408: courseName = "Law and French"; break;
	    	case 409: courseName = "Law and German"; break;
	    	case 478: courseName = "Law and Business"; break;
	    	case 479: courseName = "Law and Political Science"; break;
	    	case 18: courseName = "Management Science and Information Systems Studies (MSISS)"; break;
	    	case 439: courseName = "Manufacturing Engineering with Management Science (MEMS)"; break;
	    	case 89: courseName = "Mathematics"; break;
	    	case 66: courseName = "Medicine"; break;
	    	case 455: courseName = "Medicine (5-year)"; break;
	    	case 436: courseName = "Medicinal Chemistry"; break;
	    	case 6: courseName = "Mental and Moral Science"; break;
	    	case 458: courseName = "Midwifery (BSc)"; break;
	    	case 448: courseName = "Midwifery Studies (Bachelor)"; break;
	    	case 41: courseName = "Music"; break;
	    	case 55: courseName = "Music Education (B.Ed.)"; break;
	    	case 15: courseName = "Natural Sciences"; break;
	    	case 446: courseName = "Nursing Studies (BSc)"; break;
	    	case 444: courseName = "Nursing Studies (October intake)"; break;
	    	case 54: courseName = "Occupational Therapy"; break;
	    	case 59: courseName = "Pharmaceutical Technicians (Diploma)"; break;
	    	case 16: courseName = "Pharmacy"; break;
	    	case 472: courseName = "Philosophy"; break;
	    	case 414: courseName = "Philosophy and Political Science"; break;
	    	case 470: courseName = "Philosophy, Political Science, Economics and Sociology"; break;
	    	case 440: courseName = "Physics and Chemistry of Advanced Materials"; break;
	    	case 17: courseName = "Physiotherapy"; break;
	    	case 480: courseName = "Political Science and Geography"; break;
	    	case 12: courseName = "Psychology"; break;
	    	case 451: courseName = "Radiation Therapy"; break;
	    	case 462: courseName = "Religions and Theology"; break;
	    	case 85: courseName = "Social Studies&nbsp;"; break;
	    	case 413: courseName = "Sociology and Social Policy"; break;
	    	case 52: courseName = "Theology"; break;
	    	case 404: courseName = "Theoretical Physics"; break;
	    	case 84: courseName = "World Religions and Theology (New Course 2010/11)"; break;
	    	case 706: courseName = "Applied Building Repair and Conservation (PG Diploma)"; break;
	    	case 743: courseName = "Applied Psychology (M.Sc)"; break;
	    	case 110: courseName = "Business Administration (Masters)"; break;
	    	case 794: courseName = "Business Administration (PT) (Masters)"; break;
	    	case 694: courseName = "Business and Management (M.Sc)"; break;
	    	case 802: courseName = "Cancer Care (M.Sc)"; break;
	    	case 676: courseName = "Cardiac Rehabilitation (M.Sc)"; break;
	    	case 224: courseName = "Child and Adolescent Analytic Psychotherapy (M.Sc)"; break;
	    	case 755: courseName = "Children's Nursing(RCN) (Higher Diploma)"; break;
	    	case 211: courseName = "Civil Engineering (old course) (M.Sc)"; break;
	    	case 525: courseName = "Civil Engineering (new course) (M.Sc)"; break;
	    	case 778: courseName = "Classics (Master in Philosophy)"; break;
	    	case 629: courseName = "Clinical Practice (PG Diploma)"; break;
	    	case 609: courseName = "Cognitive Psychotherapy (M.Sc)"; break;
	    	case 612: courseName = "Cognitive Psychotherapy (PG Diploma)"; break;
	    	case 569: courseName = "Comparative European Politics (PT) (M.Sc)"; break;
	    	case 619: courseName = "Computer Science (Networks and Distributed Systems)(M.Sc)"; break;
	    	case 739: courseName = "Computer Science (Mobile and Ubiquitous Computing)(M.Sc)"; break;
	    	case 769: courseName = "Computer Science (Interactive Entertainment Technology)(M.Sc)"; break;
	    	case 702: courseName = "Computer Science (Ubiqutous Computing) (Master)"; break;
	    	case 130: courseName = "Computers for Engineers (PG Diploma)"; break;
	    	case 805: courseName = "Computing with Advanced Interdisciplinary Outlook (PG Diploma)"; break;
	    	case 807: courseName = "Computing (Conversion) with Advanced Interdisciplinary Outlook (PG Diploma)"; break;
	    	case 614: courseName = "Construction Law and Contract Administration (PG Diploma)"; break;
	    	case 622: courseName = "Dental Surgery (Master)"; break;
	    	case 628: courseName = "Early Irish (Master in Philosophy)"; break;
	    	case 645: courseName = "Economic Policy (M.Sc)"; break;
	    	case 601: courseName = "Education (Primary Teaching - Higher Diploma)"; break;
	    	case 762: courseName = "Education (PG Diploma)"; break;
	    	case 201: courseName = "Environmental Engineering (PG Diploma)"; break;
	    	case 799: courseName = "Exercise Physiology (MSc)"; break;
	    	case 566: courseName = "Finance (FT) (MSc)"; break;
	    	case 567: courseName = "Finance (PT) (MSc)"; break;
	    	case 637: courseName = "Fire Safety Practice (PG Diploma)"; break;
	    	case 248: courseName = "German Language Literature and Language (Master in Philosophy)"; break;
	    	case 705: courseName = "Health and Safety in Construction (PG Diploma)"; break;
	    	case 616: courseName = "Health Informatics (PG Piploma)"; break;
	    	case 626: courseName = "High Performance Computing (M.Sc)"; break;
	    	case 186: courseName = "Highway and Geotechnical Engineering (PG Diploma)"; break;
	    	case 781: courseName = "Interactive Digital Media (M.Sc)"; break;
	    	case 792: courseName = "International Management (Masters)"; break;
	    	case 607: courseName = "LLM (Master in Laws)"; break;
	    	case 700: courseName = "Management Information Systems (M.Sc)"; break;
	    	case 733: courseName = "Mechanical Engineering (Erasmus Mundus - M.Sc)"; break;
	    	case 438: courseName = "Midwifery (PG Diploma)"; break;
	    	case 605: courseName = "Midwifery (M.Sc)"; break;
	    	case 767: courseName = "Midwifery (Higher Diploma)"; break;
	    	case 263: courseName = "Multimedia Systems (M.Sc)"; break;
	    	case 604: courseName = "Nursing (M.Sc)"; break;
	    	case 421: courseName = "Nursing Studies (PG Diploma)"; break;
	    	case 627: courseName = "Old Irish (PG Diploma)"; break;
	    	case 264: courseName = "Paediatrics (M.Sc)"; break;
	    	case 633: courseName = "Pharmaceutical Analysis (M.Sc) (98/99-06/07)"; break;
	    	case 544: courseName = "Pharmaceutical Analysis (M.Sc) (07/08-10/11)"; break;
	    	case 210: courseName = "Physical Planning for Engineers (PG Diploma)"; break;
	    	case 136: courseName = "Project Management (PG Diploma)"; break;
	    	case 720: courseName = "Psychology (Higher Diploma) "; break;
	    	case 272: courseName = "Quality Improvement (PG Diploma)"; break;
	    	case 669: courseName = "Social Work (Masters)"; break;
	    	case 649: courseName = "Specialist Nursing (PG Diploma)"; break;
	    	case 689: courseName = "Specialist Nursing (M.Sc)"; break;
	    	case 208: courseName = "Sports Medicine (M.Sc )"; break;
	    	case 129: courseName = "Statistics (PG Diploma)"; break;
	    	default: courseName = "Course unknown";
    	}
    	return courseName;
    }
    
    
    //Adds all foundation scholarship papers from before 2013 to an ArrayList
    private static List<PastPaper> addOldScholsPapers(List<PastPaper> pastPapers) throws IOException{

        //Cycle through every searchValue
		for(int searchValue=0;searchValue<MAX_SEARCH_VALUE_SCHOLS;searchValue++){	
			System.out.println(searchValue);
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
        	        if(containsPastPapers(doc)){
        	        	
				        Elements ps = doc.select("p");
				        Elements entireBody = doc.select("body");
				        
				        //The first past paper on each page needs to be extracted differently.
				        String firstModuleID = doc.select("a").get(0).text();
				        String firstModuleYear = ""+ year;
				        String firstModuleName = entireBody.get(0).ownText();
				        
				        //Escape all commas and quotes
		        		if(firstModuleName.contains("\"")){										
		        			firstModuleName = firstModuleName.replace('\"',' ');
		        		}
		        		
				        String firstModuleLink = ("https://www.tcd.ie"+doc.select("a").attr("href")).replace(' ','%');
				        String firstModuleAcademicYear = ""+ SCHOLS_YEAR_OF_COURSE;
				        String firstModuleSearchValue = ""+ svWithZeros;	
				        
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
				        		String yearString = ""+year;
				        		String searchValueString = ""+svWithZeros;
				        		String moduleName = p.text();
				        		
				        		//Get rid of moduleID from moduleName
				        		moduleName = moduleName.substring(moduleName.indexOf(' ')+1,moduleName.length());	
				        		
				        		//Escape all quotes
				        		if(moduleName.contains("\"")){														
				        			moduleName = moduleName.replace('\"',' ');
				        		}
				        		
				        		String link = ("https://www.tcd.ie"+p.child(0).attr("href")).replace(' ','%');
				        		
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

    
    //Adds all foundation scholarship papers since 2013 to an ArrayList
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
	        		if(!code.html().equals("") && !name.html().equals("") && !currentCourse.html().equals("") && !row.child(1).html().equals("&nbsp;") && code.hasAttr("href")){
		        		
	        			
	        			String currentCourseString = currentCourse.html();
		        		if(currentCourseString.contains(" ")){
		        			currentCourseString = currentCourseString.substring(0,currentCourse.html().lastIndexOf(' ')+1);
		        		}
		        		
		        		//Try to match the course of the past paper to a course from the past papers
		        		boolean matched = false;
		        		for(int t=0;t<pastPapers.size();t++){
		        			if(pastPapers.get(t).getModuleID().equals(code.html()) || getCourseNameSchols(pastPapers.get(t).getSearchValue().substring(1,pastPapers.get(t).getSearchValue().length()-1)).equals(currentCourseString.substring(1,currentCourseString.length()-1))){
		        				currentCourseString = pastPapers.get(t).getSearchValue();
		        				matched = true;
		        			}
		        		}
		        		
		        		//Print out if a past paper was not added to a course
		        		if(!matched){
		        			System.out.println("Couldn't match "+currentCourseString);
		        		}
		        		
		        		//Add the past paper to the list.
		        		PastPaper n = new PastPaper(code.html(),""+year,name.html(),(url+code.attr("href")).replace(' ','%'),SCHOLS_YEAR_OF_COURSE,currentCourseString);
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


	//Creates a list of all the valid searchValues
    private static ArrayList<Integer> createCourseSearchValuesArrayList(){
		ArrayList<Integer> courseSearchValues = new ArrayList<>();
		for(int i=0;i<MAX_SEARCH_VAL_ANNUAL;i++){
			if(!getCourseNameAnnual(i).equals("Course unknown")){
				courseSearchValues.add(i);
			}
		}
		return courseSearchValues;
	}


	//Creates a list of unique modules from the PastPaper List
	private static ArrayList<Module> getUniqueModules(List<PastPaper> papers){
		ArrayList<Module> modules = new ArrayList<>();
		for(int i=0;i<papers.size();i++){
			boolean alreadyPresent = false;
			String potentialNewModuleCode = papers.get(i).getModuleID();
			for(int j=0;j<modules.size();j++){
				if(modules.get(j).getID().equals(potentialNewModuleCode)){
					alreadyPresent = true;
					break;
				}
			}
			if(!alreadyPresent){
				Module m = new Module(papers.get(i).getModuleID(),papers.get(i).getModuleName());
				modules.add(m);
			}
		}
		return modules;
	}


	//Creates an ArrayList of courses
	private static ArrayList<Course> createCourses(ArrayList<Integer> courseSearchValues, int[] yearsOfEachCourse, List<PastPaper> papers){
		ArrayList<Course> courses = new ArrayList<>();
		ArrayList<String> alreadyAddedModules = new ArrayList<>();
		for(int i=0;i<courseSearchValues.size();i++){
			if(yearsOfEachCourse[i]!=0){
				Course c = new Course(getCourseNameAnnual(courseSearchValues.get(i)),courseSearchValues.get(i),yearsOfEachCourse[i]);
				for(int j=0;j<papers.size();j++){
					if(papers.get(j).getSearchValue().equals(""+courseSearchValues.get(i))){
						boolean alreadyPresent = false;
						String potentialNewModule = papers.get(j).getModuleID();
						for(int k=0;k<alreadyAddedModules.size();k++){
							if(alreadyAddedModules.get(k).equals(potentialNewModule)){
								alreadyPresent = true;
								break;
							}
						}
						if(!alreadyPresent){
							alreadyAddedModules.add(potentialNewModule);
							c.addPaper(papers.get(j));
						}
					}
				}
				alreadyAddedModules.clear();
				courses.add(c);
			}
		}
		Collections.sort(courses);
		return courses;
	}


	//Make a PaperLink for every PastPaper
	private static List<PaperLink> createPaperLinks(List<PastPaper> papers){
		List<PaperLink> paperLinks = new ArrayList<>();
		for (int i = 1; i < papers.size(); i++) {
			PaperLink l = new PaperLink(papers.get(i));
			paperLinks.add(l);
		}

		//Remove duplicates
		ArrayList<PaperLink> p2 = new ArrayList<>();
		for (int i = 0; i < paperLinks.size(); i++) {
			boolean duplicate = false;
			for (int j = i + 1; j < paperLinks.size(); j++) {
				if (paperLinks.get(i).equals(paperLinks.get(j))) {
					duplicate = true;
				}
			}
			if (!duplicate) {
				p2.add(paperLinks.get(i));
			}
		}
		return p2;
	}


	//Add the paperLinks to their modules
	private static ArrayList<Module> addPaperLinksToModules(ArrayList<Module> modules,List<PaperLink> paperLinks) {
		for (int i = 0; i < paperLinks.size(); i++) {
			for (int j = 0; j < modules.size(); j++) {
				if (paperLinks.get(i).getModule().equals(modules.get(j).getID())) {
					modules.get(j).addPaper(paperLinks.get(i));
					break;
				}
			}
		}
		//Sort the paper links in the modules
		for (int j = 0; j < modules.size(); j++) {
			Collections.sort(modules.get(j).papers);
		}
		return modules;
	}


	//Make an array corresponding to the courses where each index contains the number of years of the course
	private static int[] getYearsOfEachCourse(ArrayList<Integer> courseSearchValues, List<PastPaper> papers){
		int[] yearsOfEachCourse = new int[courseSearchValues.size()];
		for(int i=0;i<courseSearchValues.size();i++){
			int maxYear = 0;
			for(int j=0;j<papers.size();j++){
				if(papers.get(j).getSearchValue().equals(""+courseSearchValues.get(i)) && Integer.parseInt(papers.get(j).getAcademicYear())>maxYear){
					maxYear = Integer.parseInt(papers.get(j).getAcademicYear());
				}
			}
			yearsOfEachCourse[i] = maxYear;
		}
		return yearsOfEachCourse;
	}


	//Prints a list of all courses in a JSON flikeormat
	private static void printCoursesList(ArrayList<Course> courses){
    	System.out.println("Courses{");
    	for(int i=0;i<courses.size();i++){
    		System.out.println("\t"+courses.get(i).getName());
    	}
    	System.out.println("}");
	}


	//Prints all the courses in the list in a JSON like format
	private static void printAllCourses(ArrayList<Course> courses){
		for(int i=0;i<courses.size();i++){
			System.out.println(courses.get(i).getName()+"{");
			for(int j=0;j<courses.get(i).years.size();j++){
				System.out.println("\tyear"+(j+1)+"{");
				for(int k=0;k<courses.get(i).years.get(j).size();k++){
					System.out.println("\t\t"+courses.get(i).years.get(j).get(k));
				}
				System.out.println("\t}");
			}
			System.out.println("}");
		}
	}


	//Prints all the modules in the list in a JSON like format
	private static void printAllModules(ArrayList<Module> modules){
		System.out.println("Modules{");
		for(int i=0;i<modules.size();i++){
			System.out.println("\t"+modules.get(i).getID()+"{");
			for(int j=0;j<modules.get(i).papers.size();j++){
				System.out.println("\t\t"+modules.get(i).papers.get(j).getYear()+": "+modules.get(i).papers.get(j).getLink());
			}
			System.out.println("\t}");
		}
		System.out.println("}");
	}


	//Writes the courses and modules into the JSON in the format necessary for the tcdpastpapers NoSQL database
	private static void writeDBToJSON(ArrayList<Course> courses, ArrayList<Module> modules,String json){
		JSONObject db = new JSONObject();

		//Create list of course names
		JSONArray listOfCourseNames = new JSONArray();
		for(int i=0;i<courses.size();i++){
			listOfCourseNames.put(courses.get(i).getName());
		}

		//Create list of modules
		JSONObject listOfModules = new JSONObject();
		for(int i=0;i<modules.size();i++){
			Module module = modules.get(i);
			JSONObject m = new JSONObject();
			JSONArray papers = new JSONArray();
			for(int j=0;j<module.getPapers().size();j++){
				PaperLink paperLink = modules.get(i).getPapers().get(j);
				JSONObject paper = new JSONObject();
				paper.put("year",paperLink.getYear());
				paper.put("link",paperLink.getLink());
				papers.put(paper);
			}
			m.put("name",module.getName());
			m.put("papers",papers);
			if(!module.getID().equals("")) {
				listOfModules.put(module.getID(), m);
			}
		}

		//Create list of courses
		JSONObject listOfCourses = new JSONObject();
		for(int i=0;i<courses.size();i++){
			Course course = courses.get(i);
			JSONObject years = new JSONObject();
			for(int j=0;j<course.getYears().size();j++){
				JSONObject year = new JSONObject();
				JSONArray ms = new JSONArray();
				for(int k=0;k<course.getYears().get(j).size();k++){
					String m = course.getYears().get(j).get(k);
					ms.put(m);
				}
				years.put("year"+(j+1),ms);
			}
			listOfCourses.put(listOfCourseNames.get(i).toString(),years);
		}

		db.put("CourseNames",listOfCourseNames);
		db.put("Modules",listOfModules);
		db.put("Courses",listOfCourses);
		// try-with-resources statement based on post comment below :)
		try (FileWriter file = new FileWriter(json)) {
			file.write("[");

			/*
			file.write(listOfCourseNames.toString());
			for(int i=0;i<listOfModules.length();i++){
				file.write(listOfModules.get(i).toString());
			}
			for(int i=0;i<listOfCourses.length();i++){
				file.write(listOfCourses.get(i).toString());
			}
			*/
			file.write(db.toString());


			file.write("]");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


    //Writes up the data into a json in the format for a NoSQL database.
    private static void createDatabase(List<PastPaper> papers, String url){

		ArrayList<Integer> courseSearchValues = createCourseSearchValuesArrayList();

    	int[] yearsOfEachCourse = getYearsOfEachCourse(courseSearchValues,papers);

    	ArrayList<Course> courses = createCourses(courseSearchValues,yearsOfEachCourse,papers);

		ArrayList<Module> modules = getUniqueModules(papers);

    	List<PaperLink> paperLinks = createPaperLinks(papers);

    	modules = addPaperLinksToModules(modules,paperLinks);

		//printCoursesList(courses);
		//printAllCourses(courses);
		//printAllModules(modules);

		writeDBToJSON(courses,modules,url);

    }
}