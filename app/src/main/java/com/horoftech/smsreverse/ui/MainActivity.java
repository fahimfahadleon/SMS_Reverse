package com.horoftech.smsreverse.ui;

import static com.horoftech.smsreverse.viewmodel.ActivityMainViewModel.SAVE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.horoftech.smsreverse.R;
import com.horoftech.smsreverse.databinding.ActivityMainBinding;
import com.horoftech.smsreverse.utils.SmsReceiver;
import com.horoftech.smsreverse.viewmodel.ActivityMainViewModel;

import org.json.JSONObject;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    ActivityMainViewModel model;
    SmsReceiver smsReceiver;
    OkHttpClient client;
    boolean shouldSend = true;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new ViewModelProvider(this).get(ActivityMainViewModel.class);
        binding.setViewModel(model);
        binding.setLifecycleOwner(this);
        preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        if (!isInternetAvailable()) {
            binding.receivedMessage.setText("Please Turn On Internet Connection And Restart Application");
            binding.time.setVisibility(View.INVISIBLE);
            binding.receivedMessage.setTextColor(getResources().getColor(R.color.RED));
        } else {
            observe();
            client = getUnsafeOkHttpClient();
            reference = FirebaseDatabase.getInstance().getReference();
            model.setToken(getSP("token", ""));
            model.setReceiver(getSP("rec", ""));
            model.setUrl(getSP("url", ""));
            if (!TextUtils.isEmpty(model.getToken())) {
                binding.token.setText(model.getToken());
                binding.editText.setText(model.getUrl());
                binding.editText1.setText(model.getReceiver());
                validate();
            }
        }
    }


    void validate() {
        Calendar c = Calendar.getInstance();
//        c.add(Calendar.DAY_OF_MONTH,1);
        Date d = c.getTime();
//        reference.child("time").setValue();
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault());
//
//        String formattedDate = sdf.format(d);
//        Log.e("date",formattedDate);
        reference.child(model.getToken()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault());
                        Date d2 = sdf.parse(snapshot.getValue(String.class));
                        String formattedDate = sdf.format(d2);
                        long i = getDifferenceInDays(d, d2);
                        if (i < 3 && i >= 0) {
                            Toast.makeText(MainActivity.this, "Your token will expire in " + i + 1 + " days", Toast.LENGTH_SHORT).show();
                        } else if (i < 0) {
                            shouldSend = false;
                        }
                        binding.receivedMessage.setText("Everything seems fine");

                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                } else {
                    binding.receivedMessage.setText("The token you entered is wrong. Please check the token and try again.");
                    shouldSend = false;
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private long getDifferenceInDays(Date startDate, Date endDate) {
        long diffInMillis = endDate.getTime() - startDate.getTime();
        return TimeUnit.MILLISECONDS.toDays(diffInMillis);
    }

    @SuppressLint("SetTextI18n")
    private void observe() {
        model.message.observe(this, jsonObject -> {
            if (TextUtils.isEmpty(model.getUrl()) || TextUtils.isEmpty(model.getReceiver())) {
                binding.editText.setError("Empty!!!");
                binding.editText.requestFocus();
                binding.editText1.setError("Empty!!!");
                binding.editText1.requestFocus();
                Toast.makeText(this, "Post Url or receiver Not Found!", Toast.LENGTH_SHORT).show();

            } else {
                try {
                    jsonObject.put("receiver", model.getReceiver());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                new Thread(() -> {
                    if (shouldSend) {
                        sendPostRequest(model.getUrl(), jsonObject);
                    } else {
                        Toast.makeText(MainActivity.this, "Token Expired! Renew Please!", Toast.LENGTH_SHORT).show();
                    }

                }).start();
                binding.receivedMessage.setText(jsonObject.optString("Receiver") + ": \n" + jsonObject.optString("message"));
                binding.time.setText("Time: \n" + getCurrentTime());
            }
        });
        model.action.observe(this, s -> {
            if (s.equals(SAVE)) {
                String token = binding.token.getText().toString();
                String url = binding.editText.getText().toString();
                String receiver = binding.editText1.getText().toString();

                model.setToken(token);
                model.setUrl(url);
                model.setReceiver(receiver);
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                setSP("token", token);
                setSP("url", url);
                setSP("rec", receiver);

                validate();


            }
        });
    }

    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(calendar.getTime());
    }

    void registerReceiver() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        }
        try {
            smsReceiver = new SmsReceiver(model);
            IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            intentFilter.setPriority(1000);
            registerReceiver(smsReceiver, intentFilter);
        } catch (Exception e) {
            e.fillInStackTrace();
        }

    }

    @Override
    protected void onResume() {
        registerReceiver();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(smsReceiver);
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Need permission to use this app!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                registerReceiver();
            }
        }
    }


    public void sendPostRequest(String url, JSONObject jsonBody) {
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.fillInStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = null;
                    if (response.body() != null) {
                        responseData = response.body().string();
                    }
                    Log.e("response", "Response: " + responseData);
                } else {
                    Log.e("failed", "Response: " + response.code());
                }
            }
        });
    }

    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            @SuppressLint("CustomX509TrustManager") final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.connectTimeout(10, TimeUnit.SECONDS);
            builder.readTimeout(10, TimeUnit.SECONDS);
            builder.writeTimeout(10, TimeUnit.SECONDS);
            builder.hostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities networkCapabilities =
                connectivityManager.getNetworkCapabilities(network);

        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }


    SharedPreferences preferences;

    public void setSP(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getSP(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }


}