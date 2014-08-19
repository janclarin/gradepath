package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.LongSparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.BaseActivity;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.Semester;

import java.util.Collections;
import java.util.List;

public class HomeFragment extends BaseListFragment {

    private ImageButton mAddItemButton;
    private LinearLayout mButtonLayout;
    private ImageButton mAddCourseButton;
    private ImageButton mAddGradeButton;

    private FragmentHomeListener mListener;

    private LongSparseArray<Course> mCoursesById;
    private int mNumGradesToShow = 3;
    private Semester mSemester;

    public HomeFragment() {
        // Required empty public constructor.
    }

    public static HomeFragment newInstance(Semester semester) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(BaseActivity.SEMESTER_KEY, semester);
        fragment.setArguments(args);
        return fragment;
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

        mListView = (ListView) rootView.findViewById(R.id.lv_list_items);
        mEmptyTextView = (TextView) rootView.findViewById(R.id.tv_list_empty);
        mButtonLayout = (LinearLayout) rootView.findViewById(R.id.button_layout);
        mAddItemButton = (ImageButton) rootView.findViewById(R.id.btn_add_item);
        mAddCourseButton = (ImageButton) rootView.findViewById(R.id.btn_add_course);
        mAddGradeButton = (ImageButton) rootView.findViewById(R.id.btn_add_grade);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSemester = (Semester) getArguments().getSerializable(BaseActivity.SEMESTER_KEY);

        if (mSemester != null) {
            mEmptyTextView.setText(R.string.welcome_message);
            mEmptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.course_large, 0, 0);

            mCoursesById = new LongSparseArray<Course>();
            updateListItems();
            mAdapter = new ListAdapter();
            setUpListView();

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    DatabaseItem item = mListItems.get(position);

                    if (mListener != null) {
                        if (item instanceof Grade) {
                            mListener.onHomeEditGrade((Grade) item);
                        } else {
                            mListener.onHomeViewCourse((Course) item);
                        }
                    }
                }
            });

            mAddItemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAddButtons();
                }
            });

            mAddCourseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) mListener.onHomeAddCourse();
                    hideAddButtons();
                }
            });

            // If there are no courses, display toast on button press.
            mAddGradeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCoursesById.size() == 0) {
                        Toast.makeText(mContext, "You need to add a course first", Toast.LENGTH_SHORT).show();
                    } else {
                        if (mListener != null) mListener.onHomeAddGrade();
                        hideAddButtons();
                    }
                }
            });
        }
    }

    /**
     * Show add buttons.
     */
    private void showAddButtons() {
        // Show other add buttons on click.
        if (mButtonLayout.getVisibility() == View.INVISIBLE) {
            mButtonLayout.setVisibility(View.VISIBLE);
            mAddItemButton.setRotation(45f);
        } else {
            hideAddButtons();
        }
    }

    /**
     * Hide add buttons.
     */
    private void hideAddButtons() {
        if (mButtonLayout.getVisibility() == View.VISIBLE) {
            mButtonLayout.setVisibility(View.INVISIBLE);
            mAddItemButton.setRotation(0f);
        }
    }

    @Override
    public void updateListItems() {
        clearListItems();

        // Get list of current courses.
        List<Course> courses = mDatabase.getCourses(mSemester.getId());
        Collections.sort(courses);

        // For every course, map course ID to the course.
        for (Course course : courses) {
            Long courseId = course.getId();
            mCoursesById.put(courseId, course);
        }

        if (!courses.isEmpty()) {
            // Add all courses to the list now.
            mListItems.add(new Header(getString(R.string.title_fragment_list_courses)));
            mListItems.addAll(courses);

            // Get list of recent grades.
            List<Grade> grades = mDatabase.getRecentGrades(mNumGradesToShow);

            if (!grades.isEmpty()) {
                mListItems.add(new Header(getString(R.string.title_fragment_list_grades)));
                mListItems.addAll(grades);
            }

        }
        notifyAdapter();

        // Determine list view state.
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
                    case R.id.menu_set_final_grade:
                        if (mListener != null)
                            mListener.onHomeSetFinalGrade((Course) mAdapter.getItem(position));
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

        /* Calls new grade dialog. */
        public void onHomeAddGrade();

        /* Calls new course activity. */
        public void onHomeAddCourse();

        /* Calls new final grade dialog. */
        public void onHomeSetFinalGrade(Course course);

        /* Opens grade edit dialog */
        public void onHomeEditGrade(Grade grade);

        /* Displays grade list fragment. */
        public void onHomeViewGrades();

        /* Displays course list fragment. */
        public void onHomeViewCourses();

        /* Displays course detail fragment. */
        public void onHomeViewCourse(Course course);
    }

    /**
     * List adapter for the list view.
     */
    private class ListAdapter extends BaseListAdapter {

        @Override
        public int getItemViewType(int position) {
            DatabaseItem item = mListItems.get(position);

            if (item instanceof Header) {
                return ITEM_VIEW_TYPE_HEADER;
            } else if (item instanceof Course) {
                return ITEM_VIEW_TYPE_MAIN_2_LINE;
            } else {
                return ITEM_VIEW_TYPE_MAIN_3_LINE;
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final DatabaseItem item = mListItems.get(position);
            final int itemViewType = getItemViewType(position);

            ViewHolder viewHolder;

            if (convertView == null) {

                viewHolder = new ViewHolder();

                switch (itemViewType) {
                    case ITEM_VIEW_TYPE_HEADER: {
                        convertView = LayoutInflater.from(mContext)
                                .inflate(R.layout.list_header_home, parent, false);

                        viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title_header);
                        viewHolder.btnSecondary = convertView.findViewById(R.id.btn_more);
                        viewHolder.divider = convertView.findViewById(R.id.divider_view);
                        break;
                    }
                    case ITEM_VIEW_TYPE_MAIN_2_LINE: {
                        convertView = LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_general_two_line, parent, false);

                        viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
                        viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_subtitle);
                        viewHolder.ivDetail = (ImageView) convertView.findViewById(R.id.iv_detail);
                        viewHolder.btnSecondary = convertView.findViewById(R.id.btn_secondary);
                        break;
                    }
                    case ITEM_VIEW_TYPE_MAIN_3_LINE: {
                        convertView = LayoutInflater.from(mContext)
                                .inflate(R.layout.list_item_general_three_line, parent, false);

                        viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
                        viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_subtitle);
                        viewHolder.tvSubtitle2 = (TextView) convertView.findViewById(R.id.tv_subtitle_2);
                        viewHolder.ivDetail = (ImageView) convertView.findViewById(R.id.iv_detail);
                        viewHolder.btnSecondary = convertView.findViewById(R.id.btn_secondary);
                    }
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (item instanceof Header) {
                final String name = ((Header) item).getName();
                viewHolder.tvTitle.setText(name);
                viewHolder.btnSecondary.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            if (name.equals(getString(R.string.title_fragment_list_grades))) {
                                mListener.onHomeViewGrades();
                            } else {
                                mListener.onHomeViewCourses();
                            }
                        }
                    }
                });
                // If this is the first header, hide the divider.
                if (position == 0) viewHolder.divider.setVisibility(View.GONE);
            } else {
                if (item instanceof Grade) {
                    Grade grade = (Grade) item;
                    viewHolder.tvTitle.setText(grade.getName());
                    viewHolder.tvSubtitle.setText(mCoursesById.get(grade.getCourseId()).getName());
                    viewHolder.tvSubtitle2.setText(
                            grade.getGradePercentage() + " "
                                    + getString(R.string.bullet) + " "
                                    + grade.toString()
                    );
                    viewHolder.ivDetail.setImageResource(R.drawable.grade);

                    // Set button to open popup menu.
                    viewHolder.btnSecondary.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showPopupMenu(view, R.menu.list_general, position);
                        }
                    });
                } else {
                    Course course = (Course) item;
                    String instructorName = course.getInstructorName();

                    if (instructorName.isEmpty()) {
                        viewHolder.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
                        viewHolder.tvSubtitle.setVisibility(View.GONE);
                    } else {
                        if (viewHolder.tvSubtitle.getVisibility() == View.GONE)
                            viewHolder.tvSubtitle.setVisibility(View.VISIBLE);
                        viewHolder.tvSubtitle.setText(instructorName);
                    }
                    viewHolder.tvTitle.setText(course.getName());
                    viewHolder.ivDetail.setImageResource(R.drawable.course);

                    // Set button to open popup menu.
                    viewHolder.btnSecondary.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showPopupMenu(view, R.menu.list_course, position);
                        }
                    });
                }
                // Set circle background based on course's color.
                viewHolder.ivDetail.setBackground(getColorCircle(R.color.theme_primary));

            }

            return convertView;
        }
    }
}
