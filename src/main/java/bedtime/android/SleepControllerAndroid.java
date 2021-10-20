package bedtime.android;

import bedtime.models.DataModel;
import bedtime.models.Sleep;
import bedtime.repository.SleepRepository;
import bedtime.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static bedtime.utils.Helpers.*;

@Controller @Slf4j
public class SleepControllerAndroid {

    @Autowired
    private SleepRepository sleepRepository;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    DateTimeFormatter androidDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @RequestMapping(value="/upload", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public Sleep uploadRecord(@RequestBody Map<String, String> record) {
        String username = record.get("username");
        LocalDate date = LocalDate.parse(record.get("date"), dateFormatter);
        LocalDateTime fellAsleepDateTime = LocalDateTime.parse(record.get("fellAsleep"), androidDTF);
        LocalDateTime awakenedDateTime = LocalDateTime.parse(record.get("awakened"), androidDTF);
        fellAsleepDateTime.format(dtf);
        awakenedDateTime.format(dtf);

        String temp = fellAsleepDateTime.toString().replace("T", " ");
        Date sleepDate = java.sql.Timestamp.valueOf(temp);
        Calendar cal = Calendar.getInstance();
        cal.setTime(sleepDate);
        int hours = cal.get(Calendar.HOUR_OF_DAY);

        // If you slept at or before 05:00 in the morning
        // your sleep record is submitted on the day before.
        if (hours<=5) {
            date = date.minusDays(1);
            fellAsleepDateTime = fellAsleepDateTime.minusDays(1);
        }

        Sleep sleepRecord = new Sleep(
                record.get("luminosity"),
                record.get("acceleration"),
                date,
                fellAsleepDateTime,
                awakenedDateTime,
                username,
                Boolean.valueOf(record.get("isCharging")),
                record.get("location")
        );
        if (sleepRepository.findByFellAsleep(fellAsleepDateTime)!=null){
            sleepRepository.save(sleepRecord);
        }

        return sleepRecord;
    }

    @PostMapping(value = "/get", produces = "application/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HashMap<String, String> fetchData(@RequestBody Sleep sleep) {
        List<Sleep> found = sleepRepository.findByDateAndUsername(sleep.getDate(), sleep.getUsername());

        HashMap<String, String> map = new HashMap<>();

        int i = 1;
        for (Sleep data : found){
            LocalDateTime sleepLDT = data.getFellAsleep();
            LocalDateTime wakeLDT = data.getAwakened();
            String time = calculateTime(sleepLDT, wakeLDT);

            map.put("Session" + i, String.valueOf(i));
            map.put("Duration"+ i, time);
            i++;
        }

        return map;
    }

    @RequestMapping(value = "/get-week", produces = ("application/json") , consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DataModel> getWeek(@RequestBody Sleep sleep) {
        String username = sleep.getUsername();
        List<DataModel> dataModels = new ArrayList<>() ;
        for (int i = 0; i < 7; i++) {
            ArrayList<String> timestampsList = new ArrayList<>();
            LocalDate targetDate = LocalDate.now().minusDays(i);
            List<Sleep> sleepList = sleepRepository.findByDateAndUsername(targetDate, username);
            for (Sleep sleepByDay : sleepList) {
                LocalDateTime dateSlept = sleepByDay.getFellAsleep();
                LocalDateTime dateWoke = sleepByDay.getAwakened();

                timestampsList.add(calculateTime(dateSlept, dateWoke));
            }
            String totalTime = getTime(timestampsList);
            String dayOfWeek = targetDate.getDayOfWeek().toString().substring(0,3);

            dataModels.add(new DataModel(dayOfWeek, totalTime));
        }

        return dataModels;
    }
}