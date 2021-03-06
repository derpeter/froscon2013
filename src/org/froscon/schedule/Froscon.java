//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.


package org.froscon.schedule;

import com.actionbarsherlock.app.ActionBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
//import com.actionbarsherlock.view.Window;


import android.webkit.SslErrorHandler;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

import org.froscon.schedule.R;
import android.webkit.SslErrorHandler;

public class Froscon extends SherlockActivity {

	protected FrameLayout webViewPlaceholder;
	private WebView myWebView;
	private String url;
	private static final String default_url = "http://programm.froscon.org/mobil/index.html";

    public static final String PREFS_NAME = "EnyPrefs";
    private boolean fs = false;
    private Bitmap icon;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window. FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String tmp_url = settings.getString("url", null);
		WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
		
		if (tmp_url != null && !tmp_url.trim().equals("")) {
			url = tmp_url;
		} else {
			url = default_url;
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("url", url);
		    editor.commit();
		}
		
		//Handle fullscreen state fg rom stored settings
		fs = settings.getBoolean("fs", false);
		if (fs) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			fs = true;
		} else {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			fs = false;
		}
		
		initUI();
	}


	
	/** Reload the UI and create a web view is necessviewary. */
	protected void initUI() {
		webViewPlaceholder = ((FrameLayout) findViewById(R.id.webViewPlaceholder));

		if (myWebView == null) {
			myWebView = new WebView(this);


			 final Activity activity = this;
			 myWebView.setWebChromeClient(new WebChromeClient() {

			   public void onProgressChanged(WebView view, int progress) {
			     // Activities and WebViews measure progress with different scales.
			     // The progress meter will automatically disappear when we reach 100%
			     activity.setProgress(progress * 1000);
			   }
			 });
			// activity.progressDialog.show();
			myWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
			myWebView.setScrollbarFadingEnabled(true);
			myWebView.getSettings().setLoadsImagesAutomatically(true);

			// prevent links from be opened in an external browser
			myWebView.setWebViewClient(new WebViewClient(){
			    public void onReceivedError(WebView view, int errorCod,String description, String failingUrl) {
			        Toast.makeText(myWebView.getContext(), "Could not load schedule.\n Check internet connection" , Toast.LENGTH_LONG).show();
			        Log.w("FrOSCon","can't get schedule");
			        myWebView.loadUrl("file:///android_res/drawable/froschbutton.png");
			        //myWebView.setEnabled(false);
			    }
			});
			// Ignore SSL certificate errors because webview cant accept selffigned stuff
//			@Override
//			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//			    handler.proceed(); 
//			}
			
			// enable javascript
			myWebView.getSettings().setJavaScriptEnabled(true);			
			myWebView.loadUrl(url);

		}


		
		// Attach the WebView to its placeholder
		webViewPlaceholder.addView(myWebView);
	    ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

	}  
	

	
	/** Handle screen rotation */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (myWebView != null) {
			// Remove the WebView from the old placeholder
			webViewPlaceholder.removeView(myWebView);
		}

		super.onConfigurationChanged(newConfig);

		// Load the layout resource for the new configuration
		setContentView(R.layout.main);


		
		// Reinitialize the UI
		initUI();
	}

	/** Save instance state. */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the state of the WebView
		myWebView.saveState(outState);
	}

	/** Restore instance state */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		// Restore the state of the WebView
		myWebView.restoreState(savedInstanceState);
	}

	/** Handle back key. */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Check if the key event was the BACK key and if there's history
		if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
			myWebView.goBack();
			return true;
		}
		// If it wasn't the BACK key or there's no web page history, bubble up
		// to the default
		// system behavior (probably exit the activity)
		return super.onKeyDown(keyCode, event);
	}

	/** Called when the menu is created. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_options_menu, menu);
		return true;
	}
		
	/** Host choosing dialog. */
	private void hostpicker() {
		AlertDialog.Builder picker = new AlertDialog.Builder(this);
		picker.setTitle(R.string.URL);

		final EditText input = new EditText(this);
		input.setText(url);
		input.setInputType(16);
		picker.setView(input);
		picker.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int which) {
				Editable value = input.getText();
				if (!value.toString().trim().equals(""))
					url = value.toString();
				else
					url = default_url;
				myWebView.loadUrl(url);
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("url", url);
			    editor.commit();
			}
		});
		picker.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

					}
				});
		picker.show();
	}

	private void toggleFullScreen() {
		if (fs) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

			fs = false;
		} else {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

			fs = true;
		}
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("fs", fs);
	    editor.commit();
	}
	
	
	
	/** Called when menu buttons are puhsed. */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.refresh:
			//myWebView.reload();
//			myWebView.loadUrl( "javascript:window.location.reload( true )" );
			myWebView.loadUrl( default_url );
//			icon = myWebView.getFavicon();
//			//myWebView.getTouchIconUrl();
//			//if (!icon.equals(null))	{}			
//				getSupportActionBar().setIcon(new BitmapDrawable(getResources(), icon));
			return true;
		case R.id.info:
	        myWebView.loadUrl("http://froscon.org");			
//			Intent viewIntent = new Intent("android.intent.action.VIEW",
//					Uri.parse("http://www.froscon.org"));
//			startActivity(viewIntent);
			return true;
//		case R.id.host:
//			hostpicker();
//			return true;
//		case R.id.home:
//			myWebView.loadUrl(url);
//			return true;
		case android.R.id.home:
			myWebView.loadUrl(url);
            return true;
		case R.id.fs:
			toggleFullScreen();
			return true;
		case R.id.maps:
			startActivity(new Intent(this, Mapview.class).putExtra("fs", fs));
			return true;
		case R.id.nav:
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" +50.779868+","+7.182698));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}