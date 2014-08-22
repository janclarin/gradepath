package com.janclarin.gradepath.fragment;

import android.app.Activity;
import android.os.Bundle;
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

    private static final DecimalFormat gpaFormat = new DecimalFormat("#.0#");
    private ImageButton mAddItemButton;
    private OnFragmentListSemesterListener mListener;

    private List<Semester> mSemesters;

    public ListSemesterFragment() {
        // Required empty public constructor.
    }

    public static ListSemesterFragment newInstance() {
        return new ListSemesterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_semester, container, false);

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

        mSemesters = mDatabase.getSemesters();

        if (!mSemesters.isEmpty()) {
            mListItems.add(new Header(null));
            for (Semester semester : mSemesters)
                mListItems.add(semester);
        }

        notifyAdapter();

        showEmptyStateView(mListItems.isEmpty());
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
    }

    private class ListAdapter extends BaseListAdapter {
        @Override
        public int getItemViewType(int position) {
            return (mListItems.get(position) instanceof Header) ?
                    ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_MAIN_2_LINE;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final DatabaseItem listItem = mListItems.get(position);
            final int type = getItemViewType(position);
            final ViewHolder viewHolder;

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
                    viewHolder.tvSubtitle =
                            (TextView) convertView.findViewById(R.id.tv_subtitle);
                    viewHolder.ivDetail = (ImageView) convertView.findViewById(R.id.iv_detail);
                    viewHolder.btnSecondary = convertView.findViewById(R.id.btn_secondary);
                }

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == ITEM_VIEW_TYPE_HEADER) {

                String info = getString(R.string.cumulative_gpa) + " ";
                double gpa = mDatabase.getCumulativeGPA(mSemesters);

                if (gpa > -1) {
                    info += gpaFormat.format(gpa);
                } else {
                    info += mContext.getString(R.string.not_available);
                }

                viewHolder.tvTitle.setText(info);
            } else {
                Semester semester = (Semester) listItem;
                viewHolder.tvTitle.setText(semester.toString());

                // Otherwise set label to completed and information to gpa.
                double gpa = semester.getGpa();

                // Set gpa if it exists.
                viewHolder.tvSubtitle.setText(
                        getString(R.string.tv_gpa) + ": "
                                + (gpa > -1 ?
                                gpaFormat.format(semester.getGpa())
                                : mContext.getString(R.string.not_available))
                );

                viewHolder.ivDetail.setBackground(getColorCircle(R.color.theme_primary));
                viewHolder.ivDetail.setImageResource(R.drawable.semester_light);

                // Set button to open popup menu.
                viewHolder.btnSecondary.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showPopupMenu(view, R.menu.list_general, position);
                    }
                });
            }

            return convertView;
        }
    }
}
