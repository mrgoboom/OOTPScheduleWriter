package scheduleBuilder;

public interface Event {
	public int length();
	public int games();
	public Team homeTeam();
	public Boolean isInvolved(Team t);
}
