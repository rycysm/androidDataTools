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

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    dataTools dataTools;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataTools=new dataTools(this,11);

        ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 64);//申请存储卡权限
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
    public void button6(View view) {
        Log.e(String.valueOf(dataTools.copyToSdcard("/test","1.txt",getSdPath()+"/3.txt")),"1111111");///将data/test/1.txt复制到sd卡的3.txt
    }
    public void button7(View view) {
        byte[] bytes=dataTools.read("/test","1.txt");
        if (bytes==null){
            Toast.makeText(MainActivity.this,"读取文件为空",Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(MainActivity.this, new String(bytes, 0, bytes.length, StandardCharsets.UTF_8),Toast.LENGTH_SHORT).show();//读入data/test/1.txt文件内容
    }
    public void button8(View view) {
        dataTools.write("/test","1.txt","application/txt","我是测试文本".getBytes());//将文本写到data/test/1.txt
    }
    public void button9(View view) {
        dataTools.asyncRead("/test","1.txt", 1, new dataTools.AsyncRead() {//保留java1.7的写法方便工程移值
                    @Override
                    public void onRead(byte[] data, int taskId) {//此方法回调前已经发送到主线程可以直接操作UI
                        if (data==null){
                            Toast.makeText(MainActivity.this,"读取文件为空",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(MainActivity.this, new String(data, 0, data.length, StandardCharsets.UTF_8),Toast.LENGTH_SHORT).show();//读入data/test/1.txt文件内容
                    }
        });
    }
    public void button10(View view) {
        Toast.makeText(MainActivity.this,dataTools.isPermissions()?"权限已获取":"没有权限",Toast.LENGTH_SHORT).show();//判断是否有权限
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