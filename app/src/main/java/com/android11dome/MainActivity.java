package com.android11dome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    dataTools dataTools;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataTools=new dataTools(this,11);

        ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 64);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dataTools.savePermissions(requestCode, resultCode, data);//保存权限
    }




    public void button1(View view) {
        dataTools.requestPermission();//申请权限
    }

    public void button2(View view){
        dataTools.copyToData(getSdPath()+"/1.txt","/test","1.txt","application/txt");//将sd卡的1.txt文件复制到data/test/1.txt


    }
    public void button3(View view) {
        dataTools.delete("/test","1.txt");//删除data/test/1.txt文件

    }
    public void button4(View view) {
        dataTools.renameTo("/test","1.txt","2.txt");//将data/test/1.txt重命名为data/test/2.txt

    }

    public void button5(View view) {
        Toast.makeText(MainActivity.this,link(dataTools.getList("/test"),"\n"),Toast.LENGTH_SHORT).show();//获取data/test/目录下的文件列表
    }

    public static String getSdPath() {
        String state = Environment.getExternalStorageState();
        return "mounted".equals(state) && Environment.getExternalStorageDirectory().canWrite() ? Environment.getExternalStorageDirectory().getPath() : "";
    }
    public static String link(String[] array, String separator) {
        if (separator != null && array != null) {
            StringBuilder sb = new StringBuilder();
            String sep = "";
            String[] arr$ = array;
            int len$ = array.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                String a = arr$[i$];
                sb.append(sep).append(a);
                sep = separator;
            }

            return sb.toString();
        } else {
            return "";
        }
    }



}