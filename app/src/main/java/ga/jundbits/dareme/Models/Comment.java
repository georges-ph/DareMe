package ga.jundbits.dareme.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Comment {

    private String comment, user_id, username, image;
    @ServerTimestamp
    private Date timestamp;

    public Comment() {

    }

    public Comment(String comment, String user_id, String username, String image) {
        this.comment = comment;
        this.user_id = user_id;
        this.username = username;
        this.image = image;
    }

    public String getComment() {
        return comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getImage() {
        return image;
    }

    public Date getTimestamp() {
        return timestamp;
    }

}
