package scheduleBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Builder {
	private final List<List<Team>> divisions;
	private final List<Team> teams;
	private final int totalDays=181;
	private final int allStarBreakStart=95;
	private final int allStarBreakLen=3;
	private DayOfWeek weekDay;
	
	public Builder(List<List<Team>> structure) {
		this.divisions=structure;
		this.teams=new ArrayList<>();
		for (List<Team> division: structure) {
			for(Team t: division) {
				teams.add(t);
			}
		}
		Collections.shuffle(this.teams);
		this.weekDay=DayOfWeek.THURSDAY;
	}
	
	public Boolean assignSeries(List<Event> eventList) {
		for(Event e:eventList) {
			if(e instanceof Series) {
				Series s = (Series)e;
				List<Team> homeDivision=null;
				List<Team> awayDivision=null;;
				for(List<Team> division:this.divisions) {
					if(division.contains(s.homeTeam())) {
						homeDivision=division;
					}
					if(division.contains(s.awayTeam())) {
						awayDivision=division;
					}
				}
				if(homeDivision!=null&&awayDivision!=null) {
					s.homeTeam().schedule.addSeries(s, true, homeDivision==awayDivision);
					s.awayTeam().schedule.addSeries(s, false, homeDivision==awayDivision);
				}else {
					System.err.println("There is a series including a team not found in any division.");
					return false;
				}
			}else {
				e.homeTeam().schedule.addOffDay((OffDay)e);
			}
		}
		return true;
	}
	
	private void pushBack(Team team) {
		this.teams.remove(team);
		int index=0;
		int daysScheduled=team.schedule.getDaysScheduled();
		while (index<this.teams.size()) {
			if(daysScheduled <= this.teams.get(index).schedule.getDaysScheduled()) {
				break;
			}
			index ++;
		}
		if(index==this.teams.size()) {
			this.teams.add(team);
		}else {
			this.teams.add(index, team);
		}
	}
	
	private List<Team> findDivision(Team team){
		for(List<Team> division : this.divisions) {
			if(division.contains(team)) {
				return division;
			}
		}
		System.err.println("Error: Team "+team.id+" has no division.");
		return null;
	}

	private void pushToSchedule(Event event) {
		Team home=event.homeTeam();
		if(event instanceof OffDay) {
			home.scheduleEvent(event);
			pushBack(home);
		}else if(event instanceof Series) {
			home.scheduleEvent(event);
			pushBack(home);
			Series series = (Series)event;
			Team away=series.awayTeam();
			away.scheduleEvent(series);
			pushBack(away);
		}else {
			System.err.println("Error: Unknown event type.");
		}
	}
	
	private void reset() {
		List<Event> allEvents = new ArrayList<>();
		for(Team t:this.teams) {
			for(Event e:t.schedule.getEvents()) {
				if(!allEvents.contains(e)) {
					allEvents.add(e);
				}
			}
			for(Event e:t.schedule.getSchedule()) {
				if(!allEvents.contains(e)) {
					allEvents.add(e);
				}
			}
			t.reset();
		}
		this.weekDay=DayOfWeek.THURSDAY;
		//TODO: Re-add everything as new
		if(!assignSeries(allEvents)) {
			System.err.println("Reset failed. Please terminate.");
		}
	}
	/*
	private Boolean scheduleAnything(Team team,int lengthReq) {
		Event event=team.schedule.grabValidEvent(lengthReq);
		if(event!=null) {
			pushToSchedule(event);
			return true;
		}
		return false;
	}
	
	private Boolean scheduleAnySeries(Team team, int lengthReq) {
		Series series=team.schedule.grabValidSeries(lengthReq);
		if(series!=null) {
			pushToSchedule(series);
			return true;
		}
		return false;
	}
	
	private Boolean scheduleDivision(Team team) {
		if(weekDay.isRestDay()&&team.getLastSeriesVS()!=team) {
			int teamsScheduled=0;
			for(Team t:this.teams) {
				if(t.schedule.getDaysScheduled()>team.schedule.getDaysScheduled()) {
					teamsScheduled++;
				}
			}
			if(teamsScheduled>3) {
				Event takeBreak = team.schedule.grabValidEvent(0);
				if(takeBreak instanceof OffDay) {
					pushToSchedule(takeBreak);
					return true;
				}
			}
		}
		List<Team> division = findDivision(team);
		List<Team> validOpponents=new ArrayList<>();
		for(Team t : division) {
			if(t.schedule.getDaysScheduled()==team.schedule.getDaysScheduled()) {
				if(t!=team&&t!=team.getLastSeriesVS()&&t.scheduleAlert()!=1) {
					validOpponents.add(t);
				}
			}
		}
		Series series=null;
		if(validOpponents.size()>0) {
			series=team.schedule.grabDivisionSeriesVS(validOpponents);
		}
		if(series!=null) {
			pushToSchedule(series);
			return true;
		}
		if(!scheduleAnySeries(team,0)) {
			return scheduleAnything(team,0);
		}
		return true;
	}
	
	private Boolean scheduleBreak(Team team) {
		OffDay offDay=team.schedule.grabBreak();
		if(offDay!=null) {
			pushToSchedule(offDay);
			return true;
		}else{
			scheduleAnything(team,0);
		}
		return false;
	}
	
	private Boolean scheduleHome(Team team, int lengthReq) {
		if(weekDay.isRestDay()&&lengthReq==0&&team.getLastSeriesVS()!=team) {
			int teamsScheduled=0;
			for(Team t:this.teams) {
				if(t.schedule.getDaysScheduled()>team.schedule.getDaysScheduled()) {
					teamsScheduled++;
				}
			}
			if(teamsScheduled>3) {
				Event takeBreak = team.schedule.grabValidEvent(0);
				if(takeBreak instanceof OffDay) {
					pushToSchedule(takeBreak);
					return true;
				}
			}
		}
		List<Team> validOpponents = new ArrayList<>();
		for(Team t:this.teams) {
			if((t.schedule.getDaysScheduled()==team.schedule.getDaysScheduled())&&team.getLastSeriesVS()!=t&&t.scheduleAlert()!=1) {
				if(t.getAwayStand()<Team.maxConsecutive-Series.getMaxSeriesLen()&&(t.getHomeStand()==0||t.getHomeStand()>4)) {
					validOpponents.add(t);
				}
			}
		}
		Series series=team.schedule.grabHomeSeriesVS(validOpponents,lengthReq);
		if(series!=null) {
			pushToSchedule(series);
			return true;
		}
		
		if(!scheduleAnySeries(team,lengthReq)) {
			return scheduleAnything(team,lengthReq);
		}
		return true;
	}
	
	private Boolean scheduleRoad(Team team, int lengthReq) {
		if(weekDay.isRestDay()&&lengthReq==0&&team.getLastSeriesVS()!=team) {
			int teamsScheduled=0;
			for(Team t:this.teams) {
				if(t.schedule.getDaysScheduled()>team.schedule.getDaysScheduled()) {
					teamsScheduled++;
				}
			}
			if(teamsScheduled>3) {
				Event takeBreak = team.schedule.grabValidEvent(0);
				if(takeBreak instanceof OffDay) {
					pushToSchedule(takeBreak);
					return true;
				}
			}
		}
		List<Team> validOpponents = new ArrayList<>();
		for(Team t:this.teams) {
			if(t.schedule.getDaysScheduled()==team.schedule.getDaysScheduled()&&team.getLastSeriesVS()!=t&&t.scheduleAlert()!=1) {
				if(t.getHomeStand()<Team.maxConsecutive-Series.getMaxSeriesLen()&&(t.getAwayStand()==0||t.getAwayStand()>4)) {
					validOpponents.add(t);
				}
			}
		}
		Series series=team.schedule.grabAwaySeriesVS(validOpponents,lengthReq);
		if(series!=null) {
			pushToSchedule(series);
			return true;
		}
		if(!scheduleAnySeries(team,lengthReq)) {
			return scheduleAnything(team,lengthReq);
		}
		return true;
	}
	
	private Boolean scheduleInterdivision(Team team) {
		if(weekDay.isRestDay()&&team.getLastSeriesVS()!=team) {
			int teamsScheduled=0;
			for(Team t:this.teams) {
				if(t.schedule.getDaysScheduled()>team.schedule.getDaysScheduled()) {
					teamsScheduled++;
				}
			}
			if(teamsScheduled>1) {
				Event takeBreak = team.schedule.grabValidEvent(0);
				if(takeBreak instanceof OffDay) {
					pushToSchedule(takeBreak);
					return true;
				}
			}
		}
		List<Team> division = findDivision(team);
		List<Team> validOpponents=new ArrayList<>();
		for(Team t : this.teams) {
			if(!division.contains(t)&&t.schedule.getDaysScheduled()==team.schedule.getDaysScheduled()) {
				if(team.getLastSeriesVS()!=t&&t.scheduleAlert()!=1) {
					validOpponents.add(t);
				}
			}
		}
		if(validOpponents.size()>0) {
			Series series = team.schedule.grabInterdivisionSeriesVS(validOpponents);
			if(series!=null) {
				pushToSchedule(series);
				return true;
			}
		}
		if(!scheduleAnySeries(team,0)) {
			return scheduleAnything(team,0);
		}
		return true;
	}
	*/
	private List<Team> getWaiting(int dayOfSchedule){
		List<Team> waiting = new ArrayList<>();
		for(Team t:teams) {
			if(t.schedule.getDaysScheduled()==dayOfSchedule) {
				waiting.add(t);
			}
		}
		return waiting;
	}
	
	private Boolean negotiate(List<Team> toSchedule) {
		//get # of remaining series between teams.
		int size = toSchedule.size();
		int[][] remainingMatchups = new int[size][size];//access @ i<j
		int[] totalMatchups = new int[size];
		int minIndex=0;
		for(int i=0;i<size;i++) {
			for(int j=i+1;j<size;j++) {
				int iVjMatchups=toSchedule.get(i).schedule.remainingMatchups(toSchedule.get(j));
				remainingMatchups[i][j]=iVjMatchups;
				totalMatchups[i]++;
				totalMatchups[j]++;
			}
			if(totalMatchups[i]<totalMatchups[minIndex]) {
				minIndex=i;
			}
		}
		//TODO: Schedule a series for team toSchedule.get(i). Recurr with list minus teams in-game.
		return false;
	}
	
	public Boolean schedule() {
		Boolean success=true;
		for(Team t: this.teams) {
			int gameDays=t.schedule.preScheduleGameDays();
			for(int i=0;i<this.totalDays-gameDays-this.allStarBreakLen;i++) {
				t.schedule.addOffDay(new OffDay(t,1));
			}
			t.schedule.shuffleAll();
		}
		
		int scheduleDay=0;
		//First month of season
		while(scheduleDay<30) {
			List<Team> toSchedule=getWaiting(scheduleDay);
			System.out.println("There are "+toSchedule.size()+" teams waiting to be scheduled on day "+scheduleDay);
			success&=negotiate(toSchedule);
			if(!success) {
				reset();
				return false;
			}
		}
		//Until 5 days before all-star break
		
		//5 days before all-star break use this to avoid offday final day
		
		//4 days before all-star break
		
		//3 days before all-star break
		
		//2 days before all-star break
		
		//day before all-star break (not ideal if any teams actually need scheduling here)

		
		System.out.println("All star break reached.");
		//Last 30 days
		
		System.out.println("Made it to September");
		//To end of season
		
		System.out.println("Schedule completed successfully.");
		return success;
	}
}