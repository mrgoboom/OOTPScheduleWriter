package scheduleBuilder;

public class Series implements Event{
	private static int MaxSeriesLen=0;
	private final Team homeTeam;
	private final Team awayTeam;
	private final int length;
	private final int games;
	
	public Series(Team home, Team away, int numDays, int numGames) {
		this.length=numDays;
		if (numDays > Series.MaxSeriesLen) {
			Series.MaxSeriesLen = numDays;
		}
		this.homeTeam=home;
		this.awayTeam=away;
		this.games=numGames;
	}
	
	/*
	 * This series is a break
	 */
	public Series(Team team, int numDays) {
		this.homeTeam=team;
		this.length=numDays;
		this.games=0;
		this.awayTeam=null;
	}
	
	public Boolean isHome(Team team) {
		return team==this.homeTeam;
	}
	
	public Team homeTeam() {
		return this.homeTeam;
	}
	
	public Team awayTeam() {
		return this.awayTeam;
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
	
	public String toString() {
		return "Team "+this.homeTeam.id+" hosts team "+this.awayTeam.id+" for "+this.games+" games in "+this.length+" days.";
	}
	
	public int length() {
		return this.length;
	}
	
	public int games() {
		return this.games;
	}
	
	public static int getMaxSeriesLen() {
		return Series.MaxSeriesLen;
	}
}
