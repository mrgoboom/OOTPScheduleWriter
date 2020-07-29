package fileHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import scheduleBuilder.Series;
import scheduleBuilder.Team;

public class SeriesParser {	
	public static final int numFields=5;
	
	public SeriesParser() {}
	public List<Series> parse(BufferedReader input) throws IOException{
		 List<Series> output = new ArrayList<>();
		 String line;
		 int i=1;
		 try{
			 while((line=input.readLine()) != null) {
				 if (line.trim().length()==0 || line.charAt(0)=='#') {
					 //This is a comment or empty. Ignore it.
					 i++;
					 continue;
				 }
				 String[] s = line.split(",");
				 if (s.length!=numFields) {
					 
				 }else {
					 int homeID = Integer.parseInt(s[0]);
					 int awayID = Integer.parseInt(s[1]);
					 int numGames = Integer.parseInt(s[2]);
					 int numDays = Integer.parseInt(s[3]);
					 int repeat = Integer.parseInt(s[4]);
					 Team home = Team.findTeamWithID(homeID);
					 Team away = Team.findTeamWithID(awayID);
					 if(home != null && away != null) {
						 for(int j=0;j<repeat;j++) {
							 Series serie = new Series(home, away, numDays, numGames);
							 output.add(serie);
						 }
					 }else {
						 System.err.println("Incorrect series input at line "+i+":");
						 System.err.println("\t"+line);
					 }
				 }
				 i++;
			 }
		 }catch(Exception e) {
			 System.err.print(e);
			 return null;
		 }
		 System.out.println("Created "+output.size()+" series.");
		 return output;
	}
}
