package gov.moandor.androidweibo.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";

    public static ProgressDialogFragment newInstance(String message) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString(TITLE);
            String message = args.getString(MESSAGE);
            dialog.setTitle(title);
            dialog.setMessage(message);
        }
        dialog.setCanceledOnTouchOutside(true);
        dialog.setIndeterminate(true);
        return dialog;
    }
}
