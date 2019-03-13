package coldapp.giladpinker.technimilon;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class Word {

    private String pubName, wordKey;

    private String hebWord, engWord;
    private List<String> sentences, synonyms;

    public String getPubName() {
        return pubName;
    }

    public void setPubName(String pubName) {
        this.pubName = pubName;
    }

    public String getHebWord() {
        return hebWord;
    }

    public void setHebWord(String hebWord) {
        this.hebWord = hebWord;
    }

    public String getEngWord() {
        return engWord;
    }

    public void setEngWord(String engWord) {
        this.engWord = engWord;
    }

    public List<String> getSentences() {
        return sentences;
    }

    public void setSentences(List<String> sentences) {
        this.sentences = sentences;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public String getImgeUri() {
        return imgeUri;
    }

    public void setImgeUri(String imgeUri) {
        this.imgeUri = imgeUri;
    }

    private String imgeUri;

    public Word() {

    }

    public Word(String pubName, String wordKey, String hebWord, String engWord, List<String> sentences, List<String> synonyms, String imgeUri) {
        this.pubName = pubName;
        this.wordKey = wordKey;
        this.hebWord = hebWord;
        this.engWord = engWord;
        this.sentences = sentences;
        this.synonyms = synonyms;
        this.imgeUri = imgeUri;
    }

    public String getWordKey() {
        return wordKey;
    }
}
