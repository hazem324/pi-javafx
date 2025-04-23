package entities;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class Event {

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_COMPLETED = "completed";

    private int id;
    private String title;
    private String eventDescription;
    private LocalDateTime eventDate;
    private LocalDate endDate;
    private String eventLocation;
    private String status = STATUS_ACTIVE;
    private Category category;
    private int numberOfPlaces;
    private String imageFilename;

    // No-argument constructor
    public Event() {
    }

    public Event(int id, String title, String eventDescription, LocalDateTime eventDate,
                 LocalDate endDate, String eventLocation, String status, Category category,
                 int numberOfPlaces, String imageFilename) {
        this.id = id;
        this.title = title;
        this.eventDescription = eventDescription;
        this.eventDate = eventDate;
        this.endDate = endDate;
        this.eventLocation = eventLocation;
        this.setStatus(status);
        this.category = category;
        this.numberOfPlaces = numberOfPlaces;
        this.imageFilename = imageFilename;
    }

    public Event(String title, String eventDescription, LocalDateTime eventDate,
                 LocalDate endDate, String eventLocation, String status, Category category,
                 int numberOfPlaces, String imageFilename) {
        this.title = title;
        this.eventDescription = eventDescription;
        this.eventDate = eventDate;
        this.endDate = endDate;
        this.eventLocation = eventLocation;
        this.setStatus(status);
        this.category = category;
        this.numberOfPlaces = numberOfPlaces;
        this.imageFilename = imageFilename;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", eventDescription='" + eventDescription + '\'' +
                ", eventDate=" + eventDate +
                ", endDate=" + endDate +
                ", eventLocation='" + eventLocation + '\'' +
                ", status='" + status + '\'' +
                ", category=" + category +
                ", numberOfPlaces=" + numberOfPlaces +
                ", imageFilename='" + imageFilename + '\'' +
                '}';
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getEventDescription() { return eventDescription; }
    public void setEventDescription(String eventDescription) { this.eventDescription = eventDescription; }

    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getEventLocation() { return eventLocation; }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        if (!status.equals(STATUS_ACTIVE) && !status.equals(STATUS_CANCELLED) && !status.equals(STATUS_COMPLETED)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        this.status = status;
    }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public int getNumberOfPlaces() { return numberOfPlaces; }
    public void setNumberOfPlaces(int numberOfPlaces) { this.numberOfPlaces = numberOfPlaces; }

    public String getImageFilename() { return imageFilename; }
    public void setImageFilename(String imageFilename) { this.imageFilename = imageFilename; }

    public boolean hasAvailablePlaces() {
        return numberOfPlaces > 0;
    }

    public void decrementPlaces() {
        if (hasAvailablePlaces()) {
            numberOfPlaces--;
        }
    }

    public void setHeaderText(Object o) {
    }

    public void setContentText(String s) {
    }
}