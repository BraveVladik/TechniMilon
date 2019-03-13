package coldapp.giladpinker.technimilon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DisplayerSentencesAdapter  extends BaseAdapter {

    ArrayList<Sentence> sentences;
    Context context;

    public DisplayerSentencesAdapter(ArrayList<Sentence> sentences, Context context) {
        this.sentences = sentences;
        this.context = context;
    }

    @Override
    public int getCount() {
        return sentences.size();
    }

    @Override
    public Object getItem(int position) {
        return sentences.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if(view == null) {
            LayoutInflater inf = LayoutInflater.from(context);
            view = inf.inflate(R.layout.item_lv_sentence, null);
        }

        ((TextView)view.findViewById(R.id.wordprofile_tv_eng)).setText("\"" + sentences.get(position).getEngSent() + "\"");
        ((TextView)view.findViewById(R.id.wordprofile_tv_heb)).setText("\"" + sentences.get(position).getHebSent() + "\"");

        return view;
    }
}
