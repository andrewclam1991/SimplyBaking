package com.andrewclam.bakingapp;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.andrewclam.bakingapp.models.Step;

import org.parceler.Parcels;

import java.util.ArrayList;

import static com.andrewclam.bakingapp.StepDetailFragment.ARG_TWO_PANE_MODE;

public class StepDetailActivity extends AppCompatActivity implements StepDetailFragment.OnStepDetailFragmentInteraction{

    /**
     * The list of steps to populate the dropdown spinner adapter
     */
    private int mStepPosition;
    private ArrayList<Step> mSteps;
    private boolean mTwoPane;

    /**
     * Public keys for intent extras
     */
    public static final String EXTRA_STEPS_LIST = "extra.steps.list";
    public static final String EXTRA_STEP_POSITION = "extra.step.position";

    /**
     * Instance of the StepsAdapter
     */
    private StepsAdapter mStepsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Get the list of steps from intent extra
        assert getIntent() != null;

        mSteps = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_STEPS_LIST));
        mStepPosition = getIntent().getIntExtra(EXTRA_STEP_POSITION,0);
        mTwoPane = getIntent().getBooleanExtra(ARG_TWO_PANE_MODE,false);

        assert mSteps != null;

        // Initialize the StepsAdapter
        mStepsAdapter = new StepsAdapter(this,mSteps);
        mStepsAdapter.notifyDataSetChanged();

        // Setup spinner
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setAdapter(mStepsAdapter);

        // Move adapter to the parameter step position
        spinner.setSelection(mStepPosition);

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Find the selected item in the adapter, this will be used
                // to start the fragment
                Step selectedStep = mStepsAdapter.getItem(position);

                // When the given dropdown item is selected, show its contents in the
                // container view. set twoPane to false because this activity will not be
                // fired in two panes mode
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, StepDetailFragment.newInstance(
                                selectedStep, false))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    @Override
    public void setTitle(String title) {

    }


    private static class StepsAdapter extends ArrayAdapter<Step> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;
        private Context mContext;

        public StepsAdapter(Context context, ArrayList<Step> steps) {
            super(context, android.R.layout.simple_list_item_1, steps);
            mContext = context;
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the step item from the current position
            Step step = getItem(position);
            assert step != null;

            // Inflate the convertView with the style, reuse if it is already inflated
            View view;
            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }else
            {
                view = convertView;
            }

            // Set the spinner text to be the step's short description;
            TextView labelView = view.findViewById(android.R.id.text1);

            String spinnerText = mContext.getString(R.string.step, step.getId()) + " "
                    + step.getShortDescription();

            labelView.setText(spinnerText);
            labelView.setTextColor(ContextCompat.getColor(mContext,R.color.colorWhite));

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            // For styling views when spinner drops down
            // Get the step item from the current position
            Step step = getItem(position);
            assert step != null;

            // Inflate the convertView with the style, reuse if it is already inflated
            View view;
            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view =  inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }else
            {
                view = convertView;
            }

            // Set the spinner text to be the step's short description;
            TextView labelTv = view.findViewById(android.R.id.text1);

            String labelText = mContext.getString(R.string.step, step.getId()) + " "
                    + step.getShortDescription();

            labelTv.setText(labelText);

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }
}
