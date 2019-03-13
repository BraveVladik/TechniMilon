package coldapp.giladpinker.technimilon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DisplayerSynonymAdapter  extends BaseAdapter {

    ArrayList<Synonym> synonyms;
    Context context;

    public DisplayerSynonymAdapter(ArrayList<Synonym> synonyms, Context context) {
        this.synonyms = synonyms;
        this.context = context;
    }

    @Override
    public int getCount() {
        return synonyms.size();
    }

    @Override
    public Object getItem(int position) {
        return synonyms.get(position);
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
            view = inf.inflate(R.layout.item_lv_synonym, null);
        }

        ((TextView)view.findViewById(R.id.wordprofile_tv_synonymeng)).setText(synonyms.get(position).getEngSyn());
        ((TextView)view.findViewById(R.id.wordprofile_tv_synonymheb)).setText(synonyms.get(position).getHebSyn());

        return view;
    }
}

