package cn.edu.lin.graduationproject.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import cn.edu.lin.graduationproject.bean.BlogImage;

/**
 * 利用 OrmLite 工具类进行创建数据库
 * Created by liminglin on 17-2-28.
 */

public class DBHelper extends OrmLiteSqliteOpenHelper{

    private static DBHelper instance;

    public static DBHelper getInstance(Context context){
        if(instance == null){
            synchronized (DBHelper.class){
                if(instance == null){
                    instance = new DBHelper(context);
                }
            }
        }
        return instance;
    }

    public DBHelper(Context context){
        super(context,"blog_image.db",null,1);

    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        // 根据 BlogImage 类创建数据表
        try {
            TableUtils.createTableIfNotExists(connectionSource,BlogImage.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource,BlogImage.class,true);
            onCreate(database,connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
