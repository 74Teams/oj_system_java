package app;

import app.Interface.aCallBack;
import app.Interface.bCallBack;
import app.Interface.cCallBack;

import java.util.List;

import javax.swing.*;

public class Controller {
    private Services services;

    public Controller(){
        services = new Services();
    }

    public void setApiKey(String apiKey){
        services.setApiKey(apiKey);
    }

    public void anlyzeAsync(String problem, aCallBack cb){
        new SwingWorker<Services.Result, Void>(){
            @Override
            protected Services.Result doInBackground() throws Exception {
                return services.generateAnalyze(problem);
            }
            @Override
            protected void done(){
                try {
                    Services.Result result = get();
                    cb.onSuccess(result);
                } catch (Exception e) {
                    cb.onError(e.getMessage());
                }
            }
        }.execute();
    }
    public void generateCodeAsync(String problem, String type, String language, Services.Result rs, bCallBack cb){
        new SwingWorker<String, Void>(){
            @Override
            protected String doInBackground() throws Exception {
                return services.generateCode(problem, type, language, rs);
            }
            @Override
            protected void done(){
                try {
                    cb.onSuccess(get());
                } catch (Exception e) {
                    cb.onError(e.getMessage());
                }
            }
        }.execute();
    }

    public void generateTestcaseAsync(String problem, int count, String type, Services.Result rs, cCallBack cb){
        new SwingWorker<List<Services.Testcase>, Void>(){
            @Override
            protected List<Services.Testcase> doInBackground() throws Exception {
                return services.generateTestcases(problem, count, type, rs);
            }
            @Override
            protected void done(){
                try {
                    cb.onSuccess(get());
                } catch (Exception e) {
                    cb.onError(e.getMessage());
                }
            }
        }.execute();
    }
}

