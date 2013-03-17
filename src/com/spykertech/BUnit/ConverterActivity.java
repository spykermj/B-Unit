package com.spykertech.BUnit;

import java.util.Locale;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ConverterActivity extends FragmentActivity implements
		ActionBar.OnNavigationListener, OnItemSelectedListener, OnKeyListener {	

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private static final String STATE_INPUT_NUMBER = "input_number";
	private static final String STATE_SELECTED_FROM_ITEM = "selected_from_item";
	private static final String STATE_SELECTED_TO_ITEM = "selected_to_item";
	private static final String STATE_RPN_ENABLED = "rpn_enabled";
	
	private int savedFromPosition = 0;
	private int savedToPosition = 0;
	private boolean rpnEnabled = false;

	private final RpnEngine engine = new RpnEngine(10);
	
	private ArrayAdapter<CharSequence> getFromToAdapter() {
		String units = String.format(Locale.ENGLISH, "conversion_category_units%d", getActionBar().getSelectedNavigationIndex());
		Integer resourceId = getResources().getIdentifier(units, "array", getPackageName());
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, resourceId, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}
	
	private String getValueToConvert() {
		return getNumberInput().getText().toString();
	}
	
	private int getFromPosition() {
		return getFromSpinner().getSelectedItemPosition();
	}
	
	private int getToPosition() {
		return getToSpinner().getSelectedItemPosition();
	}
	
	private String getRpnExpression() {
		int spinnerCount = getFromSpinner().getAdapter().getCount();
		return getConversionMatrix(getActionBar().getSelectedNavigationIndex())[getFromPosition() * spinnerCount + getToPosition()];
	}

	private void updateDisplay() {
		String conversionValue = getValueToConvert();
		String rpnExpression = getRpnExpression();
		if(conversionValue.equals("")) {
			getResultTextView().setText(conversionValue);
		} else {
			getResultTextView().setText(engine.pushPop(conversionValue, rpnExpression));
		}
		
		if(rpnEnabled) {
			if(rpnExpression.contains("x")) {
				getStatusView().setText(rpnExpression.replace("x", conversionValue));
			} else {
				getStatusView().setText(String.format("%s,%s", conversionValue, rpnExpression));
			}
		} else {
			getStatusView().setText("");
		}
	}

	private String[] getConversionMatrix(int category) {
		String resource = String.format(Locale.ENGLISH, "conversion_matrix%d", category);
		Resources resources = getResources();
		Integer resourceId = resources.getIdentifier(resource, "array", getPackageName());
		return resources.getStringArray(resourceId);
	}
	
	private Spinner getFromSpinner() {
		return (Spinner) this.findViewById(R.id.spinner1);
	}
	
	private Spinner getToSpinner() {
		return (Spinner) this.findViewById(R.id.spinner2);
	}
	
	private TextView getResultTextView() {
		return (TextView) this.findViewById(R.id.textView3);
	}
	
	private TextView getStatusView() {
		return (TextView) this.findViewById(R.id.textView4);
	}
	
	private EditText getNumberInput() {
		return (EditText) this.findViewById(R.id.editText1);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_converter);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				ArrayAdapter.createFromResource(
						getActionBarThemedContextCompat(),
						R.array.conversion_type_array,
						android.R.layout.simple_spinner_item), this);
		getFromSpinner().setOnItemSelectedListener(this);
		getToSpinner().setOnItemSelectedListener(this);
		getNumberInput().setOnKeyListener(this);
	}

	/**
	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that
	 * simply returns the {@link android.app.Activity} if
	 * <code>getThemedContext</code> is unavailable.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private Context getActionBarThemedContextCompat() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getActionBar().getThemedContext();
		} else {
			return this;
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedState) {		
		// Restore the previously serialized current dropdown position.
		if (savedState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			int selected = savedState.getInt(STATE_SELECTED_NAVIGATION_ITEM);
			getActionBar().setSelectedNavigationItem(selected);
		}
		
		if (savedState.containsKey(STATE_INPUT_NUMBER)) {
			String inputNumber = savedState.getString(STATE_INPUT_NUMBER);
			getNumberInput().setText(inputNumber);
		}
		
		if (savedState.containsKey(STATE_SELECTED_FROM_ITEM)) {
			savedFromPosition = savedState.getInt(STATE_SELECTED_FROM_ITEM);
		}
		
		if (savedState.containsKey(STATE_SELECTED_TO_ITEM)) {
			savedToPosition = savedState.getInt(STATE_SELECTED_TO_ITEM);
		}
		
		if (savedState.containsKey(STATE_RPN_ENABLED)) {
			rpnEnabled = savedState.getBoolean(STATE_RPN_ENABLED);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
		outState.putString(STATE_INPUT_NUMBER, getValueToConvert());
		outState.putInt(STATE_SELECTED_FROM_ITEM, getFromPosition());
		outState.putInt(STATE_SELECTED_TO_ITEM, getToPosition());
		outState.putBoolean(STATE_RPN_ENABLED, rpnEnabled);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_converter, menu);
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		ArrayAdapter<CharSequence> adapter = getFromToAdapter();
		getFromSpinner().setAdapter(adapter);
		if(adapter.getCount() > savedFromPosition) {
			getFromSpinner().setSelection(savedFromPosition);
		} else {
			savedFromPosition = 0;
		}
		getToSpinner().setAdapter(adapter);
		if(adapter.getCount() > savedToPosition) {
			getToSpinner().setSelection(savedToPosition);			
		} else {
			savedToPosition = 0;
		}
		updateDisplay();
		return true;
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		updateDisplay();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}
	
	@Override
	public boolean onKey(View view, int keyCode, KeyEvent event) {
		updateDisplay();
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean returnValue = true;
		switch (item.getItemId()) {
			case R.id.menu_toggle_rpn:
				rpnEnabled = !rpnEnabled;
				updateDisplay();
				break;
			default:
				returnValue = super.onOptionsItemSelected(item);
		}
		return returnValue;
	}
}
