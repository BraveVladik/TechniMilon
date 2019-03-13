package coldapp.giladpinker.technimilon;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BrainHelper {
    static Integer uploadedWordsCount = 0, uploadedDataCount = 0;
    private Context context;


    public FirebaseDatabase getDb() {
        return db;
    }

    private FirebaseDatabase db;
    private DatabaseReference dbRef;



    static final String WORDS = "wordslist", DATA = "wordsdata";
    BrainHelper(Context context) {

        this.context = context;

        db = FirebaseDatabase.getInstance();


    }


}


