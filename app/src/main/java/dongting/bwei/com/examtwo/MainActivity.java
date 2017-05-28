package dongting.bwei.com.examtwo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dongting.bwei.com.xlistviewlib.XListView;

public class MainActivity extends Activity implements XListView.IXListViewListener{

    List<Beans.AppBean> appLists =new ArrayList<>();

    private Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==0){
                List<Beans.AppBean> appList =(List<Beans.AppBean>)msg.obj;
                appLists.addAll(appList);
                myAdpager.notifyDataSetChanged();
            }
        }
    };
    private XListView xlistView;
    private MyAdpager myAdpager;
    int page=1;

    private AlertDialog.Builder builder;
    private AlertDialog.Builder b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xlistView = (XListView) findViewById(R.id.offline_listview);

        xlistView.setPullRefreshEnable(true);
        xlistView.setPullLoadEnable(true);
        xlistView.setXListViewListener(this);

        initData(true,1);

        myAdpager = new MyAdpager();

        xlistView.setAdapter(myAdpager);

        xlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setDialog(position);

                System.out.println("clickPosition = " + position);

                builder.show();
            }
        });
    }

    private void download(String url) {
        RequestParams params = new RequestParams(url);

        params.setSaveFilePath(Environment.getExternalStorageDirectory()+"/App/");

        params.setAutoRename(true);
        x.http().post(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(result), "application/vnd.android.package-archive");
                MainActivity.this.startActivity(intent);
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }
            @Override
            public void onCancelled(CancelledException cex) {
            }
            @Override
            public void onFinished() {
            }
            //网络请求之前回调
            @Override
            public void onWaiting() {
            }
            //网络请求开始的时候回调
            @Override
            public void onStarted() {
            }
            //下载的时候不断回调的方法
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                //当前进度和文件总大小
                Log.d("download","current："+ current +"，total："+total);
            }
        });
    }

    private void setDialog(final int position) {
        b = new AlertDialog.Builder(this);
        b.setTitle("版本更新");
        b.setMessage("现在检测到新版本，是否更新?");
        b.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                get(position);

                //System.out.println("getPosition = " + position);
            }
        });
        final String[] itmes = {"wifi","手机流量"};
        builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("网络选择");
        builder.setSingleChoiceItems(itmes, -1, new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    b.show();
                    dialog.cancel();
                }else{
                    startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                }
            }
        });
    }

    private void get(final int position) {
        RequestParams params = new RequestParams
                ("http://mapp.qzone.qq.com/cgi-bin/mapp/mapp_subcatelist_qq?yyb_cateid=-10&categoryName=%E8%85%BE%E8%AE%AF%E8%BD%AF%E4%BB%B6&pageNo=1&pageSize=20&type=app&platform=touch&network_type=unknown&resolution=412x732");
       /* params.addQueryStringParameter("username","abc");
        params.addQueryStringParameter("password","123");*/
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String substring = result.substring(0, result.length() - 1);
                Gson gson=new Gson();
                Beans beans = gson.fromJson(substring, Beans.class);
                List<Beans.AppBean> app = beans.getApp();
                String url = app.get(position-1).getUrl();

                //System.out.println("urlPosition = " + position);

                download(url);
            }
            //请求异常后的回调方法
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }
            //主动调用取消请求的回调方法
            @Override
            public void onCancelled(CancelledException cex) {
            }
            @Override
            public void onFinished() {
            }
        });
    }

    private void initData(boolean refresh,int page) {
        RequestParams params = new RequestParams
                ("http://mapp.qzone.qq.com/cgi-bin/mapp/mapp_subcatelist_qq?yyb_cateid=-10&categoryName=%E8%85%BE%E8%AE%AF%E8%BD%AF%E4%BB%B6&pageNo=1&pageSize=20&type=app&platform=touch&network_type=unknown&resolution=412x732");

        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

                String substring = result.substring(0, result.length() - 1);
                Gson gson=new Gson();
                Beans beans = gson.fromJson(substring, Beans.class);
                List<Beans.AppBean> app = beans.getApp();

                Message msg = Message.obtain();
                msg.what = 0;
                msg.obj = app;
                handler.sendMessage(msg);

            }
            //请求异常后的回调方法
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }
            //主动调用取消请求的回调方法
            @Override
            public void onCancelled(CancelledException cex) {
            }
            @Override
            public void onFinished() {
            }
        });
    }

    @Override
    public void onRefresh() {

        appLists.clear();
        initData(true,1);
        myAdpager.notifyDataSetChanged();

       xlistView .stopRefresh();
        xlistView.setRefreshTime("刚刚");

    }

    @Override
    public void onLoadMore() {
        page++;

        initData(false,page);

        xlistView.stopLoadMore();
    }

    class MyAdpager extends BaseAdapter {

        @Override
        public int getCount() {
            return appLists.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView= View.inflate(MainActivity.this,R.layout.item_off_line,null);

            TextView textView= (TextView) convertView.findViewById(R.id.off_line_textview);
            textView.setText(appLists.get(position).getName());

            return convertView;
        }
    }
}
