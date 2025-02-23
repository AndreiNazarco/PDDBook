package com.pdd.book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Cuprins  extends Activity implements OnClickListener {

	public static boolean 	isViewPub 			= true;
	public static String 	isLanguage 			= "";
	public static boolean	day_night 			= true;

	final String ATTRIBUTE_NAME_TEXT1 = "text1";
	final String ATTRIBUTE_NAME_TEXT2 = "text2";

	LinearLayout    llCuprins;

	TextView    	tvAppNameCuprins;
	ImageButton	    ibConfigCuprins;

	ListView    	lvSimpleCuprins;

	LinearLayout	llPubCuprins;
	AdView 			AdViewCuprins;

	DBHelper 	 	dbHelper;

	Resources   	localResources;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //скрываем заголовок
		setContentView(R.layout.cuprins);

		// Создаем объект для создания и управления версиями БД
		dbHelper = new DBHelper(this);

		// Найдем View-элементы
		llCuprins  			=   (LinearLayout) 	findViewById(R.id.illCuprins);

		tvAppNameCuprins    =   (TextView) 		findViewById(R.id.itvAppNameCuprins);
		ibConfigCuprins		=   (ImageButton) 	findViewById(R.id.iibConfigCuprins);

		lvSimpleCuprins		=   (ListView) 		findViewById(R.id.ilvSimpleCuprins);

		llPubCuprins		=   (LinearLayout) 	findViewById(R.id.illPubCuprins);

		// Присваиваем обработчик кнопкам
		ibConfigCuprins.setOnClickListener(this);

		// Показати или скрыть рекламу
		VisibleOrGonePub();

		// Загрузка данных
		LoadDateListView();
	}

	public void VisibleOrGonePub()
	{
		try
		{
			SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
			isViewPub = myPrefs.getBoolean("isViewPub", true); // true - показать рекламу, false -  скрыти рекламу

			if(isViewPub == true)
			{
				llPubCuprins.setVisibility(View.GONE);

				// Поиск AdView в качестве ресурса и загрузка запроса. "7AD660ADB7689E4351B10F6E9E2BE622"
				AdViewCuprins = (AdView)this.findViewById(R.id.iAdViewCuprins);
				AdRequest adRequest = new AdRequest.Builder().build();
				AdViewCuprins.loadAd(adRequest);

				AdViewCuprins.setAdListener(new AdListener()
				{
					// Вызывается при получении объявление.
					public void onAdLoaded()
					{
						llPubCuprins.setVisibility(View.VISIBLE);
					}

					// Вызывается, когда пользователь собирается вернуться в приложение после нажатия на объявление
					public void onAdClosed()
					{
						llPubCuprins.setVisibility(View.GONE);

						if(AdViewCuprins != null) {AdViewCuprins.destroy();}

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
				llPubCuprins.setVisibility(View.GONE);
			}
		}
		catch (Exception $e) { }
	}

	private void LoadDateListView()
	{
		SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
		isLanguage = myPrefs.getString("isLanguage", "ro");
		day_night = myPrefs.getBoolean("day_night", true); // true - day, false - night

		// Язык пользователя
		Resources baseResources = getResources();
		Locale locale = new Locale(isLanguage);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		localResources = new Resources(baseResources.getAssets(), baseResources.getDisplayMetrics(), config);

		// Наименование программы
		tvAppNameCuprins.setText(localResources.getString(R.string.app_name_full));

		// День / Ночь
		if(day_night == false)
		{
			// Ночь
			llCuprins.setBackgroundColor(Color.parseColor("#000000"));
		}
		else
		{
			// День
			llCuprins.setBackgroundColor(Color.parseColor("#ffffff"));
		}

		// упаковываем данные в понятную для адаптера структуру
		ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
		Map<String, String> m;

		// Подключаемся к БД
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String selectQuery = " SELECT name_full " +
				" FROM cuprins " +
				" WHERE language like '" + localResources.getString(R.string.language).trim() + "' " +
				" ORDER BY cuprins_order";

		// Данные из базы --
		Cursor cCuprins = db.rawQuery(selectQuery, null);

		// Ставим позицию курсора на первую строку выборки
		// если в выборке нет строк, вернется false
		if (cCuprins.moveToFirst())
		{
			do
			{
				m = new HashMap<String, String>();
				m.put(ATTRIBUTE_NAME_TEXT1, cCuprins.getString(cCuprins.getColumnIndex("name_full")).replace("\\n", "\n"));
				data.add(m);

				// Переход на следующую строку
				// а если следующей нет (текущая - последняя), то false - выходим из цикла
			} while (cCuprins.moveToNext());
		}
		cCuprins.close();
		db.close();

		// Массив имен атрибутов, из которых будут читаться данные
		String[] from = { ATTRIBUTE_NAME_TEXT1, ATTRIBUTE_NAME_TEXT2 };
		// Массив ID View-компонентов, в которые будут вставлять данные
		int[] to = { R.id.tvText1, R.id.tvText2 };

		Parcelable state = null;
		try
		{
			state = lvSimpleCuprins.onSaveInstanceState();
		}
		catch (Exception $e){}

		// создаем адаптер
		SimpleAdapter sAdapter;
		if(day_night == false)
		{
			sAdapter = new SimpleAdapter(this, data, R.layout.item_cuprins, from, to);
		}
		else
		{
			sAdapter = new SimpleAdapter(this, data, R.layout.item_cuprins_white, from, to);
		}

		// определяем список и присваиваем ему адаптер
		lvSimpleCuprins = (ListView) findViewById(R.id.ilvSimpleCuprins);
		lvSimpleCuprins.setAdapter(sAdapter);

		try
		{
			lvSimpleCuprins.onRestoreInstanceState(state);
		}
		catch (Exception $e){}

		// Обработка нажатия
		lvSimpleCuprins.setOnItemClickListener(new  OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id)
			{
				SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
				Editor eddPub = myPrefs.edit();
				eddPub.putInt("isCaption", position);
				eddPub.commit();

				Intent intent = new Intent();
				intent.setClass(Cuprins.this, Description.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});

	}

	public void onClick(View v)
	{
		// по id определеяем кнопку, вызвавшую этот обработчик
		switch (v.getId())
		{
			case R.id.iibConfigCuprins:
				Intent intent = new Intent();
				intent.setClass(Cuprins.this, Config.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
				break;
		}
	}


	@Override
	public void onBackPressed()
	{
		finish();
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
	}


	@Override
	protected void onResume()
	{
		try
		{
			LoadDateListView();
		}
		catch (Exception $e){}
		super.onResume();
	}
}
