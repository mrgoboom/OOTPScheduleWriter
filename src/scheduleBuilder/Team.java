package scheduleBuilder;

import java.util.ArrayList;
import java.util.List;

public class Team {
	public final int id;
	public static final int maxStand = 21;
	public static final int maxConsecutive = 14;
	private static final List<Team> teams = new ArrayList<>();
	private TeamSchedule schedule;
	private int consecutiveHomeGames;
	private int consecutiveRoadGames;
	private int gamesWithoutBreak;
	
	public Team (int i) {
		this.consecutiveHomeGames=0;
		this.consecutiveRoadGames=0;
		this.gamesWithoutBreak=0;
		this.id = i;
		this.schedule = new TeamSchedule();
		Team.teams.add(this);
	}
	
	public static Team findTeamWithID(int target) {
		for(Team t : teams) {
			if (t.id==target) {
				return t;
			}
		}
		return null;
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
		if (this.gamesWithoutBreak > Team.maxConsecutive-Series.getMaxSeriesLen()) {
			return 1;
		}else if(this.consecutiveHomeGames > Team.maxStand-Series.getMaxSeriesLen()) {
			return 3;
		}else if(this.consecutiveRoadGames > Team.maxStand-Series.getMaxSeriesLen()) {
			return 2;
		}
		return 0;
	}
}
