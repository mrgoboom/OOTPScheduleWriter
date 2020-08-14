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
		//Collections.shuffle(this.teams);
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

		if(!assignSeries(allEvents)) {
			System.err.println("Reset failed. Please terminate.");
		}
	}
	
	private List<Team> getWaiting(int dayOfSchedule){
		List<Team> waiting = new ArrayList<>();
		for(Team t:this.teams) {
			if(t.schedule.getDaysScheduled()==dayOfSchedule) {
				waiting.add(t);
			}
		}
		return waiting;
	}
	
	private List<Event> filterPriority(List<Event> input, Priority priority, Team dontRest){
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
		}else if(priority==Priority.SERIES_EXISTS&&this.weekDay.isRestDay()) {
			for(Event e:input) {
				if(priority.matchesPriority(e, findDivision(e.homeTeam()), dontRest, null)) {
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
	
	private List<TeamPair> createPairs(List<Team> toSchedule, List<Event> available){
		TeamPair.clearPairs();
		List<TeamPair> allPairs = new ArrayList<>();
		for(Event e:available) {
			if((e instanceof OffDay)&&toSchedule.contains(e.homeTeam())) {
				TeamPair newPair=TeamPair.createNew(e.homeTeam(), e.homeTeam());
				if(!allPairs.contains(newPair)) {
					allPairs.add(newPair);
				}
			}else if((e instanceof Series)&&(toSchedule.contains(e.homeTeam())||toSchedule.contains(((Series)e).awayTeam()))) {
				TeamPair newPair=TeamPair.createNew(e.homeTeam(), ((Series)e).awayTeam());
				if(!allPairs.contains(newPair)) {
					allPairs.add(newPair);
				}
			}
		}
		return allPairs;
	}
	
	
	private List<TeamPair> selectMatchups(List<Team> toSchedule, List<Event> available, List<TeamPair> oldPairs) {
		List<TeamPair> allPairs=createPairs(toSchedule, available);
		List<TeamPair> matchups=TeamPair.getUniqueMatchups(allPairs, toSchedule, oldPairs);
		
		return matchups;
	}
	
	
	
	private Boolean negotiate(List<Team> toSchedule, List<Event> available, List<Priority> priorities) {	
		if(!missingEvent(toSchedule,available).isEmpty()) {
			return false;
		}
		
		@SuppressWarnings("unchecked")
		List<Event>[] levelEvents = new ArrayList[priorities.size()+1];
		levelEvents[0] = copyEventList(available);
		List<TeamPair> bestMatchups = null;

		List<Team> stillToSchedule = copyTeamList(toSchedule);
		for(int i=0;i<priorities.size();i++) {
			levelEvents[i+1]=filterPriority(levelEvents[i],priorities.get(i),stillToSchedule.get(0));

			List<TeamPair> newMatchups = selectMatchups(toSchedule,levelEvents[i], bestMatchups);
			
			if(newMatchups!=null) {
				bestMatchups = newMatchups;
			}

		}
		List<TeamPair> newMatchups = selectMatchups(toSchedule, levelEvents[priorities.size()], bestMatchups);
		if(newMatchups!=null) {
			bestMatchups=newMatchups;
		}
		if(bestMatchups==null) {
			return false;
		}
		int numMatchups=bestMatchups.size();
		List<TeamPair> scheduledMatchups = new ArrayList<>();
		List<Event> scheduledEvents = new ArrayList<>();
		for(int i=levelEvents.length-1;i>=0;i--) {
			List<TeamPair> toRemove = new ArrayList<>();
			for(TeamPair tp: bestMatchups) {
				for(Event e:levelEvents[i]) {
					if((e instanceof OffDay&&tp.team1==tp.team2) && e.isInvolved(tp.team1)) {
						scheduledMatchups.add(tp);
						scheduledEvents.add(e);
						toRemove.add(tp);
						break;
					}else if((e instanceof Series&&tp.team1!=tp.team2)&& e.isInvolved(tp.team1)&&e.isInvolved(tp.team2)){
						//TODO: Refine prefer home/away
						scheduledMatchups.add(tp);
						scheduledEvents.add(e);
						toRemove.add(tp);
						break;
					}
				}
			}
			bestMatchups.removeAll(toRemove);
		}
		if(scheduledMatchups.size()!=numMatchups||bestMatchups.size()!=0) {
			return false;
		}
		for(Event e:scheduledEvents) {
			System.out.println(e);
			pushToSchedule(e);
		}
		return true;
	}
	
	private List<Team> copyTeamList(List<Team> teamList){
		List<Team> newList = new ArrayList<>();
		for(Team t: teamList) {
			newList.add(t);
		}
		return newList;
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
		priorities.add(Priority.SERIES_EXISTS);
		priorities.add(Priority.DIVISION);
		while(scheduleDay<30) {
			priorities.remove(Priority.SERIES);
			if(!this.weekDay.isRestDay()) {
				priorities.add(2, Priority.SERIES);
			}
			List<Team> toSchedule=getWaiting(scheduleDay);
			if(toSchedule.size()==0) {
				scheduleDay++;
				this.weekDay=this.weekDay.next();
				continue;
			}
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
		priorities.remove(Priority.DIVISION);
		priorities.add(Priority.INTERDIVISION);
		while(scheduleDay<this.allStarBreakStart-(Series.getMaxSeriesLen()+1)) {
			priorities.remove(Priority.SERIES);
			if(!this.weekDay.isRestDay()) {
				priorities.add(2, Priority.SERIES);
			}
			List<Team> toSchedule=getWaiting(scheduleDay);
			if(toSchedule.size()==0) {
				scheduleDay++;
				this.weekDay=this.weekDay.next();
				continue;
			}
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
			}else {
				reset();
				return false;
			}
			
			if(!success) {
				reset();
				return false;
			}
		}
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