package coldapp.giladpinker.technimilon;

import android.animation.ObjectAnimator;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainSearchActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final String TAG_SYS = "מערכת";

    private Mainutils mainutils;

    private BrainHelper brainHelper;
    private FirebaseAuth mAuth;
    private SearchView searchView;
    private Handler mHandler;

    private TextSwitcher tv_MenuTitle;
    private TextView switcher_tv;

    private RecyclerView wordsList;
    private FirebaseRecyclerAdapter<BrainContainers.WordObject, WordsViewHolder> firebaseRecyclerAdapter;

    private NestedScrollView addword_mainNested;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_search2);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        mainutils = new Mainutils(this);
        brainHelper = new BrainHelper(this);
        setAuth();
        wordsList = findViewById(R.id.mainsearch_wordlist);
        wordsList.setVisibility(View.GONE);
        wordsList.setLayoutManager(new LinearLayoutManager(this));

        tv_MenuTitle = findViewById(R.id.mainsearch_tv_title);
        tv_MenuTitle.setFactory(new ViewSwitcher.ViewFactory() {
            Typeface face;
            @Override
            public View makeView() {
                switcher_tv = new TextView(getApplicationContext());
                switcher_tv.setTextSize(25);
                switcher_tv.setTextColor(getResources().getColor(R.color.colorWhite));
                switcher_tv.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                switcher_tv.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                face = Typeface.createFromAsset(getAssets(), "fonts/secularone.ttf");
                switcher_tv.setTypeface(face);
                return switcher_tv;
            }
        });
        tv_MenuTitle.setText("מילון");

        mHandler = new Handler();
        searchView = findViewById(R.id.mainsearch_searchview);
        EditText et= (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        et.setHint(this.getString(R.string.string_mainsearch_searchview_hint));
        mainutils.setEditTextUnfocusTimer(et);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                query = searchView.getQuery().toString();
                dbWordsSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final String queryString = newText;
                mHandler.removeCallbacksAndMessages(null);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dbWordsSearch(queryString);
                    }
                }, 300);
                return true;
            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });
        addword_mainNested = findViewById(R.id.addword_mainNested);
        initBottomSheet();
    }

    private void setAuth() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {

            /* perform your actions here*/


        } else {
            signInAnonymously();
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                /* perform your actions here*/

            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("MainActivity", "signFailed****** ", exception);
                    }
                });
    }

    private void dbWordsSearch(String searchText) {

        Log.d("SEARCH", "Started searching");
        String child;


        if (searchText.isEmpty())
        {
            wordsList.setVisibility(View.GONE);
            tv_MenuTitle.setText("מילון");
            return;
        }
        else {
            if (checkRtl(searchText)) {
                child = "hebWord";
                Log.d("SEARCH", "found hebrew word: " + searchText);
            }
            else {
                Log.d("SEARCH", "found english word" + searchText);
                child = "engWord";


                searchText = searchText.substring(0,1).toUpperCase() + searchText.substring(1).toLowerCase();

            }

        }
        wordsList.setVisibility(View.VISIBLE);
        Query query = brainHelper.getDb().getReference(BrainHelper.WORDS)
                .orderByChild(child).startAt(searchText).endAt(searchText + "\uf8ff").limitToFirst(50); /** to limit the words, add .limitToFirst(20); **/

        FirebaseRecyclerOptions<BrainContainers.WordObject> options =
                new FirebaseRecyclerOptions.Builder<BrainContainers.WordObject>()
                        .setQuery(query, BrainContainers.WordObject.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<BrainContainers.WordObject, WordsViewHolder>(options) {

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                tv_MenuTitle.setText(getItemCount() + " תוצאות");
                if(getItemCount() == 0) shakeY(addword_mainNested);
            }

            @NonNull
            @Override
            public WordsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from((parent.getContext())).inflate(R.layout.item_result_word, parent, false);

                return new WordsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull WordsViewHolder holder, int position, @NonNull final BrainContainers.WordObject model) {

                holder.setView(model.getHebWord(), model.getEngWord());
                holder.bg_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), WordDisplayActivity.class);
                        intent.putExtra("wordKey", model.getWordKey());
                        startActivity(intent);
                        //Toast.makeText(MainSearchActivity.this, "word key is: " + model.getWordKey(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };


        wordsList.setAdapter(firebaseRecyclerAdapter);
        Log.d("SEARCH", "Set Adapter");
        firebaseRecyclerAdapter.startListening();
    }

    public class WordsViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView bg_image;

        public WordsViewHolder(View itemView) {
            super(itemView);

            view = itemView;
        }

        private void setView(String hebrewWord, String englishWord) {

            bg_image = view.findViewById(R.id.dialog_changesent_bg);
            TextView hebrew_word = view.findViewById(R.id.dialog_changesent_heb);
            TextView english_word = view.findViewById(R.id.dialog_changesent_eng);

            hebrew_word.setText(hebrewWord);
            english_word.setText(englishWord);

        }



    }

    public static boolean checkRtl(String string) {
        if (TextUtils.isEmpty(string)) {
            return false;
        }

        char c = string.charAt(0);
        return c >= 0x590 && c <= 0x6ff;
    }

    private Button aw_btn;
    private EditText aw_et_wordHeb, aw_et_wordEng, aw_et_sentHeb, aw_et_sentEng, aw_et_imgLink;
    private ImageView aw_iv_img;
    public void initBottomSheet() {
        aw_btn = findViewById(R.id.bottomsheet_button);

        aw_et_wordHeb = findViewById(R.id.bottomsheet_wordHeb);
        aw_et_wordEng = findViewById(R.id.bottomsheet_wordEng);

        aw_et_sentHeb = findViewById(R.id.bottomsheet_SentHeb);
        aw_et_sentEng = findViewById(R.id.bottomsheet_SentEng);

        aw_et_imgLink = findViewById(R.id.bottomsheet_ImageLink);

        aw_iv_img = findViewById(R.id.bottomsheet_img);
        
        CheckFields();
    }

    public void AddWord(View view) {
        /** Do the next checks:
         *  1. check if nothing is empty.
         *  2. check for special characters
         *  3. check the wanted language.
         *
         *  4. check if such word does not exist.
         */
            Mainutils.DialogNoUpload(MainSearchActivity.this);

    }

    private void CheckFields() {

        aw_et_wordHeb.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    detectWordHeb();
                } else neutralInput((EditText) v);
            }
        });

        aw_et_wordEng.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    detectWordEng();
                } else neutralInput((EditText) v);
            }
        });

        aw_et_sentHeb.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    detectSentHeb();
                } else neutralInput((EditText) v);
            }
        });

        aw_et_sentEng.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    detectSentEng();
                } else neutralInput((EditText) v);
            }
        });

        aw_et_imgLink.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    detectImageLink();
                } else neutralInput((EditText) v);
            }
        });

        Mainutils mainutils = new Mainutils(this);
        mainutils.setEditTextUnfocusTimer(aw_et_imgLink);

//

    }

    private void detectWordHeb() {
        String string = aw_et_wordHeb.getText().toString();
        string = string.trim().replaceAll(" +", " ");

        if(string.isEmpty() ) {
            Toast.makeText(this, "המילה בעברית ריקה", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_wordHeb);

        }else if(string.length() <= 2) {
            Toast.makeText(this, "המילה בעברית קצרה מדיי", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_wordHeb);
        } else if(checkSpecials(string)) {
            Toast.makeText(this, "השימוש בתווים מיוחדים אסורה", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_wordHeb);
        } else if(!checkAddHebrew(string)) {
            Toast.makeText(this, "המילה בעברית כתובה באנגלית.", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_wordHeb);
        } else checkIfWordExists(aw_et_wordHeb, "hebWord", string);

    }

    private void detectWordEng() {
        String string = aw_et_wordEng.getText().toString();
        string = string.trim().replaceAll(" +", " ");

        if(string.isEmpty() ) {
            Toast.makeText(this, "המילה באנגלית ריקה", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_wordEng);

        }else if(string.length() <= 2) {
            Toast.makeText(this, "המילה באגלית קצרה מדיי", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_wordEng);
        } else if(checkSpecials(string)) {
            Toast.makeText(this, "השימוש בתווים מיוחדים אסורה", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_wordEng);
        } else if(checkAddEng(string)) {
            Toast.makeText(this, "המילה באנגלית כתובה בעברית.", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_wordEng);
        } else goodInput(aw_et_wordEng);

    }

    private void detectSentHeb() {
        String string = aw_et_sentHeb.getText().toString();
        string = string.trim().replaceAll(" +", " ");

        if(string.isEmpty() ) {
            Toast.makeText(this, "המשפט בעברית ריק", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_sentHeb);

        }else if(string.length() <= 15) {
            Toast.makeText(this, "המשפט בעברית קצר מדיי", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_sentHeb);
        } else if(checkSpecials(string)) {
            Toast.makeText(this, "השימוש בתווים מיוחדים אסורה", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_sentHeb);
        } else if(!checkAddHebrew(string)) {
            Toast.makeText(this, "המשפט בעברית כתוב באנגלית.", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_sentHeb);
        } else goodInput(aw_et_sentHeb);

    }

    private void detectSentEng() {
        String string = aw_et_sentEng.getText().toString();
        string = string.trim().replaceAll(" +", " ");

        if(string.isEmpty() ) {
            Toast.makeText(this, "המשפט באנגלית ריק", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_sentEng);

        }else if(string.length() <= 15) {
            Toast.makeText(this, "המשפט באנגלית קצר מדיי", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_sentEng);
        } else if(checkSpecials(string)) {
            Toast.makeText(this, "השימוש בתווים מיוחדים אסורה", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_sentEng);
        } else if(checkAddEng(string)) {
            Toast.makeText(this, "המשפט באנגלית כתוב בעברית.", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_sentEng);
        } else goodInput(aw_et_sentEng);

    }

    private void detectImageLink() {
        String string = aw_et_imgLink.getText().toString();
        string = string.trim().replaceAll(" +", " ");

        if(string.isEmpty() ) {
            Toast.makeText(this, "קישור לתמונה ריק", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_imgLink);

        }else if(string.length() <= 5) {
            Toast.makeText(this, "הקישור לתמונה קצר מדיי", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_imgLink);
        } else if(checkAddEng(string)) {
            Toast.makeText(this, "הקישור לא יכול להיות בעברית", Toast.LENGTH_SHORT).show();
            errorInput(aw_et_imgLink);
        } else  new DownloadImageTask(aw_iv_img, aw_et_imgLink)
                .execute(string);

    }

    private void errorInput(EditText et) {
        et.setTextColor(Color.RED);
        et.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_x_black_24dp, 0, 0, 0);
        shake(et);
    }
    private void goodInput(EditText et) {
        et.setTextColor(Color.parseColor("#34b034"));
        et.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_check_green_24dp, 0, 0, 0);
    }

    private void neutralInput(EditText et) {
        et.setTextColor(Color.BLACK);
        et.setCompoundDrawablesWithIntrinsicBounds( 0, 0, 0, 0);
    }
    private void shake(View view) {
        ObjectAnimator
                .ofFloat(view, "translationX", 0, 25, -25, 25, -25,15, -15, 6, -6, 0)
                .setDuration(500)
                .start();
    }

    private Boolean checkSpecials(String s) {
        Pattern p = Pattern.compile("[^a-zA-Z\\u0590-\\u05fe ']", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(s);
        if (m.find()) return true;
        else
            return false;
    }

    private Boolean checkAddHebrew(String string) {
        Pattern p = Pattern.compile("[a-zA-Z]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(string);
        if (m.find()) return false;
        return checkRtl(string);
    }

    private Boolean checkAddEng(String string) {
        Pattern p = Pattern.compile("[\u0590-\u05fe]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(string);
        if (m.find()) return true;
        return checkRtl(string);
    }

    private  void checkIfWordExists(EditText target, String child, String string) {
        final EditText et = target;
        final Query query = brainHelper.getDb().getReference(BrainHelper.WORDS)
                .orderByChild(child).equalTo(string).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Toast.makeText(MainSearchActivity.this, "המילה כבר קיימת, יש לך עוד מילה להציע?", Toast.LENGTH_LONG).show();
                    errorInput(et);
                }
                else {
                    Toast.makeText(MainSearchActivity.this, "המילה לא קיימת במאגר, כל הכבוד!", Toast.LENGTH_SHORT).show();
                    goodInput(et);
                }

                query.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void shakeY(View view) {
        ObjectAnimator
                .ofFloat(view, "translationY", 0, 50, -50, 50, -50,25, -25, 15, -15, 5, -5, 0)
                .setDuration(1250)
                .start();
    }

    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        EditText et;
        private DownloadImageTask(ImageView bmImage, EditText et) {
            this.bmImage = bmImage;
            this.et = et;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("ERRORRRR", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if(result == null) {
                bmImage.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "הלינק לתמונה שגוי!", Toast.LENGTH_SHORT).show();
                errorInput(et);
            }else {
                bmImage.setVisibility(View.VISIBLE);
                bmImage.setImageBitmap(result);
                goodInput(et);
            }
        }

    }


}

