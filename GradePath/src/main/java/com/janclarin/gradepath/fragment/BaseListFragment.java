package com.janclarin.gradepath.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.database.DatabaseFacade;
import com.janclarin.gradepath.model.DatabaseItem;

import java.util.ArrayList;
import java.util.List;

// TODO: Popup Menu for items
abstract public class BaseListFragment extends Fragment {

    protected static final int ITEM_VIEW_TYPE_HEADER = 0;
    protected static final int ITEM_VIEW_TYPE_DATABASE_ITEM = 1;
    protected static final int NUM_ITEM_VIEW_TYPES = 2;

    protected Context mContext;
    protected DatabaseFacade mDatabase;
    protected List<DatabaseItem> mListItems;
    protected TextView mEmptyTextView;
    protected ListView mListView;
    protected BaseListAdapter mAdapter;

    /**
     * Updates the list view within the fragment.
     */
    abstract public void updateListItems();

    /**
     * Edit select item under contextual action bar.
     */
    abstract protected void editSelectedItem(int selectedPosition);

    /**
     * Delete selected items under contextual action bar.
     * Deletes items starting with right side of the list.
     *
     * @param possibleSelectedPositions
     */
    abstract protected void deleteSelectedItems(SparseBooleanArray possibleSelectedPositions);

    /**
     * Clear items from list.
     */
    protected void clearListItems() {
        try {
            mListItems.clear();
        } catch (NullPointerException e) {
            mListItems = new ArrayList<DatabaseItem>();
        }
    }

    /**
     * Notify adapter of database set changed.
     */
    protected void notifyAdapter() {
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    /**
     * Sets up the list view.
     */
    protected void setUpListView() {
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            private int mNumSelected;
            private int mCurrentSelectedPosition;
            private SparseBooleanArray selectedPositions;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                                                  boolean checked) {
                Menu menu = mode.getMenu();
                MenuInflater inflater = mode.getMenuInflater();

                // When an item is checked keep track of how many are selected.
                if (checked) {
                    mNumSelected++;
                    selectedPositions.put(position, true);
                    // Change menu when more than one item is selected.
                    if (mNumSelected > 1) {
                        if (menu.getItem(0).getItemId() == R.id.menu_contextual_edit) {
                            menu.clear();
                            inflater.inflate(R.menu.list_item_selected, menu);
                        }
                    }
                } else {
                    if (mNumSelected > 0) {
                        mNumSelected--;
                        selectedPositions.put(position, false);
                    }

                    // Change menu when only one is left selected.
                    if (mNumSelected == 1) {
                        menu.clear();
                        inflater.inflate(R.menu.list_item_one_selected, menu);
                    }
                }

                mCurrentSelectedPosition = position;
                mode.setTitle(Integer.toString(mNumSelected));
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mNumSelected = 0;
                mCurrentSelectedPosition = 0;
                selectedPositions = new SparseBooleanArray();

                // Inflate the menu for the contextual action bar.
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.list_item_one_selected, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_contextual_edit:
                        editSelectedItem(mCurrentSelectedPosition);
                        mode.finish();
                        return true;
                    case R.id.menu_contextual_delete:
                        deleteSelectedItems(selectedPositions);
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mListView.getCheckedItemPositions().clear();
            }
        });
    }

    /**
     * Changes list state based on whether or not the list of items is empty.
     */
    protected void showEmptyStateView(boolean isEmpty) {
        if (isEmpty) {
            mEmptyTextView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mEmptyTextView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        mDatabase = DatabaseFacade.getInstance(mContext.getApplicationContext());
        mDatabase.open();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Indicate that this fragment has its own menu options.
        setHasOptionsMenu(true);
    }

    abstract protected class BaseListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mListItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mListItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return NUM_ITEM_VIEW_TYPES;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) == ITEM_VIEW_TYPE_DATABASE_ITEM;
        }

        @Override
        abstract public int getItemViewType(int position);

        @Override
        abstract public View getView(int position, View convertView, ViewGroup parent);
    }

    /**
     * Header for tasks.
     */
    protected class Header extends DatabaseItem {
        private final String name;

        public Header(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
