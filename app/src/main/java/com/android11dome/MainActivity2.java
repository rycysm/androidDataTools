package com.android11dome;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    dataToolsApi33 dataTools;
    private int requestAllFileCode =65;
    String dir="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        dataTools=new dataToolsApi33(this,11);
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 64);//申请存储卡权限
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dataTools.savePermissions(requestCode, resultCode, data);//保存权限
        if(requestCode==requestAllFileCode){
            if(dataTools.isAllFilePermission()){
                Toast.makeText(MainActivity2.this,"全部文件读写权限已成功获取",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity2.this,"您未授权全部文件读写权限",Toast.LENGTH_SHORT).show();
            }
        }
    }
    String[] s;
    boolean[] b;

    /**
     * 加载data下的app目录列表,选择后申请对应目录权限
     * @param view 组件
     */
    public void button1(View view) {
        Dialog dialog= new Dialog(this);
        new Thread(() -> {
            s=dataTools.getList2();
            b=new boolean[s.length];
            for (int i=0;i<s.length;i++){
                    b[i]=false;

            }
            b[0]=true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    dir=dialog.RadioDialogBox(MainActivity2.this,"请选择操作目录",  s,b);
                    if(!dir.equals("")){
                        if(!dataTools.isPermissions("/"+dir)){
                            dataTools.requestPermission("/"+dir);

                        }else{
                            Toast.makeText(MainActivity2.this,"已经获得权限",Toast.LENGTH_SHORT).show();

                        }

                    }

                }
            });

        }).start();
        dialog.showProgressDialog("正在加载应用列表...");

    }



    public static String[] getFiles(String path) {
        File file=new File(path);
        File[] files=file.listFiles();
        if (files == null){

            Log.e("error","空目录");return null;

        }
        List<String> s = new ArrayList<>();
        for(int i =0;i<files.length;i++){
            s.add(files[i].getAbsolutePath());
        }


        String[] strings = new String[s.size()];
        s.toArray(strings);

        return strings;
    }
    public void button2(View view){
        if(dir.equals("")){
            Toast.makeText(MainActivity2.this,"请先申请权限选择目录",Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(MainActivity2.this, link(dataTools.getList("/"+dir),"\n"),Toast.LENGTH_SHORT).show();
    }

    public void button3(View view) {
        if(dir.equals("")){
            Toast.makeText(MainActivity2.this,"请先申请权限选择目录",Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] bytes=dataTools.read("/"+dir,"1.txt");
        if (bytes==null){
            Toast.makeText(MainActivity2.this,"读取文件为空",Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(MainActivity2.this, new String(bytes, 0, bytes.length, StandardCharsets.UTF_8),Toast.LENGTH_SHORT).show();//读入data/test/1.txt文件内容

    }
    public void button4(View view) {
        if(dir.equals("")){
            Toast.makeText(MainActivity2.this,"请先申请权限选择目录",Toast.LENGTH_SHORT).show();
            return;
        }
        dataTools.write("/"+dir,"1.txt","application/txt","我是测试文本".getBytes());//将文本写到data/test/1.txt

    }
    public void button5(View view) {
        if(dir.equals("")){
            Toast.makeText(MainActivity2.this,"请先申请权限选择目录",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!dataTools.isPermissions("/"+dir)){
            Toast.makeText(MainActivity2.this,"没有权限哦",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity2.this,"权限已经获取",Toast.LENGTH_SHORT).show();
        }
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
    public void button6(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Toast.makeText(MainActivity2.this,"当前设备不是android11无法申请",Toast.LENGTH_SHORT).show();
            return;
        }
        if (dataTools.isAllFilePermission()){
            Toast.makeText(MainActivity2.this,"权限已获取",Toast.LENGTH_SHORT).show();
        }else{
            dataTools.requestAllFilePermission(requestAllFileCode);
        }
    }
}