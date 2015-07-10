package cc.seeed.iot.ui_setup;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cc.seeed.iot.MainActivity;
import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.UserResponse;
import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by tenwong on 15/7/1.
 */
public class SignInDialogFragment extends DialogFragment {
    Context context;
    User user;

    AlertDialog alertDialog;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    private View mProgressView;
    private View mLoginFormView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        user = ((MyApplication) getActivity().getApplication()).getUser();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.sign_in, null);

        mEmailView = (AutoCompleteTextView) view.findViewById(R.id.email);
        mPasswordView = (EditText) view.findViewById(R.id.password);
        mProgressView = view.findViewById(R.id.login_progress);
        mLoginFormView = view.findViewById(R.id.email_login_form);

        builder.setView(view);
        builder.setTitle("Sign In");
        builder.setPositiveButton("Sign In", null);
        builder.setNegativeButton("Cancel", null);

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
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError("invalid Password");
            focusView = mPasswordView;
            cancel = true;
            Log.e("iot", "password error");
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("Require email");
            focusView = mEmailView;
            cancel = true;

        } else if (!isEmailValid(email)) {
            mEmailView.setError("Error email");
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            final String fianlEmail = email;
            IotApi api = new IotApi();
            IotService iot = api.getService();
            iot.userLogin(email, password, new Callback<UserResponse>() {
                @Override
                public void success(UserResponse userResponse, retrofit.client.Response response) {
                    String status = userResponse.status;
                    if (status.equals("200")) {
//                        Toast.makeText(context, userResponse.msg, Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                        user.email = fianlEmail;
                        user.user_key = userResponse.token;
                        ((MyApplication) getActivity().getApplication()).setUser(user);
                        Intent intent = new Intent(context, MainScreenActivity.class);
                        context.startActivity(intent);
                    } else {
                        showProgress(false);
                        mEmailView.setError(userResponse.msg);
                        mEmailView.requestFocus();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(context, "连接服务器失败", Toast.LENGTH_LONG).show();
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }
}


