package scheduleBuilder;
import java.util.ArrayList;
import java.util.List;

public class TeamSchedule {
	private List<Series> series;
	private List<Series> homeSeries;
	private List<Series> roadSeries;
	private List<Series> divisionSeries;
	private List<Series> interdivisionSeries;
	private List<Series> teamSchedule;
	private int daysScheduled;
	
	public TeamSchedule() {
		this.series = new ArrayList<>();
		this.homeSeries = new ArrayList<>();
		this.roadSeries = new ArrayList<>();
		this.divisionSeries = new ArrayList<>();
		this.interdivisionSeries = new ArrayList<>();
		this.teamSchedule = new ArrayList<>();
		this.daysScheduled=0;
	}
	
	public void addSeries(Series s, Boolean isHome, Boolean isDivision) {
		this.series.add(s);
		if(isHome) {
			this.homeSeries.add(s);
		}else {
			this.roadSeries.add(s);
		}
		if(isDivision) {
			this.divisionSeries.add(s);
		}else {
			this.interdivisionSeries.add(s);
		}
	}
	
	public void addToSchedule(Series s) {
		this.teamSchedule.add(s);
		this.daysScheduled += s.length;
		
		this.series.remove(s);
		this.homeSeries.remove(s);
		this.roadSeries.remove(s);
		this.interdivisionSeries.remove(s);
		this.divisionSeries.remove(s);
	}
	
	public List<Series> getSchedule(){
		return this.teamSchedule;
	}
	
	public int getDaysScheduled() {
		return this.daysScheduled;
	}
}
