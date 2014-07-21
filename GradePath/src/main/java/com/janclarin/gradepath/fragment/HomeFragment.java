package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.janclarin.gradepath.R;

public class HomeFragment extends BaseFragment {

    private FragmentHomeListener mListener;
    private boolean mButtonsShown;

    private ImageButton mShowAddButton;
    private ImageButton mAddGradeButton;
    private TextView mAddGradeText;
    private ImageButton mAddReminderButton;
    private TextView mAddReminderText;
    private ListView mListView;
    private TextView mEmptyTextView;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public HomeFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mShowAddButton = (ImageButton) rootView.findViewById(R.id.btn_show_add_options);
        mAddGradeButton = (ImageButton) rootView.findViewById(R.id.btn_add_grade);
        mAddGradeText = (TextView) rootView.findViewById(R.id.tv_add_grade);
        mAddReminderButton = (ImageButton) rootView.findViewById(R.id.btn_add_reminder);
        mAddReminderText = (TextView) rootView.findViewById(R.id.tv_add_reminder);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mShowAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mButtonsShown) {
                    hideButtons();
                } else {
                    showButtons();
                }
            }
        });

        mAddGradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onHomeNewGrade();
                hideButtons();
            }
        });

        mAddReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onHomeNewReminder();
                hideButtons();
            }
        });
    }

    private void hideButtons() {
        mShowAddButton.setRotation(0f);
        mAddGradeButton.setVisibility(View.INVISIBLE);
        mAddGradeText.setVisibility(View.INVISIBLE);
        mAddReminderButton.setVisibility(View.INVISIBLE);
        mAddReminderText.setVisibility(View.INVISIBLE);
        mButtonsShown = false;
    }

    private void showButtons() {
        mShowAddButton.setRotation(45f);
        mAddGradeButton.setVisibility(View.VISIBLE);
        mAddGradeText.setVisibility(View.VISIBLE);
        mAddReminderButton.setVisibility(View.VISIBLE);
        mAddReminderText.setVisibility(View.VISIBLE);
        mButtonsShown = true;
    }

    public void updateListItems(boolean updateReminders, boolean updateGrades) {

    }

    protected void editSelectedItem(int selectedPosition) {

    }

    protected void deleteSelectedItems(SparseBooleanArray possibleSelectedPositions) {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentHomeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentHomeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentHomeListener {

        public void onHomeNewGrade();

        public void onHomeNewReminder();
    }

}
