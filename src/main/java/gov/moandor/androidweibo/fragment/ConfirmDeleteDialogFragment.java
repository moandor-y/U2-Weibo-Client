package gov.moandor.androidweibo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import gov.moandor.androidweibo.R;
import gov.moandor.androidweibo.concurrency.MyAsyncTask;

public class ConfirmDeleteDialogFragment extends DialogFragment {
    MyAsyncTask<Void, Void, Void> mTask;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.confirm_delete);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTask.execute();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        return builder.create();
    }

    public void setTask(MyAsyncTask<Void, Void, Void> task) {
        mTask = task;
    }
}
