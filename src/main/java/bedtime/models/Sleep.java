package bedtime.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "bedtime")
public class Sleep {
    private String username;
    private String luminosity;
    private String acceleration;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date = LocalDate.now();
    private LocalDateTime fellAsleep;
    private LocalDateTime awakened;
    private Boolean isCharging;
    private String location;

    public Sleep(String luminosity, String acceleration, LocalDate date,
                 LocalDateTime fellAsleep, LocalDateTime awakened,
                 String username, Boolean isCharging,
                 String location) {
        this.luminosity = luminosity;
        this.acceleration = acceleration;
        this.date = date;
        this.fellAsleep = fellAsleep;
        this.awakened = awakened;
        this.username = username;
        this.isCharging = isCharging;
        this.location = location;
    }

    public Sleep(LocalDate date) {
        this.date = date;
    }

    public Sleep() {

    }

    public Sleep(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLuminosity() {
        return luminosity;
    }

    public void setLuminosity(String luminosity) {
        this.luminosity = luminosity;
    }

    public String getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(String acceleration) {
        this.acceleration = acceleration;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDateTime getFellAsleep() {
        return fellAsleep;
    }

    public void setFellAsleep(LocalDateTime fellAsleep) {
        this.fellAsleep = fellAsleep;
    }

    public LocalDateTime getAwakened() {
        return awakened;
    }

    public void setAwakened(LocalDateTime awakened) {
        this.awakened = awakened;
    }


    public Boolean getCharging() {
        return isCharging;
    }

    public void setCharging(Boolean charging) {
        isCharging = charging;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
