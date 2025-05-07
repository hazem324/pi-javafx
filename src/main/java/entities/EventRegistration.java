package entities;

import java.time.LocalDateTime;

public class EventRegistration {
    private int id;
    private Event event;
    private User user;
    private LocalDateTime registrationDate;
    private String ticketNumber;
    private String qrCode;

    public EventRegistration() {
        this.registrationDate = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    @Override
    public String toString() {
        return "EventRegistration{" +
                "id=" + id +
                ", event=" + (event != null ? event.getId() : null) +
                ", user=" + (user != null ? user.getId() : null) +
                ", registrationDate=" + registrationDate +
                ", ticketNumber='" + ticketNumber + '\'' +
                ", qrCode='" + qrCode + '\'' +
                '}';
    }
}