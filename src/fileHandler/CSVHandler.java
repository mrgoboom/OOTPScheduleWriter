package fileHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import scheduleBuilder.Builder;
import scheduleBuilder.Event;
import scheduleBuilder.Team;

public class CSVHandler {
	public CSVHandler() {}
	
	public void writeCSV(List<Team> teams, String filename) {
		try {
			File file = new File(filename);
			file.createNewFile();
			
			FileWriter writer = new FileWriter(file);
			Boolean first=true;
			for(Team team:teams) {
				if(!first) {
					writer.write(",");
				}else {
					first=false;
				}
				writer.write("Team "+team.id);
			}
			writer.write("\r\n");
			for(int i=1;i<=Builder.totalDays;i++) {
				first=true;
				for(Team team:teams) {
					if(!first) {
						writer.write(",");
					}else {
						first=false;
					}
					Event event = team.schedule.getEventByDay(i);
					writer.write(event.csvString(team, i));
				}
				writer.write("\r\n");
			}
			writer.close();
		}catch(IOException e) {
			System.err.println("File I/O error occurred.");
			e.printStackTrace();
		}
	}
}
