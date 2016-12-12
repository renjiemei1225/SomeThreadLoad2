package ren.jiemei.somethreadload;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getCanonicalName();
    private ImageView imageview;
    private Button load;
    private String path = "http://img06.tooopen.com/images/20160916/tooopen_sl_178994132446.jpg";

    private int threadsum = 3;
    private RandomAccessFile raf;
    private File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imageview = (ImageView) findViewById(R.id.imageview);
        file = createFile(path);
        load = (Button) findViewById(R.id.load);
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null == raf){
                    try {
                        raf = new RandomAccessFile(file, "rwd");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                somethreadload(path,raf,threadsum);
            }
        });
    }

    private File createFile(String urlload) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Picture";
            int i = urlload.lastIndexOf("/");
            String filepath = absolutePath + urlload.substring(i);
           File file = new File(absolutePath);
            if (!file.exists()){
                file.mkdir();
            }
            //目标文件
           File dstfile = new File(filepath);
            if (!dstfile.exists()){
                try {
                    dstfile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                dstfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dstfile;
        }else {
            Log.e(TAG, "createFile: "+"没有sd卡" );
        }
        return null;
    }

    private void somethreadload(final String urlpath, final RandomAccessFile raf, final int sum) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlpath);
                    HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
                    httpconn.setConnectTimeout(5000);
                    //总文件大小
                    int lengthsum = httpconn.getContentLength();

                    //设置成一样大小
                    raf.setLength(lengthsum);
                    raf.close();
                    Log.e(TAG, "run: "+"设置raf的大小："+lengthsum );

                    //分配每个线程下载的大小
                    int bocket = lengthsum / sum;

                    for (int i = 0; i < sum; i++) {
                        int startsize = bocket*(i);
                        int endsize = bocket*(i+1);
                        if (sum-1 == i){
                            endsize = lengthsum;
                        }
                        Log.e(TAG, "run: "+"线程："+i+"  下载"+startsize+"-->"+endsize );

                        mythread mythread = new mythread(urlpath,i,startsize,endsize,file);
                        mythread.start();
                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


}
class mythread extends Thread{
    private final String pathuu;
    private final int threadid;
    private final File file;
    private  RandomAccessFile raf;
    private final int startsize;
    private final int endsize;
    private String TAG = mythread.class.getCanonicalName();

    public mythread(String path, int threadid, int startsize, int endsize, File file) {
        this.pathuu = path;
        this.threadid = threadid;
        this.file = file;
        this.startsize = startsize;
        this.endsize = endsize;
    }



    @Override
    public void run() {
        super.run();

        try {
            URL url = new URL(pathuu);
            Log.e(TAG, "run下载地址：: "+pathuu );
            HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
            httpconn.setConnectTimeout(5000);
            httpconn.setRequestMethod("GET");
            httpconn.setRequestProperty("Range","bytes="+startsize+"-"+endsize);

            int code = httpconn.getResponseCode();
            Log.e(TAG, "run: code:"+code );
            if (206 == code){

                InputStream is = httpconn.getInputStream();
                byte[] buff = new byte[512];
                int len;
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(startsize);
//                raf.skipBytes(startsize);

                Log.e("tag", "run: "+"线程"+threadid+"开始下载" );
                while((len = is.read(buff)) != -1){
                    raf.write(buff,0,len);
                }
                is.close();
                raf.close();
                Log.e(TAG, "run:raf的大小： "+raf.length() );
                Log.e(TAG, "run: "+"线程"+threadid+"下载完毕" );
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
