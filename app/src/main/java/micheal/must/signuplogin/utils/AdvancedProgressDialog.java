package micheal.must.signuplogin.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import micheal.must.signuplogin.R;

public class AdvancedProgressDialog {

    private AlertDialog dialog;
    private TextView messageText;
    private TextView progressText;
    private ProgressBar progressBar;

    public AdvancedProgressDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.custom_progress_dialog, null);

        messageText = customView.findViewById(R.id.dialog_message);
        progressText = customView.findViewById(R.id.progress_text);
        progressBar = customView.findViewById(R.id.progress_spinner);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomProgressDialogStyle)
                .setView(customView)
                .setCancelable(false);

        dialog = builder.create();
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void setMessage(String message) {
        if (messageText != null) {
            messageText.setText(message);
        }
    }

    public void setProgressText(String text) {
        if (progressText != null) {
            progressText.setText(text);
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        if (progressBar != null) {
            progressBar.setIndeterminate(indeterminate);
        }
    }

}
