package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.model.DatabaseItem;
import com.janclarin.gradepath.model.Semester;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Fragment with list of list_course that allows for editing and sets option for a new course.
 */
public class ListSemesterFragment extends BaseListFragment {

    private ImageButton mAddItemButton;
    private OnFragmentListSemesterListener mListener;

    public static ListSemesterFragment newInstance() {
        return new ListSemesterFragment();
    }

    public ListSemesterFragment() {
        // Required empty public constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_semester, container, false);

        mEmptyTextView = (TextView) rootView.findViewById(R.id.tv_list_empty);
        mListView = (ListView) rootView.findViewById(R.id.lv_list_items);
        mAddItemButton = (ImageButton) rootView.findViewById(R.id.btn_add_item);

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEmptyTextView.setText(R.string.tv_list_semester_empty);
        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) mListener.onListSemesterNew();
            }
        });

        updateListItems();
        mAdapter = new ListAdapter();
        setUpListView();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mListener != null)
                    mListener.onListSemesterEdit((Semester) mListItems.get(position));
            }
        });
    }

    @Override
    public void updateListItems() {
        clearListItems();

        List<Semester> semesters = mDatabase.getSemesters();
        Semester currentSemester = mDatabase.getCurrentSemester();

        // Add current semester header and semester into list if they exist.
        if (currentSemester != null) {
            mListItems.add(new Header(getString(R.string.semester_current)));
            mListItems.add(currentSemester);

            // Remove current semester from list of all semesters.
            semesters.remove(currentSemester);
        }

        // Add all other semesters under "Past" header.
        if (semesters.size() > 0) {
            mListItems.add(new Header(null));
            mListItems.addAll(semesters);
        }

        notifyAdapter();

        showEmptyStateView(mListItems.isEmpty());
    }

    @Override
    protected void editSelectedItem(int selectedPosition) {
        if (mListener != null)
            mListener.onListSemesterEdit((Semester) mAdapter.getItem(selectedPosition));
    }

    @Override
    protected void deleteSelectedItems(SparseBooleanArray possibleSelectedPositions) {
        int numItems = mListItems.size();
        for (int i = numItems - 1; i >= 0; i--) {
            if (possibleSelectedPositions.get(i, false)) {
                Semester selectedSemester = (Semester) mAdapter.getItem(i);
                if (mListener != null) {
                    mListener.onListSemesterDelete(selectedSemester);
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentListSemesterListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListSemesterCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Provides an interface to alert listeners.
     */
    public interface OnFragmentListSemesterListener {

        /* Called when the add item button is clicked. */
        public void onListSemesterNew();

        /* Called when contextual action bar edit button is clicked. */
        public void onListSemesterEdit(Semester semester);

        /* Called when contextual action bar delete button is clicked. */
        public void onListSemesterDelete(Semester semester);
    }

    private class ListAdapter extends BaseListAdapter {
        @Override
        public int getItemViewType(int position) {
            return (mListItems.get(position) instanceof Header) ?
                    ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_MAIN_2_LINE;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final DatabaseItem listItem = mListItems.get(position);
            final int type = getItemViewType(position);
            final ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();

                if (type == ITEM_VIEW_TYPE_HEADER) {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.list_header_general, parent, false);
                    viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title_header);
                    viewHolder.divider = convertView.findViewById(R.id.divider_view);
                } else {
                    convertView = LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_general_two_line, parent, false);
                    viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
                    viewHolder.tvSubtitle =
                            (TextView) convertView.findViewById(R.id.tv_subtitle);
                    viewHolder.ivDetail = (ImageView) convertView.findViewById(R.id.iv_detail);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {

                String info = ((Header) listItem).getName();

                if (info == null) {
                    double gpa = mDatabase.getCumulativeGPA();
                    info = getString(R.string.cumulative_gpa) + " ";
                    if (gpa > -1) {
                        info += new DecimalFormat("#.0#").format(gpa);
                    } else {
                        info += mContext.getString(R.string.not_available);
                    }
                }

                viewHolder.tvTitle.setText(info);

                if (position == 0) viewHolder.divider.setVisibility(View.GONE);
            } else {
                Semester semester = (Semester) listItem;
                viewHolder.tvTitle.setText(semester.toString());

                // Otherwise set label to completed and information to gpa.
                double gpa = semester.getGpa();

                // Set gpa if it exists.
                viewHolder.tvSubtitle.setText(
                        getString(R.string.tv_gpa) + ": "
                                + (gpa > -1 ?
                                Double.toString(semester.getGpa())
                                : mContext.getString(R.string.not_available))
                );

                viewHolder.ivDetail.setBackground(getColorCircle(R.color.theme_primary));
                viewHolder.ivDetail.setImageResource(R.drawable.semester_light);
            }

            return convertView;
        }
    }
}
