package scheduleBuilder;

public class OffDay implements Event {
	private final int length;
	private final Team team;

	public OffDay(Team t, int days) {
		this.team=t;
		this.length=days;
	}
	
	public int length() {
		return this.length;
	}
	
	public int games() {
		return 0;
	}
	
	public Team homeTeam() {
		return team;
	}
}
