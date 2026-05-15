package app.Interface;

import app.Services;
import java.util.List;

public interface cCallBack {
    void onSuccess(List<Services.Testcase> cases);
    void onError(String error);
}
