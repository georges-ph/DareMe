package ga.jundbits.dareme.Models;

public class ChallengeCommentsBottomSheetModel {

    private String user_id, image, username, comment;
    private long date_time_millis;

    public ChallengeCommentsBottomSheetModel() {

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getDate_time_millis() {
        return date_time_millis;
    }

    public void setDate_time_millis(long date_time_millis) {
        this.date_time_millis = date_time_millis;
    }

    public ChallengeCommentsBottomSheetModel(String user_id, String image, String username, String comment, long date_time_millis) {
        this.user_id = user_id;
        this.image = image;
        this.username = username;
        this.comment = comment;
        this.date_time_millis = date_time_millis;
    }

}
