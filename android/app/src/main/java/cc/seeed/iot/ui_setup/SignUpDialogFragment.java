package cc.seeed.iot.ui_setup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cc.seeed.iot.R;

/**
 * Created by tenwong on 15/6/30.
 */
public class SignUpDialogFragment extends DialogFragment {
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.sign_up, null);

        builder.setView(view);
        builder.setTitle("Sign Up");
        builder.setPositiveButton("Sign up", null);
        builder.setNegativeButton("Cancel", null);

        return builder.create();
    }
}
