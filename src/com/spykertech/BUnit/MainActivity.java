package com.spykertech.BUnit;


import java.util.Locale;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements
		ActionBar.OnNavigationListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private static final String SHOW_RPN = "show_rpn";

	private boolean showRpn = false;
	private OptionChangedListener changeListener = null;
	private PlaceholderFragment fragment = null;
	
	public boolean getShowRpn() {
		return showRpn;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if(savedInstanceState != null) {
			fragment = (PlaceholderFragment) getSupportFragmentManager().findFragmentById(R.id.container);
			changeListener = fragment;
		}

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				ArrayAdapter.createFromResource(actionBar.getThemedContext(),
						R.array.conversion_type_array,
						android.R.layout.simple_spinner_item), this);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.		
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}

		if (savedInstanceState.containsKey(SHOW_RPN)) {
			this.showRpn = savedInstanceState.getBoolean(SHOW_RPN);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getSupportActionBar()
				.getSelectedNavigationIndex());

		outState.putBoolean(SHOW_RPN, showRpn);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.menu_toggle_rpn) {
			showRpn = !showRpn;
			changeListener.onOptionChanged(getSupportActionBar().getSelectedNavigationIndex());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		Bundle state = null;
		if(fragment != null) {
			state = fragment.getCurrentState();
		}
		if(fragment == null) {
			fragment = PlaceholderFragment.newInstance(position, state);
			changeListener = fragment;
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, fragment).commit();
		} else {
			changeListener.onOptionChanged(position);
		}
		return true;
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements
			OnItemSelectedListener, OptionChangedListener {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		private static RpnEngine engine = new RpnEngine(10);

		private static final String FROM_POSITION = "from_position";
		private static final String TO_POSITION = "to_position";
		private static final String INPUT_VALUE = "input_value";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 * 
		 * @param state
		 */
		public static PlaceholderFragment newInstance(int sectionNumber, Bundle args) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			if (args == null) {
				args = new Bundle();
			}
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}
		
		private void setSpinnerAdapters(Spinner fromSpinner, Spinner toSpinner) {
			SpinnerAdapter adapter = getFromToAdapter();
			fromSpinner.setAdapter(adapter);
			toSpinner.setAdapter(adapter);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle state) {
			if(state == null) {
				state = getArguments();
			}
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			Spinner fromSpinner = (Spinner) rootView
					.findViewById(R.id.spinner1);
			Spinner toSpinner = (Spinner) rootView.findViewById(R.id.spinner2);
			setSpinnerAdapters(fromSpinner, toSpinner);
			fromSpinner.setOnItemSelectedListener(this);
			toSpinner.setOnItemSelectedListener(this);
			EditText numberInput = (EditText) rootView.findViewById(R.id.editText1);
			if (state != null) {
				if (state.containsKey(INPUT_VALUE)) {
					numberInput.setText(state.getString(INPUT_VALUE));
				}

				if (state.containsKey(FROM_POSITION) && fromSpinner.getCount() > state.getInt(FROM_POSITION)) {
					fromSpinner.setSelection(state.getInt(FROM_POSITION));
				}

				if (state.containsKey(TO_POSITION) && toSpinner.getCount() > state.getInt(TO_POSITION)) {
					toSpinner.setSelection(state.getInt(TO_POSITION));
				}
			}
			
			numberInput.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {

				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					updateDisplay();
				}
			});
			return rootView;
		}

		private EditText getNumberInput() {
			return (EditText) getActivity().findViewById(R.id.editText1);
		}

		private ArrayAdapter<CharSequence> getFromToAdapter() {
			String units = String.format(Locale.ENGLISH,
					"conversion_category_units%d",
					getArguments().getInt(ARG_SECTION_NUMBER));
			Integer resourceId = getResources().getIdentifier(units, "array",
					getActivity().getPackageName());
			ArrayAdapter<CharSequence> adapter = ArrayAdapter
					.createFromResource(getActivity(), resourceId,
							android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			return adapter;
		}

		private TextView getResultsTextView() {
			return (TextView) getActivity().findViewById(R.id.textView3);
		}

		private TextView getRpnTextView() {
			return (TextView) getActivity().findViewById(R.id.textView4);
		}

		private void updateDisplay() {
			CharSequence conversionValue = getNumberInput().getText();
			String rpnExpression = getRpnExpression();
			if (conversionValue.toString().equals("")) {
				getResultsTextView().setText(conversionValue);
			} else {
				getResultsTextView().setText(
						engine.pushPop(conversionValue.toString(),
								rpnExpression));
			}

			MainActivity activity = (MainActivity) getActivity();
			if (activity.getShowRpn()) {
				if (rpnExpression.contains("x")) {
					getRpnTextView().setText(
							rpnExpression.replace("x", conversionValue));
				} else {
					getRpnTextView().setText(
							String.format("%s,%s", conversionValue,
									rpnExpression));
				}
			} else {
				getRpnTextView().setText("");
			}
		}

		private Spinner getFromSpinner() {
			return (Spinner) getActivity().findViewById(R.id.spinner1);
		}

		private Spinner getToSpinner() {
			return (Spinner) getActivity().findViewById(R.id.spinner2);
		}

		private String[] getConversionMatrix(int category) {
			String resource = String.format(Locale.ENGLISH,
					"conversion_matrix%d", category);
			Resources resources = getResources();
			Integer resourceId = resources.getIdentifier(resource, "array",
					getActivity().getPackageName());
			return resources.getStringArray(resourceId);
		}

		private String getRpnExpression() {
			String returnValue;
			try {
				int spinnerCount = getFromSpinner().getAdapter().getCount();
				int fromPosition = getFromSpinner().getSelectedItemPosition();
				int toPosition = getToSpinner().getSelectedItemPosition();
				int conversionMatrixCategory = getArguments().getInt(
						ARG_SECTION_NUMBER);
				int conversionMatrixIndex = fromPosition * spinnerCount
						+ toPosition;
				returnValue = getConversionMatrix(conversionMatrixCategory)[conversionMatrixIndex];
			} catch (NullPointerException e) {
				returnValue = "";
			}
			return returnValue;
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			updateDisplay();
		}

		@Override
		public void onNothingSelected(AdapterView<?> view) {
		}

		@Override
		public void onOptionChanged(int section) {
			int currentSection = -1;
			if(getArguments().containsKey(ARG_SECTION_NUMBER)) {
				currentSection = getArguments().getInt(ARG_SECTION_NUMBER);
			}
			
			if(currentSection != section) {
				getArguments().putInt(ARG_SECTION_NUMBER, section);
				//Bundle state = getCurrentState();
				setSpinnerAdapters(getFromSpinner(), getToSpinner());
				//restoreState(state);
				getNumberInput().setText("");
			}
			updateDisplay();
		}
		
		public Bundle getCurrentState() {
			Bundle returnValue;
			if(getArguments() != null) {
				returnValue = getArguments();
			} else {
				returnValue = new Bundle();
			}
			
			returnValue.putInt(FROM_POSITION, getFromSpinner().getSelectedItemPosition());
			returnValue.putInt(TO_POSITION, getToSpinner().getSelectedItemPosition());
			returnValue.putString(INPUT_VALUE, getNumberInput().getText().toString());
			
			return returnValue;
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			if (this.getArguments() != null) {
				outState.putAll(this.getArguments());
			}

			outState.putAll(getCurrentState());
			super.onSaveInstanceState(outState);
		}
	}

}