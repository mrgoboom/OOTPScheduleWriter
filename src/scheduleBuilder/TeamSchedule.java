package scheduleBuilder;
import java.util.ArrayList;
import java.util.List;

public class TeamSchedule {
	private List<Event> events;
	private List<Series> homeSeries;
	private List<Series> awaySeries;
	private List<Series> divisionSeries;
	private List<Series> interdivisionSeries;
	private List<Series> teamSchedule;
	private int daysScheduled;
	
	public TeamSchedule() {
		this.events = new ArrayList<>();
		this.homeSeries = new ArrayList<>();
		this.awaySeries = new ArrayList<>();
		this.divisionSeries = new ArrayList<>();
		this.interdivisionSeries = new ArrayList<>();
		this.teamSchedule = new ArrayList<>();
		this.daysScheduled=0;
	}
	
	public void addSeries(Series s, Boolean isHome, Boolean isDivision) {
		this.events.add(s);
		if(isHome) {
			this.homeSeries.add(s);
		}else {
			this.awaySeries.add(s);
		}
		if(isDivision) {
			this.divisionSeries.add(s);
		}else {
			this.interdivisionSeries.add(s);
		}
	}
	
	public void clear() {
		this.events = new ArrayList<>();
		this.homeSeries = new ArrayList<>();
		this.awaySeries = new ArrayList<>();
		this.divisionSeries = new ArrayList<>();
		this.interdivisionSeries = new ArrayList<>();
		this.teamSchedule = new ArrayList<>();
		this.daysScheduled=0;
	}
	
	public void addToSchedule(Series s) {
		this.teamSchedule.add(s);
		this.daysScheduled += s.length();
		
		this.events.remove(s);
		this.homeSeries.remove(s);
		this.awaySeries.remove(s);
		this.interdivisionSeries.remove(s);
		this.divisionSeries.remove(s);
	}
	
	/*
	 * Used to check if number of home and away games are equal
	 * Returns  0 if equal
	 * Returns >0 if more home than away
	 * Returns <0 if more away than home
	 */
	public int preScheduleBalancedHomeAway() {
		int homeGames=0;
		int awayGames=0;
		for(Series s: this.homeSeries) {
			homeGames += s.games();
		}
		for(Series s: this.awaySeries) {
			awayGames += s.games();
		}
		return homeGames-awayGames;
	}

	public int preScheduleGameCount() {
		int totalGames=0;
		for (Series s:this.homeSeries) {
			totalGames+=s.games();
		}
		for (Series s:this.awaySeries) {
			totalGames+=s.games();
		}
		return totalGames;
	}
	
	public int getDaysScheduled() {
		return this.daysScheduled;
	}
}
