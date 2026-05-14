package app;

import app.Interface.aCallBack;
import app.Interface.bCallBack;

import javax.swing.*;

public class Controller {
    private Services services;

    public Controller(){
        services = new Services();
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
}

