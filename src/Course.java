import java.util.ArrayList;

public class Course implements Comparable<Course>{
	
	private String name;
	private int searchValue;
	
	public ArrayList<ArrayList<String>> getYears() {
		return years;
	}

	public void setYears(ArrayList<ArrayList<String>> years) {
		this.years = years;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(int searchValue) {
		this.searchValue = searchValue;
	}

	ArrayList<ArrayList<String>> years;
	
	public Course(String name, int searchValue, int years){
		this.name = name; 
		this.searchValue = searchValue;
		this.years = new ArrayList<ArrayList<String>>();
		
		//Add an ArrayList of modules for every year of the course
		for(int i=0;i<years;i++){
			ArrayList<String> m = new ArrayList<String>();
			this.years.add(m);
		}
	}
	
	public void addPaper(PastPaper p){
		int academicYear = Integer.parseInt(p.getAcademicYear());
		
		this.years.get(academicYear-1).add(p.getModuleID());
	}

	@Override
	public int compareTo(Course o) {
		return name.compareTo(o.name);
	}

}
