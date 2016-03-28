package com.dut.dutfinace.activity;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dut.dutfinace.AccountUtils;
import com.dut.dutfinace.Const;
import com.dut.dutfinace.JSONBuilder;
import com.dut.dutfinace.R;
import com.dut.dutfinace.URLBuilder;
import com.dut.dutfinace.network.AsyncResponseParser;

import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends ToolbarActivity {

    private EditText mAccount;
    private EditText mPassword;
    private EditText mCheckPassword;
    private EditText mPhone;
    private EditText mMail;
    private EditText mIdentity;

    private TextView mErrorAccount;
    private TextView mErrorPassword;
    private TextView mErrorCheckPassword;
    private TextView mErrorMail;
    private TextView mErrorPhone;
    private TextView mErrorIdentity;

    private Spinner mGender;

    private final OkHttpClient mClient = new OkHttpClient();
    Pattern mPasswordPattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{3,20}$");
    private String[] genders = {"男", "女"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAccount = (EditText) findViewById(R.id.account);
        mPassword = (EditText) findViewById(R.id.password);
        mCheckPassword = (EditText) findViewById(R.id.re_password);
        mPhone = (EditText) findViewById(R.id.phone);
        mMail = (EditText) findViewById(R.id.mail);
        mIdentity = (EditText) findViewById(R.id.identity);

        mErrorAccount = (TextView) findViewById(R.id.err_account);
        mErrorPassword = (TextView) findViewById(R.id.err_password);
        mErrorCheckPassword = (TextView) findViewById(R.id.err_check_password);
        mErrorAccount = (TextView) findViewById(R.id.err_account);
        mErrorMail = (TextView) findViewById(R.id.err_mail);
        mErrorPhone = (TextView) findViewById(R.id.err_password);
        mErrorIdentity = (TextView) findViewById(R.id.err_identity);

        mGender = (Spinner) findViewById(R.id.gender);
        ArrayAdapter<String> gender = new ArrayAdapter<String>(RegisterActivity.this, android.R.layout.simple_spinner_item, genders);
        mGender.setAdapter(gender);

        mAccount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) checkAccount();
            }
        });

        mPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) checkPassword();
            }
        });

        mCheckPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) checkRePassword();
            }
        });

        mPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) checkPhone();
            }
        });

        mMail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) checkMail();
            }
        });

        mIdentity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) checkIdentity();
            }
        });
    }

    public void onRegister(View view) {
        if (!checkAccount() || !checkPassword() || !checkRePassword() || !checkMail() || !checkPhone() || !checkIdentity())
            return;

        Toast.makeText(this, R.string.prompt_registering, Toast.LENGTH_SHORT).show();

        String json = new JSONBuilder().setParameter(
                "user_id", mAccount.getText().toString(),
                "pwd", mPassword.getText().toString(),
                "mobil_phone", mPhone.getText().toString(),
                "sex",(mGender.getSelectedItemPosition() + 1) + "").build();

        RequestBody body = RequestBody.create(Const.JSON, json);
        String url = new URLBuilder(RegisterActivity.this).host(R.string.host).path("DUT", "api", "User", "Register").toString();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mClient.newCall(request).enqueue(new AsyncResponseParser(this, this) {

            @Override
            protected void parseResponse(final JSONObject jsonObject) throws Exception {
                if (mAccount != null) mAccount.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Reg", jsonObject.toString());
                        int resCode = jsonObject.optInt("reg_code");
                        if (resCode == 1) {
                            AccountUtils.setToken(RegisterActivity.this, jsonObject.optString("session_id"));
                            AccountUtils.setAccount(RegisterActivity.this, mAccount.getText().toString(), mPassword.getText().toString(), jsonObject.optString("usersys_id"));
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else if (resCode == 2) {
                            Toast.makeText(RegisterActivity.this, "註冊失敗", Toast.LENGTH_SHORT).show();
                        } else if (resCode == 3) {
                            Toast.makeText(RegisterActivity.this, "身分證號碼已經註冊過", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private int checkIdentity(String identity) {
        if (identity.length() != 10) {
            return Const.ERROR_ID_LENGTH;
        }

        try {
            char idChars[] = identity.toCharArray();
            int id0 = getAlphaCode(idChars[0]) / 10;
            int id1 = getAlphaCode(idChars[0]) % 10;

            idChars[0] = (char) (id1 + 48);

            int checkoutSum = id0;
            int position = 0;
            for (int i = 9; i > 0; i--) {
                int num = Integer.valueOf(idChars[position]) - 48;
                checkoutSum += num * i;
                position++;
            }

            checkoutSum += Integer.valueOf(idChars[position]) - 48;

            if (checkoutSum % 10 == 0) {
                return Const.SUCCESS;
            }
            return Const.ERROR_ID_FORMAT;

        } catch (Exception e) {
            return Const.ERROR_ID_FORMAT;
        }
    }

    private boolean checkAccount() {
        if (mAccount.getText().length() == 0) {
            mErrorAccount.setVisibility(View.VISIBLE);
            mErrorAccount.setText(R.string.error_input_account);
        } else if (mAccount.getText().length() > 20) {
            mErrorAccount.setVisibility(View.VISIBLE);
            mErrorAccount.setText(R.string.error_account_length);
        } else {
            mErrorAccount.setVisibility(View.GONE);
            return  true;
        }
        return false;
    }

    public boolean checkPassword() {
        Matcher matcher = mPasswordPattern.matcher(mPassword.getText().toString());
        if (mPassword.getText().length() == 0) {
            mErrorPassword.setVisibility(View.VISIBLE);
            mErrorPassword.setText(R.string.error_input_password);
        } else if (!matcher.matches()) {
            mErrorPassword.setVisibility(View.VISIBLE);
            mErrorPassword.setText(R.string.error_password_format);
        } else {
            mErrorPassword.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    private boolean checkRePassword() {
        if (mCheckPassword.getText().length() == 0) {
            mErrorCheckPassword.setVisibility(View.VISIBLE);
            mErrorCheckPassword.setText(R.string.error_input_password);
        } else if (!mCheckPassword.getText().toString().equals(mPassword.getText().toString())) {
            mErrorCheckPassword.setVisibility(View.VISIBLE);
            mErrorCheckPassword.setText(R.string.error_password_not_same);
        } else {
            mErrorCheckPassword.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    private boolean checkMail() {
        if (mMail.getText().length() == 0) {
            mErrorMail.setVisibility(View.VISIBLE);
        } else {
            mErrorMail.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    private boolean checkPhone() {
        if (mPhone.getText().length() == 0) {
            mErrorPhone.setVisibility(View.VISIBLE);
        } else {
            mErrorPhone.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    private boolean checkIdentity() {
        if (mIdentity.getText().length() > 0 && checkIdentity(mIdentity.getText().toString()) == Const.SUCCESS) {
            mErrorIdentity.setVisibility(View.GONE);
            return true;
        } else if (mIdentity.getText().length() == 0) {
            mErrorIdentity.setVisibility(View.VISIBLE);
            mErrorIdentity.setText(R.string.error_input_identity);
        } else {
            mErrorIdentity.setVisibility(View.VISIBLE);
            mErrorIdentity.setText(R.string.error_invalid_identity);
        }
        return false;
    }

    private int getAlphaCode(char a) {
        if (a == 'I') return 34;
        if (a == 'O') return 35;

        int offset = 0;
        if (a > 'I' && a < 'O') offset = 1;
        if (a > 'O') offset = 2;
        int ascii = Integer.valueOf(a);
        return ascii - 55 - offset;
    }
}
