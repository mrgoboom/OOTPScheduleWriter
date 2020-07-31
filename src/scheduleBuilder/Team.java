package scheduleBuilder;

import java.util.ArrayList;
import java.util.List;

public class Team {
	public final int id;
	public static final int maxStand = 14;
	public static final int maxConsecutive = 21;
	private static final List<Team> teams = new ArrayList<>();
	public final TeamSchedule schedule;
	private int consecutiveHomeGames;
	private int consecutiveAwayGames;
	private int gamesWithoutBreak;
	
	public Team () {
		this.consecutiveHomeGames=0;
		this.consecutiveAwayGames=0;
		this.gamesWithoutBreak=0;
		this.schedule = new TeamSchedule(this);
		Team.teams.add(this);
		this.id = Team.teams.size();
	}
	
	public int getHomeStand() {
		return this.consecutiveHomeGames;
	}
	
	public int getAwayStand() {
		return this.consecutiveAwayGames;
	}
	
	public static Team findTeamWithID(int target) {
		for(Team t : teams) {
			if (t.id==target) {
				return t;
			}
		}
		return null;
	}
	
	public String areSeriesBalanced() {
		String error = null;
		int diff = this.schedule.preScheduleBalancedHomeAway();
		if(diff>0) {
			error = "Team "+this.id+" plays "+diff+" more home games than away games.";
		}else if(diff<0) {
			error = "Team "+this.id+" plays "+(0-diff)+" more away games than home games.";
		}
		return error;
	}

	public void scheduleEvent(Event event) {
		if(event instanceof Series) {
			Series s = (Series) event;
			this.gamesWithoutBreak += s.games();
			if(s.isHome(this)) {
				this.consecutiveHomeGames += s.games();
				this.consecutiveAwayGames = 0;
			}else if(s.isAway(this)) {
				this.consecutiveAwayGames += s.games();
				this.consecutiveHomeGames = 0;
			}else {
				System.err.println("Tried to schedule event for team that is neither home nor away.");
				return;
			}
		}else if(event instanceof OffDay){
			this.gamesWithoutBreak=0;
		}else {
			System.err.println("Tried to schedule unknown event.");
			return;
		}
		this.schedule.addToSchedule(event);
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
		}else if(this.consecutiveAwayGames > Team.maxStand-Series.getMaxSeriesLen()) {
			return 2;
		}
		return 0;
	}
}
