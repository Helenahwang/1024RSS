package com.appdev.a503_02.a1024rss;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;

    //ListView에 출력될 데이터 - M
    ArrayList<String> list;

    //출력을 위한 ListView - V
    ListView listView;

    //데이터와 ListView를 연결 시켜줄 Adapter - C
    ArrayAdapter<String> adapter;


    //진행상황을 출력할 대화상자
    ProgressDialog progressDialog;

    //웹에서 다운로드 받을 스레드
    class ThreadEx extends Thread{
        @Override
        public void run(){
            //다운로드 받은 문자열을 저장할 객체
            StringBuilder sb = new StringBuilder();
            try {

                //문자열을 다운로드 받는 코드 영역
                URL url = new URL("http://www.hani.co.kr/rss/sports/");

                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setUseCaches(false);
                con.setConnectTimeout(30000);

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                while(true){
                    String line = br.readLine();
                    if(line == null) break;
                    sb.append(line+"\n");
                }

                br.close();
                con.disconnect();
                //Log.e("다운로드 받은 문자열", sb.toString());


            }catch (Exception e){
                Log.e("다운로드 실패", e.getMessage());
            }


            //XML 파싱
            try{
                //파싱을 수행할 객체 생성
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                //다운로드 받은 문자열을 InputStream으로 변환
                InputStream istream = new ByteArrayInputStream(sb.toString().getBytes("utf-8"));

                //메모리에 펼치기
                Document doc = builder.parse(istream);

                //루트를 가져오기
                Element root = doc.getDocumentElement();

                //원하는 태그의 데이터를 가져오기
                NodeList items = root.getElementsByTagName("title");
                //Log.e("items", items.toString());


                //반복문으로 태그를 순회
                for(int i=1; i<items.getLength(); i++){ // 1부터 시작하면 맨 처음 보여지는 전체 타이틀 삭제 가능하다.
                    //태그를 하나씩 가져오기
                    Node node = items.item(i);

                    //태그 안의 문자열을 가져와서 리스트에 추가
                    Node contents = node.getFirstChild();
                    String title = contents.getNodeValue();
                    list.add(title);

                }

                //핸들러 호출
                handler.sendEmptyMessage(0); //데이터를 줄 것이 없으면


            }catch (Exception e){
                Log.e("XML 파싱 객체", e.getMessage());
            }





        }
    }

    //화면을 갱신할 핸들러
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            progressDialog.dismiss();
            adapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new ThreadEx().start();
            }
        });

        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView = (ListView)findViewById(R.id.listview);

        //데이터와 ListView 연결
        listView.setAdapter(adapter);

        progressDialog = ProgressDialog.show(this, "추가중", "추가중입니다.");

        ThreadEx th = new ThreadEx();
        th.start();




    }
}
