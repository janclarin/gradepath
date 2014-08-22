package com.janclarin.gradepath.activity;

import android.os.Bundle;
import android.widget.Toast;

import com.janclarin.gradepath.R;
import com.janclarin.gradepath.dialog.SemesterDialog;
import com.janclarin.gradepath.fragment.BaseListFragment;
import com.janclarin.gradepath.fragment.ListSemesterFragment;
import com.janclarin.gradepath.model.Semester;

public class ListFragmentActivity extends BaseActivity
        implements ListSemesterFragment.OnFragmentListSemesterListener,
        SemesterDialog.OnDialogSemesterListener {

    public static final String FRAGMENT_TYPE = "Fragment";
    BaseListFragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_fragment);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


        mCurrentFragment = ListSemesterFragment.newInstance();
        getActionBar().setTitle(R.string.title_fragment_list_semesters);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, mCurrentFragment)
                .commit();
    }

    private void refreshList() {
        mCurrentFragment.updateListItems();
    }

    @Override
    public void onListSemesterNew() {
        SemesterDialog semesterDialog = SemesterDialog.newInstance(
                getString(R.string.title_semester_dialog));
        semesterDialog.show(getFragmentManager(), NEW_SEMESTER_TAG);
    }

    @Override
    public void onListSemesterEdit(Semester semester) {
        SemesterDialog semesterDialog = SemesterDialog.newInstance(
                getString(R.string.title_semester_dialog), semester);
        semesterDialog.show(getFragmentManager(), EDIT_SEMESTER_TAG);
    }

    @Override
    public void onSemesterSaved(boolean isNew) {
        // String is set to "semester saved" if grade is new, if updating "semester updated."
        String toastMessage = isNew ? getString(R.string.toast_semester_saved) :
                getString(R.string.toast_semester_updated);

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

        refreshList();
    }
}
