package dongting.bwei.com.examtwo;

import android.app.Application;

import org.xutils.DbManager;
import org.xutils.x;

/**
 * Created by muhanxi on 17/5/1.
 */
//

public class IApplication extends Application {

    private static IApplication mAppApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        mAppApplication = this ;


        getDaoConfig();

    }

    public  static DbManager.DaoConfig daoConfig;
    public static DbManager.DaoConfig getDaoConfig(){
        if(daoConfig==null){
            daoConfig=new DbManager.DaoConfig()
                    .setDbVersion(1)
                    .setDbName("tt")//设置数据库的名字
                    .setAllowTransaction(true)
                    .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                        @Override
                        public void onUpgrade(DbManager db, int oldVersion, int newVersion) {

                        }
                    });
        }
        return daoConfig;
    }

}
