package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Reminder;

import java.util.Collections;
import java.util.List;

public class CourseListReminderFragment extends BaseListFragment {

    private FragmentListCourseReminderListener mListener;

    // Selected Course object.
    private Course mCourse;

    public CourseListReminderFragment() {
        // Required empty public constructor.
    }

    /**
     * Creates a new instance of this fragment.
     *
     * @return A new instance of fragment CourseDetailsFragment.
     */
    public static CourseListReminderFragment newInstance(Course course) {
        CourseListReminderFragment fragment = new CourseListReminderFragment();
        Bundle args = new Bundle();
        args.putSerializable(MainActivity.COURSE_KEY, course);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCourse = (Course) getArguments().getSerializable(MainActivity.COURSE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_course_general, container, false);

        mListView = (ListView) rootView.findViewById(R.id.lv_list_items);
        mEmptyTextView = (TextView) rootView.findViewById(R.id.tv_list_empty);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEmptyTextView.setText(R.string.tv_list_task_empty);

        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();

        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    public void updateListItems() {
        clearListItems();

        long courseId = mCourse.getId();
        List<Reminder> currentReminders = mDatabase.getCurrentReminders(courseId);
        List<Reminder> pastReminders = mDatabase.getPastReminders(courseId);

        Collections.sort(pastReminders);
        Collections.sort(currentReminders);

        // Add current reminders.
        if (currentReminders.size() > 0) {
            mListItems.add(new Header(mContext.getString(R.string.list_reminders_upcoming)));
            mListItems.addAll(currentReminders);
        }

        // Add past reminders.
        if (pastReminders.size() > 0) {
            mListItems.add(new Header(mContext.getString(R.string.list_reminders_past)));
            mListItems.addAll(pastReminders);
        }

        notifyAdapter();

        // Determine list view state.
        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void editSelectedItem(int selectedPosition) {
        if (mListener != null)
            mListener.onListCourseReminderEdit((Reminder) mAdapter.getItem(selectedPosition));
    }

    @Override
    protected void deleteSelectedItems(SparseBooleanArray possibleSelectedPositions) {
        int numItems = mListItems.size();
        for (int i = numItems - 1; i >= 0; i--) {
            if (possibleSelectedPositions.get(i, false)) {
                Reminder selectedReminder = (Reminder) mAdapter.getItem(i);
                mDatabase.deleteReminder(selectedReminder);
                mListItems.remove(selectedReminder);
            }
        }
        updateListItems();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentListCourseReminderListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListCourseTaskListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        public int getItemViewType(int position) {
            return getItem(position) instanceof Header ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_DATABASE_ITEM;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final DatabaseItem listItem = mListItems.get(position);
            final int type = getItemViewType(position);

            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();

                if (type == ITEM_VIEW_TYPE_HEADER) {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_header_general, parent, false);
                    viewHolder.tvName = (TextView) convertView;
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.fragment_list_item_general, parent, false);
                    viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                    viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_subtitle);
                    viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tv_information);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {
                viewHolder.tvName.setText(((Header) listItem).getName());
            } else {
                Reminder reminder = (Reminder) listItem;
                viewHolder.tvName.setText(reminder.getName());
                viewHolder.tvSubtitle.setText(reminder.isGraded() ? R.string.graded : R.string.not_graded);
                viewHolder.tvDate.setText(reminder.getDateString(mContext));
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tvName;
            TextView tvSubtitle;
            TextView tvDate;
        }
    }

    /**
     * Listeners.
     */
    public static interface FragmentListCourseReminderListener {

        /**
         * Called when a task is going to be updated.
         */
        public void onListCourseReminderEdit(Reminder reminder);
    }
}
