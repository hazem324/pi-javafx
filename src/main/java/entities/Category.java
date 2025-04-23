package entities;

import java.util.ArrayList;
import java.util.List;

public class Category {

    private int id;
    private String name;
    private String description;
    private List<Event> events;

    // ✅ Constructeur vide ajouté pour éviter l'erreur
    public Category() {
        this.events = new ArrayList<>();
    }

    // Constructeur avec ID
    public Category(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.events = new ArrayList<>();
    }

    // Constructeur sans ID
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.events = new ArrayList<>();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public int getEventCount() {
        return events.size();
    }

    public void addEvent(Event event) {
        if (!events.contains(event)) {
            events.add(event);
        }
    }

    public void removeEvent(Event event) {
        events.remove(event);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", eventCount=" + getEventCount() +
                '}';
    }
}
