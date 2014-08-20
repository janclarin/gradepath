package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Semester;

import java.util.Collections;
import java.util.List;

/**
 * Course list fragment.
 */
public class ListCourseFragment extends BaseListFragment {

    private OnFragmentListCourseListener mListener;

    public ListCourseFragment() {
        // Required empty public constructor.
    }

    public static ListCourseFragment newInstance() {
        return new ListCourseFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEmptyTextView.setText(R.string.tv_list_course_empty);

        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();

        // Set on item click listener to display course info.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Course course = (Course) mAdapter.getItem(position);
                if (mListener != null) mListener.onListCourseViewDetails(course);
            }
        });
    }

    @Override
    public void updateListItems() {
        clearListItems();

        // Get list of Semesters.
        List<Semester> semesters = mDatabase.getSemesters();
        Semester currentSemester = mDatabase.getCurrentSemester();

        // For all semesters that contain Courses, add the Semester and Courses to the list.
        for (Semester semester : semesters) {
            List<Course> courses = mDatabase.getCourses(semester.getId());
            if (!courses.isEmpty()) {
                // Only add title if it's a past semester.
                if (!semester.equals(currentSemester)) mListItems.add(semester);
                Collections.sort(courses);
                mListItems.addAll(courses);
            }
        }

        notifyAdapter();

        showEmptyStateView(mListItems.isEmpty());
    }

    /**
     * Show popup menu on overflow button click.
     */
    @Override
    public void showPopupMenu(View view, int menuId, final int position) {
        PopupMenu popupMenu = new PopupMenu(mContext, view);

        popupMenu.getMenuInflater().inflate(menuId, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_delete:
                        deleteSelectedItem(position);
                        return true;
                    case R.id.menu_set_final_grade:
                        if (mListener != null)
                            mListener.onListCourseSetFinalGrade((Course) mAdapter.getItem(position));
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentListCourseListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListCourseCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Interface to be implemented by all listener classes.
     */
    public interface OnFragmentListCourseListener {

        /* Called when the course item is clicked. Opens course detail fragment. */
        public void onListCourseViewDetails(Course course);

        /* Called when set final grade option is selected. */
        public void onListCourseSetFinalGrade(Course course);
    }

    private class ListAdapter extends BaseListAdapter {
        @Override
        public int getItemViewType(int position) {
            return (mListItems.get(position) instanceof Semester) ?
                    ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_MAIN_2_LINE;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final DatabaseItem listItem = mListItems.get(position);
            final int type = getItemViewType(position);

            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();

                if (type == ITEM_VIEW_TYPE_HEADER) {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.list_header_general, parent, false);
                    viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title_header);
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_general_two_line, parent, false);
                    viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
                    viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_subtitle);
                    viewHolder.ivDetail = (ImageView) convertView.findViewById(R.id.iv_detail);
                    viewHolder.btnSecondary = convertView.findViewById(R.id.btn_secondary);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {
                viewHolder.tvTitle.setText(listItem.toString());
            } else {
                Course course = (Course) listItem;
                String instructorName = course.getInstructorName();

                viewHolder.tvTitle.setText(course.getName());

                if (instructorName.isEmpty()) {
                    viewHolder.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
                    viewHolder.tvSubtitle.setVisibility(View.GONE);
                } else {
                    if (viewHolder.tvSubtitle.getVisibility() == View.GONE)
                        viewHolder.tvSubtitle.setVisibility(View.VISIBLE);
                    viewHolder.tvSubtitle.setText(instructorName);
                }

                viewHolder.ivDetail.setBackground(getColorCircle(R.color.theme_primary));
                viewHolder.ivDetail.setImageResource(R.drawable.course);
                // Set button to open popup menu.
                viewHolder.btnSecondary.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showPopupMenu(view, R.menu.list_course, position);
                    }
                });
            }

            return convertView;
        }
    }
}

