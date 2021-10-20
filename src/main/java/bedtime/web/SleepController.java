package bedtime.web;

import bedtime.models.CustomDate;
import bedtime.models.Sleep;
import bedtime.models.User;
import bedtime.repository.SleepRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

import static bedtime.utils.Helpers.*;

@Controller @Slf4j
public class SleepController {

    @Autowired
    private SleepRepository sleepRepository;

    CustomDate customDateModel = new CustomDate();
    Sleep sleep = new Sleep();

    List<String> headers = Arrays.asList("Session No. ", "Duration ", "Fell Asleep ","Woke up ");
    List<Map<String, Object>> rows;

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");


    @GetMapping({"/", "/home"})
    public String renderHome(Model model, HttpServletRequest request) throws ParseException {
        Map<String, Integer> lastWeek;

        String username = getUsernameCookie(request);
        lastWeek = getLastWeekSleep(username);

        int sum = 0;
        for (int i = 0; i < lastWeek.size(); i++) {
            sum += (int) lastWeek.values().toArray()[i];
        }

        int median = sum/lastWeek.size();

        model.addAttribute("navType", "userNavbar");
        model.addAttribute("user", new User(username));
        model.addAttribute("homeType", "userFragment");
        model.addAttribute("form", sleep);
        model.addAttribute("customDateModel", customDateModel);
        model.addAttribute("median", median);
        model.addAttribute("values", lastWeek);

        return "home";
    }

    @RequestMapping({"/global-statistics"})
    public String globalStatistics() {
        return "global-statistics";
    }

    @RequestMapping("/get-top-five")
    public ResponseEntity<LinkedHashMap<String, Integer>> getPieChart(HttpServletRequest request) {
        LinkedHashMap<String, Integer> topFour = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> results = new LinkedHashMap<>();

        String username = getUsernameCookie(request);
        List<Sleep> list = sleepRepository.findAllByUsernameAndDateLessThan(username, LocalDate.now());

        for (int i = 0; i <= 24; i++) {
            if (i < 10){
                results.put("0"+i, 0);
            }
            results.put(String.valueOf(i), 0);
        }

        for (Sleep record : list) {
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH");
            String time = record.getFellAsleep().format(timeFormat);

            results.put(time, results.get(time) + 1);
        }

        results = sortByValue(results);

        int index = 0;
        for (Map.Entry<String,Integer> e : results.entrySet() ) {
            index++;
            if (index >= 21) {
                if (e.getValue() > 0) {
                    topFour.put(e.getKey() + ":00", e.getValue());
                }
            }
        }
        return new ResponseEntity<>(topFour, HttpStatus.OK);
    }

    @GetMapping("/view-custom")
    public String customDates(@RequestParam(required = false) String first_date,
                              @RequestParam(required = false) String last_date,
                              Model model,
                              HttpServletRequest request) {
        rows = new ArrayList<>();
        ArrayList<String> timestampsList = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(first_date, dateFormatter);
        LocalDate endDate = LocalDate.parse(last_date, dateFormatter);
        String username = getUsernameCookie(request);

        List<Sleep> found = sleepRepository.findByUsernameAndDateBetween(username, startDate, endDate);

        int i = 1;
        String totalTimeSlept = iterateFound(found, timestampsList, i);
        long totalDays = ChronoUnit.DAYS.between(startDate,endDate);
        String message =  totalTimeSlept + " in " + totalDays + " day(s).";

        if (totalDays == 0) {
            totalDays = 1;
            message =  totalTimeSlept + " in " + totalDays + " day(s).";
        } else if (totalDays < 0) {
            message = "Invalid Dates";
        }

        model.addAttribute("headers", headers);
        model.addAttribute("rows", rows);
        model.addAttribute("title", first_date + " > " + last_date);
        model.addAttribute("total", message);

        return "view";
    }

    @GetMapping("/view")
    public String singleDate(@RequestParam(required = false) String date,
                             Model model,
                             HttpServletRequest request) {
        rows = new ArrayList<>();
        LocalDate localDate = LocalDate.parse(date, dateFormatter);

        String username = getUsernameCookie(request);
        List<Sleep> found = sleepRepository.findByDateAndUsername(localDate, username);
        ArrayList<String> timestampsList = new ArrayList<>();

        int i = 1;
        String totalTimeSlept = iterateFound(found, timestampsList, i);

        model.addAttribute("headers", headers);
        model.addAttribute("rows", rows);
        model.addAttribute("title", date);
        model.addAttribute("total", totalTimeSlept);

        return "view";
    }

    @GetMapping(value = "/get-global")
    @ResponseBody
    public List<Map<String, Integer>> getGlob() {
        List<Map<String, Integer>> finalList = new ArrayList<>();
        List<Sleep> allRecords = sleepRepository.findAll();
        List<String> countries = new ArrayList<>();

        for (Sleep record : allRecords) {
            String location = record.getLocation();
            if (!countries.contains(location)) {
                countries.add(location);
            }
        }

        for (String country : countries) {
            List<Sleep> countrySpecific = sleepRepository.findAllByLocation(country);
            Map<String, Integer> countryAvg = getCountriesAvg(countrySpecific);
            finalList.add(countryAvg);
        }

        return finalList;
    }

    private Map<String, Integer> getCountriesAvg(List<Sleep> countrySpecificList) {
        Map<String, Integer> sumPerDay = new LinkedHashMap<>();
        Map<String, Integer> countOfDays = new LinkedHashMap<>();
        initMap(sumPerDay);
        initMap(countOfDays);

        String[] daysArray = {"MON","TUE","WED","THU","FRI","SAT","SUN"};
        String country = countrySpecificList.get(0).getLocation();
        for (Sleep countrySpecific : countrySpecificList) {
            ArrayList<String> timestampsList = new ArrayList<>();
            LocalDateTime dateSlept = countrySpecific.getFellAsleep();
            LocalDateTime dateWoke = countrySpecific.getAwakened();
            String dayOfWeek = dateSlept.getDayOfWeek().toString().substring(0, 3);
            timestampsList.add(calculateTime(dateSlept, dateWoke));
            String totalTime = getTime(timestampsList);

            float duration = getSleepDurationInFloat(timestampsList, totalTime);

            sumPerDay.put(dayOfWeek, (sumPerDay.get(dayOfWeek) + (int) duration));
            countOfDays.put(dayOfWeek, countOfDays.get(dayOfWeek)+1);
        }
        for (String day : daysArray) {
            if (countOfDays.get(day) == 0) {
                countOfDays.put(day, 1);
            }
            sumPerDay.put(day, sumPerDay.get(day) / countOfDays.get(day));
        }

        sumPerDay.put(country, -1);

        return sumPerDay;
    }

    private float getSleepDurationInFloat(ArrayList<String> timestampsList, String totalTime) {
        if (totalTime.equals("No records")) {
            totalTime = "00:00:00";
        } else {
            totalTime = getTime(timestampsList).split(":", 2)[1].trim();
        }

        String[] time = totalTime.split(":");
        int hours =  Integer.parseInt(time[0]);
        float minutes = Integer.parseInt(time[1]);
        int seconds = Integer.parseInt(time[2])/3600;
        float duration ;
        duration = hours + minutes/60 + seconds;
        return duration;
    }

    private void initMap(Map<String, Integer> map) {
        map.put("MON", 0);
        map.put("TUE", 0);
        map.put("WED", 0);
        map.put("THU", 0);
        map.put("FRI", 0);
        map.put("SAT", 0);
        map.put("SUN", 0);
    }

    private Map<String, Integer> getLastWeekSleep(String username) {
        Map<String, Integer> lastWeek = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            ArrayList<String> timestampsList = new ArrayList<>();
            LocalDate targetDate = LocalDate.now().minusDays(i);
            List<Sleep> sleep = sleepRepository.findByDateAndUsername(targetDate, username);

            System.out.println(targetDate);
            if (sleep.size() < 1) {
                System.out.println("empty");
            }
            for (Sleep sleepByDay : sleep) {
                LocalDateTime dateSlept = sleepByDay.getFellAsleep();
                LocalDateTime dateWoke = sleepByDay.getAwakened();
                timestampsList.add(calculateTime(dateSlept, dateWoke));
            }
            String totalTime = getTime(timestampsList);
            String dayOfWeek = targetDate.getDayOfWeek().toString().substring(0, 3);

            float duration = getSleepDurationInFloat(timestampsList, totalTime);

            lastWeek.put(dayOfWeek, (int) duration);
            System.out.println("last week " + lastWeek);
        }

        return lastWeek;
    }

    private String iterateFound(List<Sleep> found, ArrayList<String> timestampsList, int i) {
        for (Sleep data : found){
            LocalDateTime sleep = data.getFellAsleep();
            LocalDateTime wake = data.getAwakened();
            String duration = calculateTime(data.getFellAsleep(), data.getAwakened());

            String[] durationInfo = duration.split(":");
            String HMS = " hour(s)";
            if (durationInfo[0].equals("0")){
                HMS = " minute(s)";
            }
            if (durationInfo[1].equals("0")) {
                HMS = " second(s)";
            }
            timestampsList.add(duration);
            populateTable(duration+HMS,wake,sleep,i);

            i++;
        }
        return  getTime(timestampsList);
    }

    public void populateTable(String duration, LocalDateTime wake, LocalDateTime sleep, int i) {
        String sleepDate = sleep.format(dateFormatter);
        String sleepTime = sleep.format(timeFormatter);
        String wakeDate = wake.format(dateFormatter);
        String wakeTime = wake.format(timeFormatter);

        rows.add(Map.of("Session No. ",  i ,
                "Duration ", duration,
                "Fell Asleep ",sleepDate + ", " + sleepTime ,
                "Woke up ", wakeDate + ", " + wakeTime
        ));
    }
}
