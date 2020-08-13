package scheduleBuilder;

import java.util.ArrayList;
import java.util.List;

public class TeamPair {
	public final Team team1;
	public final Team team2;
	public double count;
	private static List<TeamPair> thePairs=new ArrayList<>();
	
	private TeamPair(Team t1, Team t2) {
		this.team1=t1;
		this.team2=t2;
		if(t1!=t2) {
			this.count=1;
		}else {
			this.count=0.3;
		}
	}
	
	public static TeamPair createNew(Team t1, Team t2) {
		TeamPair test = new TeamPair(t1,t2);
		for(TeamPair tp:TeamPair.thePairs) {
			if(test.equals(tp)) {
				if(tp.team1==tp.team2) {
					tp.count+=0.3;
				}else {
					tp.count+=1;
				}
				return tp;
			}
		}
		TeamPair.thePairs.add(test);
		return test;
	}
	
	public static void clearPairs() {
		TeamPair.thePairs = new ArrayList<>();
	}
	
	public Boolean contains(Team team) {
		return this.team1==team||this.team2==team;
	}
	
	public Team other(Team team) {
		if(team==this.team1) {
			return this.team2;
		}else if(team==this.team2) {
			return this.team1;
		}else {
			return null;
		}
	}
	
	public Boolean equals(TeamPair other) {
		return (this.team1==other.team1&&this.team2==other.team2)||(this.team1==other.team2&&this.team2==other.team1);
	}
	
	public String toString() {
		return "("+this.team1.toString()+","+this.team2.toString()+") x"+this.count;
	}
	
	private static TeamPair mostFrequent(List<TeamPair> available, Team team) {
		if(available==null||available.size()==0) {
			return null;
		}
		TeamPair mostFrequent = available.get(0);
		for(TeamPair tp:available) {
			if(tp.count>mostFrequent.count) {
				mostFrequent=tp;
			}
		}
		return mostFrequent;
	}
	
	private static List<Team> copyList(List<Team> original){
		List<Team> newList = new ArrayList<>();
		for(Team t:original) {
			newList.add(t);
		}
		return newList;
	}
	
	public static List<TeamPair> getUniqueMatchups(List<TeamPair> available, List<Team> toMatch){
		if(toMatch.size()==0) {
			return new ArrayList<>();
		}else if(toMatch.size()==1) {
			TeamPair tp=TeamPair.mostFrequent(available, toMatch.get(0));
			if(tp!=null) {
				List<TeamPair> tpList = new ArrayList<>();
				tpList.add(tp);
				return tpList;
			}else {
				return null;
			}
		}else {
			Team tryPair=toMatch.get(0);
			int fewest=Integer.MAX_VALUE;
			for(Team t:toMatch) {
				int matches=0;
				for(TeamPair tp:available) {
					if(tp.contains(t)) {
						matches++;
					}
				}
				if(matches<fewest) {
					fewest=matches;
					tryPair=t;
				}
			}
			List<TeamPair> includesTeam=new ArrayList<>();
			List<TeamPair> excludesTeam=new ArrayList<>();
			for(TeamPair tp:available) {
				if(tp.contains(tryPair)) {
					includesTeam.add(tp);
				}else {
					excludesTeam.add(tp);
				}
			}
			TeamPair thePair=mostFrequent(includesTeam, tryPair);
			if(thePair==null) {
				return null;
			}
			List<Team> remaining = copyList(toMatch);
			remaining.remove(tryPair);
			if(thePair.team1!=thePair.team2) {
				remaining.remove(thePair.other(tryPair));
				List<TeamPair> toRemove = new ArrayList<>();
				for(TeamPair tp: excludesTeam) {
					if(tp.contains(thePair.other(tryPair))) {
						toRemove.add(tp);
					}
				}
				excludesTeam.removeAll(toRemove);
			}
			List<TeamPair> otherPairs = getUniqueMatchups(excludesTeam, remaining);
			if(otherPairs!=null) {
				otherPairs.add(thePair);
				return otherPairs;
			}else {
				return null;
			}
		}
	}
}
