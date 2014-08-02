package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Reminder;
import com.janclarin.gradepath.model.Semester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends BaseFragment {

    private static final String PREF_USER_HIDE_WELCOME = "show_welcome";
    private static final int NUM_ITEMS_SHOWN = 5;

    private FragmentHomeListener mListener;
    private boolean mHideWelcome;
    private boolean mButtonsShown;
    private boolean isUpdating;
    private LongSparseArray<Course> mCoursesById;

    private TextView mSemesterName;
    private TextView mSemesterDaysLeft;
    private TextView mSemesterDaysLeftLabel;
    private ListView mListView;
    private ImageButton mShowAddButton;
    private ImageButton mAddReminderButton;
    private ImageButton mAddGradeButton;
    private ImageButton mAddCourseButton;

    private CardList[] mCardItems;
    private Semester mCurrentSemester;
    private List<Course> mCourses;
    private List<Reminder> mReminders;
    private List<Grade> mGrades;
    private BaseAdapter mAdapter;

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

        // Get saved boolean to indicate whether or not to show welcome card.
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(mContext.getApplicationContext());
        mHideWelcome = sp.getBoolean(PREF_USER_HIDE_WELCOME, false);

        mCurrentSemester = mDatabase.getCurrentSemester();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Find all views.
        mSemesterName = (TextView) rootView.findViewById(R.id.tv_semester_name);
        mSemesterDaysLeft = (TextView) rootView.findViewById(R.id.tv_semester_days_left);
        mSemesterDaysLeftLabel = (TextView) rootView.findViewById(R.id.tv_semester_days_left_header);
        mListView = (ListView) rootView.findViewById(R.id.lv_list_items);
        mShowAddButton = (ImageButton) rootView.findViewById(R.id.btn_show_add_options);
        mAddReminderButton = (ImageButton) rootView.findViewById(R.id.btn_add_reminder);
        mAddGradeButton = (ImageButton) rootView.findViewById(R.id.btn_add_grade);
        mAddCourseButton = (ImageButton) rootView.findViewById(R.id.btn_add_course);

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

        mAddCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onHomeNewCourse();
                hideButtons();
            }
        });

        if (mHideWelcome) {
            mCardItems = new CardList[]{CardList.REMINDERS, CardList.COURSES, CardList.GRADES};
        } else {
            mCardItems = CardList.values();
        }

        // Get current courses if there is a current semester.
        // Set semester header information accordingly.
        if (mCurrentSemester == null) {
            mCourses = new ArrayList<Course>();
            mSemesterName.setText("No current semester");
        } else {
            mCourses = mDatabase.getCourses(mCurrentSemester.getId());
            mSemesterName.setText(mCurrentSemester.toString());
            mSemesterDaysLeftLabel.setText(R.string.semester_days_left);
            mSemesterDaysLeft.setText(mCurrentSemester.getDaysLeft());
        }

        mReminders = mDatabase.getUpcomingReminders();
        mGrades = mDatabase.getGrades();
        Collections.sort(mCourses);
        Collections.sort(mReminders);
        Collections.sort(mGrades);

        // Set up map for quick course name querying in lists.
        mCoursesById = new LongSparseArray<Course>();

        for (Course course : mCourses) {
            mCoursesById.put(course.getId(), course);
        }

        mAdapter = new ListAdapter();
        mListView.setAdapter(mAdapter);
    }

    private void hideButtons() {
        mShowAddButton.setRotation(0f);
        mAddGradeButton.setVisibility(View.INVISIBLE);
        mAddReminderButton.setVisibility(View.INVISIBLE);
        mAddCourseButton.setVisibility(View.INVISIBLE);
        mButtonsShown = false;
    }

    private void showButtons() {
        mShowAddButton.setRotation(45f);
        mAddGradeButton.setVisibility(View.VISIBLE);
        mAddReminderButton.setVisibility(View.VISIBLE);
        mAddCourseButton.setVisibility(View.VISIBLE);
        mButtonsShown = true;
    }


    public void updateListItems(boolean updateReminders, boolean updateCourses, boolean updateGrades) {
        isUpdating = true;

        // Update lists based on parameters.
        if (updateReminders) {
            mReminders = mDatabase.getUpcomingReminders();
            Collections.sort(mReminders);
            refreshCard(0);
        }
        if (updateCourses) {
            mCourses = mDatabase.getCurrentCourses();

            // Refresh courses by id.
            for (Course course : mCourses) {
                mCoursesById.put(course.getId(), course);
            }
            Collections.sort(mCourses);
            refreshCard(1);
        }
        if (updateGrades) {
            mGrades = mDatabase.getGrades();
            Collections.sort(mGrades);
            refreshCard(2);
        }

        isUpdating = false;
    }

    /**
     * Refreshes a card in list view.
     *
     * @param position
     */
    private void refreshCard(int position) {
        // Add one to position since welcome card item is at position 0.
        if (!mHideWelcome) position++;

        int firstPosition = mListView.getFirstVisiblePosition();
        for (int i = firstPosition, j = mListView.getLastVisiblePosition(); i <= j; i++) {
            if (position == mListView.getItemIdAtPosition(i)) {
                View view = mListView.getChildAt(i - firstPosition);
                mListView.getAdapter().getView(position, view, mListView);
                break;
            }
        }
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

    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCardItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mCardItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            CardList item = mCardItems[position];

            final ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext)
                        .inflate(R.layout.fragment_list_item_home, parent, false);
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_card_title);
                viewHolder.vDivider = convertView.findViewById(R.id.divider);
                viewHolder.llList = (LinearLayout) convertView.findViewById(R.id.ll_list_items);
                viewHolder.btnAll = (Button) convertView.findViewById(R.id.btn_all);

                // Prevents problems with removing welcome message.
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tvTitle.setText(item.titleId);
            viewHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(item.iconId, 0, 0, 0);

            // Only refresh the linear layout if updating or it hasn't been loaded yet.
            if (!isUpdating && viewHolder.llList.getChildCount() > 0) {
                return convertView;
            }

            // Clear list to prevent duplicates.
            viewHolder.llList.removeAllViews();

            // Number of items to show.
            int numShow;

            // Check that the list is empty to prevent duplicates in list.
            switch (item) {
                case WELCOME:
                    TextView tvMessage1 = (TextView) LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_item_home_text, viewHolder.llList, false);
                    TextView tvMessage2 = (TextView) LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_item_home_text, viewHolder.llList, false);
                    tvMessage1.setText(R.string.welcome_message);
                    tvMessage2.setText(R.string.welcome_message2);
                    viewHolder.llList.addView(tvMessage1);
                    viewHolder.llList.addView(tvMessage2);

                    // Set up custom button for welcome message.
                    viewHolder.btnAll.setText(R.string.welcome_confirmation);
                    viewHolder.btnAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mHideWelcome = true;
                            SharedPreferences sp = PreferenceManager
                                    .getDefaultSharedPreferences(mContext.getApplicationContext());
                            sp.edit().putBoolean(PREF_USER_HIDE_WELCOME, mHideWelcome).apply();
                            mCardItems = new CardList[]{CardList.REMINDERS, CardList.COURSES, CardList.GRADES};
                            mAdapter = new ListAdapter();
                            mListView.setAdapter(mAdapter);
                        }
                    });
                    break;
                case REMINDERS: // Populate Reminder card.
                    // Check if there are at least NUM_ITEM_SHOWN elements.
                    numShow = mReminders.size() < NUM_ITEMS_SHOWN ? mReminders.size() : NUM_ITEMS_SHOWN;

                    for (int i = 0; i < numShow; i++) {
                        final Reminder reminder = mReminders.get(i);
                        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(mContext)
                                .inflate(R.layout.fragment_list_item_home_general, viewHolder.llList, false);
                        TextView tvName = (TextView) layout.findViewById(R.id.tv_name);
                        TextView tvSubtitle = (TextView) layout.findViewById(R.id.tv_subtitle);
                        TextView tvDate = (TextView) layout.findViewById(R.id.tv_information);
                        Button btnViewCourse = (Button) layout.findViewById(R.id.btn_view_course);

                        btnViewCourse.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mListener != null)
                                    mListener.onHomeViewCourse(
                                            mCoursesById.get(reminder.getCourseId()), reminder);
                            }
                        });

                        // Set text in views.
                        tvName.setText(reminder.getName());
                        tvSubtitle.setText(mCoursesById.get(reminder.getCourseId()).getName());
                        tvDate.setText(reminder.getDateString(mContext));

                        // Add layout to list.
                        viewHolder.llList.addView(layout);
                    }

                    // Set button on click listener to open reminders fragment.
                    viewHolder.btnAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null) mListener.onHomeAllReminders();
                        }
                    });

                    if (mReminders.isEmpty()) {
                        viewHolder.vDivider.setVisibility(View.GONE);
                    } else {
                        viewHolder.vDivider.setVisibility(View.VISIBLE);
                    }
                    break;
                case COURSES:   // Populate the Course card.
                    for (final Course course : mCourses) {
                        FrameLayout layout = (FrameLayout) LayoutInflater.from(mContext)
                                .inflate(R.layout.fragment_list_item_home_course, viewHolder.llList, false);
                        TextView tvName = (TextView) layout.findViewById(R.id.tv_name);
                        TextView tvInstructor = (TextView) layout.findViewById(R.id.tv_subtitle);
                        Button btnViewCourse = (Button) layout.findViewById(R.id.btn_view_course);

                        // Set text in views.
                        tvName.setText(course.getName());
                        tvInstructor.setText(course.getInstructorName());

                        btnViewCourse.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mListener != null) mListener.onHomeViewCourse(course);
                            }
                        });

                        viewHolder.llList.addView(layout);
                    }

                    viewHolder.btnAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null) mListener.onHomeAllCourses();
                        }
                    });

                    if (mCourses.isEmpty()) {
                        viewHolder.vDivider.setVisibility(View.GONE);
                    } else {
                        viewHolder.vDivider.setVisibility(View.VISIBLE);
                    }
                    break;
                case GRADES:    // Populate the Grade card.
                    // Check if there are at least NUM_ITEMS_SHOWN elements.
                    numShow = mGrades.size() < NUM_ITEMS_SHOWN ? mGrades.size() : NUM_ITEMS_SHOWN;

                    for (int i = 0; i < numShow; i++) {
                        final Grade grade = mGrades.get(i);
                        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(mContext)
                                .inflate(R.layout.fragment_list_item_home_general, viewHolder.llList, false);
                        TextView tvName = (TextView) layout.findViewById(R.id.tv_name);
                        TextView tvSubtitle = (TextView) layout.findViewById(R.id.tv_subtitle);
                        TextView tvGrade = (TextView) layout.findViewById(R.id.tv_information);
                        Button btnViewCourse = (Button) layout.findViewById(R.id.btn_view_course);

                        btnViewCourse.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mListener != null) {
                                    mListener.onHomeViewCourse(
                                            mCoursesById.get(grade.getCourseId()), grade);
                                }
                            }
                        });

                        // Set text in views.
                        tvName.setText(grade.getName());
                        tvSubtitle.setText(mCoursesById.get(grade.getCourseId()).getName());
                        tvGrade.setText(grade.getGradePercentage());

                        // Add layout to list.
                        viewHolder.llList.addView(layout);
                    }

                    // Set button on click listener to open grades fragment.
                    viewHolder.btnAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener != null) mListener.onHomeAllGrades();
                        }
                    });

                    if (mGrades.isEmpty()) {
                        viewHolder.vDivider.setVisibility(View.GONE);
                    } else {
                        viewHolder.vDivider.setVisibility(View.VISIBLE);
                    }
                    break;
            }
            return convertView;
        }

        private class ViewHolder {
            TextView tvTitle;
            View vDivider;
            LinearLayout llList;
            Button btnAll;
        }
    }

    private enum CardList {
        WELCOME(R.string.welcome_card_title, R.drawable.welcome),
        REMINDERS(R.string.title_fragment_list_reminders, R.drawable.reminder),
        COURSES(R.string.title_fragment_list_courses, R.drawable.course),
        GRADES(R.string.title_fragment_list_grades, R.drawable.grade);

        public final int titleId;
        public final int iconId;

        private CardList(int titleId, int iconId) {
            this.titleId = titleId;
            this.iconId = iconId;
        }
    }

    public interface FragmentHomeListener {

        /* Calls new grade dialog. */
        public void onHomeNewGrade();

        /* Calls new reminder dialog. */
        public void onHomeNewReminder();

        /* Calls new course activity. */
        public void onHomeNewCourse();

        /* Displays grades fragment. */
        public void onHomeAllGrades();

        /* Displays reminders fragment. */
        public void onHomeAllReminders();

        /* Displays courses fragment. */
        public void onHomeAllCourses();

        /* Displays course detail fragment. */
        public void onHomeViewCourse(Course course);

        /* Displays course detail fragment. Set to reminder page. */
        public void onHomeViewCourse(Course course, Reminder reminder);

        /* Displays course detail fragment. Set to grades page. */
        public void onHomeViewCourse(Course course, Grade grade);
    }

}
