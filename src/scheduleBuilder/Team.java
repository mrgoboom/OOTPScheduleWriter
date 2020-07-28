package scheduleBuilder;

public class Team {
	public final int id;
	public static int maxStand = 21;
	public static int maxConsecutive = 14;
	private TeamSchedule schedule;
	private int consecutiveHomeGames;
	private int consecutiveRoadGames;
	private int gamesWithoutBreak;
	private int totalGames;
	private int homeGames;
	
	public Team (int i) {
		this.consecutiveHomeGames=0;
		this.consecutiveRoadGames=0;
		this.gamesWithoutBreak=0;
		this.totalGames=0;
		this.homeGames=0;
		this.id = i;
		this.schedule = new TeamSchedule();
	}
	
	public TeamSchedule getSchedule() {
		return this.schedule;
	}
	
	/*
	 * Returns 1 if need Break
	 * Returns 2 if need HomeSeries
	 * Returns 3 if need RoadSeries
	 * Else returns 0
	 */
	public int scheduleAlert() {
		if (this.gamesWithoutBreak > Team.maxConsecutive-Series.MaxSeriesLen) {
			return 1;
		}else if(this.consecutiveHomeGames > Team.maxStand-Series.MaxSeriesLen) {
			return 3;
		}else if(this.consecutiveRoadGames > Team.maxStand-Series.MaxSeriesLen) {
			return 2;
		}
		return 0;
	}
	
	public Boolean checkTotal() {
		if (this.totalGames==162&&this.homeGames==81) {
			return true;
		}else {
			return false;
		}
	}
}
