package coldapp.giladpinker.technimilon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WordDisplayActivity extends AppCompatActivity {


    String wordKey;
    private FirebaseDatabase db;
    private DatabaseReference dbRef;

    private ScrollView mainscroll;
    private TextView tv_engWord, tv_hebWord;
    private ImageView iv_img;
    private ProgressBar pb_imgload;
    private ListView lv_sentences, lv_synonyms;

    private TextToSpeech tts;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worddisplay_new);

        wordKey = getIntent().getStringExtra("wordKey");
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        initViews();
        getWordFromDB();
    }

    private void initViews() {
        tv_engWord = findViewById(R.id.displayer_tv_engmain);
        tv_hebWord = findViewById(R.id.displayer_tv_hebmain);
        iv_img = findViewById(R.id.displayer_iv_img);
        pb_imgload = findViewById(R.id.displayer_imgload);
        mainscroll = findViewById(R.id.displayer_mainscroll);
        final RelativeLayout tts_holder = findViewById(R.id.displayer_ttsholder);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    tts_holder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!tv_engWord.getText().toString().equals("Word"))
                                Speak(tv_engWord.getText().toString());
                        }
                    });
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    }
                } else
                    Log.e("error", "Initilization Failed!");
            }
        });

    }


    private void initSentList(final List<String> sentences) {
        ArrayList<Sentence> sentencesArrayList = new ArrayList<>();
        if(!(sentences.size() % 2 == 0)) {
            sentences.add("אין תרגום");
        }
        for (int i = 0, y = 1; y < sentences.size(); i+=2, y+=2) {
            sentencesArrayList.add(new Sentence(sentences.get(i), sentences.get(y)));
        }
        final List<String> sentences_final = sentences;
        lv_sentences = findViewById(R.id.displayer_lv_sentences);

        DisplayerSentencesAdapter displayerSentencesAdapter = new DisplayerSentencesAdapter(sentencesArrayList, this);
        lv_sentences.setAdapter(displayerSentencesAdapter);

        // Long press to detect if the user wants to edit the sentence.
        lv_sentences.setLongClickable(true);
        lv_sentences.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                /*//Create the edit dialog
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(getApplicationContext());
                View promptsView = li.inflate(R.layout.dialog_changesentence, null);

                AlertDialog alertDialog;

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        WordDisplayActivity.this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText hebrewSent = promptsView.findViewById(R.id.dialog_changesent_heb);
                hebrewSent.setText(sentences.get(1));
                final EditText engSent = promptsView.findViewById(R.id.dialog_changesent_eng);
                engSent.setText(sentences.get(0));
                alertDialog = alertDialogBuilder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // show it
                alertDialog.show();
                //Get the item pressed.
                View v = parent.getChildAt(position -
                        parent.getFirstVisiblePosition());

                if(v == null)
                    return false;

                TextView sentence =  v.findViewById(R.id.wordprofile_tv_heb);
                sentence.setText("היי שיניתי אותך!");
                sentence =  v.findViewById(R.id.wordprofile_tv_eng);
                sentence.setText("I've changed you!");

                return true;*/

                Mainutils.DialogNoUpload(WordDisplayActivity.this);


                Snackbar snackbar = Snackbar
                        .make(parent, "לא ניתן להציע הצעות כרגע", Snackbar.LENGTH_LONG);

                snackbar.show();

                return true;
            }
        });
    }

    private void initSynList(List<String> synonyms) {
        ArrayList<Synonym> synonymArrayList = new ArrayList<>();
        if(!(synonyms.size() % 2 == 0)) {
            synonyms.add("אין תרגום");
        }
        for (int i = 0, y = 1; y < synonyms.size(); i+=2, y+=2) {
            synonymArrayList.add(new Synonym(synonyms.get(i), synonyms.get(y)));
        }
        lv_synonyms = findViewById(R.id.displayer_lv_synonyms);
        DisplayerSynonymAdapter displayerSynonymAdapter = new DisplayerSynonymAdapter(synonymArrayList, this);
        lv_synonyms.setAdapter(displayerSynonymAdapter);
    }

    private void getWordFromDB() {
        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference(BrainHelper.WORDS);
        Query query = dbRef.orderByChild("wordKey").equalTo(wordKey);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        BrainContainers.WordObject wordObject = issue.getValue(BrainContainers.WordObject.class);
                        setWord(wordObject);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                finish();
            }
        });
    }

    private void setWord(final BrainContainers.WordObject wordObject) {

        dbRef = db.getReference(BrainHelper.DATA);
        Query query = dbRef.orderByChild("wordKey").equalTo(wordKey);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        BrainContainers.WordData wordData = snapshot.getValue(BrainContainers.WordData.class);

                        loadWord(wordObject, wordData);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                finish();
            }
        });

    }

    private void loadWord(BrainContainers.WordObject wordObject, BrainContainers.WordData wordData) {

        Word word = new Word(wordObject.getPubName(), wordObject.getWordKey(), wordObject.getHebWord(), wordObject.getEngWord(), wordData.getSentences(), wordData.getSynonyms(), wordData.getImgUri());

        tv_engWord.setText(word.getEngWord());
        tv_hebWord.setText(word.getHebWord());

        new DownloadImageTask(iv_img, mainscroll, pb_imgload)
                .execute(word.getImgeUri());

        initSentList(word.getSentences());

        initSynList(word.getSynonyms());

    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        if (tts != null) {

            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    private void Speak(String text) {
        if (text == null || "".equals(text)) {
            text = "Content not available";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "READVOICE");
            }
        } else {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if(currentVolume < 10f)
                Toast.makeText(this, "אנא הגבר את הווליום.", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "READVOICE");
            }
        }

    }

    static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        ScrollView sv;
        ProgressBar pb_imgload;
        public DownloadImageTask(ImageView bmImage, ScrollView sv, ProgressBar pb_imgload) {
            this.bmImage = bmImage;
            if(sv != null) this.sv = sv;
            this.pb_imgload = pb_imgload;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            pb_imgload.setVisibility(View.GONE);
            if(result != null) {

                bmImage.setImageBitmap(result);
                if (sv != null) sv.fullScroll(ScrollView.FOCUS_UP);
            } else {
                bmImage.setImageResource(R.drawable.ic_broken_image_black_24dp);
            }
        }

    }

}


