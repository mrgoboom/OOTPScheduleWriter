package scheduleBuilder;

import java.util.ArrayList;
import java.util.Collections;
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
			this.count=0.087;
		}
	}
	
	public static TeamPair createNew(Team t1, Team t2) {
		TeamPair test = new TeamPair(t1,t2);
		for(TeamPair tp:TeamPair.thePairs) {
			if(test.equals(tp)) {
				if(tp.team1==tp.team2) {
					tp.count+=0.087;
				}else {
					tp.count+=1;
				}
				return tp;
			}
		}
		TeamPair.thePairs.add(test);
		return test;
	}
	
	public static TeamPair findPair(Team t1, Team t2) {
		for(TeamPair tp:thePairs) {
			if(tp.contains(t1)&&tp.contains(t2)) {
				return tp;
			}
		}
		return null;
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
	
	private static TeamPair mostFrequent(List<TeamPair> available) {
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
	
	private static List<TeamPair> copyTPList(List<TeamPair> original){
		List<TeamPair> newList = new ArrayList<>();
		for(TeamPair tp:original) {
			newList.add(tp);
		}
		return newList;
	}
	
	public static List<TeamPair> getUniqueMatchups(List<TeamPair> available, List<Team> toMatch, List<TeamPair> oldPairs){
		Boolean success=false;
		int attempts=0;
		List<TeamPair> newPairs=null;
		while(!success&&attempts<16) {
			newPairs = new ArrayList<>();
			List<Team> stillToMatch = copyList(toMatch);
			Collections.shuffle(stillToMatch);
			List<TeamPair> stillAvailable = copyTPList(available);
			outer: while(stillToMatch.size()>0) {
				Team fewestMatches=stillToMatch.get(0);
				int fewest = Integer.MAX_VALUE;
				for(Team t:stillToMatch) {
					int matches=0;
					for(TeamPair tp:stillAvailable) {
						if(tp.contains(t)) {
							matches++;
						}
					}
					if(matches<fewest) {
						fewest=matches;
						fewestMatches=t;
					}
				}
				List<Team> opponents=new ArrayList<>();
				List<TeamPair> includesTeam=new ArrayList<>();
				List<TeamPair> excludesTeam=new ArrayList<>();
				for(TeamPair tp:stillAvailable) {
					if(tp.contains(fewestMatches)) {
						includesTeam.add(tp);
						opponents.add(tp.other(fewestMatches));
					}else {
						excludesTeam.add(tp);
					}
				}
				//Team opponent = Team.findTeamWithID(fewestMatches.schedule.mostSeriesRemaining(opponents));
				//TeamPair thePair=TeamPair.findPair(fewestMatches, opponent);
				TeamPair thePair=mostFrequent(includesTeam);
				if(fewest==0||thePair==null) {
					boolean foundPair=false;
					if(oldPairs!=null) {
						for(TeamPair op: oldPairs) {
							if(op.contains(fewestMatches)) {
								for(TeamPair np:newPairs) {
									if(np.contains(op.other(fewestMatches))) {
										break outer;
									}
								}
								thePair=op;
								foundPair=true;
								break;
							}
						}
					}
					if(!foundPair) {
						break outer;
					}
				}
				stillToMatch.remove(thePair.team1);
				stillToMatch.remove(thePair.team2);
				newPairs.add(thePair);
				for(TeamPair tp:excludesTeam) {
					if(tp.contains(thePair.other(fewestMatches))) {
						includesTeam.add(tp);
					}
				}
				stillAvailable.removeAll(includesTeam);
			}
			if(stillToMatch.size()==0) {
				success=true;
			}else {
				attempts++;
			}
		}
		if(success) {
			return newPairs;
		}else {
			return null;
		}
	}
}
