package com.janclarin.gradepath.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.GradeComponent;

import java.text.DecimalFormat;

public class GradeComponentDialog extends BaseDialog {

    private static final String COURSE_ID = "Course ID";
    private static final String GRADE_COMPONENT_KEY = "Grade Component";
    private OnDialogGradeComponentListener mListener;
    private EditText mComponentName;
    private EditText mComponentWeight;
    private EditText mComponentNumItems;

    private GradeComponent mGradeComponentToUpdate;
    private long mCourseId;


    public GradeComponentDialog() {
        // Required empty public constructor.
    }

    public static GradeComponentDialog newInstance(String title) {
        GradeComponentDialog fragment = new GradeComponentDialog();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    public static GradeComponentDialog newInstance(String title, GradeComponent gradeComponent) {
        GradeComponentDialog fragment = new GradeComponentDialog();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putSerializable(GRADE_COMPONENT_KEY, gradeComponent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCourseId = getArguments().getLong(COURSE_ID);
        mGradeComponentToUpdate = (GradeComponent) getArguments().getSerializable(GRADE_COMPONENT_KEY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_grade_component, null);

        // Find views.
        mComponentName = (EditText) rootView.findViewById(R.id.et_grade_component_name);
        mComponentWeight = (EditText) rootView.findViewById(R.id.et_grade_component_weight);
        mComponentNumItems = (EditText) rootView.findViewById(R.id.et_grade_component_number_of_items);

        final boolean isNew = mGradeComponentToUpdate == null;

        String positiveButton;
        if (isNew) {
            positiveButton = getString(R.string.dialog_save);
        } else {
            positiveButton = getString(R.string.dialog_update);
            mComponentName.setText(mGradeComponentToUpdate.getName());
            mComponentWeight.setText(new DecimalFormat("#.##").format((mGradeComponentToUpdate.getWeight())));
            mComponentNumItems.setText(Integer.toString(mGradeComponentToUpdate.getNumberOfItems()));
        }

        // Return new dialog using builder. Gets title from arguments.
        // Show neutral button if they are new grade components. Save only if not.
        final AlertDialog alertDialog = isNew ?
                new AlertDialog.Builder(mContext)
                        .setView(rootView)
                        .setTitle(getArguments().getString(DIALOG_TITLE))
                        .setCancelable(false)
                        .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Overridden after. Prevents dialog from being dismissed without checks.
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNeutralButton(R.string.dialog_another, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Overridden after. Prevents dialog from being dismissed without checks.
                            }
                        })
                        .create()
                : new AlertDialog.Builder(mContext)
                .setView(rootView)
                .setTitle(getArguments().getString(DIALOG_TITLE))
                .setCancelable(false)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Overridden after. Prevents dialog from being dismissed without checks.
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        // Show dialog to allow positive and neutral buttons to be found.
        alertDialog.show();

        // Set positive button to check for proper edit text fields.
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mComponentName.getText().toString().trim();
                String weight = mComponentWeight.getText().toString().trim();
                String numItems = mComponentNumItems.getText().toString().trim();

                if (inputValid(name, weight, numItems)) {
                    GradeComponent gradeComponent;

                    if (isNew) {
                        gradeComponent = new GradeComponent();
                    } else {
                        gradeComponent = mGradeComponentToUpdate;
                    }

                    gradeComponent.setName(name);
                    gradeComponent.setWeight(Double.parseDouble(weight));
                    gradeComponent.setNumberOfItems(Integer.parseInt(numItems));

                    // Notify listeners.
                    if (mListener != null)
                        mListener.onGradeComponentSaved(gradeComponent, isNew);
                    alertDialog.dismiss();
                }
            }
        });

        if (isNew)
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = mComponentName.getText().toString().trim();
                    String weight = mComponentWeight.getText().toString().trim();
                    String numItems = mComponentNumItems.getText().toString().trim();

                    if (inputValid(name, weight, numItems)) {
                        GradeComponent gradeComponent;
                        if (isNew) {
                            gradeComponent = new GradeComponent();
                        } else {
                            gradeComponent = mGradeComponentToUpdate;
                        }

                        gradeComponent.setName(name);
                        gradeComponent.setWeight(Double.parseDouble(weight));
                        gradeComponent.setNumberOfItems(Integer.parseInt(numItems));

                        // Notify listeners.
                        if (mListener != null)
                            mListener.onGradeComponentSaved(gradeComponent, isNew);

                        // Clear edit text fields.
                        mComponentName.getText().clear();
                        mComponentWeight.getText().clear();
                        mComponentNumItems.getText().clear();

                        // Request focus back to the first edit text field.
                        mComponentName.requestFocus();
                    }
                }
            });

        return alertDialog;
    }

    /**
     * Checks edit text fields for valid input.
     *
     * @return boolean indicating whether or not the user input is valid.
     */
    private boolean inputValid(String name, String weight, String numItems) {
        String toastMessage = "";

        if (name.isEmpty()) {
            toastMessage = mContext.getString(R.string.name_invalid);
        } else if (weight.isEmpty()) {
            toastMessage = mContext.getString(R.string.weight_invalid);
        } else if (numItems.isEmpty()) {
            toastMessage = mContext.getString(R.string.num_items_invalid);
        }

        // Display toast if the message is not empty.
        if (!toastMessage.isEmpty()) {
            Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDialogGradeComponentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDialogGradeComponentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Interface for listeners.
     */
    public static interface OnDialogGradeComponentListener {

        /* Called when a grade component is saved. */
        public void onGradeComponentSaved(GradeComponent gradeComponent, boolean isNew);
    }
}
