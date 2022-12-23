package ga.jundbits.dareme.Models;

public class NewChallengePlayersModel {

    String id, image, username;

    public NewChallengePlayersModel() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public NewChallengePlayersModel(String id, String image, String username) {
        this.id = id;
        this.image = image;
        this.username = username;
    }

}
