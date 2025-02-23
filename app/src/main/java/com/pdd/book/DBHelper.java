package com.pdd.book;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

public class DBHelper extends SQLiteOpenHelper
{
	//стандартный системный путь к базе данных приложения 
	private static int DB_VERSION = 7; // версия БД
	private static String DB_PATH;
	private static String DB_NAME = "rcr_book.db3";
	private SQLiteDatabase myDataBase;
	private final Context myContext;

	// Конструктор
	// Принимает и сохраняет ссылку на переданный контекст для доступа к ресурсам приложения
	// @param context
	public DBHelper(Context context) 
	{
		super(context, DB_NAME, null, DB_VERSION);
		this.myContext = context;

		//Составим полный путь к базам для вашего приложения
		if (Build.VERSION.SDK_INT >= 17)
		{
			DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
		}
		else
		{
			DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
		}

		openDataBase();
	}

	//Создает пустую базу данных и перезаписывает ее нашей собственной базой
	public void createDataBase() 
	{
		boolean dbExist = checkDataBase();
		if(dbExist)
		{
			//ничего не делать - база уже есть
		}
		else
		{
			//вызывая этот метод создаем пустую базу, позже она будет перезаписана
			this.getReadableDatabase();
			try
			{
				copyDataBase();
			}
			catch (IOException e) 
			{
				throw new Error("Error copying database");
			}
		}
	}

	// Проверяет, существует ли уже эта база, чтобы не копировать каждый раз при запуске приложения
	// @return true если существует, false если не существует
	private boolean checkDataBase()
	{
		File dbFile = new File(DB_PATH + DB_NAME);
		return dbFile.exists();
	}

	public void copyDataBase() throws IOException
	{
		//Открываем локальную БД как входящий поток
		InputStream myInput = myContext.getAssets().open(DB_NAME);
		
		//Путь ко вновь созданной БД
		String outFileName = DB_PATH + DB_NAME;
		OutputStream myOutput = new FileOutputStream(outFileName);
		
		//перемещаем байты из входящего файла в исходящий
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0)
		{
			myOutput.write(buffer, 0, length);
		}

		//закрываем потоки
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	public void openDataBase() throws SQLException
	{
		createDataBase();
		//открываем БД
		String myPath = DB_PATH + DB_NAME;

		if(myDataBase != null)
			myDataBase.close();

		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	}

	@Override
	public synchronized void close() 
	{
		if(myDataBase != null)
			myDataBase.close();

		super.close();
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			db.disableWriteAheadLogging();
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		if((oldVersion == 1  && newVersion ==  2) ||
		   (oldVersion <= 2  && newVersion ==  3) ||
		   (oldVersion <= 3  && newVersion ==  4) ||
		   (oldVersion <= 4  && newVersion ==  5) ||
		   (oldVersion <= 5  && newVersion ==  6) ||
		   (oldVersion <= 6  && newVersion ==  7) ||
		   (oldVersion <= 7  && newVersion ==  8) ||
		   (oldVersion <= 8  && newVersion ==  9) ||
		   (oldVersion <= 9  && newVersion == 10) ||
		   (oldVersion <= 10 && newVersion == 11) ||
		   (oldVersion <= 11 && newVersion == 12) ||
		   (oldVersion <= 12 && newVersion == 13) ||
		   (oldVersion <= 13 && newVersion == 14) ||
		   (oldVersion <= 14 && newVersion == 15) ||
		   (oldVersion <= 15 && newVersion == 16) ||
		   (oldVersion <= 16 && newVersion == 17) ||
		   (oldVersion <= 17 && newVersion == 18) ||
		   (oldVersion <= 18 && newVersion == 19) ||
		   (oldVersion <= 19 && newVersion == 20) ||
		   (oldVersion <= 20 && newVersion == 21) ||
		   (oldVersion <= 21 && newVersion == 22) ||
		   (oldVersion <= 22 && newVersion == 23) ||
		   (oldVersion <= 23 && newVersion == 24) ||
		   (oldVersion <= 24 && newVersion == 25) )
		{	
			try 
			{	
				copyDataBase();
			}
			catch (IOException e) 
			{
				throw new Error("Error copying database");
			}
		}
	}

	// Здесь можно добавить вспомогательные методы для доступа и получения данных из БД
	// вы можете возвращать курсоры через "return myDataBase.query(....)", это облегчит их использование
	// в создании адаптеров для ваших view
}
