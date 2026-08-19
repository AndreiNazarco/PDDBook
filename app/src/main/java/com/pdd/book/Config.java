package com.pdd.book;

import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class Config extends Activity implements OnClickListener {

	public static boolean 	isViewPub 			= true;
	public static String 	isLanguage 			= "";
	public static boolean	day_night 			= true;
	public static int 		text_size 			= 10;

	ImageButton 	ibBackConfig;
	TextView 		tvNameConfig;

	TextView    	tvDayNight;
	RadioButton 	rbDayDesc;
	RadioButton 	dbNightDesc;

	TextView 		tvLanguageConfig;
	ImageButton 	ibMainLanguageRomanian;
	ImageButton 	ibMainLanguageRussian;

	TextView		tvAboutConfig;
	TextView		tvAboutInfoConfig;

	LinearLayout	llPubConfig;

	Resources   	localResources;

	private View decorViewConfig;
	private WindowInsetsControllerCompat insetsControllerConfig;
	private View vStatusSpacer;
	private View headerConfig;
	private View rootConfig;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //скрываем заголовок

		setContentView(R.layout.config);

		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		getWindow().setNavigationBarColor(Color.WHITE);
		decorViewConfig = getWindow().getDecorView();
		insetsControllerConfig = new WindowInsetsControllerCompat(getWindow(), decorViewConfig);
		decorViewConfig.setSystemUiVisibility(decorViewConfig.getSystemUiVisibility()
				| View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

		vStatusSpacer = findViewById(R.id.vStatusSpacerConfig);
		headerConfig = findViewById(R.id.illHeaderConfig);
		final ViewGroup scrollViewForInsets = findViewById(R.id.idScrollOption);
		scrollViewForInsets.setClipToPadding(false);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
			applyStatusBarTheme();
			decorViewConfig.setSystemUiVisibility(decorViewConfig.getSystemUiVisibility()
					| View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			ViewGroup.LayoutParams lp = vStatusSpacer.getLayoutParams();
			lp.height = systemBars.top;
			vStatusSpacer.setLayoutParams(lp);
			scrollViewForInsets.setPadding(scrollViewForInsets.getPaddingLeft(), scrollViewForInsets.getPaddingTop(), scrollViewForInsets.getPaddingRight(), systemBars.bottom);
			return insets;
		});

		// Найдем View-элементы
		rootConfig				=   findViewById(R.id.illRootConfig);
		ibBackConfig			=   (ImageButton) 	findViewById(R.id.iibBackConfig);
		tvNameConfig 			=   (TextView) 		findViewById(R.id.itvNameConfig);

		tvDayNight				=   (TextView) 		findViewById(R.id.itvDayNight);
		rbDayDesc				=   (RadioButton) 	findViewById(R.id.irbDayDesc);
		dbNightDesc				=   (RadioButton) 	findViewById(R.id.idbNightDesc);

		tvLanguageConfig		=   (TextView) 		findViewById(R.id.itvLanguageConfig);
		ibMainLanguageRomanian	=   (ImageButton) 	findViewById(R.id.iibMainLanguageRomanian);
		ibMainLanguageRussian	=   (ImageButton) 	findViewById(R.id.iibMainLanguageRussian);

		tvAboutConfig			=   (TextView) 		findViewById(R.id.itvAboutConfig);
		tvAboutInfoConfig		=   (TextView) 		findViewById(R.id.itvAboutInfoConfig);

		// Присваиваем обработчик кнопкам
		ibBackConfig.setOnClickListener(this);
		rbDayDesc.setOnClickListener(this);
		dbNightDesc.setOnClickListener(this);
		ibMainLanguageRomanian.setOnClickListener(this);
		ibMainLanguageRussian.setOnClickListener(this);

		// Загрузка данных
		LoadDate();
	}

	private void LoadDate()
	{
		SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
		isLanguage 				  = myPrefs.getString("isLanguage", "ro");
		text_size 		          = Integer.valueOf(myPrefs.getString("text_size", "10"));
		day_night 				  = myPrefs.getBoolean("day_night", true); // true - day, false - night

		applyStatusBarTheme();

		// Язык пользователя
		Resources baseResources = getResources();
		Locale locale = new Locale(isLanguage);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		localResources = new Resources(baseResources.getAssets(), baseResources.getDisplayMetrics(), config);

		// Наименование программы
		tvNameConfig.setText(localResources.getString(R.string.congif));

		tvDayNight.setText(localResources.getString(R.string.day_night));
		rbDayDesc.setText(localResources.getString(R.string.day));
		dbNightDesc.setText(localResources.getString(R.string.night));

		tvLanguageConfig.setText(localResources.getString(R.string.recipes_lang));

		tvAboutConfig.setText(localResources.getString(R.string.about));
		tvAboutInfoConfig.setText(localResources.getString(R.string.about_info));

		if(day_night == false)
		{
			rbDayDesc.setChecked(false);
			dbNightDesc.setChecked(true);
		}
		else
		{
			rbDayDesc.setChecked(true);
			dbNightDesc.setChecked(false);
		}

		applyBodyTheme();
	}

	private void applyBodyTheme() {
		int bgMain = ContextCompat.getColor(this, day_night ? R.color.cDayBG : R.color.cNightBG);
		int textMain = ContextCompat.getColor(this, day_night ? R.color.cDayText : R.color.cNightText);
		int sectionBg = ContextCompat.getColor(this, day_night ? R.color.cDayCaptionTextBG : R.color.cNightCaptionTextBG);
		int sectionText = ContextCompat.getColor(this, day_night ? R.color.cDayCaptionText : R.color.cNightCaptionText);

		if (rootConfig != null) {
			rootConfig.setBackgroundColor(bgMain);
		}
		if (tvDayNight != null) {
			tvDayNight.setBackgroundColor(sectionBg);
			tvDayNight.setTextColor(sectionText);
		}
		if (rbDayDesc != null) {
			rbDayDesc.setTextColor(textMain);
		}
		if (dbNightDesc != null) {
			dbNightDesc.setTextColor(textMain);
		}
		if (tvLanguageConfig != null) {
			tvLanguageConfig.setBackgroundColor(sectionBg);
			tvLanguageConfig.setTextColor(sectionText);
		}
		if (tvAboutConfig != null) {
			tvAboutConfig.setBackgroundColor(sectionBg);
			tvAboutConfig.setTextColor(sectionText);
		}
		if (tvAboutInfoConfig != null) {
			tvAboutInfoConfig.setTextColor(textMain);
		}
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
				Editor edd = myPrefs.edit();
				edd.putBoolean("day_night", true);
				edd.commit();

				day_night = true;
				applyStatusBarTheme();
				applyBodyTheme();
				break;

			case R.id.idbNightDesc:
				Editor edn = myPrefs.edit();
				edn.putBoolean("day_night", false);
				edn.commit();

				day_night = false;
				applyStatusBarTheme();
				applyBodyTheme();
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

	private void applyStatusBarTheme() {
		int statusBarColor = ContextCompat.getColor(this, day_night ? R.color.colorPrimaryDark : R.color.cNightPrimaryDark);
		int headerColor = ContextCompat.getColor(this, day_night ? R.color.colorPrimary : R.color.cNightPrimary);
		getWindow().setStatusBarColor(statusBarColor);
		if (headerConfig != null) {
			headerConfig.setBackgroundColor(headerColor);
		}
		if (vStatusSpacer != null) {
			vStatusSpacer.setBackgroundColor(statusBarColor);
		}
		if (tvNameConfig != null) {
			tvNameConfig.setBackgroundColor(headerColor);
		}
		if (ibBackConfig != null) {
			ibBackConfig.setBackgroundColor(headerColor);
		}
		if (insetsControllerConfig != null) {
			insetsControllerConfig.setAppearanceLightStatusBars(day_night);
		}
		if (decorViewConfig != null) {
			int flags = decorViewConfig.getSystemUiVisibility();
			if (day_night) {
				flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
			} else {
				flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
			}
			decorViewConfig.setSystemUiVisibility(flags);
		}
	}
}
