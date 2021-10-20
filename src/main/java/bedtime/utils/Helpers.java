package bedtime.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {

    public static void giveCookie(String username, HttpServletResponse response) {
        Cookie cookie = new Cookie("username", username);
        cookie.setPath("/");
        cookie.isHttpOnly();
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    public static void removeCookie(HttpServletResponse response){
        Cookie cookie = new Cookie("username", null);
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    public static String getUsernameCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        Cookie usernameCookie = null;
        for(Cookie cookie : cookies){
            if (cookie.getName().equals("username")) {
                usernameCookie = cookie;
                break;
            }
        }

        String username = null;
        if (usernameCookie != null) {
            username = usernameCookie.getValue();
        }
        return username;
    }

    public static String getTime(ArrayList<String> timestampsList) {
        if (timestampsList.isEmpty()) {
            return "No records";
        }
        long tm = 0;
        for (String tmp : timestampsList){
            String[] arr = tmp.split(":");
            tm += Integer.parseInt(arr[2]);
            tm += 60L * Integer.parseInt(arr[1]);
            tm += 3600L * Integer.parseInt(arr[0]);
        }

        long hh = tm / 3600;
        tm %= 3600;
        long mm = tm / 60;
        tm %= 60;
        long ss = tm;

        return "Total time slept: " + format(hh) + ":" + format(mm) + ":" + format(ss);
    }

    public static String calculateTime(LocalDateTime sleep, LocalDateTime wake) {
        Date sleepDate = java.sql.Timestamp.valueOf(sleep);
        Date wakeDate = java.sql.Timestamp.valueOf(wake);
        long difference_In_Time = wakeDate.getTime() - sleepDate.getTime();

        long difference_In_Seconds
                = (difference_In_Time
                / 1000)
                % 60;

        long difference_In_Minutes
                = (difference_In_Time
                / (1000 * 60))
                % 60;

        long difference_In_Hours
                = (difference_In_Time
                / (1000 * 60 * 60))
                % 24;

        return difference_In_Hours + ":" + difference_In_Minutes + ":" + difference_In_Seconds;
    }


    public static LinkedHashMap<String, Integer> sortByValue(LinkedHashMap<String, Integer> unsortMap) {

        List<Map.Entry<String, Integer>> list =
                new LinkedList<>(unsortMap.entrySet());

        list.sort(Map.Entry.comparingByValue());

        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }


        return sortedMap;
    }

    private static String format(long s){
        if (s < 10) return "0" + s;
        else return "" + s;
    }

}
