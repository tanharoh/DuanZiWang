package io.github.tanharoh.duanziwang;

import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.melnykov.fab.FloatingActionButton;

public class MainActivity extends AppCompatActivity
{
    ListView listview;
    private List<Map<String, Object>> list = new ArrayList<>();
    private String url;
    private String next_page_url = "";
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        listview = (ListView) findViewById(R.id.list);
        assert fab != null;
        assert listview != null;
        fab.attachToListView(listview);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(page < 40)
                {
                    page ++;
                    start(page);
                }else
                {
                Snackbar.make(view, "木有段子了....", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                }
            }
        });

        listview = (ListView) findViewById(R.id.list);
        start(page);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public void start(int page)
    {
        url = "http://www.lify.info/?page=" + page;
        list.clear();
        new Thread(runnable).start();
    }

    private void show()
    {
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.list_item,
                new String[]{"title", "time", "content", "category"},
                new int[]{R.id.title, R.id.time, R.id.content, R.id.category});
        listview.setAdapter(adapter);
    }


    Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            Log.e("URL", url);
            Connection conn = Jsoup.connect(url);
            // 修改http包中的header,伪装成浏览器进行抓取
            conn.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0");
            Document doc = null;
            try
            {
                doc = conn.get();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            // 获取下一页的链接
            assert doc != null;
            Elements link = doc.select("div.cbx").select("span.next");
            next_page_url = link.select("a").attr("href");
            Log.e("TAG", next_page_url);

            Elements elements = doc.select("div.cbx.post");
            for (Element element : elements)
            {
                String title = element.getElementsByTag("h2").text();
                String time = element.getElementsByClass("meta-date").text();
                String content = element.getElementsByClass("post-content").text();
                String category = element.getElementsByClass("meta-cat").text();

                Map<String, Object> map = new HashMap<>();
                map.put("title", title);
                map.put("time", time);
                map.put("content", content);
                map.put("category", category);
                list.add(map);
            }
            // 执行完毕后给handler发送一个空消息
            handler.sendEmptyMessage(0);
        }

    };

    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            // 收到消息后执行handler
            show();
        }
    };




    //按两次返回键退出程序
    private long mPressedTime = 0;

    @Override
    public void onBackPressed()
    {
        long mNowTime = System.currentTimeMillis();//获取第一次按键时间
        if ((mNowTime - mPressedTime) > 2000)
        {//比较两次按键时间差
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            mPressedTime = mNowTime;
        } else
        {//退出程序
            this.finish();
            System.exit(0);
        }
    }
}
