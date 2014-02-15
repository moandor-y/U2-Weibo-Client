package gov.moandor.androidweibo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import gov.moandor.androidweibo.R;

public class AddPictureDialogFragment extends DialogFragment {
    private DialogInterface.OnClickListener mOnClickListener;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.add_picture);
        builder.setItems(R.array.add_picture_dialog_items, mOnClickListener);
        return builder.create();
    }
    
    public void setOnClickListener(DialogInterface.OnClickListener l) {
        mOnClickListener = l;
    }
}
