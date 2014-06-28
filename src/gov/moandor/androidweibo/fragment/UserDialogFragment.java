package gov.moandor.androidweibo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import gov.moandor.androidweibo.bean.WeiboUser;

public class UserDialogFragment extends DialogFragment {
    public static final String USER = "user";

    private WeiboUser mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = getArguments().getParcelable(USER);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mUser.name);
        // TODO: Implement this method
        return builder.create();
    }
}
