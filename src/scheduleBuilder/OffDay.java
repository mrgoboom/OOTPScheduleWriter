package scheduleBuilder;

public class OffDay implements Event {
	private final int length;
	private final Team team;
	private int startDay;

	public OffDay(Team t, int days) {
		this.team=t;
		this.length=days;
		this.startDay=-1;
	}
	
	public int length() {
		return this.length;
	}
	
	public int games() {
		return 0;
	}
	
	public Team homeTeam() {
		return this.team;
	}
	
	public Boolean isInvolved(Team t) {
		return t==this.team;
	}
	
	public Boolean isHome(Team t) {
		return t==this.team;
	}
	
	public String toString() {
		return this.team.toString()+" has "+this.length+" days off.";
	}
	
	public void schedule(int day) {
		this.startDay=day;
	}
	
	public Boolean coversDay(int day) {
		return this.startDay<=day&&this.startDay+this.length>day;
	}
	
	public String csvString(Team t, int day) {
		return "Off";
	}
	
	public String ootpString(int day) {
		return "";
	}
}
