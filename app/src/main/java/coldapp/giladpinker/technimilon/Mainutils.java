package coldapp.giladpinker.technimilon;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

public class Mainutils {

    private Context context;

    public Mainutils(Context context) {
        this.context = context;
    }
    void setEditTextUnfocusTimer(EditText et) {

        final EditText editText = et;
        final long STOP_TYPE_DELAY = 750; // Timer of stp of typing.
        final long[] last_text_edit = {0}; // Last Time typed array.
        final Handler handler = new Handler(); // Threads handler.
        final String[] final_string = new String[1]; // This is the final written text in the searchbar.

        final Runnable input_finish_checker = new Runnable() {
            public void run() {
                if (System.currentTimeMillis() > (last_text_edit[0] + STOP_TYPE_DELAY - 500)) {

                    if (editText.isFocused()) {
                        hideKeyboard(editText);
                        editText.clearFocus();
                    }
                }
            }
        };

        editText.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged(CharSequence s, int start, int count,
                                                                          int after) {
                                            }

                                            @Override
                                            public void onTextChanged(final CharSequence s, int start, int before,
                                                                      int count) {
                                                // You need to remove this to run only once
                                                handler.removeCallbacks(input_finish_checker);
                                            }

                                            @Override
                                            public void afterTextChanged(final Editable s) {
                                                // avoid triggering event when text is empty
                                                if (s.length() > 0) {
                                                    last_text_edit[0] = System.currentTimeMillis();
                                                    final_string[0] = s.toString();
                                                    handler.postDelayed(input_finish_checker, STOP_TYPE_DELAY);
                                                }
                                            }
                                        }
        );
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    static void DialogNoUpload(Context context) {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dialog_changesentence, null);

        AlertDialog alertDialog;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        alertDialogBuilder.setView(promptsView);

        final Button btn_accept = promptsView.findViewById(R.id.dialog_btn_accept);
        alertDialog = alertDialogBuilder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        final AlertDialog copyAlert = alertDialog;
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyAlert.dismiss();
            }
        });
    }

}
