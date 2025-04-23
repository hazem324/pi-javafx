package models;

import enums.CategoryGrp;

import java.time.LocalDateTime;

public class Community {

    private int id;
    private String name;
    private String description;
    private String banner;
    private LocalDateTime creationDate;
    private CategoryGrp category;


    public Community() {}

    public Community(String name, String description, String banner, LocalDateTime creationDate, CategoryGrp category) {
        this.name = name;
        this.description = description;
        this.banner = banner;
        this.creationDate = creationDate;
        this.category = category;
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

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public CategoryGrp getCategory() {
        return category;
    }

    public void setCategory(CategoryGrp category) {
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Community{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", banner='" + banner + '\'' +
                ", creationDate=" + creationDate +
                ", category=" + category +
                '}';
    }
}
