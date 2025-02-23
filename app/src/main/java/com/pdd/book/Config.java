package com.pdd.book;

import java.util.Locale;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class Config extends Activity implements OnClickListener, SeekBar.OnSeekBarChangeListener {

	public static boolean 	isViewPub 			= true;
	public static String 	isLanguage 			= "";
	public static boolean	day_night 			= true;
	public static int 		text_size 			= 10;

	ImageButton 	ibBackConfig;
	TextView 		tvNameConfig;

	LinearLayout 	llExampleText;
	TextView 		tvExampleText;

	TextView    	tvFontSize;
	TextView    	tvTextLess;
	TextView    	tvTextMore;
	SeekBar     	sbTextSize;

	TextView    	tvDayNight;
	RadioButton 	rbDayDesc;
	RadioButton 	dbNightDesc;

	TextView 		tvLanguageConfig;
	ImageButton 	ibMainLanguageRomanian;
	ImageButton 	ibMainLanguageRussian;

	TextView		tvAboutConfig;
	TextView		tvAboutInfoConfig;

	LinearLayout	llPubConfig;
	AdView 			AdViewConfig;

	Resources   	localResources;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //скрываем заголовок
		setContentView(R.layout.config);

		// Найдем View-элементы
		ibBackConfig			=   (ImageButton) 	findViewById(R.id.iibBackConfig);
		tvNameConfig 			=   (TextView) 		findViewById(R.id.itvNameConfig);

		llExampleText			=   (LinearLayout) 	findViewById(R.id.illExampleText);
		tvExampleText			=   (TextView) 		findViewById(R.id.itvExampleText);

		tvFontSize				=   (TextView) 		findViewById(R.id.itvFontSize);
		tvTextLess				=   (TextView) 		findViewById(R.id.itvTextLess);
		tvTextMore				=   (TextView) 		findViewById(R.id.itvTextMore);
		sbTextSize				=   (SeekBar) 		findViewById(R.id.isbTextSize);

		tvDayNight				=   (TextView) 		findViewById(R.id.itvDayNight);
		rbDayDesc				=   (RadioButton) 	findViewById(R.id.irbDayDesc);
		dbNightDesc				=   (RadioButton) 	findViewById(R.id.idbNightDesc);

		tvLanguageConfig		=   (TextView) 		findViewById(R.id.itvLanguageConfig);
		ibMainLanguageRomanian	=   (ImageButton) 	findViewById(R.id.iibMainLanguageRomanian);
		ibMainLanguageRussian	=   (ImageButton) 	findViewById(R.id.iibMainLanguageRussian);

		tvAboutConfig			=   (TextView) 		findViewById(R.id.itvAboutConfig);
		tvAboutInfoConfig		=   (TextView) 		findViewById(R.id.itvAboutInfoConfig);

		llPubConfig				=   (LinearLayout) 	findViewById(R.id.illPubConfig);

		// Присваиваем обработчик кнопкам
		ibBackConfig.setOnClickListener(this);
		sbTextSize.setOnSeekBarChangeListener(this);
		rbDayDesc.setOnClickListener(this);
		dbNightDesc.setOnClickListener(this);
		ibMainLanguageRomanian.setOnClickListener(this);
		ibMainLanguageRussian.setOnClickListener(this);



		//-- Показати или скрыть рекламу
		VisibleOrGonePub();

		// Загрузка данных
		LoadDate();
	}

	public void VisibleOrGonePub()
	{
		try
		{
			SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
			isViewPub  			= myPrefs.getBoolean("isViewPub", true); // true - показать рекламу, false -  скрыти рекламу

			if(isViewPub == true)
			{
				llPubConfig.setVisibility(View.GONE);

				//Поиск AdView в качестве ресурса и загрузка запроса.
				AdViewConfig = (AdView)this.findViewById(R.id.iAdViewConfig);
				AdRequest adRequest = new AdRequest.Builder().build();
				AdViewConfig.loadAd(adRequest);

				AdViewConfig.setAdListener(new AdListener()
				{
					//Вызывается при получении объявление.
					public void onAdLoaded()
					{
						llPubConfig.setVisibility(View.VISIBLE);
					}

					// Вызывается, когда пользователь собирается вернуться в приложение после нажатия на объявление
					public void onAdClosed()
					{
						llPubConfig.setVisibility(View.GONE);

						if(AdViewConfig != null) {AdViewConfig.destroy();}

						isViewPub = false;
						SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
						Editor eddPub = myPrefs.edit();
						eddPub.putBoolean("isViewPub", isViewPub);
						eddPub.commit();
					}

					//Вызывается, когда объявление оставляет приложение (например, пойти в браузере).
					public void onAdLeftApplication()
					{}
				});
			}
			else
			{
				llPubConfig.setVisibility(View.GONE);
			}
		}
		catch (Exception $e) { }
	}

	private void LoadDate()
	{
		SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
		isLanguage 				  = myPrefs.getString("isLanguage", "ro");
		text_size 		          = Integer.valueOf(myPrefs.getString("text_size", "10"));
		day_night 				  = myPrefs.getBoolean("day_night", true); // true - day, false - night

		// Язык пользователя
		Resources baseResources = getResources();
		Locale locale = new Locale(isLanguage);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		localResources = new Resources(baseResources.getAssets(), baseResources.getDisplayMetrics(), config);

		// Наименование программы
		tvNameConfig.setText(localResources.getString(R.string.congif));

		tvExampleText.setText(localResources.getString(R.string.example_text));

		tvFontSize.setText(localResources.getString(R.string.font_size));
		tvTextLess.setText(localResources.getString(R.string.less));
		tvTextMore.setText(localResources.getString(R.string.more));

		tvDayNight.setText(localResources.getString(R.string.day_night));
		rbDayDesc.setText(localResources.getString(R.string.day));
		dbNightDesc.setText(localResources.getString(R.string.night));

		tvLanguageConfig.setText(localResources.getString(R.string.recipes_lang));

		tvAboutConfig.setText(localResources.getString(R.string.about));
		tvAboutInfoConfig.setText(localResources.getString(R.string.about_info));

		tvExampleText.setTextSize(text_size);
		sbTextSize.setProgress(text_size - 10);

		if(day_night == false)
		{
			rbDayDesc.setChecked(false);
			dbNightDesc.setChecked(true);
			tvExampleText.setTextColor(Color.parseColor("#ffffff"));

			llExampleText.setBackgroundColor(Color.parseColor("#000000"));
			tvExampleText.setBackgroundColor(Color.parseColor("#000000"));
		}
		else
		{
			rbDayDesc.setChecked(true);
			dbNightDesc.setChecked(false);
			tvExampleText.setTextColor(Color.parseColor("#000000"));

			llExampleText.setBackgroundColor(Color.parseColor("#ffffff"));
			tvExampleText.setBackgroundColor(Color.parseColor("#ffffff"));
		}

	}


	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		// TODO Auto-generated method stub
	}

	public void onStartTrackingTouch(SeekBar seekBar)
	{
		// TODO Auto-generated method stub
	}

	public void onStopTrackingTouch(SeekBar seekBar)
	{
		tvExampleText.setTextSize(10 + sbTextSize.getProgress());

		SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
		Editor edd = myPrefs.edit();
		edd.putString("text_size", String.valueOf(10 + sbTextSize.getProgress()));
		edd.commit();
	}

	public void onClick(View v)
	{
		SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);

		// по id определеяем кнопку, вызвавшую этот обработчик
		switch (v.getId())
		{
			case R.id.iibBackConfig:
				finish();
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
				break;

			case R.id.irbDayDesc:
				llExampleText.setBackgroundColor(Color.parseColor("#ffffff"));
				tvExampleText.setBackgroundColor(Color.parseColor("#ffffff"));
				tvExampleText.setTextColor(Color.parseColor("#000000"));

				Editor edd = myPrefs.edit();
				edd.putBoolean("day_night", true);
				edd.commit();
				break;

			case R.id.idbNightDesc:
				llExampleText.setBackgroundColor(Color.parseColor("#000000"));
				tvExampleText.setBackgroundColor(Color.parseColor("#000000"));
				tvExampleText.setTextColor(Color.parseColor("#ffffff"));

				Editor edn = myPrefs.edit();
				edn.putBoolean("day_night", false);
				edn.commit();
				break;

			case R.id.iibMainLanguageRomanian:
				myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
				edd = myPrefs.edit();
				edd.putString("isLanguage", "ro");
				edd.commit();

				LoadDate();
				break;

			case R.id.iibMainLanguageRussian:
				myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
				edd = myPrefs.edit();
				edd.putString("isLanguage", "ru");
				edd.commit();

				LoadDate();
				break;
		}
	}

	@Override
	public void onBackPressed()
	{
		finish();
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
	}
}
