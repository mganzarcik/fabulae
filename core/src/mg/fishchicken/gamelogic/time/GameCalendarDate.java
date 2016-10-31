package mg.fishchicken.gamelogic.time;

import mg.fishchicken.core.util.CoreUtil;
import mg.fishchicken.gamelogic.time.GameCalendar.Month;
import mg.fishchicken.gamelogic.weather.WeatherProfile;

/**
 * 
 * Represents a date in the game world.
 * 
 * All dates have 60 seconds in a minute, 60 minutes in an hour.
 * 
 * Everything else depends on the calendar (hours in a day,
 * days in a month, months in a year).
 * 
 * @author Annun
 *
 */
public class GameCalendarDate extends GameDate {

	private GameCalendar calendar;
	
	public GameCalendarDate(GameCalendarDate dateToCopy) {
		this.calendar = dateToCopy.getCalendar();
		setFrom(dateToCopy);
	}
	
	public GameCalendarDate(GameCalendar calendar) {
		this.calendar = calendar;
		setFrom(calendar.getStartDate());
	}
	
	public void setDay(int day) {
		super.setDay(day);
		if (getDay() > calendar.getDaysInWeek()) {
			int monhts = getDay() / calendar.getDaysInWeek();
			super.setDay(getDay() % calendar.getDaysInWeek());
			addToMonth(monhts);
		}
	}
	
	public void setMonth(int month) {
		super.setMonth(month);
		if (getMonth() > calendar.getMonthsInYear()) {
			int years = getMonth() / calendar.getMonthsInYear();
			super.setMonth(getMonth() % calendar.getMonthsInYear());
			addToYear(years);
		}
	}
	
	@Override
	public void setHour(int hour) {
		super.setHour(hour);
		if (getHour() >= 24) {
			int days = getHour() / 24;
			super.setHour(getHour() % 24);
			addToDay(days);
		}
	}
	
	@Override
	public void setMinute(int minute) {
		super.setMinute(minute);
		if (getMinute() >= 60) {
			int hours = getMinute() / 60;
			super.setMinute(getMinute() % 60);
			addToHour(hours);
		}
	}
	
	@Override
	public void setSecond(float second) {
		super.setSecond(second);
		if (getSecond() >= 60) {
			int minutes = (int) (getSecond() / 60);
			super.setSecond(getSecond() % 60);
			addToMinute(minutes);
		}
	}
	
	public GameCalendar getCalendar() {
		return calendar;
	}
	
	protected Month getMonthInfo() {
		return calendar.getMonthInfo(getMonth());
	}
	
	public WeatherProfile getWeatherProfile() {
		return calendar.getMonthInfo(getMonth()).getWeatherProfile();
	}
	
	/**
	 * Returns true if it is currently night according to 
	 * dawn and dusk times for the current month and time of day.
	 * @return
	 */
	public boolean isNight() {
		Month currentMonth = calendar.getMonthInfo(getMonth());
		int distanceToDawn = substractTimeOnly(currentMonth.dawnTime);
		int distanceToDusk = substractTimeOnly(currentMonth.duskTime);
		
		return !(distanceToDawn > 0 && distanceToDusk <= 0);
		//return currentMonth.dawnTime.compareTimeOnly(this) > 0 && currentMonth.duskTime.compareTimeOnly(this) < 0;
	}
	
	public String toString() {
		return toString(true);
	}
	
	
	public String toString(boolean monthName) {
		int intSeconts = (int)getSecond();
		String seconds = intSeconts > 9 ? Integer.toString(intSeconts) : "0"+intSeconts;
		return toStringNoTime(monthName)+"; "+ getHour()+":"+getMinute()+":"+seconds;
	}
	
	public String toStringNoTime() {
		return toStringNoTime(true);
	}
	
	public String toStringNoTime(boolean monthName) {
		if (calendar != null) {
			return (monthName ? calendar.getDayName(getDay()) + ", " : "") + (getDay()+1) +". "+calendar.getMonthName(getMonth()) + " "+getYear();
		}
		return (getDay()+1) +"-"+(getMonth()+1) + "-"+getYear();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof GameCalendarDate
				&& super.equals(obj)
				&& CoreUtil.equals(this.calendar,
						((GameCalendarDate) obj).calendar);
	}
	
	@Override
	public int hashCode() {
		return (""+getYear()+getMonth()+getDay()+getHour()+getMinute()+getSecond()+calendar.hashCode()).hashCode();
	}
}
