package com.janclarin.gradepath.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.BaseActivity;
import com.janclarin.gradepath.model.Course;

public class FinalGradeDialogFragment extends BaseDialogFragment {

    private Callbacks mListener;

    private Course mCourseToUpdate;

    private SeekBar mSeekBar;
    private TextView mFinalGrade;

    public FinalGradeDialogFragment() {
        // Required empty public constructor.
    }

    public static FinalGradeDialogFragment newInstance(String title, Course course) {
        FinalGradeDialogFragment fragment = new FinalGradeDialogFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putSerializable(BaseActivity.COURSE_KEY, course);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCourseToUpdate = (Course) getArguments().getSerializable(BaseActivity.COURSE_KEY);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Inflate the layout for this dialog.
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_final_grade, null);

        // Find views.
        mSeekBar = (SeekBar) rootView.findViewById(R.id.seek_final_grade);
        mFinalGrade = (TextView) rootView.findViewById(R.id.tv_final_grade);

        // Set up grade seeker.
        // Set max value to the number of possible letter grades values.
        mSeekBar.setMax(Course.LetterGrade.values().length - 1);
        mSeekBar.setKeyProgressIncrement(1);

        int finalGradeValue = mCourseToUpdate.getFinalGradeValue();

        mSeekBar.setProgress(finalGradeValue > -1 ? finalGradeValue : 6);

        mFinalGrade.setText(Course.LetterGrade.values()[finalGradeValue].toString());

        // Set seek bar change listener.
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Set text view to display corresponding
                mFinalGrade.setText(Course.LetterGrade.values()[progress].toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final String positiveButton = mCourseToUpdate.getFinalGradeValue() > -1
                ? getString(R.string.dialog_update)
                : getString(R.string.dialog_save);

        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setView(rootView)
                .setTitle(getArguments().getString(DIALOG_TITLE))
                .setCancelable(false)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (mListener != null) {
                            mListener.onFinalGradeSaved(mCourseToUpdate.getFinalGradeValue() == -1);
                        }

                        mDatabase.updateCourse(
                                mCourseToUpdate.getId(),
                                mCourseToUpdate.getSemesterId(),
                                mCourseToUpdate.getName(),
                                mCourseToUpdate.getInstructorName(),
                                mCourseToUpdate.getInstructorEmail(),
                                mCourseToUpdate.getCredits(),
                                mSeekBar.getProgress()
                        );
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .create();

        return alertDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static interface Callbacks {

        /**
         * Called when the final grade is saved.
         */
        public void onFinalGradeSaved(boolean isNew);
    }
}
