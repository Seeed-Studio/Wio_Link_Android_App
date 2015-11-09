package cc.seeed.iot.ui_login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.Common;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.Response;
import cc.seeed.iot.webapi.model.UserResponse;
import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by tenwong on 15/7/1.
 */
public class SignInDialogFragment extends DialogFragment {
    public static final String TAG = "SignInDialogFragment";
    Context context;
    User user;

    AlertDialog alertDialog;
    AlertDialog resetPasswordDialog;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private TextView mForgotPwdView;
    private TextView mSwitchAreaView;

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
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_sign_in, null);


        mEmailView = (AutoCompleteTextView) view.findViewById(R.id.email);
        mPasswordView = (EditText) view.findViewById(R.id.password);
        mForgotPwdView = (TextView) view.findViewById(R.id.forgot_password);
        mSwitchAreaView = (TextView) view.findViewById(R.id.switch_area);
        mProgressView = view.findViewById(R.id.login_progress);
        mLoginFormView = view.findViewById(R.id.email_login_form);

//        boolean result = Util.checkIsChina(getActivity());
//        if (result) {
        mSwitchAreaView.setVisibility(View.VISIBLE);
//        } else {
//            mSwitchAreaView.setVisibility(View.GONE);
//        }

        if (((MyApplication) getActivity().getApplication()).getServerUrl().equals(Common.OTA_CHINA_URL)) {
            mSwitchAreaView.setText(R.string.setup_switch_international);
            ((MyApplication) getActivity().getApplication()).setExchangeServerUrl(Common.EXCHANGE_CHINA_URL);
        } else if (((MyApplication) getActivity().getApplication()).getServerUrl().equals(Common.OTA_INTERNATIONAL_URL)) {
            mSwitchAreaView.setText(R.string.setup_switch_china);
            ((MyApplication) getActivity().getApplication()).setExchangeServerUrl(Common.EXCHANGE_INTERNATIONAL_URL);
        } else
            mSwitchAreaView.setText(((MyApplication) getActivity().getApplication()).getServerUrl());



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(R.string.setup_signin);
        builder.setPositiveButton(R.string.setup_signin, null);
        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }


    @Override
    public void onStart() {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        alertDialog = (AlertDialog) getDialog();
        if (alertDialog != null) {
            Button positiveButton;
            positiveButton = (Button) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attemptLogin();
                }
            });
        }


        mSwitchAreaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSwitchAreaView.getText().toString().equals(getString(R.string.setup_switch_international))) {
                    mSwitchAreaView.setText(R.string.setup_switch_china);
                    ((MyApplication) getActivity().getApplication()).setServerUrl(Common.OTA_INTERNATIONAL_URL);
                    ((MyApplication) getActivity().getApplication()).setExchangeServerUrl(Common.EXCHANGE_INTERNATIONAL_URL);
                } else {
                    mSwitchAreaView.setText(R.string.setup_switch_international);
                    ((MyApplication) getActivity().getApplication()).setServerUrl(Common.OTA_CHINA_URL);
                    ((MyApplication) getActivity().getApplication()).setExchangeServerUrl(Common.EXCHANGE_CHINA_URL);
                }
            }
        });

        mForgotPwdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View forgetPwdView = inflater.inflate(R.layout.dialog_email_input, null);
                final AutoCompleteTextView emailView =
                        (AutoCompleteTextView) forgetPwdView.findViewById(R.id.email);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Reset Password");
                builder.setView(forgetPwdView);
                builder.setPositiveButton(R.string.ok, null);
                builder.setNegativeButton(R.string.cancel, null);
                builder.setCancelable(false);
                resetPasswordDialog = builder.create();
                resetPasswordDialog.show();

                Button positiveButton = resetPasswordDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e(TAG, "sd");
                        String email = emailView.getText().toString();
                        if (!isEmailValid(email)) {
                            emailView.setError("Invalid email");
                            return;
                        }
                        resetPassword(email, emailView);
                    }
                });
            }
        });
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
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("Require email");
            focusView = mEmailView;
            cancel = true;

        } else if (!isEmailValid(email)) {
            mEmailView.setError("Invalid email");
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
                        alertDialog.dismiss();
                        user.email = fianlEmail;
                        user.user_key = userResponse.token;
                        user.user_id = userResponse.user_id;
                        ((MyApplication) getActivity().getApplication()).setUser(user);
                        ((MyApplication) getActivity().getApplication()).setLoginState(true);
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
                    Toast.makeText(context, "connect server fail...", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private boolean resetPassword(String email, final AutoCompleteTextView emailView) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Resetting your password...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        IotApi api = new IotApi();
        IotService iot = api.getService();
        iot.userRetrievePassword(email, new Callback<Response>() {
            @Override
            public void success(Response response, retrofit.client.Response response1) {
                String status = response.status;
                if (status.equals("200")) {
                    resetPasswordDialog.dismiss();
                    progressDialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setPositiveButton(R.string.ok, null).create();
                    builder.setTitle("Success");
                    builder.setMessage(response.msg);
                    builder.show();
                } else {
                    progressDialog.dismiss();
                    emailView.setError(response.msg);
                    emailView.requestFocus();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(context, "connect server fail...", Toast.LENGTH_LONG).show();
            }
        });

        return true;
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


