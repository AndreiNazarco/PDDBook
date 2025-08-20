package com.pdd.book;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class Description extends Activity implements OnClickListener  {

	public static boolean 	isViewPub 			= true;
	public static String 	isLanguage 			= "";
	public static boolean	day_night 			= true;
	public static int 		size 				= 18;
	public static int 		text_size			= 10;
	public static int 		isCaption			= 0;

	private float x1, x2;
	private final int MIN_DISTANCE = 150; // минимальная дистанция для свайпа
	private GestureDetector gestureDetector;

	ImageButton  	ibBackDesc;
	ImageButton  	ibConfigDesc;

	TextView     	tvCaptionDesc;

	EditText 		etSearch;
	Button 			btnNext;
	Button 			btnPrev;
	LinearLayout 	searchPanel;

	WebView 	 	webView;

	LinearLayout	llPubDesc;

	DBHelper 	 	dbHelper;

	Resources    	localResources;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //скрываем заголовок
		setContentView(R.layout.description);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Создаем объект для создания и управления версиями БД
		dbHelper = new DBHelper(this);

		// Найдем View-элементы
		tvCaptionDesc	 	= (TextView) 		findViewById(R.id.itvCaptionDesc);

		ibBackDesc			= (ImageButton) 	findViewById(R.id.iibBackDesc);
		ibConfigDesc		= (ImageButton) 	findViewById(R.id.iibConfigDesc);

//		etSearch 			= (EditText)		findViewById(R.id.etSearch);
//		btnNext 			= (Button)			findViewById(R.id.btnNext);
//		btnPrev 			= (Button)			findViewById(R.id.btnPrev);
//		searchPanel 		= (LinearLayout)	findViewById(R.id.searchPanel);

		webView 	      	= (WebView) 		findViewById(R.id.iwvHTML);

		gestureDetector = new GestureDetector(this, new GestureListener());
		webView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

		// Присваиваем обработчик кнопкам
		ibBackDesc.setOnClickListener(this);
		ibConfigDesc.setOnClickListener(this);

//		btnNext.setOnClickListener(v -> webView.findNext(true));
//		btnPrev.setOnClickListener(v -> webView.findNext(false));

//		etSearch.setOnEditorActionListener((v, actionId, event) -> {
//			String text = etSearch.getText().toString().trim();
//			searchInWebView(text);
//			return true;
//		});

		// Загрузка данных
		LoadDateToDescription();
	}

	@SuppressLint("Range")
    public void LoadDateToDescription()
	{
		SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
		isLanguage 				  = myPrefs.getString("isLanguage", "ro");
		day_night 				  = myPrefs.getBoolean("day_night", true); // true - day, false - night
		text_size 				  = Integer.valueOf(myPrefs.getString("text_size", "10"));
		isCaption 				  = myPrefs.getInt("isCaption", 0);

		//Язык пользователя
		Resources baseResources = getResources();
		Locale locale = new Locale(isLanguage);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		localResources = new Resources(baseResources.getAssets(), baseResources.getDisplayMetrics(), config);

		String strValue = "";

		// Подключаемся к БД
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String selectQuery = " SELECT name " +
				" FROM cuprins " +
				" WHERE language like '" + localResources.getString(R.string.language).trim() + "' " +
				"   AND cuprins_order = " + isCaption +
				" ORDER BY cuprins_order";

		// Данные из базы --
		Cursor cCuprins = db.rawQuery(selectQuery, null);

		// Ставим позицию курсора на первую строку выборки
		// если в выборке нет строк, вернется false
		if (cCuprins.moveToFirst())
		{
			do
			{
				strValue = cCuprins.getString(cCuprins.getColumnIndex("name")).replace("\\n", "\n");
				// Переход на следующую строку
				// а если следующей нет (текущая - последняя), то false - выходим из цикла
			} while (cCuprins.moveToNext());
		}
		cCuprins.close();
		db.close();
		tvCaptionDesc.setText(strValue);

		try {

			webView.setWebViewClient(new WebViewClient() {
				private Bitmap bitmap;

				@Override
				public void onPageFinished(WebView view, String url) {
					applyThemeToWebView();
				}

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					//Log.d("Resolution", "shouldOverrideUrlLoading: " + url);
					try {
						Uri uri = Uri.parse(url);
						String lastPath = uri.getLastPathSegment();

						if ("next".equalsIgnoreCase(lastPath)) {
							isCaption++;
							saveCaptionIndex();
							LoadDateToDescription();
							return true;
						} else if ("previous".equalsIgnoreCase(lastPath)) {
							isCaption--;
							saveCaptionIndex();
							LoadDateToDescription();
							return true;
						}

						int exist = 0;
						String img = "", str = "", strValue = "";
						LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

						if ((url.trim().compareTo("file:///android_res/drawable/previous") == 0) || (url.trim().compareTo("file:///android_res/drawable/next") == 0)) {
//							if (url.trim().compareTo("file:///android_res/drawable/previous") == 0) {
//								isCaption = isCaption - 1;
//
//								SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
//								Editor eddPub = myPrefs.edit();
//								eddPub.putInt("isCaption", isCaption);
//								eddPub.commit();
//
//								LoadDateToDescription();
//							}
//
//							if (url.trim().compareTo("file:///android_res/drawable/next") == 0) {
//								isCaption = isCaption + 1;
//
//								SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
//								Editor eddPub = myPrefs.edit();
//								eddPub.putInt("isCaption", isCaption);
//								eddPub.commit();
//
//								LoadDateToDescription();
//							}
						} else {
							final Dialog dialog = new Dialog(Description.this);
							dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
							dialog.setContentView(R.layout.dialog);

							LinearLayout lDialog = (LinearLayout) dialog.findViewById(R.id.llDialog);

							LinearLayout lDialogIndex = (LinearLayout) dialog.findViewById(R.id.llDialogIndex);

							if (day_night == false) {
								lDialogIndex.setBackgroundColor(Color.parseColor("#000000"));
							} else {
								lDialogIndex.setBackgroundColor(Color.parseColor("#ffffff"));
							}

							for (int I = 0; I <= url.length(); I++) {
								if ((url.length() - I >= 6) && (url.substring(I, I + 6).toLowerCase().compareTo("image_") == 0)) {
									//-- Картинка если ести
									for (int J = I; J <= url.length(); J++) {
										if ((url.length() - J >= 1) && (url.substring(J, J + 1).toLowerCase().compareTo(";") == 0)) {
											img = url.substring(I, J);
											//Log.d("Resolution", "img:" + img + ":");
											exist++;
											break;
										}
									}

									try {
										BitmapFactory.Options opts = new Options();
										opts.inPurgeable = true;
										bitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(img.trim(), "drawable", getPackageName()), opts);

											/*int idImage = getResources().getIdentifier(img.trim(), "drawable", getPackageName());
			          						  bitmap = BitmapFactory.decodeResource(getResources(), idImage);*/
									} catch (Exception $e) {
										bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
									}

									ImageView newImage = new ImageView(Description.this);
									newImage.setImageBitmap(this.bitmap);
									newImage.setScaleType(ScaleType.FIT_CENTER);
									newImage.setPadding(0, 10, 0, 0);
									lDialog.addView(newImage, lParams);
								}

								if ((url.length() - I >= 12) && (url.substring(I, I + 12).toLowerCase().compareTo("indicatoare_") == 0)) {
									//-- Картинка если ести
									for (int J = I; J <= url.length(); J++) {
										if (((url.length() - J >= 1) && (url.substring(J, J + 1).toLowerCase().compareTo(";") == 0)) || (J == url.length())) {
											str = url.substring(I, J);
											//Log.d("Resolution", "str: " + str);
											exist++;
											break;
										}
									}

									// Подключаемся к БД
									SQLiteDatabase db = dbHelper.getWritableDatabase();

									String selectQuery = " SELECT name " +
											" FROM indicatoare " +
											" WHERE language like '" + localResources.getString(R.string.language).trim() + "' " +
											"   AND code like '" + str + "' ";

									// Данные из базы --
									Cursor cIndicatoare = db.rawQuery(selectQuery, null);

									// Ставим позицию курсора на первую строку выборки
									// если в выборке нет строк, вернется false
									strValue = "";
									if (cIndicatoare.moveToFirst()) {
										do {
											strValue = cIndicatoare.getString(cIndicatoare.getColumnIndex("name")).replace("\\n", "\n");
											// Переход на следующую строку
											// а если следующей нет (текущая - последняя), то false - выходим из цикла
										} while (cIndicatoare.moveToNext());
									}
									cIndicatoare.close();
									db.close();

									TextView newTextRasp = new TextView(Description.this);
									newTextRasp.setText(strValue);
									newTextRasp.setPadding(0, 0, 10, 0);

									if (day_night == false) {
										newTextRasp.setTextColor(Color.parseColor("#ffffff"));
									} else {
										newTextRasp.setTextColor(Color.parseColor("#000000"));
									}
									lDialog.addView(newTextRasp, lParams);
								}
							}


							if (exist == 0) {
								TextView newTextRasp = new TextView(Description.this);
								newTextRasp.setText(localResources.getString(R.string.image_not_found));

								if (day_night == false) {
									newTextRasp.setTextColor(Color.parseColor("#ffffff"));
								} else {
									newTextRasp.setTextColor(Color.parseColor("#000000"));
								}

								lDialog.addView(newTextRasp, lParams);
							}

							TextView newTextNull = new TextView(Description.this);
							newTextNull.setText("   ");
							lDialog.addView(newTextNull, lParams);

							Button newButton1 = new Button(Description.this);
							newButton1.setText(localResources.getString(R.string.close));

							if (day_night == false) {
								newButton1.setTextColor(Color.parseColor("#ffffff"));
							} else {
								newButton1.setTextColor(Color.parseColor("#000000"));
							}

							newButton1.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									dialog.dismiss();
								}
							});

							// Создаем параметры макета с отступами
							LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.MATCH_PARENT,
									LinearLayout.LayoutParams.WRAP_CONTENT
							);
							layoutParams.setMargins(30, 0, 30, 20); // лево, верх, право, низ
							newButton1.setLayoutParams(layoutParams);
							lDialogIndex.addView(newButton1, layoutParams);

							dialog.show();
						}
					} catch (Exception $e) {
					}

					return true;
				}
			});

			WebSettings settings = webView.getSettings();
			settings.setDefaultFontSize(text_size);
			webView.setBackgroundColor(day_night ? Color.WHITE : Color.BLACK);
			settings.setAllowFileAccess(true);
			settings.setJavaScriptEnabled(true);
			settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

			//-- Open file html assets - read and convert to string
			InputStream is = getAssets().open(isLanguage + "_" + String.format("%03d", isCaption) + ".xhtml");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			String content = new String(buffer);
			content = content.replace("<head>", "<head><style type=\"text/css\">body{color: " + (day_night ? "#000000" : "#ffffff") + "; background-color: " + (day_night ? "#ffffff" : "#000000") + ";}</style>");
			webView.loadDataWithBaseURL("file:///android_res/drawable/", content, "text/html", "utf-8", null);
		}
		catch (Exception $e){}
	}

	private void saveCaptionIndex() {
		SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
		Editor eddPub = myPrefs.edit();
		eddPub.putInt("isCaption", isCaption);
		eddPub.apply();
	}

	private void applyThemeToWebView() {
		String js = "document.body.style.backgroundColor = '" + (day_night ? "#ffffff" : "#000000") + "';" +
				"document.body.style.color = '" + (day_night ? "#000000" : "#ffffff") + "';";
		webView.evaluateJavascript(js, null);
	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		try
		{
			super.onSaveInstanceState(outState);
			// Save the state of the WebView
			webView.saveState(outState);
		}
		catch (Exception $e){}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		try
		{
			super.onRestoreInstanceState(savedInstanceState);

			// Restore the state of the WebView
			webView.restoreState(savedInstanceState);
		}
		catch (Exception $e){}
	}

	public void onClick(View v)
	{
		// по id определеяем кнопку, вызвавшую этот обработчик
		switch (v.getId())
		{
			case R.id.iibBackDesc:
				onBackPressed();
				break;

			case R.id.iibConfigDesc:
				Intent intent = new Intent();
				intent.setClass(Description.this, Config.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
				break;
		}
	}

	@Override
	protected void onResume()
	{
		LoadDateToDescription();
		super.onResume();
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		private static final int SWIPE_THRESHOLD = 100;
		private static final int SWIPE_VELOCITY_THRESHOLD = 100;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			float diffX = e2.getX() - e1.getX();
			float diffY = e2.getY() - e1.getY();

			if (Math.abs(diffX) > Math.abs(diffY)) {
				if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
					if (diffX > 0) {
						if (isCaption > 0) {
							isCaption--;
							saveCaptionIndex();
							LoadDateToDescription();
						}
					} else {
						if (isCaption < 35) {
							isCaption++;
							saveCaptionIndex();
							LoadDateToDescription();
						}
					}
					return true;
				}
			}
			return false;
		}
	}

	public void searchInWebView2(String text) {
		if (text == null || text.trim().isEmpty()) return;
		webView.findAllAsync(text);
		webView.setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
			if (numberOfMatches > 0) {
				// можно показать UI с результатами
			}
		});
	}


	private void searchInWebView3(String query) {
		if (query == null || query.isEmpty()) return;
		webView.findAllAsync(query);
		try {
			Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
			m.invoke(webView, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void searchInWebView(String query) {
		if (query == null || query.trim().isEmpty()) return;

		webView.findAllAsync(query);

		webView.setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
			if (isDoneCounting && numberOfMatches > 0) {
				webView.findNext(true); // переходим к первому совпадению
			}
		});

		try {
			Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
			m.invoke(webView, true); // только для отображения UI-индикатора (до Android 8.0)
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void searchNext() {
		webView.findNext(true);
	}

	public void searchPrevious() {
		webView.findNext(false);
	}
}
