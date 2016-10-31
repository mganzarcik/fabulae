package mg.fishchicken.gamelogic.time;

import java.io.IOException;

import com.badlogic.gdx.utils.XmlWriter;
import com.badlogic.gdx.utils.XmlReader.Element;

public class GameDate implements Comparable<GameDate> {
	private int day, month, year, hour, minute;
	private float second;

	public static final String XML_YEAR = "year";
	public static final String XML_MONTH = "month";
	public static final String XML_DAY = "day";
	public static final String XML_HOUR = "hour";
	public static final String XML_MINUTE = "minute";
	public static final String XML_SECOND = "second";
	
	public GameDate() {
		
	}
	
	public GameDate(Element dateElement) {
		readFromXML(dateElement);
	}
	
	public GameDate(GameDate dateToCopy) {
		setFrom(dateToCopy);
	}
	
	protected void setFrom(GameDate dateToCopy) {
		setDay(dateToCopy.getDay());
		setMonth(dateToCopy.getMonth());
		setYear(dateToCopy.getYear());
		setHour(dateToCopy.getHour());
		setMinute(dateToCopy.getMinute());
		setSecond(dateToCopy.getSecond());
	}
	
	/**
	 * Returns the day in month.
	 * @return
	 */
	public int getDay() {
		return day;
	}
	
	public void setDay(int day) {
		this.day = day;
	}
	
	public void addToDay(int days) {
		setDay(this.day + days);
	}
	
	public int getMonth() {
		return month;
	}
	
	public void setMonth(int month) {
		this.month = month;
	}
	
	public void addToMonth(int months) {
		setMonth(this.month+months);
	}
	
	public int getYear() {
		return year;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public void addToYear(int years) {
		setYear(this.year + years);
	}
	
	public int getHour() {
		return hour;
	}
	
	public void setHour(int hour) {
		this.hour = hour;
	}
	
	public void addToHour(int hours) {
		setHour(this.hour+hours);
	}
	
	public int getMinute() {
		return minute;
	}
	
	public void setMinute(int minute) {
		this.minute = minute;
	}
	
	public void addToMinute(int minutes) {
		setMinute(this.minute+minutes);
	}
	
	public float getSecond() {
		return second;
	}
	
	public void setSecond(float second) {
		this.second = second;
	}
	
	public void addToSecond(float seconds) {
		setSecond(this.second+seconds);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof GameDate) {
			GameDate other = (GameDate) obj;
			if (getYear() == other.getYear()
					&& getMonth() == other.getMonth()
					&& getDay() == other.getDay()
					&& getHour() == other.getHour()
					&& getMinute() == other.getMinute()
					&& getSecond() == other.getSecond()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (""+getYear()+getMonth()+getDay()+getHour()+getMinute()+getSecond()).hashCode();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(GameDate dateToCompare) {
		if (this.equals(dateToCompare)) {
			return 0;
		}
		
		if (getYear() > dateToCompare.getYear()) {
			return 1;
		} else if (getYear() < dateToCompare.getYear()) {
			return -1;
		}
		
		if (getMonth() > dateToCompare.getMonth()) {
			return 1;
		} else if (getMonth() < dateToCompare.getMonth()) {
			return -1;
		}
		
		if (getDay() > dateToCompare.getDay()) {
			return 1;
		} else if (getDay() < dateToCompare.getDay()) {
			return -1;
		}
		
		if (getHour() > dateToCompare.getHour()) {
			return 1;
		} else if (getHour() < dateToCompare.getHour()) {
			return -1;
		}
		
		if (getMinute() > dateToCompare.getMinute()) {
			return 1;
		} else if (getMinute() < dateToCompare.getMinute()) {
			return -1;
		}
		
		if (getSecond() > dateToCompare.getSecond()) {
			return 1;
		} 

		return -1;
	}

	/**
	 * Compares this date to the supplied date, only comparing time information
	 * (hours, minutes, seconds) and ignoring date information (years, months,
	 * days). Returns 1 if the time of of this date is larger than the supplied
	 * time, 0 if they are equal and -1 if it is smaller.
	 * 
	 * @param hour
	 * @param minute
	 * @param second
	 * @return
	 */
	public int compareTimeOnly(GameDate date) {
		return compareTimeOnly(date.getHour(), date.getMinute(),
				date.getSecond());
	}

	/**
	 * Compares this date to the supplied time. Returns 1 if the time of 
	 * of this date is larger than the supplied time, 0 if they 
	 * are equal and -1 if it is smaller.
	 * 
	 * @param hour
	 * @param minute
	 * @param second
	 * @return
	 */
	public int compareTimeOnly(int hour, int minute, float second) {
		if (getHour() == hour
				&& getMinute() == minute
				&& getSecond() == second) {
			return 0;
		}
		
		if (getHour() > hour) {
			return 1;
		} else if (getHour() < hour) {
			return -1;
		}
		
		if (getMinute() > minute) {
			return 1;
		} else if (getMinute() < minute) {
			return -1;
		}
		
		if (getSecond() > second) {
			return 1;
		} 

		return -1;
	}
	
	/**
	 * Substracts the time of the supplied date from the time of this date and
	 * returns the resulting number of seconds.
	 * 
	 * This only works with time information (hours, minutes, seconds) and
	 * ignores date information (years, months, days). It also
	 * ignores fraction seconds.
	 * 
	 * @param dateToSubstract
	 * @return
	 */
	public int substractTimeOnly(GameDate dateToSubstract) {
		return (int) ((getHour() - dateToSubstract.getHour()) * 3600
				+ (getMinute() - dateToSubstract.getMinute()) * 60
				+ (getSecond() - dateToSubstract.getSecond()));
	}
	
	public void writeToXML(XmlWriter writer) throws IOException {
		writer.attribute(XML_DAY, getDay()+1);
		writer.attribute(XML_MONTH, getMonth()+1);
		writer.attribute(XML_YEAR, getYear());
		writer.attribute(XML_HOUR, getHour());
		writer.attribute(XML_MINUTE, getMinute());
		writer.attribute(XML_SECOND, getSecond());
	}
	
	public void readFromXML(Element dateElement) {
		if (dateElement == null) {
			return;
		}
	
		setDay(dateElement.getInt(XML_DAY, 1)-1);
		setMonth(dateElement.getInt(XML_MONTH, 1)-1);
		setYear(dateElement.getInt(XML_YEAR, 0));
		setHour(dateElement.getInt(XML_HOUR, 0));
		setMinute(dateElement.getInt(XML_MINUTE, 0));
		setSecond(dateElement.getFloat(XML_SECOND, 0));
	}
	
}
