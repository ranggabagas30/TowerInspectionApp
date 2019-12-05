package com.sap.inspection.util;

import com.google.gson.JsonSyntaxException;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.sap.inspection.tools.DebugLog;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

public class NetworkUtil {
    public static String handleApiError(Throwable error) {
        final int API_STATUS_CODE_LOCAL_ERROR = 0;
        String errorMessage;
        if (error instanceof HttpException) {
            switch (((HttpException) error).code()) {
                case HttpsURLConnection.HTTP_BAD_REQUEST:
                    errorMessage = "Bad Request (Code: 400)";
                    break;
                case HttpsURLConnection.HTTP_UNAUTHORIZED:
                    errorMessage = "Username dan password tidak dikenali (Code: 401)";
                    break;
                case HttpsURLConnection.HTTP_FORBIDDEN:
                    errorMessage = "Forbidden access (Code: 404)";
                    break;
                case HttpsURLConnection.HTTP_INTERNAL_ERROR:
                    errorMessage = "Masalah pada server (Code: 500)";
                    break;
                case HttpURLConnection.HTTP_BAD_GATEWAY:
                    errorMessage = "Masalah pada server (Code: 502)";
                    break;
                case HttpURLConnection.HTTP_UNAVAILABLE:
                    errorMessage = "Service tidak tersedia (Code: 503)";
                    break;
                case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                    errorMessage = "Masalah pada server (Code: 504)";
                    break;
                case API_STATUS_CODE_LOCAL_ERROR:
                    errorMessage = "Masalah pada API (Code: 0)";
                    break;
                default:
                    errorMessage = error.getLocalizedMessage();
            }
        } else if (error instanceof JsonSyntaxException) {
            errorMessage = "Masalah pada format API (Code: 666)";
        } else if (error instanceof UnknownHostException) {
            errorMessage = "Koneksi terputus. Pastikan koneksi stabil";
        } else if (error instanceof SocketTimeoutException) {
            errorMessage = "Koneksi ke server terputus (Socket Timeout)";
        } else if (error instanceof ConnectException) {
            errorMessage = "Gagal melakukan koneksi ke server";
        } else {
            errorMessage = error.getMessage();
        }

        DebugLog.e("Connection Error: " + errorMessage, error);
        return errorMessage;
    }
}
