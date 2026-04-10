package com.example.spendsmart;

public interface GeminiCallBack {
    public void onSuccess(String result);
    public void onFailure(Throwable error);
}