package ga.jayp.dareme;

public class MyChallengesProfileChallengesModel {

    String image, username, challenges_username;
    long date_time_millis;

    public MyChallengesProfileChallengesModel() {

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

    public long getDate_time_millis() {
        return date_time_millis;
    }

    public void setDate_time_millis(long date_time_millis) {
        this.date_time_millis = date_time_millis;
    }

    public MyChallengesProfileChallengesModel(String image, String username, String challenges_username, long date_time_millis) {
        this.image = image;
        this.username = username;
        this.challenges_username = challenges_username;
        this.date_time_millis = date_time_millis;
    }

}
