package cc.seeed.iot.ui_setup;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ViewSwitcher;

import cc.seeed.iot.R;

/**
 * Created by xiaobo on 15/7/1.
 */
public class SignInDialogFragment_backup extends DialogFragment {

    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        LayoutInflater inflater = getActivity().getLayoutInflater();
//        View view = inflater.inflate(R.layout.dialog_signin, null);
//
//        builder.setView(view).setTitle("Sign In");
//
//        return builder.create();
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_signin, container);
        // ******************************************
        // Only for Test Dialog View, pleas delete it
        // ******************************************
        Button mOk = (Button) view.findViewById(R.id.dialog_signin_yes);

        final ViewSwitcher mViewSwitcher = (ViewSwitcher) view.findViewById(R.id.signin_viewSwitcher);
        mViewSwitcher.setInAnimation(context, R.anim.abc_fade_in);
        mViewSwitcher.setOutAnimation(context, R.anim.abc_fade_out);

        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                dismiss();
                if (mViewSwitcher.getDisplayedChild() == 0) {
                    mViewSwitcher.showNext();
                } else {
                    mViewSwitcher.showPrevious();
                }
            }
        });

        return view;
    }


}
