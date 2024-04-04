package ga.jundbits.dareme.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class CommentModel {

    private String comment, user_id, username, image;
    @ServerTimestamp
    private Date timestamp;
    private long date_time_millis;

    public CommentModel() {

    }

    public CommentModel(String comment, String user_id, String username, String image, long date_time_millis) {
        this.comment = comment;
        this.user_id = user_id;
        this.username = username;
        this.image = image;
        this.date_time_millis = date_time_millis;
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

    public long getDate_time_millis() {
        return date_time_millis;
    }


}
