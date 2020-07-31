import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import fileHandler.SeriesParser;
import scheduleBuilder.Series;
import scheduleBuilder.Team;

public class OOTPScheduleWriter {

	private static void printSeries(List<Series> seriesList) {
		for(Series s : seriesList) {
			System.out.println(s.toString());
		}
	}
	
	private static List<Series> loadSeries(Scanner scanner) {
		List<Series> seriesList=null;
		System.out.println("Enter file to load series from: ");
		String filename = scanner.nextLine();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			SeriesParser parser = new SeriesParser();
			seriesList=parser.parse(br);
			br.close();
			
		}catch(Exception e) {
			System.err.println(e);
		}
		return seriesList;
	}
	
	private static Boolean assignSeries(List<Series> seriesList,List<List<Team>> divisions) {
		for(Series s:seriesList) {
			List<Team> homeDivision=null;
			List<Team> awayDivision=null;;
			for(List<Team> division:divisions) {
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
	
	private static List<Team> createDivision(int size){
		List<Team> division = new ArrayList<>();
		for(int i=0;i<size;i++) {
			division.add(new Team());
		}
		return division;
	}
	private static Boolean checkBalance(List<Team> teams) {
		Boolean balanced = true;
		int minGames = teams.get(0).schedule.preScheduleGameCount();
		int maxGames = minGames;
		for(Team t : teams) {
			String error = t.areSeriesBalanced();
			if(error != null) {
				System.out.println(error);
				balanced=false;
			}
			int numGames = t.schedule.preScheduleGameCount();
			if(numGames<minGames) {
				minGames = numGames;
			}else if(numGames>maxGames) {
				maxGames = numGames;
			}
		}
		if(minGames!=maxGames) {
			System.out.println("Error: Teams do not play same number of games.\r\nNumber of games played varies from "+minGames+" to "+maxGames+".");
			balanced=false;
		}else {
			System.out.println("All teams in division play "+minGames+" games.");
		}
		return balanced;
	}
	
	public static void main(String[] args) {
		int eastLen=5;
		int centralLen=5;
		int westLen=4;
		List<List<Team>> divisions = new ArrayList<>();
		List<Team> east = createDivision(eastLen);
		List<Team> central = createDivision(centralLen);
		List<Team> west = createDivision(westLen);
		divisions.add(east);
		divisions.add(central);
		divisions.add(west);
		
		//Create Series
		Scanner scanner = new Scanner(System.in);
		List<Series> allSeries;
		allSeries=loadSeries(scanner);
		if(allSeries==null) {
			allSeries = new ArrayList<>();
		}else {
			if(!assignSeries(allSeries,divisions)) {
				allSeries = new ArrayList<>();
			}
		}
		for(;;) {
			System.out.println("Please enter command (print/load/clear/check/help/quit): ");
			String command = scanner.nextLine();
			if(command.equals("print")) {
				printSeries(allSeries);
			}else if(command.equals("load")) {
				List<Series> newSeries;
				if((newSeries=loadSeries(scanner))!=null) {
					if(assignSeries(newSeries,divisions)) {
						allSeries.addAll(newSeries);
					}
				}
			}else if(command.equals("clear")){
				for(List<Team> division: divisions) {
					for(Team t:division) {
						t.schedule.clear();
					}
				}
				allSeries=new ArrayList<>();
			}else if(command.equals("check")) {
				Boolean allPass=true;
				if(checkBalance(east)) {
					System.out.println("East Division gamesets are balanced.");
				}else {
					allPass=false;
				}
				if(checkBalance(central)) {
					System.out.println("Central Division gamesets are balanced.");
				}else {
					allPass=false;
				}
				if(checkBalance(west)) {
					System.out.println("West Division gamesets are balanced.");
				}else {
					allPass=false;
				}
				if(allPass) {
					System.out.println("All gamesets are balanced.");
				}
			}else if(command.equals("help")) {
				System.out.println("print");
				System.out.println("\tPrints out all scheduled series in a verbose but readable format.\r\n");
				System.out.println("load");
				System.out.println("\tLoad more series from another file.\r\n\tLoading the same file will add a second copy of the series to the schedule.\r\n");
				System.out.println("clear");
				System.out.println("\tClears all loaded series.\r\n");
				System.out.println("check");
				System.out.println("\tChecks if each team plays a balanced schedule.\r\n");
				System.out.println("quit");
				System.out.println("\tExits the program.\r\n");
			}else if(command.equals("quit")) {
				scanner.close();
				return;
			}else {
				System.out.println("Please enter valid command.");
			}
		}
		//scanner.close();
	}

}
