
public class PastPaper implements Comparable<PastPaper> {
	
	private String moduleID, year, moduleName, link, academicYear, searchValue;
	
	public PastPaper(String moduleID, String year, String moduleName, String link,String academicYear,String searchValue){
		this.setModuleID(moduleID);
		this.setYear(year);
		this.setAcademicYear(academicYear);
		this.setModuleName(moduleName);
		this.setLink(link);
		this.setSearchValue(searchValue);
	}
	
	public String toString(){
		return "ModuleID: "+moduleID+" Year:"+year+" ModuleName:"+moduleName+" Link:"+link+" AcademicYear:"+academicYear+" SearchValue:"+searchValue;
	}
	
	public String CSVFormat(){
		return ""+moduleID+","+year+","+moduleName+","+link+","+academicYear+","+searchValue+"\n";
	}

	public String getModuleID() {
		return moduleID;
	}

	public void setModuleID(String moduleID) {
		this.moduleID = moduleID;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}
	
	public String getAcademicYear() {
		return academicYear;
	}

	public void setAcademicYear(String academicYear) {
		this.academicYear = academicYear;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}
	
	
	@Override
	public int compareTo(PastPaper p) {
		/*Three cases:
		 * 	0.moduleID, searchValue, yearStanding & academicYear are equal.				They are the same.									result = 0
		 *  1.moduleID, searchValue & yearStanding are equal, academicYear differs.		Different exam paper of module.						result = 1/-1
		 *  2.moduleID & searchValue are equal, yearStanding differs.					Different year of the course.						result = 2/-2
		 *  3.moduleID is equal & searchValue differs.									Same module different course.						result = 3/-3
		 * 	4.Modules codes differ in the 3rd character.								Different year of the same module.					result = 4/-4		
		 * 	5.Modules codes have the same first four characters.						They are in the same course.						result = 5/-5
		 * 	6.Modules codes have the same first 2 characters.							They are from the same school.						result = 6/-6
		 * 	7.Modules don't even have the same course code.								They are quite different.							result = 7/-7
		 */
		String m1 = moduleID;
		String m2 = ((PastPaper) p).getModuleID();
		String s1 = searchValue;
		String s2 = ((PastPaper) p).getSearchValue();
		String y1 = year;
		String y2 = ((PastPaper) p).getYear();
		String a1 = academicYear;
		String a2 = ((PastPaper) p).getAcademicYear();
		
		// binary search doesn't find the right thing in the java when comparing.
		
		//Negative if first is less.
		int resultM = m1.substring(0,m1.length()).compareTo(m2.substring(0, m2.length()));
		
		if(m1.length()<3 || m2.length()<3){ return -8; }
		
		if(resultM==0){
			int resultS = s1.substring(0,s1.length()).compareTo(s2.substring(0, s2.length()));
			
			
			if(resultS==0){
				int resultY = y1.substring(0,y1.length()).compareTo(y2.substring(0, y2.length()));
				
				
				if(resultY==0){
					int resultA = a1.substring(0,a1.length()).compareTo(a2.substring(0, a2.length()));
					
					//Case 0
					if(resultA==0){
						return 0;
					}
					//Case 1
					else{
						return resultA<0? -1 : 1 ;
					}
					
				}
				//Case 2
				else{
					return resultY<0? -2 : 2 ;
				}
				
			}
			//Case 3
			else{
				return resultS<0? -3 : 3 ;
			}
		}
		//Case 4
		else if((m1.substring(0,2)+m1.substring(3,m1.length())).equals(m2.substring(0, 2)+m2.substring(3,m2.length()))){
			return resultM<0? -4 : 4 ;
		}
		//Case 5
		else if(m1.substring(0,4).equals(m2.substring(0, 4))){
			return resultM<0? -5 : 5 ;
		}
		//Case 6
		else if(m1.substring(0,2).equals(m2.substring(0, 2))){
			return resultM<0? -6 : 6 ;
		}
		//Case 7
		else{
			return resultM<0? -7 : 7 ;
		}
	}
}
