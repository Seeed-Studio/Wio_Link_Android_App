package cc.seeed.iot.ui_setup;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cc.seeed.iot.R;

/**
 * Created by tenwong on 15/6/30.
 */
public class SignUpDialog extends AlertDialog {
    Context context;

    public SignUpDialog(Context context) {
        super(context);
        this.context = context;
        init();
    }

    void init() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.sign_up, (ViewGroup) findViewById(R.id.sign_up));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sign Up");
        builder.setView(layout);
        builder.setPositiveButton("Sign up", null);
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }

}
