package mg.fishchicken.gamelogic.time;

import java.io.IOException;

import mg.fishchicken.core.i18n.Strings;
import mg.fishchicken.core.util.XMLUtil;
import mg.fishchicken.gamelogic.weather.WeatherProfile;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * Represents a Calendar in the Game World.
 * 
 * 
 * @author Annun
 *
 */
public class GameCalendar extends Object {

	public static final String XML_DAYS = "days";
	public static final String XML_YEARS = "years";
	public static final String XML_START_DATE = "startDate";
	public static final String XML_MONTHS = "months";
	public static final String XML_DAWN_TIME = "dawnTime";
	public static final String XML_DUSK_TIME = "duskTime";
	public static final String XML_SUN_INTENSITY = "sunIntensity";
	
	private int s_hoursInDay, s_daysInWeek, s_weeksInMoth, s_monthsInYear, s_dawnDuskDuration;
	
	private Array<String> dayNames;
	private ObjectMap<Integer, String> yearNames;
	private GameDate startDate;
	private ObjectMap<Integer, Month> months;
	
	public GameCalendar(FileHandle calendarFile) {
		dayNames = new Array<String>();
		yearNames = new ObjectMap<Integer, String>();
		startDate = new GameDate();
		months = new ObjectMap<Integer, Month>();
		
		try {
			loadFromXML(calendarFile);
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	public void loadFromXML(FileHandle calendarFile) throws IOException {
		XmlReader xmlReader = new XmlReader();
		Element root = xmlReader.parse(calendarFile);
		XMLUtil.readPrimitiveMembers(this, root.getChildByName(XMLUtil.XML_PROPERTIES));
		readNames(root.getChildByName(XML_DAYS), dayNames);
		readYearNames(root.getChildByName(XML_YEARS));
		startDate.readFromXML(root.getChildByName(XML_START_DATE));
		readMonthInfos(root.getChildByName(XML_MONTHS));
	}
	
	private void readMonthInfos(Element sunInfosElement) {
		for (int i = 0; i < sunInfosElement.getChildCount(); ++i) {
			Element monthElement = sunInfosElement.getChild(i);
			int month = monthElement.getIntAttribute(XMLUtil.XML_ATTRIBUTE_ID)-1;
			months.put(month, new Month(monthElement));	
		}
	}
	
	private void readNames(Element element, Array<String> nameList) {
		if (element != null) {
			String[] daysStrings = element.getText().split(",");
			for (String dayName : daysStrings) {
				nameList.add(dayName.trim());
			}
		}
	}
	
	private void readYearNames(Element element) {
		if (element != null) {
			for (int i = 0; i < element.getChildCount(); ++i ){
				Element yearElement = element.getChild(i);
				yearNames.put(yearElement.getInt(XMLUtil.XML_ATTRIBUTE_ID), yearElement.getText().trim());
			}
		}
	}
	
	public int getHoursInDay() {
		return s_hoursInDay;
	}
	
	public int getDaysInWeek() {
		return s_daysInWeek;
	}
	
	public int getWeeksInMonth() {
		return s_weeksInMoth;
	}
	
	public int getMonthsInYear() {
		return s_monthsInYear;
	}
	
	public GameDate getStartDate() {
		return startDate;
	}
	
	/**
	 * Returns the number of game time seconds it takes the sun to rise or set
	 * during dawn and dusk.
	 * 
	 * @return
	 */
	public int getDawnDuskDuration() {
		return s_dawnDuskDuration;
	}
	
	/**
	 * 
	 * @param date
	 * @param timeStamp
	 * @deprecated
	 */
	@SuppressWarnings("unused")
	private void setDateFromTimeStamp(GameCalendarDate date, long timeStamp) {
		int second = (int) ((timeStamp+startDate.getSecond()) % 60);
		
		long minutes = (timeStamp / 60)+startDate.getMinute();
		int minute = (int) ((minutes) % 60);
		
		long hours = (minutes / 60)+startDate.getHour();
		int hour = (int) ((hours) % getHoursInDay());
		
		long days = (hours / getHoursInDay())+startDate.getDay();
		int day =  (int) ((days) % (s_daysInWeek*s_weeksInMoth));
		
		long months = (days / s_daysInWeek / s_weeksInMoth)+startDate.getMonth();
		int month = (int) (months) % s_monthsInYear;
		
		int year = (int) (months / s_monthsInYear);
		
		date.setDay(day);
		date.setMonth(month);
		date.setYear(year+startDate.getYear());
		date.setHour(hour);
		date.setMinute(minute);
		date.setSecond(second);
	}
	
	public String getDayName(int day) {
		day = day % s_daysInWeek;
		if (day < dayNames.size) {
			return Strings.getString(dayNames.get(day));
		}
		return Integer.toString(day);
	}
	
	public String getMonthName(int month) {
		if (month < months.size) {
			return months.get(month).getName();
		}
		return Integer.toString(month);
	}
	
	public String getYearName(int year) {
		if (yearNames.containsKey(year)) {
			return Strings.getString(yearNames.get(year));
		}
		return Integer.toString(year);
	}
	
	public Month getMonthInfo(int month) {
		if (!months.containsKey(month)) {
			return null;
		}
		return months.get(month);
	}
	
	protected static class Month {
		private float s_sunIntensity;
		private String s_name = "";
		public GameDate dawnTime, duskTime;
		private WeatherProfile s_weather = null;
		
		public Month(Element monthElement) {
			dawnTime = new GameDate(monthElement.getChildByName(XML_DAWN_TIME));
			duskTime = new GameDate(monthElement.getChildByName(XML_DUSK_TIME));
			XMLUtil.readPrimitiveMembers(this, monthElement);
		}
		
		public String getName() {
			return Strings.getString(s_name);
		}
		
		
		public WeatherProfile getWeatherProfile() {
			return s_weather;
		}
		
		public float getSunIntensity() {
			return s_sunIntensity;
		}
	}
}
