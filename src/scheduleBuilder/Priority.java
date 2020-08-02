package scheduleBuilder;

import java.time.temporal.ValueRange;
import java.util.List;

public enum Priority implements Cloneable {
	DIVISION,
	INTERDIVISION,
	ALERT,
	SERIES,
	LENGTH,
	LENGTH_FORCE;
		
	public Boolean matchesPriority(Event event, List<Team> division, Team team, ValueRange length) {
		switch(this) {
		case ALERT:
			int alert = team.scheduleAlert();
			if(event instanceof Series) {
				alert |= ((Series)event).getOpponent(team).scheduleAlert();
			}
			switch(alert) {
			case 1:
				return event instanceof OffDay;
			case 2:
				return (event instanceof OffDay)||((Series)event).isHome(team);
			case 3:
				return (event instanceof OffDay)||((Series)event).isAway(team);
			default:
				return true;
			}
		case DIVISION:
			return (event instanceof OffDay)||division.contains(((Series)event).getOpponent(team));
		case INTERDIVISION:
			return (event instanceof OffDay)||!division.contains(((Series)event).getOpponent(team));
		case SERIES:
			return event instanceof Series;
		case LENGTH:
			return (event instanceof OffDay)||length.isValidIntValue(event.length());
		case LENGTH_FORCE:
			return length.isValidIntValue(event.length());
		default:
			return false;
		}
	}
}
