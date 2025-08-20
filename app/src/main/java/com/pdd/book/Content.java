package com.pdd.book;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.pdd.book.adapter.ContentInfo;
import com.pdd.book.adapter.ContentRecyclerAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Content extends Activity implements OnClickListener {

	public static boolean 	isViewPub 			= true;
	public static String 	isLanguage 			= "";
	public static boolean	day_night 			= true;

	final String ATTRIBUTE_NAME_TEXT1 = "text1";
	final String ATTRIBUTE_NAME_TEXT2 = "text2";

	private RecyclerView mRecyclerView;
	private RecyclerView.LayoutManager mLayoutManager;
	private ContentRecyclerAdapter mAdapter;

	List<ContentInfo> contentInfoList = new ArrayList<>();

	LinearLayout    llContent;

	TextView    	tvAppNameContent;
	ImageButton	    ibConfigContent;

	LinearLayout	llPubContent;

	DBHelper 	 	dbHelper;

	Resources   	localResources;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //скрываем заголовок
		setContentView(R.layout.content);

		// Создаем объект для создания и управления версиями БД
		dbHelper = new DBHelper(this);

		// Найдем View-элементы
		llContent  			=   (LinearLayout) 	findViewById(R.id.illContent);

		tvAppNameContent    =   (TextView) 		findViewById(R.id.itvAppNameContent);
		ibConfigContent		=   (ImageButton) 	findViewById(R.id.iibConfigContent);

		mRecyclerView 		=   (RecyclerView)  findViewById(R.id.irvContent);


		// Присваиваем обработчик кнопкам
		ibConfigContent.setOnClickListener(this);

		mRecyclerView.setHasFixedSize(true);
		mLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(mLayoutManager);

		mAdapter = new ContentRecyclerAdapter(contentInfoList);
		mRecyclerView.setAdapter(mAdapter);

		// Загрузка данных
		LoadDateListView();

		mRecyclerView.addOnItemTouchListener(new Content.RecyclerTouchListener(this, mRecyclerView, new Content.ClickListener()
		{
			public void onClick(View view, final int position) {
				//Values are passing to activity & to fragment as well
				try
				{
					SharedPreferences myPrefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
					Editor eddPub = myPrefs.edit();
					eddPub.putInt("isCaption", position);
					eddPub.commit();

					Intent intent = new Intent();
					intent.setClass(Content.this, Description.class);
					startActivity(intent);
					overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
				}
				catch (Exception $e) {
				}
			}

			public void onLongClick(View view, int position) {
				//Toast.makeText(MainActivity.this, "Long press on position :"+position,  Toast.LENGTH_LONG).show();
			}
		}));
	}

	@SuppressLint("Range")
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
		tvAppNameContent.setText(localResources.getString(R.string.app_name_full));

		// День / Ночь
		if(day_night == false)
		{
			// Ночь
			llContent.setBackgroundColor(Color.parseColor("#000000"));
		}
		else
		{
			// День
			llContent.setBackgroundColor(Color.parseColor("#ffffff"));
		}

		// Подключаемся к БД
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		ContentInfo ciItem;
		contentInfoList.clear();

		String selectQuery = " SELECT name_full " +
				" FROM cuprins " +
				" WHERE language like '" + localResources.getString(R.string.language).trim() + "' " +
				" ORDER BY cuprins_order";

		// Данные из базы --
		Cursor cCuprins = db.rawQuery(selectQuery, null);

		// Ставим позицию курсора на первую строку выборки
		// если в выборке нет строк, вернется false
		int iCount = 0;
		if (cCuprins.moveToFirst())
		{
			do
			{
				ciItem = new ContentInfo(cCuprins.getString(cCuprins.getColumnIndex("name_full")).replace("\\n", "\n"),
										 "",
										 ""
										);
				contentInfoList.add(ciItem);
				iCount = iCount + 1;
				// Переход на следующую строку
				// а если следующей нет (текущая - последняя), то false - выходим из цикла
			} while (cCuprins.moveToNext());
		}
		cCuprins.close();
		db.close();

		if(iCount == 0)
		{
			ciItem = new ContentInfo(localResources.getString(R.string.subject_empty),
									 "",
									 ""
									);
			contentInfoList.add(ciItem);
		}

		mAdapter.notifyDataSetChanged();
	}

	public void onClick(View v)
	{
		// по id определеяем кнопку, вызвавшую этот обработчик
		switch (v.getId())
		{
			case R.id.iibConfigContent:
				Intent intent = new Intent();
				intent.setClass(Content.this, Config.class);
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


	public static interface ClickListener{
		public void onClick(View view, int position);
		public void onLongClick(View view, int position);
	}

	class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

		private Content.ClickListener clicklistener;
		private GestureDetector gestureDetector;

		public RecyclerTouchListener(Context context, final RecyclerView recycleView, final Content.ClickListener clicklistener){

			this.clicklistener=clicklistener;
			gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					return true;
				}

				@Override
				public void onLongPress(MotionEvent e) {
					View child=recycleView.findChildViewUnder(e.getX(),e.getY());
					if(child!=null && clicklistener!=null){
						clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
					}
				}
			});
		}

		@Override
		public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
			View child=rv.findChildViewUnder(e.getX(),e.getY());
			if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
				clicklistener.onClick(child,rv.getChildAdapterPosition(child));
			}

			return false;
		}

		@Override
		public void onTouchEvent(RecyclerView rv, MotionEvent e) {

		}

		@Override
		public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

		}
	}
}
