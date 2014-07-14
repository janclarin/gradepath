package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.activity.MainActivity;
import com.janclarin.gradepath.database.DatabaseFacade;
import com.janclarin.gradepath.model.Course;
import com.janclarin.gradepath.model.Grade;
import com.janclarin.gradepath.model.GradeComponent;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

/**
 * Course fragment to display course information, grades, etc.
 */
public class ListCourseGradeFragment extends Fragment {

    private FragmentListCourseGradeCallbacks mListener;

    // Selected Course object.
    private Course mCourse;

    private Context mContext;

    private ExpandableListView mGradeListView;
    private TextView mGradesTextView;
    private Button mAddFirstGradeButton;

    public ListCourseGradeFragment() {
        // Required empty public constructor.
    }

    /**
     * Creates a new instance of this fragment.
     *
     * @return A new instance of fragment CourseDetailsFragment.
     */
    public static ListCourseGradeFragment newInstance(Course course) {
        ListCourseGradeFragment fragment = new ListCourseGradeFragment();
        Bundle args = new Bundle();
        args.putSerializable(MainActivity.COURSE_KEY, course);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCourse = (Course) getArguments().getSerializable(MainActivity.COURSE_KEY);
        }

        mContext = getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_course_grades, container, false);
        mGradeListView = (ExpandableListView) rootView.findViewById(R.id.elv_course_fragment_grades);
        mGradesTextView = (TextView) rootView.findViewById(R.id.tv_grades_header);
        mAddFirstGradeButton = (Button) rootView.findViewById(R.id.btn_add_first_grade);

        // Get data from mDatabase and initialize mAdapter.
        fillData();

        return rootView;
    }

    /**
     * Updates information for expandable list view mAdapter with data from mDatabase.
     */
    public void fillData() {

        // Get mDatabase.
        DatabaseFacade database = DatabaseFacade.getInstance(mContext);
        database.open();

        // List of grade components for course.
        List<GradeComponent> gradeComponents = database.getGradeComponents(mCourse.getId());

        // HashMap of grades for grade components.
        HashMap<GradeComponent, List<Grade>> gradeMap = new HashMap<GradeComponent, List<Grade>>();

        boolean gradesExist = false;

        // Get grades for each grade component.
        for (GradeComponent gradeComponent : gradeComponents) {
            List<Grade> grades = database.getComponentGrades(gradeComponent.getId());
            gradeMap.put(gradeComponent, grades);

            // If there are any grades, set it to true.
            if (!grades.isEmpty()) gradesExist = true;
        }

        // Check if there are any grade components, if not display add first grade button.
        if (!gradesExist) {
            // Hide all other views.
            mGradeListView.setVisibility(View.GONE);
            mGradesTextView.setVisibility(View.GONE);

            // Display "add grade" button.
            mAddFirstGradeButton.setVisibility(View.VISIBLE);
            mAddFirstGradeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Notify listener to display new grade dialog.
                    mListener.onListCourseGradeAddGrade(mCourse);
                }
            });
        } else {
            // Ensure that button is gone.
            mAddFirstGradeButton.setVisibility(View.GONE);

            // Show normal views.
            mGradeListView.setVisibility(View.VISIBLE);
            mGradesTextView.setVisibility(View.VISIBLE);


            // Adapter for expandable list view.
            GradesListAdapter adapter = new GradesListAdapter(mContext, gradeComponents, gradeMap);

            // Set mAdapter for grades list view.
            mGradeListView.setAdapter(adapter);

            // Expand all groups by default.
            for (int i = 0; i < adapter.getGroupCount(); i++) mGradeListView.expandGroup(i);

            // Sum the weight averages for each component.
            double currentAverage = 0;

            for (GradeComponent gradeComponent : gradeComponents) {
                currentAverage += gradeComponent.getComponentAverage();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentListCourseGradeCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate options menu.
        inflater.inflate(R.menu.list_course, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    /**
     * Listeners.
     */
    public static interface FragmentListCourseGradeCallbacks {

        public void onListCourseGradeAddGrade(Course course);
    }

    /**
     * Expandable list mAdapter for grades list.
     */
    public static class GradesListAdapter extends BaseExpandableListAdapter {

        private static final String LOG_TAG = GradesListAdapter.class.getSimpleName();

        private final Context mContext;
        private final List<GradeComponent> mListGroups;
        private final HashMap<GradeComponent, List<Grade>> mListChildren;

        public GradesListAdapter(Context context, List<GradeComponent> listGroups,
                                 HashMap<GradeComponent, List<Grade>> listChildren) {
            mContext = context;
            mListGroups = listGroups;
            mListChildren = listChildren;
        }

        @Override
        public int getGroupCount() {
            return mListGroups.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mListChildren.get(mListGroups.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mListGroups.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mListChildren.get(mListGroups.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            // View holder.
            GroupViewHolder viewHolder = null;

            // Current grade component to create view for.
            GradeComponent gradeComponent = (GradeComponent) getGroup(groupPosition);

            // Grade component current average.
            double average = 0;

            // List of grades for this grade component.
            List<Grade> gradesForComponent = mListChildren.get(gradeComponent);

            // Overall weight for each item in a grade component.
            double weightPerItem = gradeComponent.getWeight() / gradeComponent.getNumberOfItems();

            // Calculate average for grade component.
            for (Grade grade : gradesForComponent) {
                average += (grade.getPointsEarned() / grade.getPointsPossible()) * weightPerItem;
            }

            // Set grade component's average
            gradeComponent.setComponentAverage(average);

            // Format double to two decimal places, dropping trailing zeros.
            DecimalFormat df = new DecimalFormat("#.##");
            String currentAverage = df.format(average) + "/" + df.format(gradeComponent.getWeight());

            int numGrades = getChildrenCount(groupPosition);
            int expectedNumGrades = gradeComponent.getNumberOfItems();

            // Number of grades of expected number of grades.
            String numItems = String.valueOf(numGrades) + " " + mContext.getString(R.string.of) + " "
                    + expectedNumGrades + " ";

            // Inflate view if it doesn't already exist.
            if (convertView == null) {
                viewHolder = new GroupViewHolder();

                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_course_grades_group,
                        parent, false);

                // Find views.
                viewHolder.tvComponentName =
                        (TextView) convertView.findViewById(R.id.tv_grade_component_title);
                viewHolder.tvComponentAverage =
                        (TextView) convertView.findViewById(R.id.tv_grade_component_average);
                viewHolder.tvNumberOfItems =
                        (TextView) convertView.findViewById(R.id.tv_grade_component_number_of_items);

                // Set tag for convertView to view holder.
                convertView.setTag(viewHolder);
            } else {
                // Get view holder from convertView tag.
                viewHolder = (GroupViewHolder) convertView.getTag();
            }

            // Set text to views.
            viewHolder.tvComponentName.setText(gradeComponent.getName());
            viewHolder.tvComponentAverage.setText(currentAverage);
            viewHolder.tvNumberOfItems.setText(numItems);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            // View holder.
            ChildViewHolder viewHolder = null;

            // Current grade to create view for.
            Grade grade = (Grade) getChild(groupPosition, childPosition);

            // Decimal format to format double grade values.
            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            // Raw grade value * 100 for percentage.
            double rawGrade = 100 * (grade.getPointsEarned() / grade.getPointsPossible());

            // Get string value for grade percentage.
            String gradePercentage = decimalFormat.format(rawGrade) + "%";

            // String values for grade fraction.
            String gradeFraction = decimalFormat.format(grade.getPointsEarned())
                    + "/" + decimalFormat.format(grade.getPointsPossible());

            // Inflate view if it doesn't already exist.
            if (convertView == null) {
                viewHolder = new ChildViewHolder();

                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_course_grades_child,
                        parent, false);

                // Find views.
                viewHolder.tvGradeName = (TextView) convertView.findViewById(R.id.tv_grade_name);
                viewHolder.tvGradePercentage = (TextView) convertView.findViewById(R.id.tv_grade_percentage);
                viewHolder.tvGradeFraction = (TextView) convertView.findViewById(R.id.tv_grade_fraction);

                // Set tag for convertView to view holder;
                convertView.setTag(viewHolder);
            } else {
                // Get view holder form convertView tag.
                viewHolder = (ChildViewHolder) convertView.getTag();
            }

            // Set text to views.
            viewHolder.tvGradeName.setText(grade.getName());
            viewHolder.tvGradePercentage.setText(gradePercentage);
            viewHolder.tvGradeFraction.setText(gradeFraction);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private static class GroupViewHolder {
            TextView tvComponentName;
            TextView tvComponentAverage;
            TextView tvNumberOfItems;
        }

        private static class ChildViewHolder {
            TextView tvGradeName;
            TextView tvGradePercentage;
            TextView tvGradeFraction;
        }
    }
}
