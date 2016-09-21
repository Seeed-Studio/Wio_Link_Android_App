package cc.seeed.iot.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class TimeUtil {
	/**
	 * 获取系统当前时间
	 * 
	 * @param outFormate
	 *            输出格式:yy MM dd HH:mm:ss
	 * @return 返回当前时间
	 */
	public static String getCurTimeToString(String outFormate) {
		SimpleDateFormat formatter = new SimpleDateFormat(outFormate);
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		return str;

	}

	public static Date getCurTimeToDate(String outFormate) {
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		return curDate;

	}

	public static Date string2Date(String str, String formate) {
		SimpleDateFormat formatter = new SimpleDateFormat(formate);
		Date date = null;
		try {
			date = formatter.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			date = null;
		}
		return date;

	}


    /**
     * 日期转字符串
     * @param time 日期时间戳
     * @param outFormate
     * @return
     */
	public static String long2String(long time, String outFormate) {
		SimpleDateFormat formatter = new SimpleDateFormat(outFormate);
		Date curDate = new Date(time*1000);// 获取当前时间
		String str = formatter.format(curDate);
		return str;
	}

	public static String long2Txt(long time) {
		
		String result="";
		long currentTime = System.currentTimeMillis();
		long currentTime1=TimeUtil.string2Date(TimeUtil.long2String(currentTime/1000, "yyyy-MM-dd")+" 23:59:59", "yyyy-MM-dd HH:mm:ss").getTime();
		int day = (int) ((currentTime1 - time*1000)/ (24 * 60 * 60 * 1000L));
		Date curDate = new Date(time*1000);
		SimpleDateFormat formatter=null;
		if (day <= 2) {
		    formatter = new SimpleDateFormat("HH:mm");
			String str = formatter.format(curDate);
			switch (day) {
			case 0:
                long between=currentTime - time*1000;
                int hour= (int) (between/(60 * 60 * 1000L));
                int min= (int) (between%(60 * 60 * 1000L)/(60 * 1000));
                StringBuffer sb=new StringBuffer();
                if(hour>0){
                    sb.append(hour);
                    sb.append("小时");
                }
                if(min>0){
                    sb.append(min);
                    sb.append("分钟");
                    sb.append("之前");
                }else{
                    if(hour>0){
                        sb.append("之前");
                    }else {
                        sb.append("刚刚");
                    }
                }
                result=sb.toString();
				break;

			case 1:
				result="昨天"+str;
				break;
			case 2:
				result="前天"+str;
				break;
			}
		} else {
			formatter = new SimpleDateFormat("yy年MM月dd日HH:mm");
			String str = formatter.format(curDate);
			result=str;
		}
        return result;
	}


	public static  long getCurrentDate(){
        return string2Date(getCurTimeToString("yyyy-MM-dd")+" 00:00:00","yyyy-MM-dd hh:mm:ss").getTime()/1000;
	}
}
