package scheduleBuilder;

public interface Event {
	public int length();
	public int games();
	public Team homeTeam();
	public Boolean coversDay(int day);
	public Boolean isInvolved(Team t);
	public Boolean isHome(Team t);
	public void schedule(int day);
	public String csvString(Team t, int day);
	public String ootpString(int day);
}
