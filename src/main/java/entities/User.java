package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private boolean isVerified;
    private boolean isBlocked;
    private String profileIMG;
    private List<String> roles;
    private String twoFactorToken;
    private LocalDateTime twoFactorTokenExpiry;

    public User(int id, String firstName, String lastName, String email, String password, boolean isVerified, boolean isBlocked, String profileIMG, List<String> roles) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.isVerified = isVerified;
        this.isBlocked = isBlocked;
        this.profileIMG = profileIMG;
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
    }

    public User(String firstName, String lastName, String email, String password, boolean isVerified, boolean isBlocked, String profileIMG, List<String> roles) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.isVerified = isVerified;
        this.isBlocked = isBlocked;
        this.profileIMG = profileIMG;
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
    }

    public User() {

    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", isVerified=" + isVerified +
                ", isBlocked=" + isBlocked +
                ", profileIMG='" + profileIMG + '\'' +
                ", roles=" + roles +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getProfileIMG() {
        return profileIMG;
    }

    public void setProfileIMG(String profileIMG) {
        this.profileIMG = profileIMG;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
    }


    public String getTwoFactorToken() { return twoFactorToken; }
    public void setTwoFactorToken(String twoFactorToken) { this.twoFactorToken = twoFactorToken; }

    public LocalDateTime getTwoFactorTokenExpiry() { return twoFactorTokenExpiry; }
    public void setTwoFactorTokenExpiry(LocalDateTime twoFactorTokenExpiry) { this.twoFactorTokenExpiry = twoFactorTokenExpiry; }
}