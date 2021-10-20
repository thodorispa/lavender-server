package bedtime.models;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "bedtime")
@AllArgsConstructor
@NoArgsConstructor
public class CustomDate {
    private String first_date = String.valueOf(LocalDate.now());
    private String last_date = String.valueOf(LocalDate.now());

    public String getFirst_date() {
        return first_date;
    }

    public void setFirst_date(String first_date) {
        this.first_date = first_date;
    }

    public String getLast_date() {
        return last_date;
    }

    public void setLast_date(String last_date) {
        this.last_date = last_date;
    }
}
