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
	
	public Boolean assignSeries(List<Series> seriesList) {
		for(Series s:seriesList) {
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
		if(weekDay.isRestDay()) {
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
				validOpponents.add(t);
			}
		}
		Series series=null;
		if(validOpponents.size()>=2) {
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
		if(weekDay.isRestDay()&&lengthReq==0) {
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
			if(t.schedule.getDaysScheduled()==team.schedule.getDaysScheduled()) {
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
		if(weekDay.isRestDay()&&lengthReq==0) {
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
			if(t.schedule.getDaysScheduled()==team.schedule.getDaysScheduled()) {
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
		if(weekDay.isRestDay()) {
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
		for(Team t : this.teams) {
			if(!division.contains(t)&&t.schedule.getDaysScheduled()==team.schedule.getDaysScheduled()) {
				validOpponents.add(t);
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
			Team active = teams.get(0);
			if(active.schedule.getDaysScheduled()>scheduleDay) {
				scheduleDay++;
				weekDay=weekDay.next();
			}else {
				int alert = active.scheduleAlert();
				switch(alert) {
					case 1:
						success=scheduleBreak(active);
						break;
					case 2:
						success=scheduleHome(active,0);
						break;
					case 3:
						success=scheduleRoad(active,0);
						break;
					default:
						success=scheduleDivision(active);
						break;
				}
			}
		}
		//Until 4 days before all-star break
		while(scheduleDay<this.allStarBreakStart-Series.getMaxSeriesLen()) {
			Team active = teams.get(0);
			if(active.schedule.getDaysScheduled()>scheduleDay) {
				scheduleDay++;
				weekDay=weekDay.next();
			}else {
				int alert = active.scheduleAlert();
				switch(alert) {
					case 1:
						success=scheduleBreak(active);
						break;
					case 2:
						success=scheduleHome(active,0);
						break;
					case 3:
						success=scheduleRoad(active,0);
						break;
					default:
						success=scheduleInterdivision(active);
						break;
				}
			}
		}
		//4 days before all-star break
		for(;;) {
			Team active = teams.get(0);
			if(active.schedule.getDaysScheduled()>scheduleDay) {
				scheduleDay++;
				weekDay=weekDay.next();
				break;
			}
			int alert = active.scheduleAlert();
			switch(alert) {
				case 1:
					success=scheduleBreak(active);
					break;
				case 2:
					success=scheduleHome(active,4);
					break;
				case 3:
					success=scheduleRoad(active,4);
					break;
				default:
					success=scheduleAnything(active,4);
					break;
			}
		}
		return success;
	}
}