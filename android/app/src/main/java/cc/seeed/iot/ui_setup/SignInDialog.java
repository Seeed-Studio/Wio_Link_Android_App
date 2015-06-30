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
public class SignInDialog extends AlertDialog {
    Context context;

    public SignInDialog(Context context) {
        super(context);
        this.context = context;
        init();
    }

    void init() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.sign_in, (ViewGroup) findViewById(R.id.sign_in));

        Builder builder = new Builder(context);
        builder.setTitle("Sign In");
        builder.setView(layout);
        builder.setPositiveButton("Sign In", null);
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }

}
