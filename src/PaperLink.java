
public class PaperLink  implements Comparable<PaperLink>{
	
	int year;
	String module;
	String link;

	public PaperLink(int y, String m, String l){
		this.year = y;
		this.module = m;
		this.link = l;
	}

	public PaperLink(PastPaper p){
		this.year = Integer.parseInt(p.getYear());
		this.module = p.getModuleID();
		this.link = p.getLink();
	}
	
	public int getYear(){
		return this.year;
	}
	
	public String getLink(){
		return this.link;
	}
	
	public String getModule(){
		return this.module;
	}
	
	public String toString(){
		return this.year+": "+this.getLink();
	}
	
	/*@Override
	public boolean equals(Object o){
	    if(o == null){ return false; }
	    if(!(o instanceof PaperLink)){ return false; }

	    PaperLink p = (PaperLink) o;
		if(this.year==p.getYear() && this.module.equals(p.getModule()) && this.link.equals(p.getLink())){
			return true;
		}
		else{
			return false;
		}
	}*/

	
	@Override
	public int compareTo(PaperLink p){
		if(this.year==p.getYear() && this.module.equals(p.getModule()) && this.link.equals(p.getLink())){
			return 0;
		}
		else if(this.module.equals(p.getModule()) && this.year==p.getYear()){
			return this.link.compareTo(p.getLink());
		}
		else if(this.module.equals(p.getModule())){
			int result = 0;
			result = this.year<p.getYear()? -1 : 1;
			return result;
		}
		else{
			return this.module.compareTo(p.getModule());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PaperLink other = (PaperLink) obj;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (module == null) {
			if (other.module != null)
				return false;
		} else if (!module.equals(other.module))
			return false;
		if (year != other.year)
			return false;
		return true;
	}
	
}
