package scheduleBuilder;

public class Series {
	public static int MaxSeriesLen = 4;
	private Team homeTeam;
	private Team awayTeam;
	public final int length;
	
	public Series(Team home, Team away, int numDays, Boolean division) {
		this.length=numDays;
		if (numDays > Series.MaxSeriesLen) {
			return;
		}
		this.homeTeam=home;
		this.awayTeam=away;
	}
	
	/*
	 * This series is a break
	 */
	public Series(Team team, int numDays) {
		this.homeTeam=team;
		this.length=numDays;
		this.awayTeam=null;
	}
	
	public Boolean isHome(Team team) {
		return team==this.homeTeam;
	}
	
	public Boolean isAway(Team team) {
		return team==this.awayTeam;
	}
	
	public Team getOpponent(Team team) {
		if (this.isHome(team)) {
			return this.awayTeam;
		}else if(this.isAway(team)) {
			return this.homeTeam;
		}
		return null;
	}
}
