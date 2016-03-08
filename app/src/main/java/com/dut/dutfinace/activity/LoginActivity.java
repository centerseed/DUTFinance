package com.dut.dutfinace.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dut.dutfinace.AccountUtils;
import com.dut.dutfinace.Const;
import com.dut.dutfinace.JSONBuilder;
import com.dut.dutfinace.R;
import com.dut.dutfinace.URLBuilder;
import com.dut.dutfinace.network.AsyncResponseParser;
import com.dut.dutfinace.provider.MainProvider;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements AsyncResponseParser.NetError {

    private static final int REQUEST_READ_CONTACTS = 0;
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mRegister;
    private final OkHttpClient mClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mRegister = (TextView) findViewById(R.id.register);

        mRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String account = AccountUtils.getAccount(this);
        String password = AccountUtils.getPassword(this);

        mEmailView.setText(account);
        mPasswordView.setText(password);

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)) {
            String json = new JSONBuilder().setParameter(
                    "usersys_id", AccountUtils.getSysId(this),
                    "session_id", AccountUtils.getToken(this)).build();

            RequestBody body = RequestBody.create(Const.JSON, json);
            String url = new URLBuilder(this).host(R.string.host).path("DUT", "api", "UserInfo").toString();
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            mClient.newCall(request).enqueue(new AsyncResponseParser(this) {

                @Override
                protected void parseResponse(final JSONObject obj) throws Exception {
                    if (obj.optInt("session_status") == 1) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
    }

    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);

            String json = new JSONBuilder().setParameter(
                    "user_id", email,
                    "pwd", password).build();

            RequestBody body = RequestBody.create(Const.JSON, json);
            String url = new URLBuilder(LoginActivity.this).host(R.string.host).path("DUT", "api", "Login").toString();
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            mClient.newCall(request).enqueue(new AsyncResponseParser(this) {

                @Override
                protected void parseResponse(final JSONObject jsonObject) throws Exception {
                    if (mEmailView != null) mEmailView.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Login", jsonObject.toString());
                            showProgress(false);
                            int resCode = jsonObject.optInt("login_code");
                            if (resCode == 1) {
                                AccountUtils.setToken(LoginActivity.this, jsonObject.optString("session_id"));
                                AccountUtils.setAccount(LoginActivity.this, mEmailView.getText().toString(), mPasswordView.getText().toString(), jsonObject.optString("usersys_id"));
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else if (resCode == 2) {
                                Toast.makeText(LoginActivity.this, "登入失敗", Toast.LENGTH_SHORT).show();
                            } else if (resCode == 3) {
                                Toast.makeText(LoginActivity.this, "超過登入數量（最多支援兩個裝置同時登入）", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
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
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onNetError() {
        showProgress(false);
    }
}

