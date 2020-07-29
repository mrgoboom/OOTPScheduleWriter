import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import fileHandler.SeriesParser;
import scheduleBuilder.Series;
import scheduleBuilder.Team;

public class OOTPScheduleWriter {

	public static void main(String[] args) {
		List<Team> div1 = new ArrayList<>();
		int div1len=5;
		int i=1;
		int offset=i;
		// Create Teams
		for(;i<div1len+offset;i++) {
			Team t = new Team(i);
			div1.add(t);
		}
		System.out.println("Created division with "+div1len + " teams.");
		
		//Create Series
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter file to load series from: ");
		String filename = scanner.nextLine();
		scanner.close();
		List<Series> allSeries;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			SeriesParser parser = new SeriesParser();
			allSeries=parser.parse(br);
			br.close();
			for(Series s : allSeries) {
				System.out.println(s.toString());
			}
		}catch(Exception e) {
			System.err.println(e);
		}
	}

}
