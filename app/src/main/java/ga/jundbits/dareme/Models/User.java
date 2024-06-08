package ga.jundbits.dareme.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class User {

    private String id, name, username, email, type, image, description, fcm_token;
    @ServerTimestamp
    private Date created_at;

    public User() {

    }

    public User(String id, String name, String username, String email, String type, String image, String description, String fcm_token) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.type = type;
        this.image = image;
        this.description = description;
        this.fcm_token = fcm_token;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getType() {
        return type;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public String getFcm_token() {
        return fcm_token;
    }

}
