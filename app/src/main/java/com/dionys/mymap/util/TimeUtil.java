package com.dionys.mymap.util;

public class TimeUtil {


    /**
     * 根据毫秒数返回格式化的 小时：分：秒
     * @return
     */
    public static String getFormateTime(long timeMillinis){

        long timeSeconds = timeMillinis / 1000;

        int seconds = (int) (timeSeconds % 60);
        int minutes = (int) (timeSeconds / 60);
        int hours = (int) (timeSeconds /3600);

        return String.format("%02d:%02d:%02d",hours,minutes,seconds);

    }

    public static String getFormatTime(String timeSeconds) {
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(timeSeconds);

        int minu = t / 60 % 60;
        int hour = t / 3600;

        if(hour > 0){
            sb.append( hour + "小时");
        }
        sb.append(minu + "分钟");

        return sb.toString();
    }
}
