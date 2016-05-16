package com.sap.inspection.tools;

import android.content.Context;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateTools {

	public static String getCurrentDate(){
		Date currentDate = Calendar.getInstance().getTime();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault());
		return simpleDateFormat.format(currentDate);
	}

	//	public final static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
	public final static SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

	/**
	 * Helper class for handling ISO 8601 strings of the following format:
	 * "2008-03-01T13:00:00+01:00". It also supports parsing the "Z" timezone.
	 */
	/** Transform Calendar to ISO 8601 string. */
	public static String fromCalendar(final Calendar calendar) {
		Date date = calendar.getTime();
		String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
		.format(date);
		return formatted.substring(0, 22) + ":" + formatted.substring(22);
	}

	/** Get current date and time formatted as ISO 8601 string. */
	public static String now() {
		return fromCalendar(GregorianCalendar.getInstance());
	}

	/** Transform ISO 8601 string to Calendar. */
	public static Calendar toCalendar(final String iso8601string)
			throws ParseException {
		Calendar calendar = GregorianCalendar.getInstance();
		String s = iso8601string.replace("Z", "+07:00");
		try {
			s = s.substring(0, 22) + s.substring(23);
		} catch (IndexOutOfBoundsException e) {
			throw new ParseException("Invalid length", 0);
		}
		Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
		calendar.setTime(date);
		return calendar;
	}

	public static String timeElapse(Context context, Calendar timeToCheck){
		return (String) DateUtils.getRelativeDateTimeString(context, timeToCheck.getTimeInMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_NO_NOON, 0);
	}

	public static String timeElapse(Calendar timeToCheck){
		if (timeToCheck == null)
			return null;
//		if (timeToCheck != null)
//		return sdf.format(timeToCheck.getTime());
		//		String timeElapse = sdf.format(timeToCheck.getTime());
		//		timeElapse = timeToCheck.getTime().toLocaleString();
		//		return timeElapse;
		Calendar c = Calendar.getInstance();
		long elapsed = 60*1000;
		long param;
		long timeElapsed = c.getTimeInMillis() - timeToCheck.getTimeInMillis();
		// less than a minute
		if (timeElapsed <= elapsed)
			return "a moment ago";
		// less than an hour
		param = elapsed;
		elapsed *= 60;
		if (timeElapsed <= elapsed){
			if (timeElapsed/param > 1)
				return ((timeElapsed)/param) + " minutes ago";
			else return "a minutes ago";
		}
		// less than a day
		param = elapsed;
		elapsed *= 24;
		if (timeElapsed <= elapsed){
			DebugLog.d("elapsed : "+timeElapsed);
			if (timeElapsed/param > 1)
				return ((timeElapsed)/param) + " hours ago";
			else return "an hour ago";
		}
		// less than 7 day
		param = elapsed;
		elapsed *= 7;
		if (timeElapsed <= elapsed){
			if (timeElapsed/param > 1)
				return ((timeElapsed)/param) + " days ago";
			else return "yesterday";
		}
		// less than a month
		param = elapsed;
		elapsed = elapsed * 30 / 7;
		if (timeElapsed <= elapsed){
			if (timeElapsed/param > 1)
				return ((timeElapsed)/param) + " weeks ago";
			else return "a week ago";
		}
		// less than a year
		param = elapsed;
		elapsed *= 12;
		if (timeElapsed <= elapsed){
			if (timeElapsed/param > 1)
				return ((timeElapsed)/param) + " months ago";
			else return "a month ago";
		}
		
		if (timeElapsed/elapsed > 1)
			return ((timeElapsed)/elapsed) + " years ago";
		else return "a year ago";
		
	}

}
