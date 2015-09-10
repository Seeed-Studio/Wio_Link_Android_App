package cc.seeed.iot.ui_main;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.R;
import cc.seeed.iot.webapi.model.NodeResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by tenwong on 15/7/1.
 */
public class AddNodeDialogFragment extends DialogFragment {
    Context context;
    NoticeDialogListener mListener;

    AlertDialog alertDialog;

    private EditText mNodeName;

    private View mProgressView;
    private View mAddNodeView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        try {
            mListener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_node, null);

        mNodeName = (EditText) view.findViewById(R.id.node_name);
        mProgressView = view.findViewById(R.id.login_progress);
        mAddNodeView = view.findViewById(R.id.add_node);

        builder.setView(view);
        builder.setTitle("Add Node");
        builder.setPositiveButton("Create", null);
        builder.setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        alertDialog = (AlertDialog) getDialog();
        if (alertDialog != null) {
            Button positiveButton = (Button) alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = false;
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    attemptLogin();
                    if (wantToCloseDialog)
                        alertDialog.dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }

    private void attemptLogin() {

        final String node_name = mNodeName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            IotApi api = new IotApi();
            User user = ((MyApplication) getActivity().getApplication()).getUser();
            api.setAccessToken(user.user_key);
            IotService iot = api.getService();
            iot.nodesCreate(node_name, new Callback<NodeResponse>() {
                @Override
                public void success(NodeResponse nodeResponse, Response response) {
                    String status = nodeResponse.status;
                    if (status.equals("200")) {
                        alertDialog.dismiss();
                        Node node = new Node();
                        node.name = node_name;
                        node.node_key = nodeResponse.node_key;
                        node.node_sn = nodeResponse.node_sn;

                        mListener.onAddNode(node);
                    } else {
                        showProgress(false);
                        mNodeName.setError(nodeResponse.msg);
                        mNodeName.requestFocus();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    showProgress(false);
                    Toast.makeText(context, "Connect sever fail...", Toast.LENGTH_LONG).show();
                }
            });
        }

    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mAddNodeView.setVisibility(show ? View.GONE : View.VISIBLE);
            mAddNodeView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAddNodeView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mAddNodeView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public interface NoticeDialogListener {
        public void onAddNode(Node node);
    }
}


