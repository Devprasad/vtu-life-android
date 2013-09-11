package com.V4Creations.vtulife.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.V4Creations.vtulife.R;
import com.V4Creations.vtulife.adapters.VTULifeFragmentAdapter.FragmentInfo;
import com.V4Creations.vtulife.ui.VTULifeMainActivity;
import com.V4Creations.vtulife.util.ActionBarStatus;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.keyboardsurfer.android.widget.crouton.Style;

public class VTULifeWebFragment extends SherlockFragment implements
		FragmentInfo {

	String TAG = "VtuLifeWebFragment";
	private VTULifeMainActivity vtuLifeMainActivity;
	private WebView vtuLifeWebView;
	private boolean isForwardEnabled = false, isBackEnabled = true,
			isRefresh = true;
	private final int BACK_MENU = 0, REFRESH_MENU = 1, FORWARD_MENU = 2;
	private ProgressBar loadingProgressBar;
	private TextView progressTextView;
	private String currentUrl = "http://www.vtulife.com";
	private ActionBarStatus mActionBarStatus;

	public VTULifeWebFragment() {
		mActionBarStatus = new ActionBarStatus();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		vtuLifeMainActivity = (VTULifeMainActivity) getActivity();
		return inflater.inflate(R.layout.vtu_life_web_layout, null, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initViews();
		vtuLifeWebView.loadUrl(currentUrl);
	}

	private void initViews() {
		loadingProgressBar = (ProgressBar) getView().findViewById(
				R.id.loadingProgressBar);
		progressTextView = (TextView) getView().findViewById(
				R.id.progressTextView);
		hideLodingProgressBar();
		vtuLifeWebView = (WebView) getView().findViewById(R.id.vtuLifeWebView);
		initWebView();
	}

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	private void initWebView() {
		vtuLifeWebView.getSettings().setJavaScriptEnabled(true);
		vtuLifeWebView.addJavascriptInterface(new JavaScriptInterface(),
				"HTMLOUT");

		vtuLifeWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				loadingProgressBar.setProgress(progress);
			}
		});

		vtuLifeWebView.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				vtuLifeMainActivity
						.showCrouton(description, Style.ALERT, false);
				showReload();
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				isRefresh = false;
				isBackEnabled = vtuLifeWebView.canGoBack();
				isForwardEnabled = vtuLifeWebView.canGoForward();
				mActionBarStatus.subTitle = "Loading...";
				vtuLifeMainActivity.reflectActionBarChange(mActionBarStatus,
						VTULifeMainActivity.ID_VTU_LIFE_WEB_FRAGMENT);
				showLoadingProgressBar();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				currentUrl = url;
				vtuLifeWebView
						.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
				isRefresh = true;
				isBackEnabled = vtuLifeWebView.canGoBack();
				isForwardEnabled = vtuLifeWebView.canGoForward();
				mActionBarStatus.subTitle = null;
				vtuLifeMainActivity.reflectActionBarChange(mActionBarStatus,
						VTULifeMainActivity.ID_VTU_LIFE_WEB_FRAGMENT);
				hideLodingProgressBar();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.matches("http://www.vtulife.com/resource.php.*")) {
					String currentPostion = url.substring(35);
					view.loadUrl("http://www.vtulife.com/mresource.php"
							+ currentPostion);
				} else if (url
						.equals("https://play.google.com/store/apps/details?id=com.V4Creations.vtulife"))
					vtuLifeMainActivity.rateAppOnPlayStore();
				// TODO Parse url
				else
					view.loadUrl(url);
				return true;
			}
		});
		vtuLifeWebView.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
	}

	class JavaScriptInterface {
		public void showHTML(String html) {
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.vtu_life_web_layout, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (isForwardEnabled) {
			menu.getItem(FORWARD_MENU).setEnabled(true);
			menu.getItem(FORWARD_MENU).setIcon(R.drawable.forward);
		} else {
			menu.getItem(FORWARD_MENU).setEnabled(false);
			menu.getItem(FORWARD_MENU).setIcon(R.drawable.forward_desable);
		}
		if (isBackEnabled) {
			menu.getItem(BACK_MENU).setEnabled(true);
			menu.getItem(BACK_MENU).setIcon(R.drawable.back);
		} else {
			menu.getItem(BACK_MENU).setEnabled(false);
			menu.getItem(BACK_MENU).setIcon(R.drawable.back_desable);
		}
		if (isRefresh)
			menu.getItem(REFRESH_MENU).setIcon(R.drawable.ic_refresh);
		else
			menu.getItem(REFRESH_MENU).setIcon(
					android.R.drawable.ic_menu_close_clear_cancel);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.menu_back:
			vtuLifeWebView.goBack();
			return true;
		case R.id.menu_forward:
			vtuLifeWebView.goForward();
			return true;
		case R.id.menu_refresh:
			if (isRefresh)
				vtuLifeWebView.reload();
			else {
				isRefresh = true;
				vtuLifeMainActivity.supportInvalidateOptionsMenu();
				vtuLifeWebView.stopLoading();
			}
			return true;
		}
		return false;
	}

	private void hideLodingProgressBar() {
		loadingProgressBar.setVisibility(View.GONE);
	}

	private void showLoadingProgressBar() {
		loadingProgressBar.setProgress(0);
		loadingProgressBar.setVisibility(View.VISIBLE);
		progressTextView.setVisibility(View.GONE);
	}

	private void showReload() {
		hideLodingProgressBar();
		progressTextView.setVisibility(View.VISIBLE);
	}

	@Override
	public String getTitle() {
		return "Website";
	}

	@Override
	public ActionBarStatus getActionBarStatus() {
		return mActionBarStatus;
	}
}
