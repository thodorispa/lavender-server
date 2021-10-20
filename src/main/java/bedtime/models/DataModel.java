package bedtime.models;

public class DataModel {
    String title;
    String duration;

    public DataModel(String title, String duration) {
        this.duration = duration;
        this.title = title;
    }
    public DataModel() {
    }

    public String getText() {
        return duration;
    }

    public void setText(String text) {
        this.duration = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
