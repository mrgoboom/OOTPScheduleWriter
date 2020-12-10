package scheduleBuilder;

import java.util.Random;

public class Series implements Event{
	private static int MaxSeriesLen=0;
	private final Team homeTeam;
	private final Team awayTeam;
	private final int length;
	private final int games;
	private int startDay;
	private int[] gamesOnDay;
	
	public Series(Team home, Team away, int numDays, int numGames) {
		this.length=numDays;
		if (numDays > Series.MaxSeriesLen) {
			Series.MaxSeriesLen = numDays;
		}
		this.homeTeam=home;
		this.awayTeam=away;
		this.games=numGames;
		this.startDay=-1;
		this.gamesOnDay=new int[this.length];
		for(int i=0;i<this.length;i++) {
			this.gamesOnDay[i]=0;
		}
	}
	
	/*
	 * This series is a break
	 */
	public Series(Team team, int numDays) {
		this.homeTeam=team;
		this.length=numDays;
		this.games=0;
		this.awayTeam=null;
	}
	
	public Boolean isHome(Team team) {
		return team==this.homeTeam;
	}
	
	public Team homeTeam() {
		return this.homeTeam;
	}
	
	public Team awayTeam() {
		return this.awayTeam;
	}
	
	public Boolean isAway(Team team) {
		return team==this.awayTeam;
	}
	
	public Boolean isInvolved(Team t) {
		return this.isAway(t)||this.isHome(t);
	}
	
	public Team getOpponent(Team team) {
		if (this.isHome(team)) {
			return this.awayTeam;
		}else if(this.isAway(team)) {
			return this.homeTeam;
		}
		return null;
	}
	
	public String toString() {
		return "Team "+this.homeTeam.id+" hosts team "+this.awayTeam.id+" for "+this.games+" games in "+this.length+" days.";
	}
	
	public int length() {
		return this.length;
	}
	
	public int games() {
		return this.games;
	}
	
	public boolean hasDoubleHeader() {
		return this.length!=this.games;
	}
	
	public static int getMaxSeriesLen() {
		return Series.MaxSeriesLen;
	}
	
	public void schedule(int day) {
		this.startDay=day;
		int minGames=this.games/this.length;
		for(int i=0;i<this.length;i++) {
			this.gamesOnDay[i]=minGames;
		}
		if(this.games%this.length!=0) {
			Random generator = new Random();
			for(int assigned=0;assigned<this.games%this.length;assigned++) {
				int rand = Math.abs(generator.nextInt())%(this.length-assigned);
				int j=0;
				int index=0;
				while(j<rand) {
					if(this.gamesOnDay[index]==minGames) {
						j++;
					}
					index++;
				}
				this.gamesOnDay[index]++;
			}
		}
	}
	
	public Boolean coversDay(int day) {
		return this.startDay<=day&&this.startDay+this.length>day;
	}
	
	public String csvString(Team t, int day) {
		String csvString;
		if(this.isHome(t)) {
			csvString=t.id+"v"+this.awayTeam.id;
		}else {
			csvString=t.id+"a"+this.homeTeam.id;
		}
		int gameDay=day-this.startDay;
		if(this.gamesOnDay[gameDay]>1) {
			csvString+="x"+this.gamesOnDay[gameDay];
		}else if(this.gamesOnDay[gameDay]==0) {
			return "Off";
		}
		return csvString;
	}
	
	public String ootpString(int day, Boolean duplicate) {
		DayOfWeek weekDay = DayOfWeek.THURSDAY;
		weekDay = weekDay.advanceDays(day-1);
		int gameDay=day-this.startDay;
		if(this.gamesOnDay[gameDay]==1) {
			Random generator = new Random();
			int startTime=1905;
			if(weekDay.isWeekend()) {
				if(generator.nextBoolean()){
					startTime-=300;
				}
			}
			if(gameDay==this.length-1) {
				if(Math.abs(generator.nextInt())%4!=0) {
					startTime-=600;
				}
			}else {
				if(Math.abs(generator.nextInt())%10==0) {
					startTime-=300;
				}
			}
			if(startTime<1305) {
				startTime=1305;
			}
			String games="<Game day=\""+day+"\" time=\""+startTime+"\" away=\""+this.awayTeam.id+"\" home=\""+this.homeTeam.id+"\" />\r\n";
			if(duplicate) {
				games+="<Game day=\""+day+"\" time=\""+startTime+"\" away=\""+(this.awayTeam.id+14)+"\" home=\""+(this.homeTeam.id+14)+"\" />\r\n";
			}
			return games;
		}else if(this.gamesOnDay[gameDay]==0) {
			return "";
		}else {
			Random generator = new Random();
			int startTime=1535;
			int threshHold=2;
			if(weekDay.isWeekend()) {
				threshHold+=18;
			}
			if(gameDay!=this.length-1) {
				threshHold+=4;
			}
			if(Math.abs(generator.nextInt())%24<threshHold) {
				startTime-=300;
			}
			String games="";
			for(int i=0;i<gamesOnDay[gameDay];i++) {
				games+="<Game day=\""+day+"\" time=\""+startTime+"\" away=\""+this.awayTeam.id+"\" home=\""+this.homeTeam.id+"\" />\r\n";
				if(duplicate) {
					games+="<Game day=\""+day+"\" time=\""+startTime+"\" away=\""+(this.awayTeam.id+14)+"\" home=\""+(this.homeTeam.id+14)+"\" />\r\n";
				}
				startTime+=370;
			}
			return games;
		}
	}
}
