package coldapp.giladpinker.technimilon;

public class Sentence {

    private String engSent, hebSent;

    public Sentence(String engSent, String hebSent) {
        this.engSent = engSent;
        this.hebSent = hebSent;
    }

    public String getEngSent() {
        return engSent;
    }

    public String getHebSent() {
        return hebSent;
    }
}
