package scheduleBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeamSchedule {
	private final Team team;
	private List<Event> events;
	private List<Series> homeSeries;
	private List<Series> awaySeries;
	private List<Series> divisionSeries;
	private List<Series> interdivisionSeries;
	private List<Event> teamSchedule;
	private int daysScheduled;
	
	public TeamSchedule(Team theTeam) {
		this.events = new ArrayList<>();
		this.homeSeries = new ArrayList<>();
		this.awaySeries = new ArrayList<>();
		this.divisionSeries = new ArrayList<>();
		this.interdivisionSeries = new ArrayList<>();
		this.teamSchedule = new ArrayList<>();
		this.team = theTeam;
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
	
	public void addOffDay(OffDay offDay) {
		this.events.add(offDay);
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
	
	public void shuffleAll() {
		Collections.shuffle(events);
		Collections.shuffle(homeSeries);
		Collections.shuffle(awaySeries);
		Collections.shuffle(divisionSeries);
		Collections.shuffle(interdivisionSeries);
	}
	
	public OffDay grabBreak() {
		for(int i=0;i<events.size();i++) {
			Event test=events.get(i);
			if(test instanceof OffDay) {
				return (OffDay)test;
			}
		}
		return null;
	}
	
	public Event grabValidEvent(int lengthReq) {
		for(int i=0;i<events.size();i++) {
			Event test = events.get(i);
			if(test instanceof OffDay) {
				if(lengthReq==0||lengthReq==4) {
					return test;
				}
			}else if(test instanceof Series){
				Series s = (Series)test;
				if(s.getOpponent(this.team).schedule.getDaysScheduled()==this.daysScheduled) {
					if(lengthReq==0||s.length()==lengthReq) {
						return s;
					}
				}
			}else {
				System.err.println("Found Unknown Event.");
			}
		}
		System.err.println("No valid events.");
		return null;
	}
	
	public Series grabValidSeries(int lengthReq) {
		for(int i=0;i<events.size();i++) {
			Event test = events.get(i);
			if(test instanceof Series) {
				Series s = (Series)test;
				if(s.getOpponent(this.team).schedule.getDaysScheduled()==this.daysScheduled) {
					if(lengthReq==0||s.length()==lengthReq) {
						return s;
					}
				}
			}
		}
		return null;
	}
	
	public Series grabInterdivisionSeriesVS(List<Team> valid) {
		for(int i=0;i<this.interdivisionSeries.size();i++) {
			Series test = this.interdivisionSeries.get(i);
			if(valid.contains(test.getOpponent(this.team))) {
				return test;
			}
		}
		return null;
	}
	
	public Series grabDivisionSeriesVS(List<Team> valid) {
		for(int i=0;i<this.divisionSeries.size();i++) {
			Series test = this.divisionSeries.get(i);
			if(valid.contains(test.getOpponent(this.team))) {
				return test;
			}
		}
		return null;
	}
	
	public Series grabHomeSeriesVS(List<Team> valid, int lengthReq) {
		for(int i=0;i<this.homeSeries.size();i++) {
			Series test = this.homeSeries.get(i);
			if(valid.contains(test.getOpponent(this.team))) {
				if(lengthReq != 0 || test.length()==lengthReq) {
						return test;
				}
			}
		}
		return null;
	}
	
	public Series grabAwaySeriesVS(List<Team> valid, int lengthReq) {
		for(int i=0;i<this.awaySeries.size();i++) {
			Series test = this.awaySeries.get(i);
			if(valid.contains(test.getOpponent(this.team))) {
				if(lengthReq != 0 || test.length()==lengthReq) {
					return test;
				}
			}
		}
		return null;
	}
	
	public void addToSchedule(Event e) {
		this.teamSchedule.add(e);
		this.daysScheduled += e.length();
		
		this.events.remove(e);
		if(e instanceof Series) {
			this.homeSeries.remove(e);
			this.awaySeries.remove(e);
			this.interdivisionSeries.remove(e);
			this.divisionSeries.remove(e);
		}
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
	
	public int preScheduleGameDays() {
		int gameDays=0;
		for(Series s: this.homeSeries) {
			gameDays += s.length();
		}
		for(Series s: this.awaySeries) {
			gameDays += s.length();
		}
		return gameDays;
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
