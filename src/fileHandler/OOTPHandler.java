package fileHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import scheduleBuilder.Builder;
import scheduleBuilder.Event;
import scheduleBuilder.Series;
import scheduleBuilder.Team;

public class OOTPHandler {
	public OOTPHandler() {}
	
	public void writeOOTP(List<List<Team>> divisions, Scanner scanner) {
		Boolean validInput=false;
		Boolean interleague=false;
		while(!validInput) {
			System.out.println("Schedule has interleague? (y/n)");
			String interleagueString = scanner.nextLine();
			String c = interleagueString.substring(0, 1);
			if(c.equalsIgnoreCase("y")) {
				interleague=true;
				validInput=true;
			}else if(c.equalsIgnoreCase("n")) {
				interleague=false;
				validInput=true;
			}else {
				System.out.println("Invalid input.");
			}
		}
		Boolean balancedGames=false;
		validInput=false;
		while(!validInput) {
			System.out.println("Schedule has balanced games? (y/n)");
			String balancedString = scanner.nextLine();
			String c = balancedString.substring(0, 1);
			if(c.equalsIgnoreCase("y")) {
				balancedGames=true;
				validInput=true;
			}else if(c.equalsIgnoreCase("n")) {
				balancedGames=false;
				validInput=true;
			}else {
				System.out.println("Invalid input.");
			}
		}
		System.out.println("Please write a short comment (no spaces)");
		String comment = scanner.nextLine();
		//TODO: Handle possibility of interleague properly. Allow for alternative settings.
		int startMonth=4;
		int startDay=1;
		int startDayOfWeek=5;
		int games=divisions.get(0).get(0).schedule.postScheduleGameCount();
		String filename="IL";
		if(interleague) {
			filename+="N_BG";
		}else {
			filename+="Y_BG";
		}
		if(balancedGames) {
			filename+="Y_G";
		}else {
			filename+="N_G";
		}
		filename+=games+"_SL1_";
		for(int i=1;i<=divisions.size();i++) {
			filename+="D"+i+"_T"+divisions.get(i-1).size()+"_";
		}
		filename+="C_";
		String scheduleLine="<SCHEDULE type=\""+filename+"\" inter_league=\"";
		filename+=comment+".lsdl";
		if(interleague) {
			scheduleLine+="1\" balanced_games=\"";
		}else {
			scheduleLine+="0\" balanced_games=\"";
		}
		if(balancedGames) {
			scheduleLine+="1";
		}else {
			scheduleLine+="0";
		}
		scheduleLine+="\" games_per_team=\""+games+"\" start_month=\""+startMonth+"\" start_day=\""+startDay+"\" start_day_of_week=\""+startDayOfWeek+"\" allstar_game_day=\""+(Builder.allStarBreakStart+1)+"\">\r\n";
		try {
			File file = new File(filename);
			file.createNewFile();
			
			FileWriter writer = new FileWriter(file);
			writer.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n\r\n");
			
			writer.write(scheduleLine+"\r\n<GAMES>\r\n");
			
			for(int i=1;i<=Builder.totalDays;i++) {
				for(List<Team> division:divisions) {
					for(Team team:division) {
						Event event = team.schedule.getEventByDay(i);
						if(event instanceof Series && event.isHome(team)) {
							String gameLine = event.ootpString(i);
							writer.write(gameLine);
						}
					}
				}
			}
			writer.write("</GAMES>\r\n\r\n</SCHEDULE>\r\n");
			
			writer.close();
		}catch(IOException e) {
			System.err.println("File I/O error occurred.");
			e.printStackTrace();
		}
	}
}
