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
	public boolean lastSeriesDoubleHeader;
	public int restDays;
	private Team lastSeriesVS;
	private Team last2SeriesVS;
	
	public Team () {
		this.consecutiveHomeGames=0;
		this.consecutiveAwayGames=0;
		this.lastSeriesDoubleHeader=true;
		this.gamesWithoutBreak=-1;
		this.lastSeriesVS=null;
		this.last2SeriesVS=null;
		this.schedule = new TeamSchedule(this);
		Team.teams.add(this);
		this.id = Team.teams.size();
		this.restDays=23;
	}
	
	public Team getLastSeriesVS() {
		return this.lastSeriesVS;
	}
	
	public boolean recentOpponent(Team other) {
		return this.last2SeriesVS==other||this.lastSeriesVS==other;
	}
	
	public int getGamesWithoutBreak() {
		return this.gamesWithoutBreak;
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
	
	public void reset() {
		this.consecutiveHomeGames=0;
		this.consecutiveAwayGames=0;
		this.gamesWithoutBreak=-1;
		this.lastSeriesVS=null;
		this.last2SeriesVS=null;
		this.schedule.clear();
		this.restDays=23;
		this.lastSeriesDoubleHeader=true;
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

	public void resetLastEvent() {
		Event e = this.schedule.getLastEvent();
		List<Event> theSchedule = this.schedule.getSchedule();
		if(e instanceof OffDay) {
			int gamesSinceBreak=0;
			this.restDays++;
			for(int i=theSchedule.size()-2;i>=0;i--) {
				Event previous = theSchedule.get(i);
				if(previous instanceof OffDay) {
					break;
				}else {
					gamesSinceBreak += previous.games();
				}
			}
			this.gamesWithoutBreak = gamesSinceBreak;
		}else {
			this.gamesWithoutBreak -= e.games();
			
			Event last = theSchedule.get(theSchedule.size()-2);
			int offSet=3;
			while (last instanceof OffDay) {
				last = theSchedule.get(theSchedule.size()-offSet);
				offSet++;
			}
			lastSeriesDoubleHeader=((Series)last).hasDoubleHeader();
			if(e.homeTeam()==this) {
				this.consecutiveHomeGames -= e.games();
				if(this.consecutiveHomeGames==0) {
					int awayGames = 0;
					for(int i=theSchedule.size()-2;i>=0;i--) {
						Event previous = theSchedule.get(i);
						if(previous instanceof Series && ((Series) previous).isHome(this)){
							break;
						}else {
							awayGames += previous.games();
						}
					}
					this.consecutiveAwayGames = awayGames;
				}
			}else {
				this.consecutiveAwayGames -= e.games();
				if(this.consecutiveAwayGames==0) {
					int homeGames = 0;
					for(int i=theSchedule.size()-2;i>=0;i--) {
						Event previous = theSchedule.get(i);
						if(previous instanceof Series && ((Series) previous).isAway(this)){
							break;
						}else {
							homeGames += previous.games();
						}
					}
					this.consecutiveHomeGames = homeGames;
				}
			}
		}
		this.schedule.resetLastEvent();
		if(this.schedule.getLastSeries()==null) {
			this.lastSeriesVS=null;
		}else {
			this.lastSeriesVS=this.schedule.getLastSeries().getOpponent(this);
		}
		if(this.schedule.getLast2Series()==null) {
			this.last2SeriesVS=null;
		}else {
			this.last2SeriesVS=this.schedule.getLast2Series().getOpponent(this);
		}
	}
	
	public Boolean teamFromSameDivision(Team other) {
		//TODO: Less hardcodey, please
		return (this.id-1)/5==(other.id-1)/5;
	}
	
	public void scheduleEvent(Event event) {
		if(event instanceof Series) {
			Series s = (Series) event;
			if(this.gamesWithoutBreak>=0) {
				this.gamesWithoutBreak += s.games();
			}else {
				this.gamesWithoutBreak = s.games();
			}
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
			this.last2SeriesVS=this.lastSeriesVS;
			this.lastSeriesVS=s.getOpponent(this);
			this.lastSeriesDoubleHeader = s.hasDoubleHeader();
		}else if(event instanceof OffDay){
			this.gamesWithoutBreak=0;
			if(event.length()==1) {
				this.restDays--;
			}
		}else {
			System.err.println("Tried to schedule unknown event.");
			return;
		}
		this.schedule.addToSchedule(event);
	}
	
	/*
	 * Supports multiple alerts.
	 * +1 if need Break
	 * +2 if need HomeSeries
	 * +4 if need AwaySeries
	 * +8 if no doubleheaders
	 * Else returns 0
	 */
	public int scheduleAlert() {
		int retVal=0;
		if (this.gamesWithoutBreak > Team.maxConsecutive-Series.getMaxSeriesLen()) {
			retVal+=1;
		}
		if(this.consecutiveHomeGames > Team.maxStand-Series.getMaxSeriesLen()) {
			retVal+=2;
		}else if(this.consecutiveAwayGames > Team.maxStand-Series.getMaxSeriesLen()) {
			retVal+=4;
		}
		if(this.lastSeriesDoubleHeader) {
			retVal+=8;
		}
		
		return retVal;
	}
	
	public String toString() {
		return "T"+this.id;
	}
}
