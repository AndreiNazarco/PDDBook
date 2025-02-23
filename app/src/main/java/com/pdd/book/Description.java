package com.pdd.book;

import java.io.InputStream;
import java.util.Locale;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
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

	ImageButton  	ibBackDesc;
	ImageButton  	ibConfigDesc;

	TextView     	tvCaptionDesc;
	WebView 	 	webView;

	LinearLayout	llPubDesc;
	AdView 			AdViewDesc;

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

		webView 	      	= (WebView) 		findViewById(R.id.iwvHTML);

		llPubDesc	 		= (LinearLayout) 	findViewById(R.id.illPubDesc);

		// Присваиваем обработчик кнопкам
		ibBackDesc.setOnClickListener(this);
		ibConfigDesc.setOnClickListener(this);

		// Показати или скрыть рекламу
		VisibleOrGonePub();

		// Загрузка данных
		LoadDateToDescription();
	}

	public void VisibleOrGonePub()
	{
		try
		{
			SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
			isViewPub = myPrefs.getBoolean("isViewPub", true); // true - показать рекламу, false -  скрыти рекламу

			if(isViewPub == true)
			{
				llPubDesc.setVisibility(View.GONE);

				// Поиск AdView в качестве ресурса и загрузка запроса.
				AdViewDesc = (AdView)this.findViewById(R.id.iAdViewDesc);
				AdRequest adRequest = new AdRequest.Builder().build();
				AdViewDesc.loadAd(adRequest);

				AdViewDesc.setAdListener(new AdListener()
				{
					// Вызывается при получении объявление.
					public void onAdLoaded()
					{
						llPubDesc.setVisibility(View.VISIBLE);
					}

					// Вызывается, когда пользователь собирается вернуться в приложение после нажатия на объявление
					public void onAdClosed()
					{
						llPubDesc.setVisibility(View.GONE);

						if(AdViewDesc != null) {AdViewDesc.destroy();}

						isViewPub = false;
						SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
						Editor eddPub = myPrefs.edit();
						eddPub.putBoolean("isViewPub", isViewPub);
						eddPub.commit();
					}

					// Вызывается, когда объявление оставляет приложение (например, пойти в браузере).
					public void onAdLeftApplication()
					{}
				});
			}
			else
			{
				llPubDesc.setVisibility(View.GONE);
			}
		}
		catch (Exception $e) { }
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

		try
		{
			try
			{
				webView.setWebViewClient(new WebViewClient()
				{
					private Bitmap bitmap;

					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url)
					{
						//Log.d("Resolution", "shouldOverrideUrlLoading: " + url);
						try
						{
							int exist = 0;
							String img = "", str = "", strValue = "";
							LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

							if((url.trim().compareTo("file:///android_res/drawable/previous") == 0)||(url.trim().compareTo("file:///android_res/drawable/next") == 0))
							{
								if(url.trim().compareTo("file:///android_res/drawable/previous") == 0)
								{
									isCaption = isCaption - 1;

									SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
									Editor eddPub = myPrefs.edit();
									eddPub.putInt("isCaption", isCaption);
									eddPub.commit();

									LoadDateToDescription();
								}

								if(url.trim().compareTo("file:///android_res/drawable/next") == 0)
								{
									isCaption = isCaption + 1;

									SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
									Editor eddPub = myPrefs.edit();
									eddPub.putInt("isCaption", isCaption);
									eddPub.commit();

									LoadDateToDescription();
								}
							}
							else
							{
								final Dialog dialog = new Dialog(Description.this);
								dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
								dialog.setContentView(R.layout.dialog);

								LinearLayout lDialog = (LinearLayout) dialog.findViewById(R.id.llDialog);

								LinearLayout lDialogIndex = (LinearLayout) dialog.findViewById(R.id.llDialogIndex);

								if(day_night == false)
								{
									lDialogIndex.setBackgroundColor(Color.parseColor("#000000"));
								}
								else
								{
									lDialogIndex.setBackgroundColor(Color.parseColor("#ffffff"));
								}

								for(int I = 0; I <= url.length(); I ++)
								{
									if((url.length() - I >= 6)&&(url.substring(I, I + 6).toLowerCase().compareTo("image_") 	== 0))
									{
										//-- Картинка если ести
										for(int J = I; J <= url.length(); J ++)
										{
											if((url.length() - J >= 1)&&(url.substring(J, J + 1).toLowerCase().compareTo(";") 	== 0))
											{
												img = url.substring(I, J);
												//Log.d("Resolution", "img:" + img + ":");
												exist ++;
												break;
											}
										}

										try
										{
											BitmapFactory.Options opts = new Options();
											opts.inPurgeable = true;
											bitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(img.trim(), "drawable", getPackageName()), opts);

											/*int idImage = getResources().getIdentifier(img.trim(), "drawable", getPackageName());
			          						  bitmap = BitmapFactory.decodeResource(getResources(), idImage);*/
										}
										catch (Exception $e)
										{
											bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
										}

										ImageView newImage = new ImageView(Description.this);
										newImage.setImageBitmap(this.bitmap);
										newImage.setScaleType(ScaleType.FIT_CENTER);
										newImage.setPadding(0, 10, 0, 0);
										lDialog.addView(newImage, lParams);
									}

									if((url.length() - I >= 12)&&(url.substring(I, I + 12).toLowerCase().compareTo("indicatoare_") 	== 0))
									{
										//-- Картинка если ести
										for(int J = I; J <= url.length(); J ++)
										{
											if(((url.length() - J >= 1)&&(url.substring(J, J + 1).toLowerCase().compareTo(";") 	== 0)) || (J == url.length()))
											{
												str = url.substring(I, J);
												//Log.d("Resolution", "str: " + str);
												exist ++;
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
										if (cIndicatoare.moveToFirst())
										{
											do
											{
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

										if(day_night == false)
										{
											newTextRasp.setTextColor(Color.parseColor("#ffffff"));
										}
										else
										{
											newTextRasp.setTextColor(Color.parseColor("#000000"));
										}
										lDialog.addView(newTextRasp, lParams);
									}
								}


								if(exist == 0)
								{
									TextView newTextRasp = new TextView(Description.this);
									newTextRasp.setText(localResources.getString(R.string.image_not_found));

									if(day_night == false)
									{
										newTextRasp.setTextColor(Color.parseColor("#ffffff"));
									}
									else
									{
										newTextRasp.setTextColor(Color.parseColor("#000000"));
									}

									lDialog.addView(newTextRasp, lParams);
								}

								TextView newTextNull = new TextView(Description.this);
								newTextNull.setText("   ");
								lDialog.addView(newTextNull, lParams);

								Button newButton1 = new Button(Description.this);
								newButton1.setText(localResources.getString(R.string.close));

								if(day_night == false)
								{
									newButton1.setTextColor(Color.parseColor("#ffffff"));
								}
								else
								{
									newButton1.setTextColor(Color.parseColor("#000000"));
								}

								newButton1.setOnClickListener(new OnClickListener()
								{
									public void onClick(View v)
									{
										dialog.dismiss();
									}
								});
								lDialogIndex.addView(newButton1, lParams);

								dialog.show();
							}
						}
						catch(Exception $e) { }

						return true;
					}
				});
			}
			catch(Exception $e) { }

			WebSettings settings = webView.getSettings();
			settings.setDefaultFontSize(text_size);

			if(day_night == false)
			{
				webView.setBackgroundColor(Color.parseColor("#000000"));
			}
			else
			{
				webView.setBackgroundColor(Color.parseColor("#ffffff"));
			}

			webView.getSettings().setAllowFileAccess(true);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

			//-- Open file html assets - read and convert to string
			InputStream is = getAssets().open(isLanguage + "_" + String.format("%03d", Integer.valueOf(isCaption)) + ".xhtml");
			int size = is.available();

			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();

			if(day_night == false)
				webView.loadDataWithBaseURL("file:///android_res/drawable/", new String(buffer).replace("<head>", "<head><style type=\"text/css\">body{color: #ffffff; background-color: #000000;}</style>"), "text/html", "utf-8", null);
			else
				webView.loadDataWithBaseURL("file:///android_res/drawable/", new String(buffer).replace("<head>", "<head><style type=\"text/css\">body{color: #000000; background-color: #ffffff;}</style>"), "text/html", "utf-8", null);
			//dedcc3
		}
		catch (Exception $e){}
	}

	@Override
	public void onBackPressed()
	{
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
				finish();
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
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
}
