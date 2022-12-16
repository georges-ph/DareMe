package ga.jayp.dareme;

public class MainHomeChallengesModel {

    String image, username, challenges_username, color, text, prize, user_id, player_user_id;
    long date_time_millis;
    boolean accepted, completed, failed;

    public MainHomeChallengesModel() {

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

    public String getChallenges_username() {
        return challenges_username;
    }

    public void setChallenges_username(String challenges_username) {
        this.challenges_username = challenges_username;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPlayer_user_id() {
        return player_user_id;
    }

    public void setPlayer_user_id(String player_user_id) {
        this.player_user_id = player_user_id;
    }

    public long getDate_time_millis() {
        return date_time_millis;
    }

    public void setDate_time_millis(long date_time_millis) {
        this.date_time_millis = date_time_millis;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public MainHomeChallengesModel(String image, String username, String challenges_username, String color, String text, String prize, String user_id, String player_user_id, long date_time_millis, boolean accepted, boolean completed, boolean failed) {
        this.image = image;
        this.username = username;
        this.challenges_username = challenges_username;
        this.color = color;
        this.text = text;
        this.prize = prize;
        this.user_id = user_id;
        this.player_user_id = player_user_id;
        this.date_time_millis = date_time_millis;
        this.accepted = accepted;
        this.completed = completed;
        this.failed = failed;
    }

}
