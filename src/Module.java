import java.util.ArrayList;

public class Module {
	
	String ID;
	String name;
	int course;
	int academicYear;
	ArrayList<PaperLink> papers;
	
	public Module(String ID, String name,int course, int academicYear){
		this.ID = ID;
		this.name = name;
		this.course = course;
		this.academicYear = academicYear;
		this.papers = new ArrayList<PaperLink>();
	}
	
	public Module(String ID, String name){
		this.ID = ID;
		this.name = name;
		this.papers = new ArrayList<PaperLink>();
	}

	public void addPaper(int year, String link){
		PaperLink p = new PaperLink(year,this.ID,link);
		this.papers.add(p);
	}

	public void addPaper(PaperLink p){
		this.papers.add(p);
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCourse() {
		return course;
	}

	public void setCourse(int course) {
		this.course = course;
	}

	public int getAcademicYear() {
		return academicYear;
	}

	public void setAcademicYear(int academicYear) {
		this.academicYear = academicYear;
	}

	public ArrayList<PaperLink> getPapers() {
		return papers;
	}

	public void setPapers(ArrayList<PaperLink> papers) {
		this.papers = papers;
	}
	
	

}
