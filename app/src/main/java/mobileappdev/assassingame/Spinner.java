package mobileappdev.assassingame;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/24/2017
 */

public class Spinner extends ProgressDialog {

    public Spinner(Context context) {
        super(context);
    }

    public void show(String title, String msg, boolean cancelable) {
        setTitle(title);
        setMessage(msg);
        setCancelable(cancelable);
        show();
    }
}
