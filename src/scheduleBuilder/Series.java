package scheduleBuilder;

public class Series {
	private static int MaxSeriesLen=0;
	private Team homeTeam;
	private Team awayTeam;
	public final int length;
	public final int games;
	
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
	
	public static int getMaxSeriesLen() {
		return Series.MaxSeriesLen;
	}
}
