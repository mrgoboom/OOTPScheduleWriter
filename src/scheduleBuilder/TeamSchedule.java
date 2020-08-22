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
	public List<Series>[] seriesVsTeam;
	private int daysScheduled;
	
	@SuppressWarnings("unchecked")
	public TeamSchedule(Team theTeam) {
		this.events = new ArrayList<>();
		this.homeSeries = new ArrayList<>();
		this.awaySeries = new ArrayList<>();
		this.divisionSeries = new ArrayList<>();
		this.interdivisionSeries = new ArrayList<>();
		this.teamSchedule = new ArrayList<>();
		this.team = theTeam;
		this.daysScheduled=0;
		this.seriesVsTeam = new List[Builder.numTeams];
		for(int i=0;i<Builder.numTeams;i++) {
			this.seriesVsTeam[i] = new ArrayList<>();
		}
	}
	
	public List<Event> getEvents(){
		return this.events;
	}
	
	public List<Event> getSchedule(){
		return this.teamSchedule;
	}
	
	public Series getLastSeries() {
		for(int i=this.teamSchedule.size()-1;i>=0;i--) {
			Event e = this.teamSchedule.get(i);
			if(e instanceof Series) {
				return (Series)e;
			}
		}
		return null;
	}
	
	public int mostSeriesRemaining(List<Team> fromList) {
		if(fromList.size()<=0) {
			return -1;
		}
		int teamID=fromList.get(0).id;
		for(Team team: fromList) {
			if(this.seriesVsTeam[team.id-1].size()<this.seriesVsTeam[teamID-1].size()) {
				teamID=team.id;
			}
		}
		return teamID;
	}
	
	public void addSeries(Series s, Boolean isHome, Boolean isDivision) {
		this.events.add(s);
		this.seriesVsTeam[s.getOpponent(this.team).id-1].add(s);
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
	
	@SuppressWarnings("unchecked")
	public void clear() {
		this.events = new ArrayList<>();
		this.homeSeries = new ArrayList<>();
		this.awaySeries = new ArrayList<>();
		this.divisionSeries = new ArrayList<>();
		this.interdivisionSeries = new ArrayList<>();
		this.teamSchedule = new ArrayList<>();
		this.daysScheduled=0;
		this.seriesVsTeam = new List[Builder.numTeams];
		for(int i=0;i<Builder.numTeams;i++) {
			this.seriesVsTeam[i] = new ArrayList<>();
		}
	}
	
	public void shuffleAll() {
		Collections.shuffle(events);
		Collections.shuffle(homeSeries);
		Collections.shuffle(awaySeries);
		Collections.shuffle(divisionSeries);
		Collections.shuffle(interdivisionSeries);
	}

	public Event getLastEvent() {
		return this.teamSchedule.get(this.teamSchedule.size()-1);
	}
	
	public void resetLastEvent() {
		Event e = this.teamSchedule.get(this.teamSchedule.size()-1);
		this.teamSchedule.remove(e);
		this.daysScheduled -= e.length();
		
		if(e instanceof OffDay) {
			addOffDay((OffDay)e);
		}else {
			addSeries((Series)e,e.homeTeam()==this.team, this.team.teamFromSameDivision(((Series)e).getOpponent(this.team)));
		}
		shuffleAll();
	}
	
	public List<Event> remainingMatchups(List<Team> opponents) {
		List<Event> fitMatchup = new ArrayList<>();
		for(Event e:this.events) {
			if(e instanceof Series) {
				if(opponents.contains(((Series)e).getOpponent(this.team))&&((Series)e).getOpponent(this.team)!=this.team.getLastSeriesVS()) {
					fitMatchup.add(e);
				}
			}else {
				if(this.team.getGamesWithoutBreak()!=0) {
					fitMatchup.add(e);
				}
			}
		}
		return fitMatchup;
	}
	
	public void addToSchedule(Event e) {
		this.teamSchedule.add(e);
		this.daysScheduled += e.length();
		
		this.events.remove(e);
		if(e instanceof Series) {
			this.seriesVsTeam[((Series)e).getOpponent(this.team).id-1].remove(e);
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
		for(Event e: this.events) {
			gameDays+=e.length();
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
