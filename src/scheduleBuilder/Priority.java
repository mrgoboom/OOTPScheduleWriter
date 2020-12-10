package scheduleBuilder;

import java.util.List;

public enum Priority implements Cloneable {
	DIVISION,
	INTERDIVISION,
	ALERT,
	CATCHUP,
	SERIES_EXISTS,
	SERIES,
	LENGTH,
	PREFERRED_LENGTH,
	SHOULD_REST,
	LENGTH_FORCE;
		
	public Boolean matchesPriority(Event event, List<Team> division, Team team, int[] length) {
		switch(this) {
		case ALERT:
			int alert = team.scheduleAlert();
			int opponentAlert=0;
			if(event instanceof Series) {
				opponentAlert = ((Series)event).getOpponent(team).scheduleAlert();
			}
			if((alert|opponentAlert)%2==1) {
				return event instanceof OffDay;
			}
			boolean retBool=true;
			if((alert>>1)%2==1||(opponentAlert>>2)%2==1) {
				retBool&=(event instanceof OffDay)||((Series)event).isHome(team);
			}else if((alert>>2)%2==1||(opponentAlert>>1)%2==1) {
				retBool&=(event instanceof OffDay)||((Series)event).isAway(team);
			}
			if(((alert>>3)|(opponentAlert>>3))%2==1) {
				retBool&=(event instanceof OffDay)||!((Series)event).hasDoubleHeader();
			}
			return retBool;
		case CATCHUP:
			for(int len:length) {
				if(event.length()==len) {
					return true;
				}
			}
			return false;
		case DIVISION:
			return (event instanceof OffDay)||division.contains(((Series)event).getOpponent(team));
		case INTERDIVISION:
			return (event instanceof OffDay)||!division.contains(((Series)event).getOpponent(team));
		case SERIES_EXISTS:
			return team==null||(!event.isInvolved(team))||event instanceof Series;
		case SERIES:
			return event instanceof Series;
		case PREFERRED_LENGTH:
		case LENGTH:
			Boolean lengthAcceptable=false;
			for(int len:length) {
				if(event.length()==len) {
					lengthAcceptable=true;
				}
			}
			return (event instanceof OffDay)||lengthAcceptable;
		case LENGTH_FORCE:
			for(int len:length) {
				if(event.length()==len) {
					return true;
				}
			}
			return false;
		case SHOULD_REST:
			double restTarget = ((double)Builder.totalDays)/23.0;
			double restValue = ((double)(Builder.totalDays-team.schedule.getDaysScheduled()))/(double)team.restDays;
			if(event instanceof OffDay) {
				return restValue<(restTarget*1.1);
			}else {
				return restValue>(restTarget*0.9);
			}
		default:
			return false;
		}
	}
}
