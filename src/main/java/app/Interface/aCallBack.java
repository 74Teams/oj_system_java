package app.Interface;

import app.Services;

public interface aCallBack {
    void onSuccess(Services.Result rs);
    void onError(String err);
}
