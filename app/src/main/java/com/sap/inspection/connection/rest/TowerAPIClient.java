package com.sap.inspection.connection.rest;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.rindang.zconfig.AppConfig;
import com.sap.inspection.R;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.util.PrefUtil;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TowerAPIClient {

    private static String API_BASE_URL = AppConfig.getInstance().getV1() + "/";

    private static HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC);

    private static AuthenticationInterceptor authenticationInterceptor = new AuthenticationInterceptor();

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Gson gson = new GsonBuilder().setLenient().create();

    private static Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson));

    private static Retrofit retrofit = retrofitBuilder.build();

    static <S> S createService(Class<S> serviceClass) {

        if (!httpClient.interceptors().contains(loggingInterceptor))
            httpClient.addInterceptor(loggingInterceptor);

        if (!httpClient.interceptors().contains(authenticationInterceptor)) {
            httpClient.addInterceptor(authenticationInterceptor);
            retrofitBuilder.client(httpClient.build());
            retrofit = retrofitBuilder.build();
        }
        return retrofit.create(serviceClass);
    }


    public static class AuthenticationInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {

            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();
            HttpUrl modifiedHttpUtl;
            HttpUrl.Builder modifiedHttpUrlBuilder = originalHttpUrl.newBuilder();

            String accessToken = PrefUtil.getStringPref(R.string.user_authToken, null);
            if (!TextUtils.isEmpty(accessToken)) {
                modifiedHttpUrlBuilder.addQueryParameter("access_token", accessToken);
            }

            modifiedHttpUtl = modifiedHttpUrlBuilder.build();
            Request.Builder modifiedReqBuilder = original.newBuilder()
                    .url(modifiedHttpUtl);

            Request modified = modifiedReqBuilder.build();
            return chain.proceed(modified);
        }
    }
}
