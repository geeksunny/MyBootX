package com.radicalninja.bootiescreen;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	// TODO Add code that checks build.prop for device name and force quit if user does not have Moto X (Or other possibly compatible devices, like the mini or G maybe?)
	// TODO Go through and standardize Javadoc comments.
	// TODO Review project's object code organization and variable privileges.
	final MainActivity parent = this;
	private static final String LOG_TAG = "MainActivity";
	private static final String PREFS_NAME = "BootscreenPrefs";

	DrawerLayout drawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
	FrameLayout leftDrawer;
	ImageView previewView;
	BootscreenHelper mBootscreenHelper;

	EditText inputMessage;
	SeekBar inputFontSize;
	TextView textFontSizeValue;
	Spinner inputTypeface;
	Button buttonColorPicker;
	Button buttonPreview;
	Button buttonSave;

	Integer textColor;
	TextView textColorPickerPreview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawer_layout);
		// Interface bindings
		LayoutInflater factory = getLayoutInflater();
		FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
		View bootControls = factory.inflate(R.layout.boot_controls, null);
		contentFrame.addView(bootControls);
		leftDrawer = (FrameLayout) findViewById(R.id.left_drawer);
		View bootPreview = factory.inflate(R.layout.boot_preview, null);
		leftDrawer.addView(bootPreview);
		// Navigation Drawer Layout
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(
				this,
				drawerLayout,
				R.drawable.ic_navigation_drawer,
				R.string.actionBarContentOpen,
				R.string.actionBarContentClose) {

			public void onDrawerClosed(View drawerView) {
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				invalidateOptionsMenu();
			}
		};
		drawerLayout.post(new Runnable() {
			@Override
			public void run() {
				parent.mDrawerToggle.syncState();
			}
		});
		drawerLayout.setDrawerListener(mDrawerToggle);

		previewView = (ImageView) findViewById(R.id.imagePreview);
		inputMessage = (EditText) findViewById(R.id.inputMessage);
		// - Font Size Selection
		inputFontSize = (SeekBar) findViewById(R.id.inputFontSize);
		textFontSizeValue = (TextView) findViewById(R.id.textFontSizeValue);
		inputFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				textFontSizeValue.setText(String.format("%ddp", (int) parent.getFontSize() ));
			}
		});
		// - Color Selection (PICKER)
		// TODO: Investigate the possibility / benefits / detriments to stuffing the color well + button into part of ColorPickerDialogBuilder and make it a more universal widget package.
		buttonColorPicker = (Button) findViewById(R.id.buttonColorPicker);
		buttonColorPicker.setOnClickListener(colorPicker);
		textColorPickerPreview = (TextView) findViewById(R.id.textColorPickerPreview);
		textColorPickerPreview.setOnClickListener(colorPicker);
		// - Typeface Selection
		inputTypeface = (Spinner) findViewById(R.id.inputTypeface);
		ArrayAdapter<CharSequence> adapterTypeface = ArrayAdapter.createFromResource(this, R.array.inputTypeface, android.R.layout.simple_spinner_item);
		adapterTypeface.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		inputTypeface.setAdapter(adapterTypeface);
		// - Preview Button
		buttonPreview = (Button) findViewById(R.id.buttonPreview);
		buttonPreview.setOnClickListener(previewButtonClicked);
		// - Save Button
		buttonSave = (Button) findViewById(R.id.buttonSave);
		buttonSave.setOnClickListener(saveButtonClicked);

		// Saved Preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		// - Personal Message
		inputMessage.setText(settings.getString("inputMessage", ""));
		// - Font Size
		int fontSize = settings.getInt("inputFontSize", 36);
		inputFontSize.setProgress(fontSize - 20);
		textFontSizeValue.setText(String.format("%ddp", fontSize ));
		// - Text Color
		textColor = settings.getInt("inputTextColor", Color.BLACK);
		textColorPickerPreview.setBackgroundColor(textColor);
		// - Typeface
		int inputTypefacePos = settings.getInt("inputTypefacePos", 0);
		if (inputTypeface.getCount() >= inputTypefacePos) {
			// Ensures that we don't try to select an item outside the list's bounds. Should probably be handled by a try / catch?
			inputTypeface.setSelection(inputTypefacePos);
		}
	}

	@Override
	public void onStart() {

		super.onStart();
		// Start off with the DEVICE_BACKUP image, automatically pulling one if it does not exist.
		loadImage();
	}

	@Override
	public void onResume() {

		super.onResume();
		// Reset bitmap to original state.
		//bootscreen.
	}
	
	/**
	 * Save all current Bootscreen control settings.
	 */
	public void saveSettings() {
		// Saved Preferences & Editor
		SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
		// - Personal Message
		editor.putString("inputMessage", inputMessage.getText().toString());
		// - Font Size
		editor.putInt("inputFontSize", (int) getFontSize());
		// - Text Color
		editor.putInt("inputTextColor", textColor);
		// - Typeface
		editor.putInt("inputTypefacePos", inputTypeface.getSelectedItemPosition());
		// Commit new settings
		editor.commit();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// TODO: Build out the settings of this app.
		getMenuInflater().inflate(R.menu.main, menu);
		if (!getSharedPreferences(PREFS_NAME, 0).getBoolean("bootscreenIsCustomized", false)) {
			menu.findItem(R.id.action_restoreBackup).setEnabled(false);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

        // Trigger the navigation drawer by tapping the action bar title/icon.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		// Handle menu item selection
		switch (item.getItemId()) {
		case R.id.action_restoreBackup:

			// AlertDialogs for handling the result of the installation procedure.
			final DialogInterface.OnClickListener handleRestorationOutcome = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						// YES (Success)
						RootHelper.restartDevice();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						// NO (Success)
						Toast.makeText(parent, "Restoration SUCCESS!", Toast.LENGTH_SHORT).show();
						invalidateOptionsMenu();
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						// NEUTRAL (Failure)
						Toast.makeText(parent, "Restoration FAILURE!", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			};
            final BootscreenHelperCallback restorationCallback = new BootscreenHelperCallback() {
                @Override
                void onSuccess(String successMessage) {
                    getSharedPreferences(PREFS_NAME, 0).edit().putBoolean("bootscreenIsCustomized", false).commit();
                    final AlertDialog.Builder handleRestorationSuccess = new AlertDialog.Builder(parent);
                    handleRestorationSuccess
                            .setMessage("Your device's original bootscreen was successfully installed! Would you like to reboot now and test it out?")
                            .setPositiveButton("Yes", handleRestorationOutcome)
                            .setNegativeButton("No", handleRestorationOutcome)
                            .show();
                }

                @Override
                void onFailure(String failureMessage, int flag) {
                    // TODO: Possibly add in code that conditions the response based on the flag. If that appears to be necessary.
                    final AlertDialog.Builder handleRestorationFailure = new AlertDialog.Builder(parent);
                    handleRestorationFailure
                            .setTitle("Restoration Failure")
                            .setIcon(android.R.drawable.stat_sys_warning)
                            .setMessage(failureMessage)
                            .setNeutralButton("Ok", handleRestorationOutcome)
                            .show();
                }

                @Override
                void onNeutral(String neutralMessage) { }
            };

			// AlertDialog for proceeding with the bootscreen installation.
			DialogInterface.OnClickListener doRestoration = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						// Yes button clicked
						saveSettings();
						mBootscreenHelper
                                .setCallback(restorationCallback)
                                .restoreDeviceOriginalBootscreen();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						// No button clicked
						Toast.makeText(parent, "Restoration CANCELLED!", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			};
			AlertDialog.Builder doInstallationDialog = new AlertDialog.Builder(parent);
			doInstallationDialog
				.setMessage("This will write your Device's original bootscreen to your phone's clogo block device. ARE YOU SURE YOU WANT TO DO THIS?")
				.setPositiveButton("Yes", doRestoration)
				.setNegativeButton("No", doRestoration)
				.show();


			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * OnClickListener for the Preview button
	 */
	private OnClickListener previewButtonClicked = new OnClickListener() {
		public void onClick(View v) {

            Bootscreen bootscreen = mBootscreenHelper.getBootscreen();
			// Reset the bootscreen to its original state for a new preview.
            bootscreen.resetBitmap();
			// Update settings for the bootscreen.
            bootscreen
                    .setTextSize(getFontSize())
                    .setColor(textColor)
                    .setTypeface(getTypeface());
			// Write to the bootscreen.
            bootscreen.doPersonalization(inputMessage.getText().toString());
            previewView.setImageBitmap(bootscreen.getBitmap());
			// Open the preview pane.
			drawerLayout.openDrawer(leftDrawer);
		}
	};
	
	/**
	 * OnClickListener for the Save button
	 */
	private OnClickListener saveButtonClicked = new OnClickListener() {

		public void onClick(View v) {

			// AlertDialogs for handling the result of the installation procedure.
			final DialogInterface.OnClickListener handleInstallationOutcome = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						// YES (Success)
						RootHelper.restartDevice();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						// NO (Success)
						Toast.makeText(parent, "Installation SUCCESS!", Toast.LENGTH_SHORT).show();
						invalidateOptionsMenu();
						drawerLayout.closeDrawer(leftDrawer);
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						// NEUTRAL (Failure)
						Toast.makeText(parent, "Installation FAILURE!", Toast.LENGTH_SHORT).show();
						drawerLayout.closeDrawer(leftDrawer);
						break;
					}
				}
			};
            final BootscreenHelperCallback installationCallback = new BootscreenHelperCallback() {
                @Override
                void onSuccess(String successMessage) {
                    // - SUCCESSFUL INSTALLTION AlertDialog
                    getSharedPreferences(PREFS_NAME, 0).edit().putBoolean("bootscreenIsCustomized", true).commit();
                    final AlertDialog.Builder handleInstallationSuccess = new AlertDialog.Builder(parent);
                    handleInstallationSuccess
                            .setMessage("Your personalized bootscreen was successfully installed! Would you like to reboot now and test it out?")
                            .setPositiveButton("Yes", handleInstallationOutcome)
                            .setNegativeButton("No", handleInstallationOutcome)
                            .show();
                }

                @Override
                void onFailure(String failureMessage, int flag) {
                    // TODO: Possibly add in code that conditions the response based on the flag. If that appears to be necessary.
                    final AlertDialog.Builder handleInstallationFailure = new AlertDialog.Builder(parent);
                    handleInstallationFailure
                            .setTitle("Installation Failure")
                            .setIcon(android.R.drawable.stat_sys_warning)
                            .setMessage(failureMessage)
                            .setNeutralButton("Ok", handleInstallationOutcome)
                            .show();
                }

                @Override
                void onNeutral(String neutralMessage) { }
            };

			// This AlertDialog will be shown to the user first.
			DialogInterface.OnClickListener doInstallation = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						// Yes button clicked
						saveSettings();
						mBootscreenHelper
                                .setCallback(installationCallback)
                                .installPersonalizedBootscreen();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						// No button clicked
						Toast.makeText(parent, "Installation CANCELLED!", Toast.LENGTH_SHORT).show();
						drawerLayout.closeDrawer(leftDrawer);
						break;
					}
				}
			};
			AlertDialog.Builder doInstallationDialog = new AlertDialog.Builder(parent);
			doInstallationDialog
				.setMessage("This will write your personalized bootscreen to your phone's clogo block device. ARE YOU SURE YOU WANT TO DO THIS?")
				.setPositiveButton("Yes", doInstallation)
				.setNegativeButton("No", doInstallation)
				.show();
		}
	};
	
	/**
	 * Calling forth a color picker!
	 */
	private OnClickListener colorPicker = new OnClickListener() {
		public void onClick(View v) {
			final ColorDialogBuilder builder = new ColorDialogBuilder(parent);
			builder.setCancelable(true);
			builder.setColor(textColor);
			builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.i(LOG_TAG, "Color picker was success!");
					updateTextColor(builder.getColorPicker().getColor());
				}

			});
			builder.create().show();
		}
	 };
	 
	 /**
	  * Update the color well's value.
	  * @param color The given color value.
	  */
	 private void updateTextColor(int color) {
		 textColor = color;
		 textColorPickerPreview.setBackgroundColor(color);
	 }

    /**
     * Get float value of current selected font size.
     * @return Returns a the font size as a float.
     */
	private float getFontSize() {
		int fontSize = inputFontSize.getProgress() + 16;
		return (float) fontSize;
	}

	/**
	 * Get the Typeface object for the current selected typeface
	 */
	private Typeface getTypeface() {
		//TODO: Change this from a Spinner to a radio selection group, and each option shows a preview of the typeface.

		Map<String, Typeface> typefaceMap = new HashMap<String, Typeface>();
		typefaceMap.put("Sans Serif", Typeface.DEFAULT);
		typefaceMap.put("Sans Serif, Bold", Typeface.DEFAULT_BOLD);
		typefaceMap.put("Monospace", Typeface.MONOSPACE);
		typefaceMap.put("Serif", Typeface.SERIF);

		return typefaceMap.get((String) inputTypeface.getAdapter().getItem(inputTypeface.getSelectedItemPosition()));
	}

    /**
     * Loads the bootscreen graphic from the device into the app's editor.
     */
	private void loadImage() {
        // Create the BootscreenHelperCallback object to be used during the .loadDeviceBootscreen() action.
        BootscreenHelperCallback loadScreenCallback = new BootscreenHelperCallback() {
            @Override
            void onSuccess(String successMessage) {
                Toast.makeText(getApplicationContext(), successMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            void onFailure(String failureMessage, int flag) {
                Toast.makeText(getApplicationContext(), failureMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            void onNeutral(String neutralMessage) { }
        };
		// Load the bitmap into the Bootscreen object
		mBootscreenHelper = new BootscreenHelper(parent)
                .setCallback(loadScreenCallback)
                .loadDeviceBootscreen();
        previewView.setImageBitmap(mBootscreenHelper.getBitmap());
	}

	/**
	 * Loads the given com.android.graphics.Bitmap into memory / the preview pane.
	 * @param	bitmap	The given Bitmap object to load.
	 */
	private void loadImage(Bitmap bitmap) {
		previewView.setImageBitmap(bitmap);
	}
	
	/**
	 * Handling the back-button when the preview pane is open.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (drawerLayout.isDrawerOpen(leftDrawer)) {
				drawerLayout.closeDrawers();
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
