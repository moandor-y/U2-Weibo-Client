package gov.moandor.androidweibo.fragment;
import android.support.v4.app.DialogFragment;
import android.app.Dialog;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.View;
import gov.moandor.androidweibo.R;
import android.widget.EditText;
import android.content.DialogInterface;
import gov.moandor.androidweibo.util.ActivityUtils;

public class FindUserDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_find_user, null);
		final EditText editText = (EditText) view.findViewById(R.id.edit);
		builder.setView(view);
		builder.setTitle(R.string.find_user);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getActivity().startActivity(ActivityUtils.userActivity(editText.getText().toString()));
			}
		});
		builder.setNegativeButton(R.string.cancel, null);
		return builder.create();
	}
}
