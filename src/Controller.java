package src;

import javax.swing.SwingWorker;

public class Controller {
    private Phantich phantich;
    public Controller() {
        this.phantich = new Phantich();
    }
    public void analyzeAsync(String problemText, AnalysisCallback callback) {
        new SwingWorker<Phantich.Result, Void>() {
            @Override
            protected Phantich.Result doInBackground() throws Exception {
                return phantich.analyze(problemText);
            }
            @Override
            protected void done() {
                try {
                    Phantich.Result result = get();
                    callback.onSuccess(result);
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        }.execute();
    }

    public void generateCodeAsync(String problemText, String type, String language, CodeCallback callback) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return phantich.generateCode(problemText, type, language);
            }
            @Override
            protected void done() {
                try {
                    callback.onSuccess(get());
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        }.execute();
    }

    public interface AnalysisCallback {
        void onSuccess(Phantich.Result result);
        void onError(String error);
    }

    public interface CodeCallback {
        void onSuccess(String code);
        void onError(String error);
    }

}
