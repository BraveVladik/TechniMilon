package coldapp.giladpinker.technimilon;

import java.util.List;

public class BrainContainers {

    public static class WordObject {

        private String engWord, hebWord, pubName, wordKey;

        public WordObject() {

        }
        public WordObject(String engWord, String hebWord, String pubName, String wordKey) {
            this.engWord = engWord;
            this.hebWord = hebWord;
            this.pubName = pubName;
            this.wordKey = wordKey;
        }

        public String getEngWord() {
            return engWord;
        }

        public String getHebWord() {
            return hebWord;
        }

        public String getPubName() {
            return pubName;
        }

        public String getWordKey() {
            return wordKey;
        }
    }

    public static class WordData {

        private String wordKey, imgUri;
        private List<String> sentences, synonyms;

        public String getWordKey() {
            return wordKey;
        }

        public String getImgUri() {
            return imgUri;
        }

        public List<String> getSentences() {
            return sentences;
        }

        public List<String> getSynonyms() {
            return synonyms;
        }

        public WordData() {

        }

        public WordData(String wordKey, String imgUri, List<String> sentences, List<String> synonyms) {
            this.wordKey = wordKey;
            this.imgUri = imgUri;
            this.sentences = sentences;
            this.synonyms = synonyms;
        }
    }
}
