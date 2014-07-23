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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Reminder;

import java.util.Collections;
import java.util.List;

public class HomeFragment extends BaseFragment {

    private static final String PREF_USER_HIDE_WELCOME = "show_welcome";
    private static final int NUM_ITEMS_SHOWN = 3;

    private FragmentHomeListener mListener;
    private boolean mHideWelcome;
    private boolean mButtonsShown;
    private LongSparseArray<Course> mCoursesById;

    private ImageButton mShowAddButton;
    private ImageButton mAddReminderButton;
    private ImageButton mAddGradeButton;
    private ImageButton mAddCourseButton;
    private ListView mListView;
    private CardList[] mCardItems;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Find all views.
        mShowAddButton = (ImageButton) rootView.findViewById(R.id.btn_show_add_options);
        mAddReminderButton = (ImageButton) rootView.findViewById(R.id.btn_add_reminder);
        mAddGradeButton = (ImageButton) rootView.findViewById(R.id.btn_add_grade);
        mAddCourseButton = (ImageButton) rootView.findViewById(R.id.btn_add_course);
        mListView = (ListView) rootView.findViewById(R.id.lv_list_items);

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

        // Set up map for quick course name querying in lists.
        mCoursesById = new LongSparseArray<Course>();
        List<Course> courseList = mDatabase.getCourses();

        for (Course course : courseList) {
            mCoursesById.put(course.getId(), course);
        }

        if (mHideWelcome) {
            mCardItems = new CardList[]{CardList.REMINDERS, CardList.GRADES, CardList.COURSES};
        } else {
            mCardItems = CardList.values();
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


    public void updateListItems(boolean updateReminders, boolean updateGrades, boolean updateCourses) {
        // TODO: Fix updating.
        mAdapter.notifyDataSetChanged();
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
                viewHolder.llList = (LinearLayout) convertView.findViewById(R.id.ll_list_items);
                viewHolder.btnAll = (Button) convertView.findViewById(R.id.btn_all);

                // Prevents problems with removing welcome message.
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tvTitle.setText(item.titleId);
            viewHolder.tvTitle.setCompoundDrawablesWithIntrinsicBounds(item.iconId, 0, 0, 0);

            // Number of items to show.
            int numShow;

            // Check that the list is empty to prevent duplicates in list.
            if (viewHolder.llList.getChildCount() == 0) {
                switch (item) {
                    case WELCOME:
                        TextView tvMessage = (TextView) LayoutInflater.from(mContext)
                                .inflate(R.layout.fragment_list_item_home_welcome, viewHolder.llList, false);
                        viewHolder.llList.addView(tvMessage);

                        // Set up custom button for welcome message.
                        viewHolder.btnAll.setText(R.string.welcome_confirmation);
                        viewHolder.btnAll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SharedPreferences sp = PreferenceManager
                                        .getDefaultSharedPreferences(mContext.getApplicationContext());
                                sp.edit().putBoolean(PREF_USER_HIDE_WELCOME, true).apply();
                                mHideWelcome = true;
                                mCardItems = new CardList[]{CardList.REMINDERS, CardList.GRADES, CardList.COURSES};
                                mAdapter = new ListAdapter();
                                mListView.setAdapter(mAdapter);
                            }
                        });
                        break;
                    case REMINDERS:
                        List<Reminder> reminders = mDatabase.getIncompleteReminders();
                        Collections.sort(reminders);

                        // Check if there are at least NUM_ITEM_SHOWN elements.
                        numShow = reminders.size() < NUM_ITEMS_SHOWN ? reminders.size() : NUM_ITEMS_SHOWN;

                        for (int i = 0; i < numShow; i++) {
                            Reminder reminder = reminders.get(i);
                            RelativeLayout layout = (RelativeLayout) LayoutInflater.from(mContext)
                                    .inflate(R.layout.fragment_list_item_home_reminder, viewHolder.llList, false);
                            TextView tvName = (TextView) layout.findViewById(R.id.tv_reminder_name);
                            TextView tvCourse = (TextView) layout.findViewById(R.id.tv_reminder_subtitle);
                            TextView tvDate = (TextView) layout.findViewById(R.id.tv_reminder_date);

                            // Set text in views.
                            tvName.setText(reminder.getName());
                            tvCourse.setText(mCoursesById.get(reminder.getCourseId()).getName());
                            tvDate.setText(reminder.getDueDate(mContext));

                            // Add layout to list.
                            viewHolder.llList.addView(layout);
                        }

                        if (reminders.isEmpty()) {
                            viewHolder.btnAll.setText(R.string.none_use_button);
                        } else {
                            viewHolder.btnAll.setText(R.string.see_all);
                            // Set button on click listener to open reminders fragment.
                            viewHolder.btnAll.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mListener != null) mListener.onHomeAllReminders();
                                }
                            });
                        }
                        break;
                    case GRADES:
                        List<Grade> grades = mDatabase.getGrades();
                        Collections.sort(grades);

                        // Check if there are at least NUM_ITEMS_SHOWN elements.
                        numShow = grades.size() < NUM_ITEMS_SHOWN ? grades.size() : NUM_ITEMS_SHOWN;

                        for (int i = 0; i < numShow; i++) {
                            Grade grade = grades.get(i);
                            RelativeLayout layout = (RelativeLayout) LayoutInflater.from(mContext)
                                    .inflate(R.layout.fragment_list_item_home_grade, viewHolder.llList, false);
                            TextView tvName = (TextView) layout.findViewById(R.id.tv_grade_name);
                            TextView tvSubtitle = (TextView) layout.findViewById(R.id.tv_grade_subtitle);
                            TextView tvGrade = (TextView) layout.findViewById(R.id.tv_grade);

                            // Set text in views.
                            tvName.setText(grade.getName());
                            tvSubtitle.setText(mCoursesById.get(grade.getCourseId()).getName());
                            tvGrade.setText(grade.getGradePercentage());

                            // Add layout to list.
                            viewHolder.llList.addView(layout);
                        }

                        if (grades.isEmpty()) {
                            viewHolder.btnAll.setText(R.string.none_use_button);
                        } else {
                            viewHolder.btnAll.setText(R.string.see_all);
                            // Set button on click listener to open grades fragment.
                            viewHolder.btnAll.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mListener != null) mListener.onHomeAllGrades();
                                }
                            });
                        }
                        break;
                    case COURSES:
                        List<Course> courses = mDatabase.getCurrentCourses();
                        Collections.sort(courses);

                        for (Course course : courses) {
                            RelativeLayout layout = (RelativeLayout) LayoutInflater.from(mContext)
                                    .inflate(R.layout.fragment_list_item_home_course, viewHolder.llList, false);
                            TextView tvName = (TextView) layout.findViewById(R.id.tv_course_name);
                            TextView tvInstructor = (TextView) layout.findViewById(R.id.tv_instructor_name);

                            // Set text in views.
                            tvName.setText(course.getName());
                            tvInstructor.setText(course.getInstructorName());

                            viewHolder.llList.addView(layout);
                        }

                        if (courses.isEmpty()) {
                            viewHolder.btnAll.setText(R.string.none_click_to_add);
                        } else {
                            viewHolder.btnAll.setText(R.string.see_all);
                            viewHolder.btnAll.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mListener != null) mListener.onHomeAllCourses();
                                }
                            });
                        }
                        break;
                }
            }
            return convertView;
        }

        private class ViewHolder {
            TextView tvTitle;
            LinearLayout llList;
            Button btnAll;
        }
    }

    private enum CardList {
        WELCOME(R.string.hello, R.drawable.welcome),
        REMINDERS(R.string.title_fragment_list_reminders, R.drawable.reminder),
        GRADES(R.string.title_fragment_list_grades, R.drawable.grade),
        COURSES(R.string.title_fragment_list_courses, R.drawable.course);

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
    }

}
