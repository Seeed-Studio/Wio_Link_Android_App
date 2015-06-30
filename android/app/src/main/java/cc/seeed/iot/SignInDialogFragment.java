package cc.seeed.iot;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ViewSwitcher;

/**
 * Created by xiaobo on 15/7/1.
 */
public class SignInDialogFragment extends DialogFragment {

    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_signin, null);

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
