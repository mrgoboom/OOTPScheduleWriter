package scheduleBuilder;

public enum DayOfWeek {
	SUNDAY,
	MONDAY,
	TUESDAY,
	WEDNESDAY,
	THURSDAY,
	FRIDAY,
	SATURDAY;
	
	DayOfWeek(){
	}
	
	public Boolean isRestDay() {
		return this==MONDAY||this==THURSDAY;
	}
	
	public Boolean isWeekend() {
		return this==SUNDAY||this==SATURDAY;
	}
	
	public DayOfWeek next() {
		return values()[(this.ordinal()+1)%values().length];
	}

	public DayOfWeek advanceDays(int n) {
		return values()[(this.ordinal()+n)%values().length];
	}
}
