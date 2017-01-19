import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

//For CSV Writing
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//For JSON writing
import org.json.JSONObject;



//import com.fasterxml.jackson.core.JsonGenerationException;
//import com.fasterxml.jackson.core.JsonMappingException;
//import com.fasterxml.jackson.core.ObjectMapper;

public class DownloadingAnnualExams {
	
	private static final String FILE_HEADER = "moduleID,year,moduleName,link,academicYear,searchValue";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final int MAX_SEARCH_VAL_OLD = 807;			//807 is the max searchValue value at http://www.tcd.ie/Local/Exam_Papers/summer_nonTSM.html
	private static final int MAX_YEAR_OF_COURSE = 6;			//6 is the max yearOfCourse value at http://www.tcd.ie/Local/Exam_Papers/summer_nonTSM.html
	
    public static void main(String[] args) throws IOException {
        
        //Download the 1998-2012 papers from the archive.
        //Sort the past papers.
        //Download the 2013-2016 papers and set searchValues from old versions of the same module from 1998-2012
        
        List<PastPaper> pastPapers = new ArrayList<PastPaper>();
        
        pastPapers = addOldAnnualPapers(pastPapers);
        pastPapers = addNewAnnualPapers(pastPapers);
        
        writePapersToCSV(pastPapers,"/Users/GeoffreyNatin/Documents/GithubRepositories/examinating/past-papers/AnnualPapers.csv");
        writePapersToJSON(pastPapers,"/Users/GeoffreyNatin/Documents/GithubRepositories/examinating/past-papers/AnnualPapers.json");
    }
    
    //Adds all the past papers up and including 2012 to an ArrayList
    private static List<PastPaper> addOldAnnualPapers(List<PastPaper> pastPapers) throws IOException{

        //Add all exam papers up to 2012
		for(int searchValue=0;searchValue<MAX_SEARCH_VAL_OLD;searchValue++){	
			if(!getCourseNameAnnual(searchValue).equals("Course unknown")){
	    		for(int yearOfCourse=1;yearOfCourse<=MAX_YEAR_OF_COURSE;yearOfCourse++){
	    			for(int year=2012;year>1998;year--){
	    				
	    				//Get web page
	        	        String url = "http://www.tcd.ie/Local/Exam_Papers/annual_search.cgi?Course="+searchValue+"&Standing="+yearOfCourse+"&acyear="+year+"&annual_search.cgi=Search";
	        	        Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
	        	        	     .get();
	        	        
	        	        //If page contains "No papers found" then ignore
	        	        Elements entireBody = doc.select("body");
        	        	boolean noPapersFound = false;
	        			for (Element elem : entireBody) {
	        				if(elem.text().contains("No papers found")){			
	        					noPapersFound = true;
	        		        }
	        		    }
	        			if(noPapersFound){
	        				break;
	        			}
				        Elements ps = doc.select("p");
				        
				        //The first module on each page needs to be extracted differently.
				        String firstModuleID = doc.select("a").get(0).text();
		        		if(firstModuleID.startsWith("X")){
		        			firstModuleID = firstModuleID.substring(1, firstModuleID.length());		//Get rid of leading 'X'
		        		}
		        		if(firstModuleID.length()>6){
		        			firstModuleID = firstModuleID.substring(0, firstModuleID.length()-1);	//Get rid of trailing Paper Number
		        		}
				        String firstModuleYear = ""+year;
				        String firstModuleName = entireBody.get(0).ownText();
		        		firstModuleName.replace('\"',' ');
				        String firstModuleLink = "http://www.tcd.ie"+doc.select("a").attr("href");
				        String firstModuleAcademicYear = ""+ yearOfCourse;
				        String firstModuleSearchValue = ""+searchValue;	
		    			if(!firstModuleID.equals("\"\"")){
					        PastPaper f = new PastPaper(firstModuleID,firstModuleYear,firstModuleName,firstModuleLink,firstModuleAcademicYear,firstModuleSearchValue);
			        		pastPapers.add(f);
			        	}
				        
		    			//Add each past paper on the web page to the ArrayList.
				        for (Element p : ps) {			  
				        	
				        	//Ignore the <p> explaining number of results.
				        	if(p.children().size()!=0 && p.text().startsWith("Your")){
				        		
				        	}
				        	else if(p.children().size()!=0 && !p.child(0).html().startsWith("/Local") && !p.child(0).attr("href").startsWith("http://")){
				    			
				        		
				        		
				        		String moduleID = p.child(0).html();
				        		if(moduleID.startsWith("X")){
				        			moduleID = moduleID.substring(1, moduleID.length());	//Get rid of leading 'X'
				        		}
				        		if(moduleID.length()>6){
				        			moduleID = moduleID.substring(0, moduleID.length()-1);	//Get rid of trailing Paper Number
				        		}							
				        		String yearString = ""+year;						//Escape all commas
				        		String searchValueString = ""+searchValue;
				        		String moduleName = p.text();
				        		String academicYear = ""+yearOfCourse;
				        		
				        		
				        		moduleName = moduleName.substring(moduleName.indexOf(' ')+1,moduleName.length());	//Get rid of moduleID from moduleName
				        		if(moduleName.contains("\"")){														//Escape all commas and quotes
				        			moduleName = moduleName.replace('\"',' ');
				        		}
				        		String link = "http://www.tcd.ie"+p.child(0).attr("href");
				        		PastPaper n = new PastPaper(moduleID,yearString,moduleName,link,academicYear,searchValueString);
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
    	
    	//Make an ArrayList for the past papers after 2012
        List<PastPaper> newPastPapers = new ArrayList<PastPaper>();
        List<PastPaper> newCourses = new ArrayList<PastPaper>();
        
        //Add all exam papers from after 2012
        for(int year=2013;year<2017;year++){
	        String url = "https://www.tcd.ie/academicregistry/exams/past-papers/annual-"+(year-1)%100+""+(year)%100+"/";
	        //Document doc = Jsoup.connect(url).get();
        	
	        File in = new File("/Users/GeoffreyNatin/Desktop/Exam_sites/annual-"+(year-1)%100+""+(year)%100+"/");
	        Document doc = Jsoup.parse(in,null);
	        
	        Elements rows = doc.select("tr");
	        
	        String lastModuleID = "";			//Used for a special case of module where it has two past papers and needs to be extracted from the html differently.
	
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
	        		
	        		//Find all courses that this module was a part of
	        		int index = Collections.binarySearch(pastPapers,p);
        			List<String> coursesWithModule = new ArrayList<String>();
	        		if(index>=0){
	        			while(index>0 && pastPapers.get(index-1).getModuleID().equals(row.child(0).html())){
	        				index--;
	        			}
	        			while(index<pastPapers.size() && pastPapers.get(index).getModuleID().equals(row.child(0).html())){
	        				if(!coursesWithModule.contains(pastPapers.get(index).getSearchValue())){
	        					coursesWithModule.add(pastPapers.get(index).getSearchValue());
	        				}
	        				index++;
	        			}
	        		}
	        		//Find what is the best course to put this module into
	        		else{
	        			
	        			//Change index to positive from result of Binary Search and subtract 1 if appropriate
	        			index = Math.abs(index+1);
	        			int a = index;
	        			int b = index-1;
	        			if(index!=0){ 
	        				index = (Math.abs(p.compareTo(pastPapers.get(b))) < Math.abs(p.compareTo(pastPapers.get(a))))? b : a;
	        			}
	        			//	COURSE INFERENCE
	        			//If a past paper with the same course is found; give it the same searchValue. 
	        			if(Math.abs(p.compareTo(pastPapers.get(index))) < 6){
		        			coursesWithModule.add(pastPapers.get(index).getSearchValue());
		        		}
	        			//If no past paper is found to be in the same course; then make searchValue unknown.
		        		else{
		        			if(index!=0){
		        				//System.out.println(code.html()+" closer to:("+pastPapers.get(index).getModuleID()+")"+" didn't match:"+pastPapers.get(b).getModuleID()+" ("+Math.abs(p.compareTo(pastPapers.get(b)))+") or:"+pastPapers.get(a).getModuleID()+" ("+Math.abs(p.compareTo(pastPapers.get(a)))+")");
		        			}
		        			else{
		        				//System.out.println(code.html()+" didn't match:"+pastPapers.get(index).getModuleID()+" ("+Math.abs(p.compareTo(pastPapers.get(a)))+") . And none came before it.");
			        		}
		        			coursesWithModule.add("unknown");
		        			boolean isNewCourse = true;
		        			for(int i=0;i<newCourses.size();i++){
		        				if(p.compareTo(newCourses.get(i))<6){
		        					isNewCourse = false;
		        				}
		        			}
		        			if(isNewCourse){
			        			newCourses.add(p);
			        			System.out.println(p.getModuleID());
		        			}
		        		}
	        		}
	        		
	        		//Add a past paper for each course that the module is in.
	        		for(int t=0;t<coursesWithModule.size();t++){
		    			p = new PastPaper(code.html(),""+year,name.html(),url+row.child(2).child(0).attr("href"),""+code.html().charAt(2),coursesWithModule.get(t));
		        		newPastPapers.add(p);
	        		}
	        		
	        		//Set the lastModuleID for the special case of <tr><tr> combo of a single module with two past papers.
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
        			List<String> coursesWithModule = new ArrayList<String>();
	        		if(index>=0){
	        			while(index>0 && pastPapers.get(index-1).getModuleID().equals(lastModuleID)){
	        				index--;
	        			}
	        			while(index<pastPapers.size() && pastPapers.get(index).getModuleID().equals(lastModuleID)){
	        				if(!coursesWithModule.contains(pastPapers.get(index).getSearchValue())){
	        					coursesWithModule.add(pastPapers.get(index).getSearchValue());
	        				}
	        				index++;
	        			}
	        		}
	        		else{
	        			
	        			//Change index to positive from result of Binary Search and subtract 1 if appropriate *****Untested in this position (Works if only above)******
	        			index = (pastPapers.get(Math.abs(index)).compareTo(p) < pastPapers.get(Math.abs(index)-1).compareTo(p))? Math.abs(index) : Math.abs(index) -1;
	        			
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
		        		p = new PastPaper(lastModuleID,""+year,name.html(),row.child(1).child(0).attr("href"),""+lastModuleID.charAt(2),coursesWithModule.get(t));
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
    
    //Writes an ArrayList of past papers to a CSV file
    private static void writePapersToCSV(List<PastPaper> pastPapers,String csv){
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
    
    //Writes an ArrayList of past papers to a JSON file
    private static void writePapersToJSON(List<PastPaper> pastPapers,String json){
    	
     
    		// try-with-resources statement based on post comment below :)
    		try (FileWriter file = new FileWriter(json)) {
    			file.write("[");
    			for(int i=0;i<pastPapers.size();i++){
    	    		PastPaper p = pastPapers.get(i);
    	    		JSONObject obj = new JSONObject();
    	    		obj.put("moduleID", p.getModuleID());
    	    		obj.put("yearOfCourse", p.getYearOfCourse());
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
}
