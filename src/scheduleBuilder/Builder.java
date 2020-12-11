package scheduleBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Builder {
	private final List<List<Team>> divisions;
	private final List<Team> teams;
	public static final int numTeams = 14;
	public static final int totalDays=182;
	public static final int allStarBreakStart=95;
	private final int allStarBreakLen=3;
	private DayOfWeek weekDay;
	private int furthestProgress;
	private double day150Avg;
	private int day150Count;
	
	public Builder(List<List<Team>> structure) {
		this.divisions=structure;
		this.teams=new ArrayList<>();
		for (List<Team> division: structure) {
			for(Team t: division) {
				teams.add(t);
			}
		}
		Collections.shuffle(this.teams);
		this.furthestProgress=-1;
		this.day150Avg = 0;
		this.day150Count = 0;
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
	
	private List<Event> filterPriority(List<Event> input, Priority priority, Team dontRest, int catchup){
		List<Event> output = new ArrayList<>();
		if(priority==Priority.PREFERRED_LENGTH) {
			int[] desiredLength;
			switch(this.weekDay) {
			case MONDAY:
			case THURSDAY:
				desiredLength=new int[] {4};
				break;
			case FRIDAY:
				desiredLength=new int[] {3};
				break;
			case TUESDAY:
			case SATURDAY:
			case WEDNESDAY:
			case SUNDAY:
				desiredLength=new int[] {2};
				break;
			default:
				desiredLength=new int[] {2,3,4};
				break;
			}
			for(Event e:input) {
				if(priority.matchesPriority(e, findDivision(e.homeTeam()), e.homeTeam(), desiredLength)) {
					output.add(e);
				}
			}
		}else if(priority==Priority.LENGTH||priority==Priority.LENGTH_FORCE) {
			int[] desiredLength;
			switch(this.weekDay) {
			case MONDAY:
			case FRIDAY:
				if(priority==Priority.LENGTH) {
					desiredLength=new int[] {3,4};
				}else {
					desiredLength=new int[] {3};
				}
				break;
			case TUESDAY:
			case SATURDAY:
				if(priority==Priority.LENGTH) {
					desiredLength=new int[] {2,3};
				}else {
					desiredLength=new int[] {2};
				}
				break;
			case WEDNESDAY:
			case SUNDAY:
				desiredLength=new int[] {2};
				break;
			case THURSDAY:
				desiredLength=new int[] {4};
				break;
			default:
				desiredLength=new int[] {2,3,4};
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
		}else if(priority==Priority.CATCHUP){
			int[] desiredLength = new int[] {catchup};
			for(Event e:input) {
				if(priority.matchesPriority(e, findDivision(e.homeTeam()), dontRest, desiredLength)) {
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
			if(event.homeTeam()==team&&event instanceof OffDay&&!(team.schedule.getLastEvent() instanceof OffDay)) {
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
	
	private Boolean walkBack(List<Team> scheduleIssue) {
		Boolean success=true;
		int issueDay = Integer.MAX_VALUE;
		for(Team team:scheduleIssue) {
			if(team.schedule.getDaysScheduled()<issueDay) {
				issueDay=team.schedule.getDaysScheduled();
			}
			team.resetLastEvent();
		}
		int scheduleDay=issueDay;
		for(Team team:this.teams) {
			while(team.schedule.getDaysScheduled()>issueDay) {
				team.resetLastEvent();
			}
			if(team.schedule.getDaysScheduled()<scheduleDay) {
				scheduleDay=team.schedule.getDaysScheduled();
			}
		}
		this.weekDay=DayOfWeek.THURSDAY;
		this.weekDay=this.weekDay.advanceDays(scheduleDay);
		
		List<Priority> matchDay = new ArrayList<>();
		matchDay.add(Priority.CATCHUP);
		matchDay.add(Priority.ALERT);
		matchDay.add(Priority.SHOULD_REST);
		while(scheduleDay<issueDay) {
			List<Team> toSchedule = getWaiting(scheduleDay);
			if(toSchedule.size()==0) {
				scheduleDay++;
				this.weekDay=this.weekDay.next();
				continue;
			}
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
				success&=negotiate(toSchedule, possible, copyPriorityList(matchDay),0);
			}else {
				return false;
			}
			if(!success) {
				return false;
			}
		}
		matchDay.remove(Priority.CATCHUP);
		matchDay.add(0,Priority.LENGTH);
		matchDay.add(Priority.SERIES_EXISTS);
		for(;;) {
			List<Team> toSchedule = getWaiting(scheduleDay);
			if(toSchedule.size()==0) {
				break;
			}
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
				success&=negotiate(toSchedule, possible, copyPriorityList(matchDay),0);
			}else {
				return false;
			}
			if(!success) {
				return false;
			}
		}
		
		return success;
	}
	
	private Boolean negotiate(List<Team> toSchedule, List<Event> available, List<Priority> priorities, int forceLength) {	
		if(!missingEvent(toSchedule,available).isEmpty()) {
			return false;
		}
		
		@SuppressWarnings("unchecked")
		List<Event>[] levelEvents = new ArrayList[priorities.size()+1];
		levelEvents[0] = copyEventList(available);
		List<TeamPair> bestMatchups = null;

		List<Team> stillToSchedule = copyTeamList(toSchedule);
		for(int i=0;i<priorities.size();i++) {
			Team dontRest=null;
			if(stillToSchedule.size()==this.teams.size()) {
				dontRest=stillToSchedule.get(0);
			}
			levelEvents[i+1]=filterPriority(levelEvents[i],priorities.get(i),dontRest,0);

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
			//System.out.println(e);
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
			for(int i=0;i<Builder.totalDays-gameDays-this.allStarBreakLen;i++) {
				t.schedule.addOffDay(new OffDay(t,1));
			}
			t.schedule.shuffleAll();
		}
		
		int scheduleDay=0;
		//First month of season
		List<Priority> priorities = new ArrayList<>();
		priorities.add(Priority.LENGTH);
		priorities.add(Priority.ALERT);
		priorities.add(Priority.FRESH_OPPONENT);
		priorities.add(Priority.SHOULD_REST);
		priorities.add(Priority.SERIES_EXISTS);
		priorities.add(Priority.DOUBLEHEADER);
		priorities.add(Priority.DIVISION);
		priorities.add(Priority.PREFERRED_LENGTH);
		while(scheduleDay<30) {
			priorities.remove(Priority.SERIES);
			if(!this.weekDay.isRestDay()) {
				priorities.add(4, Priority.SERIES);
			}
			List<Team> toSchedule=getWaiting(scheduleDay);
			if(toSchedule.size()==0) {
				scheduleDay++;
				this.weekDay=this.weekDay.next();
				continue;
			}
			//System.out.println("There are "+toSchedule.size()+" teams waiting to be scheduled on day "+scheduleDay);
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
				success&=negotiate(toSchedule, possible, copyPriorityList(priorities),0);
			}else {
				success&=walkBack(toSchedule);
				if(!success) {
					reset();
					return false;
				}
			}
			
			if(!success) {
				success|=walkBack(toSchedule);
				if(!success) {
					reset();
					return false;
				}
			}
		}
		//Until 5 days before all-star break
		priorities.remove(Priority.DIVISION);
		priorities.add(7,Priority.INTERDIVISION);
		//priorities.add(Priority.PREFERRED_LENGTH);
		while(scheduleDay<Builder.allStarBreakStart-(Series.getMaxSeriesLen()+1)) {
			priorities.remove(Priority.SERIES);
			if(!this.weekDay.isRestDay()) {
				priorities.add(4, Priority.SERIES);
			}
			List<Team> toSchedule=getWaiting(scheduleDay);
			if(toSchedule.size()==0) {
				scheduleDay++;
				this.weekDay=this.weekDay.next();
				continue;
			}
			//System.out.println("There are "+toSchedule.size()+" teams waiting to be scheduled on day "+scheduleDay);
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
				success&=negotiate(toSchedule, possible, copyPriorityList(priorities),0);
			}else {
				success&=walkBack(toSchedule);
				if(!success) {
					reset();
					return false;
				}
			}
			
			if(!success) {
				success|=walkBack(toSchedule);
				if(!success) {
					reset();
					return false;
				}
			}
		}
		
		//4-5 days before all-star break use this to avoid offday final day
		priorities.remove(Priority.INTERDIVISION);
		priorities.remove(Priority.SHOULD_REST);
		while(scheduleDay<Builder.allStarBreakStart-(Series.getMaxSeriesLen()-1)) {
			priorities.remove(Priority.SERIES);
			if(!this.weekDay.isRestDay()) {
				priorities.add(3, Priority.SERIES);
			}
			List<Team> toSchedule=getWaiting(scheduleDay);
			if(toSchedule.size()==0) {
				scheduleDay++;
				this.weekDay=this.weekDay.next();
				continue;
			}
			//System.out.println("There are "+toSchedule.size()+" teams waiting to be scheduled on day "+scheduleDay);
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
				success&=negotiate(toSchedule, possible, copyPriorityList(priorities),0);
			}else {
				success&=walkBack(toSchedule);
				if(!success) {
					reset();
					return false;
				}
			}
			
			if(!success) {
				success|=walkBack(toSchedule);
				if(!success) {
					reset();
					return false;
				}
			}
		}
		
		//2-3 days before all-star break
		priorities.remove(Priority.LENGTH);
		priorities.remove(Priority.PREFERRED_LENGTH);
		priorities.add(0, Priority.LENGTH_FORCE);
		while(scheduleDay<Builder.allStarBreakStart-1) {
			priorities.remove(Priority.SERIES);
			if(!this.weekDay.isRestDay()) {
				priorities.add(3, Priority.SERIES);
			}
			List<Team> toSchedule=getWaiting(scheduleDay);
			if(toSchedule.size()==0) {
				scheduleDay++;
				this.weekDay=this.weekDay.next();
				continue;
			}
			//System.out.println("There are "+toSchedule.size()+" teams waiting to be scheduled on day "+scheduleDay);
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
				success&=negotiate(toSchedule, possible, copyPriorityList(priorities),0);
			}else {
				success&=walkBack(toSchedule);
				if(!success) {
					reset();
					return false;
				}
			}
			
			if(!success) {
				success|=walkBack(toSchedule);
				if(!success) {
					reset();
					return false;
				}
			}
		}
		
		//day before all-star break (not ideal if any teams actually need scheduling here)
		while(scheduleDay<Builder.allStarBreakStart) {
			List<Team> toSchedule=getWaiting(scheduleDay);
			if(toSchedule.size()==0) {
				scheduleDay++;
				this.weekDay=this.weekDay.next();
				continue;
			}
			//System.out.println("There are "+toSchedule.size()+" teams waiting to be scheduled on day "+scheduleDay);
			for(Team team:toSchedule) {
				Event rest = restDay(team, team.schedule.getEvents());
				if(rest==null) {
					reset();
					return false;
				}
				pushToSchedule(rest);
			}
		}

		if(getWaiting(scheduleDay).size()!=this.teams.size()) {
			reset();
			return false;
		}
		//System.out.println("All star break reached.");
		while(scheduleDay<this.allStarBreakLen+Builder.allStarBreakStart) {
			List<Team> toSchedule=getWaiting(scheduleDay);
			if(toSchedule.size()==0) {
				scheduleDay+=this.allStarBreakLen;
				this.weekDay=this.weekDay.advanceDays(3);
				continue;
			}
			for(Team team:toSchedule) {
				Event allStarBreak = new OffDay(team, 3);
				pushToSchedule(allStarBreak);
			}
		}
		
		
		//To last 30 days of season
		priorities.remove(Priority.LENGTH_FORCE);
		priorities.add(0, Priority.LENGTH);
		priorities.add(3,Priority.SHOULD_REST);
		priorities.add(Priority.INTERDIVISION);
		priorities.add(Priority.PREFERRED_LENGTH);
		while(scheduleDay<(Builder.totalDays)) {
			if(scheduleDay==Builder.totalDays-30) {
				priorities.remove(Priority.INTERDIVISION);
			}
			priorities.remove(Priority.SERIES);
			if(!this.weekDay.isRestDay()) {
				priorities.add(4, Priority.SERIES);
			}
			List<Team> toSchedule=getWaiting(scheduleDay);
			if(toSchedule.size()==0) {
				scheduleDay++;
				this.weekDay=this.weekDay.next();
				continue;
			}
			if(scheduleDay>174) {
				System.out.println("Day "+scheduleDay+"!");
			}
			//System.out.println("There are "+toSchedule.size()+" teams waiting to be scheduled on day "+scheduleDay);
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
				success&=negotiate(toSchedule, possible, copyPriorityList(priorities),0);
			}else {
				success&=walkBack(toSchedule);
				if(!success) {
					if(scheduleDay>=150) {
						double diff = ((double)scheduleDay)-this.day150Avg;
						this.day150Count++;
						this.day150Avg+=diff/((double)this.day150Count);
						if(this.day150Count%250==0) {
							System.out.println("Have failed after day 150 "+this.day150Count+" times. (Average fail day: "+this.day150Avg+").");
						}
					}
					if(scheduleDay>this.furthestProgress) {
						this.furthestProgress=scheduleDay;
						System.out.println("New record! Failed at day "+scheduleDay);
					}
					reset();
					return false;
				}
			}
			
			if(!success) {
				success|=walkBack(toSchedule);
				if(!success) {
					if(scheduleDay>=150) {
						double diff = ((double)scheduleDay)-this.day150Avg;
						this.day150Count++;
						this.day150Avg+=diff/((double)this.day150Count);
						if(this.day150Count%250==0) {
							System.out.println("Have failed after day 150 "+this.day150Count+" times. (Average fail day: "+this.day150Avg+").");
						}
					}
					if(scheduleDay>this.furthestProgress) {
						this.furthestProgress=scheduleDay;
						System.out.println("New record! Failed at day "+scheduleDay);
					}
					reset();
					return false;
				}
			}
		}
		
		System.out.println("Schedule completed successfully.");
		for(Team team: this.teams) {
			team.schedule.scheduleGames();
		}
		
		return success;
	}
}