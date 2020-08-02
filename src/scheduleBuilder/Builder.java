package scheduleBuilder;

import java.time.temporal.ValueRange;
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
	
	private List<Event> filterPriority(List<Event> input, Priority priority){
		List<Event> output = new ArrayList<>();
		if(priority==Priority.LENGTH||priority==Priority.LENGTH_FORCE) {
			ValueRange desiredLength;
			switch(this.weekDay) {
			case MONDAY:
			case FRIDAY:
				desiredLength=ValueRange.of(3, 4);
				break;
			case TUESDAY:
			case SATURDAY:
				desiredLength=ValueRange.of(2, 3);
				break;
			case WEDNESDAY:
			case SUNDAY:
				desiredLength=ValueRange.of(2, 2);
				break;
			default:
				desiredLength=ValueRange.of(4, 4);
			}
			for(Event e:input) {
				if(priority.matchesPriority(e, findDivision(e.homeTeam()), e.homeTeam(), desiredLength)) {
					output.add(e);
				}
			}
		}else {
			for(Event e:input) {
				if(priority.matchesPriority(e, findDivision(e.homeTeam()), e.homeTeam(), null)) {
					output.add(e);
				}
			}
		}
		return output;
	}
	
	private List<Team> missingEvent(List<Team> teamList, List<Event> available) {
		List<Team> missing = new ArrayList<>();
		for(Team team:teamList) {
			boolean success=false;
			for(Event event:available) {
				if(event.isInvolved(team)) {
					success=true;
					break;
				}
			}
			if(!success) {
				missing.add(team);
			}
		}
		return missing;
	}
	
	private Event restDay(Team team, List<Event> toSearch) {
		for(Event event:toSearch) {
			if(event.homeTeam()==team&&event instanceof OffDay) {
				return event;
			}
		}
		return null;
	}
	
	private List<TeamPair> createPairs(List<Event> available, Team dontRest){
		TeamPair.clearPairs();
		List<TeamPair> allPairs = new ArrayList<>();
		for(Event e:available) {
			if((e instanceof OffDay)&&e.homeTeam()!=dontRest) {
				TeamPair newPair=TeamPair.createNew(e.homeTeam(), e.homeTeam());
				if(!allPairs.contains(newPair)) {
					allPairs.add(newPair);
				}
			}else {
				TeamPair newPair=TeamPair.createNew(e.homeTeam(), ((Series)e).awayTeam());
				if(!allPairs.contains(newPair)) {
					allPairs.add(newPair);
				}
			}
		}
		return allPairs;
	}
	
	private List<Event> selectEvents(List<Team> toSchedule, List<Event> available) {
		List<Event> events = new ArrayList<>();
		/*List<Team> scheduling=toSchedule;
		if(this.weekDay.isRestDay()&&(this.teams.size()-scheduling.size()>3)) {
			List<Team> canRest = new ArrayList<>();
			List<Team> cantRest = new ArrayList<>();
			
			for(Team t:scheduling) {
				Event rest = restDay(t, available);
				if(rest==null) {
					cantRest.add(t);
				}else {
					canRest.add(t);
					events.add(rest);
				}
			}
			if(cantRest.size()%2==1) {
				if(canRest.size()>0) {
					Team leastRest=canRest.get(0);
					for(Team team:canRest) {
						if(team.restDays<leastRest.restDays) {
							leastRest=team;
						}
					}
					final Team toSwitch=leastRest;
					canRest.remove(toSwitch);
					cantRest.add(toSwitch);
					events.removeIf(e->(e.isInvolved(toSwitch)));
				}else {
					return null;
				}
				scheduling=cantRest;
			}
		}*/
		Team dontRest=null;
		if(this.weekDay.isRestDay()&&(this.teams.size()==toSchedule.size())) {
			dontRest=toSchedule.get(0);
			int fewestRemaining=Integer.MAX_VALUE;
			for(Team t:toSchedule) {
				if(t.restDays<fewestRemaining) {
					fewestRemaining=t.restDays;
					dontRest=t;
				}
			}
		}
		List<TeamPair> allPairs=createPairs(available,dontRest);
		List<TeamPair> matchups=TeamPair.getUniqueMatchups(allPairs, toSchedule);
		if(matchups!=null) {
			for(Event e:available) {
				for(TeamPair tp:matchups) {
					if(e.isInvolved(tp.team1)&&e.isInvolved(tp.team2)) {
						events.add(e);
						matchups.remove(tp);
						break;
					}
				}
			}
			return events;
		}
		return null;
	}
	
	
	
	private Boolean negotiate(List<Team> toSchedule, List<Event> available, List<Priority> priorities) {	
		if(!missingEvent(toSchedule,available).isEmpty()) {
			return false;
		}
		if(priorities.isEmpty()) {
			//TODO: Schedule event for everyone in toSchedule
		}else {
			List<Event> nextEvents=filterPriority(available, priorities.get(0));
			List<Team> scheduleNow = missingEvent(toSchedule, available);
			//TODO: Schedule event for everyone in scheduleNow. Negotiate further with rest.
		}
		
		return true;
	}
	
	private List<Event> copyEventList(List<Event> eventList){
		List<Event> newList = new ArrayList<>();
		for(Event e: eventList) {
			newList.add(e);
		}
		return newList;
	}
	
	private List<Priority> copyPriorityList(List<Priority> priorityList){
		List<Priority> newList = new ArrayList<>();
		for(Priority p:priorityList) {
			newList.add(p);
		}
		return newList;
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
		List<Priority> priorities = new ArrayList<>();
		priorities.add(Priority.ALERT);
		priorities.add(Priority.LENGTH);
		priorities.add(Priority.DIVISION);
		while(scheduleDay<30) {
			if(!this.weekDay.isRestDay()) {
				priorities.add(2, Priority.SERIES);
			}
			List<Team> toSchedule=getWaiting(scheduleDay);
			System.out.println("There are "+toSchedule.size()+" teams waiting to be scheduled on day "+scheduleDay);
			List<Event> possible = new ArrayList<>();
			for(Team team:toSchedule) {
				List<Event> teamEvent=team.schedule.remainingMatchups(toSchedule);
				for(Event e: teamEvent){
					if(!possible.contains(e)) {
						possible.add(e);
					}
				}
			}
			if(missingEvent(toSchedule, possible).isEmpty()) {
				success&=negotiate(toSchedule, possible, copyPriorityList(priorities));
				priorities.remove(Priority.SERIES);
			}else {
				reset();
				return false;
			}
			
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