package com.pdd.book;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;

public class MainActivity extends Activity implements OnClickListener {

	public static boolean 	isViewPub 			= true;
	public static boolean 	isSelectLanguage 	= true;
	public static String 	isLanguage 			= "";

	ImageButton		ibLanguageRomaniaMain;
	ImageButton		ibLanguageRussianMain;

	DBHelper 	 	dbHelper;

	Resources    	localResources;

	/** Called when the activity is first created. */
	@SuppressLint("Range")
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //скрываем заголовок
		setContentView(R.layout.main);

		// Найдем View-элементы
		ibLanguageRomaniaMain			=   (ImageButton) 	findViewById(R.id.iibLanguageRomaniaMain);
		ibLanguageRussianMain			=   (ImageButton) 	findViewById(R.id.iibLanguageRussianMain);

		// Присваиваем обработчик кнопкам
		ibLanguageRomaniaMain.setOnClickListener(this);
		ibLanguageRussianMain.setOnClickListener(this);

		SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
		isLanguage 				  = myPrefs.getString("isLanguage", "");

		if(isLanguage.trim().length() == 0)
		{
			isSelectLanguage = true;
			isLanguage = "ro"; //default
			Configuration sysConfig = getResources().getConfiguration();
			Locale curLocale = sysConfig.locale;

			if(curLocale.toString().substring(0,2).toLowerCase().compareTo("ru") == 0)
			{
				isLanguage = "ru";
			}

			if(curLocale.toString().substring(0,2).toLowerCase().compareTo("ro") == 0)
			{
				isLanguage = "ro";
			}
		}
		else
		{
			isSelectLanguage = false;
		}

		Resources baseResources = getResources();
		Locale locale = new Locale(isLanguage);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		localResources = new Resources(baseResources.getAssets(), baseResources.getDisplayMetrics(), config);

		// создаем объект для создания и управления версиями БД
		try
		{
			dbHelper = new DBHelper(this);

			String strValue = "";
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			String selectQuery = " SELECT name " +
					" FROM indicatoare " +
					" WHERE language like '" + localResources.getString(R.string.language).trim() + "' " +
					"   AND code like 'indicatoare_07_05_37_02' ";

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
		}
		catch(Exception $e)
		{
			try
			{
				dbHelper.copyDataBase();

				ibLanguageRomaniaMain.setVisibility(View.GONE);
				ibLanguageRussianMain.setVisibility(View.GONE);

				createAndShowDialog(getString(R.string.sql_lite_exception), getString(R.string.recommendation));
				return;
			}
			catch(Exception $e1)
			{
				createAndShowDialog("Restart the application", "Updating.");
				return;
			}
		}

		Editor edd = myPrefs.edit();
		edd.putBoolean("isViewPub", true);
		edd.commit();

		if(isSelectLanguage == false)
		{
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, Content.class);
			startActivity(intent);
			finish();
			overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
		}
	}

	private void createAndShowDialog(String message, String title)
	{
		try
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setMessage(message);
			builder.setTitle(title);
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					finish();
				}
			});
			builder.create().show();
		}
		catch(Exception $e1)
		{
			finish();
		}
	}

	public void onClick(View v)
	{
		Intent intent;
		SharedPreferences myPrefs;
		Editor edd;

		switch (v.getId())
		{
			case R.id.iibLanguageRomaniaMain:
				myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
				edd = myPrefs.edit();
				edd.putString("isLanguage", "ro");
				edd.putBoolean("isViewPub", true);
				edd.commit();

				intent = new Intent();
				intent.setClass(MainActivity.this, Content.class);
				startActivity(intent);
				finish();
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
				break;

			case R.id.iibLanguageRussianMain:
				myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
				edd = myPrefs.edit();
				edd.putString("isLanguage", "ru");
				edd.putBoolean("isViewPub", true);
				edd.commit();

				intent = new Intent();
				intent.setClass(MainActivity.this, Content.class);
				startActivity(intent);
				finish();
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
				break;
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}
}