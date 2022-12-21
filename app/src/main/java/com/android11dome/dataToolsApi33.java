package com.android11dome;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *dataToolsApi33 此类适配与api33 android13
 * by 若忧愁
 * qq 2557594045
 *
 */
public class dataToolsApi33 {
    Activity context ;//内部操作Activity对象
    int requestCode=11;//请求标识
    /**
     * 构造方法
     * @context # Activity对象
     * @requestCode  #请求码
     */
    public  dataToolsApi33(Activity context,int requestCode) {
        this.context=context;
        this.requestCode=requestCode;
    }


    /**
     *申请data访问权限请在onActivityResult事件中调用savePermissions方法保存权限
     * @dir 在api13后必须申请某个目录权限不能只申请data
     */
    public void requestPermission(String dir) {
        Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+dir.replaceAll("/","%2F"));
        uri1= DocumentFile.fromTreeUri(this.context,uri1).getUri();
        Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri1);
        context.startActivityForResult(intent1, requestCode);

    }

    /**
     * 保存权限onActivityResult返回的参数全部传入即可
     * @requestCode #onActivityResult
     * @resultCode  #onActivityResult
     * @data #onActivityResult
     */
    public void savePermissions(int requestCode, int resultCode, Intent data) {
        if (this.requestCode!=requestCode)return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Uri uri = data.getData();
                if (uri==null)return;
                this.context.getContentResolver().takePersistableUriPermission(uri,data.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 判断是否获取使用data目录权限
     * @dir 判断是否拥有该目录权限 在android13中必须判断某个目录是否有权限 即android/data/下某个文件目录
     * @return #返回一个boolean true有权限 false 无权限
     */
    public boolean isPermissions(String dir) {
        Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+dir.replaceAll("/","%2F"));
        DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
        if(documentFile==null)return false;
        return documentFile.canWrite();
    }

    /**
     * 将sdcard中的文件拷贝至data目录中
     * @sourcePath #sdcard中的完整文件路径
     * @targetDir  #拷贝至的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @targetName #目标文件名
     * @fileType 目录文件类型 如txt文件 application/txt
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean copyToData(String sourcePath, String targetDir ,String targetName , String fileType) {
        targetDir=textual(targetDir,targetName,"");
        if ((new File(sourcePath)).exists()) {
            try {
                InputStream inStream = new FileInputStream(sourcePath);
                String[] list = targetDir.split("/");
                if (list.length<2)return false;
                Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1] );
                DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
                int i=2;
                while (i<list.length) {
                    if (!list[i].equals("")) {
                        DocumentFile a = getDocumentFile1(documentFile,list[i]);
                        if(a==null){
                            documentFile=documentFile.createDirectory(list[i]);
                        }else{
                            documentFile=a;
                        }
                    }
                    i++;
                }
                DocumentFile newFile = null;
                if (exists(documentFile,targetName)) {
                    newFile = documentFile.findFile(targetName);
                } else {
                    newFile = documentFile.createFile(fileType, targetName);
                }
                OutputStream excelOutputStream = this.context.getContentResolver().openOutputStream(newFile.getUri());
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = inStream.read(buffer)) != -1)
                {
                    excelOutputStream.write(buffer, 0, len);
                }
                inStream.close();
                excelOutputStream.close();
                return true;
            } catch (Exception var8) {
                var8.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
    /**
     * 将sdcard中的文件拷贝至data目录中与copyToData方法不同的是,此方法在遇到已经存在的文件时,将自动删除原文件然后重新创建,可以实现文件覆盖。
     * @sourcePath #sdcard中的完整文件路径
     * @targetDir  #拷贝至的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @targetName #目标文件名
     * @fileType 目录文件类型 如txt文件 application/txt
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean copyToData_cover(String sourcePath, String targetDir ,String targetName , String fileType) {
        targetDir=textual(targetDir,targetName,"");
        if ((new File(sourcePath)).exists()) {
            try {
                String[] list = targetDir.split("/");
                if (list.length<2)return false;
                InputStream inStream = new FileInputStream(sourcePath);
                // byte[] buffer = new byte[inStream.available()];
                Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1] );
                DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
                int i=2;
                while (i<list.length) {
                    if (!list[i].equals("")) {
                        DocumentFile a = getDocumentFile1(documentFile,list[i]);
                        if(a==null){
                            documentFile=documentFile.createDirectory(list[i]);
                        }else{
                            documentFile=a;
                        }
                    }
                    i++;
                }
                DocumentFile newFile = null;
                if (exists(documentFile,targetName)) {
                    newFile = documentFile.findFile(targetName);
                } else {
                    newFile = documentFile.createFile(fileType, targetName);
                }
                OutputStream excelOutputStream = this.context.getContentResolver().openOutputStream(newFile.getUri());
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = inStream.read(buffer)) != -1)
                {
                    excelOutputStream.write(buffer, 0, len);
                }
                inStream.close();
                excelOutputStream.close();
                return true;
            } catch (Exception var8) {
                var8.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
    /**
     * 通过目录路径,获取该目录的DocumentFile对象,如目录不存在将自动创建,获取失败将返回null,这样做可以避免重复获取同一个路径的DocumentFile导致的耗时,推荐配合copyToData_find_cover使用
     * @dir  #获取DocumentFile的目录名
     * @return #返回DocumentFile对象,获取失败将null
     */
    public DocumentFile getDocumentFile(String dir) {
        try {
            String[] list=dir.split("/");
            if (list.length<2)return null;
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1] );
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            int i=2;
            while (i<list.length) {
                if (!list[i].equals("")) {
                    DocumentFile a = getDocumentFile1(documentFile,list[i]);
                    if(a==null){
                        documentFile=documentFile.createDirectory(list[i]);
                    }else{
                        documentFile=a;
                    }
                }
                i++;
            }
            return documentFile;
        } catch (Exception var8) {
            var8.printStackTrace();
            return null;
        }
    }
    /**
     * 根据传入的路径,获取文件的输入流
     * @targetDir  #获取输入流文件的目录 如拷贝至data/test/目录 那就是 /test
     * @targetName #目标文件名
     * @return #返回该文件的输入流,如果失败则返回null
     */
    public InputStream getInputStream( String targetDir ,String targetName) {
        targetDir=textual(targetDir,targetName,"");
        try {
            String[] list = targetDir.split("/");
            if (list.length<2)return null;
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1] );
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            int i=0;
            while (i<list.length) {
                if (!list[i].equals("")) {
                    DocumentFile a = getDocumentFile1(documentFile,list[i]);
                    if(a==null){
                        documentFile=documentFile.createDirectory(list[i]);
                    }else{
                        documentFile=a;
                    }
                }
                i++;
            }
            DocumentFile newFile = null;
            if (exists(documentFile,targetName)) {
                newFile = documentFile.findFile(targetName);
            } else {
                return null;
            }
            return  this.context.getContentResolver().openInputStream(newFile.getUri());

        } catch (Exception var8) {
            var8.printStackTrace();
            return null;
        }

    }
    /**
     * 将sdcard中的文件拷贝至data目录中与copyToData_cover方法不同的是,此方法支持,传入一个起始的DocumentFile对象,这样可以更高性能的操作,避免重复获取同一个目录对象的耗时。
     * @sourcePath #sdcard中的完整文件路径
     * @targetDir  #拷贝至的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @targetName #目标文件名
     * @fileType 目录文件类型 如txt文件 application/txt
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean copyToData_find_cover(DocumentFile startDocumentFile,String sourcePath, String targetDir ,String targetName , String fileType) {
        targetDir=textual(targetDir,targetName,"");
        if ((new File(sourcePath)).exists()) {
            try {
                InputStream inStream = new FileInputStream(sourcePath);
                DocumentFile documentFile = startDocumentFile;
                String[] list=targetDir.split("/");
                int i=0;
                while (i<list.length) {
                    if (!list[i].equals("")) {
                        DocumentFile a = getDocumentFile1(documentFile,list[i]);
                        if(a==null){
                            documentFile=documentFile.createDirectory(list[i]);
                        }else{
                            documentFile=a;
                        }
                    }
                    i++;
                }
                DocumentFile newFile = null;
                if (exists(documentFile,targetName)) {
                    newFile=documentFile.findFile(targetName);
                }else {
                    newFile = documentFile.createFile(fileType, targetName);
                }
                OutputStream excelOutputStream=null;
                InputStream inputStream2 =null;
                if (newFile !=null){
                    inputStream2=this.context.getContentResolver().openInputStream(newFile.getUri());
                    excelOutputStream = this.context.getContentResolver().openOutputStream(newFile.getUri());
                }
                int ys= inStream.available();
                int news= inputStream2.available();
                int c = news-ys;
                if(c>0){
                    byte[] bytes=new byte[c];
                    for(int i1=0;i1<c;i1++){
                        bytes[i1]=0;
                    }
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = inStream.read(buffer)) != -1)
                    {
                        excelOutputStream.write(buffer, 0, len);
                    }
                    inStream.close();
                    excelOutputStream.write(bytes);
                    excelOutputStream.close();
                    return true;
                }else {
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = inStream.read(buffer)) != -1)
                    {
                        excelOutputStream.write(buffer, 0, len);
                    }
                    inStream.close();
                    excelOutputStream.close();
                    return true;
                }

            } catch (Exception var8) {
                var8.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
    /**
     * 将sdcard中的文件拷贝至data目录中与copyToData_cover方法不同的是,此方法支持,传入一个起始的DocumentFile对象,这样可以更高性能的操作,避免重复获取同一个目录对象的耗时。
     * @sourcePath #sdcard中的完整文件路径
     * @targetDir  #拷贝至的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @targetName #目标文件名
     * @fileType 目录文件类型 如txt文件 application/txt
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean copyToData_cover(DocumentFile startDocumentFile,String sourcePath, String targetDir ,String targetName , String fileType) {
        targetDir=textual(targetDir,targetName,"");
        if ((new File(sourcePath)).exists()) {
            try {
                InputStream inStream = new FileInputStream(sourcePath);
                DocumentFile documentFile = startDocumentFile;
                String[] list=targetDir.split("/");
                int i=0;
                while (i<list.length) {
                    if (!list[i].equals("")) {
                        DocumentFile a = getDocumentFile1(documentFile,list[i]);
                        if(a==null){
                            documentFile=documentFile.createDirectory(list[i]);
                        }else{
                            documentFile=a;
                        }
                    }
                    i++;
                }
                DocumentFile newFile = null;
                if (exists(documentFile,targetName)) {
                    DocumentFile  documentFileDelete=documentFile.findFile(targetName);

                    boolean isDelete= documentFile.delete();
                    if (!isDelete){
                        return false;
                    }
                }

                newFile = documentFile.createFile(fileType, targetName);


                OutputStream excelOutputStream=null;
                //  InputStream inputStream2 =null;
                if (newFile !=null){
                    //  inputStream2=this.context.getContentResolver().openInputStream(newFile.getUri());
                    excelOutputStream = this.context.getContentResolver().openOutputStream(newFile.getUri());
                }else {  return false;}

                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = inStream.read(buffer)) != -1)
                {
                    excelOutputStream.write(buffer, 0, len);
                }
                inStream.close();
                excelOutputStream.close();
                return true;

                // }

            } catch (Exception var8) {
                var8.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 将Android/data中的文件拷贝至sdcard
     * @sourceDir #文件原目录以data开始 如拷贝data/test/目录中的文件 那就是 /test
     * @sourceFilename #拷贝的文件名 如拷贝 data/test/1.txt 那就是1.txt
     * @targetPath #目标文件路径需提供完整的路径目录+文件名
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean copyToSdcard(String sourceDir,String sourceFilename, String targetPath) {
        try {
            String[] list = sourceDir.split("/");
            if (list.length<2)return false;
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1]);
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);

            int i = 0;
            while (i < list.length) {
                if (!list[i].equals("")) {
                    DocumentFile a = getDocumentFile1(documentFile, list[i]);
                    if (a == null) {
                        documentFile = documentFile.createDirectory(list[i]);
                    } else {
                        documentFile = a;
                    }
                }
                i++;
            }
            documentFile=documentFile.findFile(sourceFilename);
            InputStream   inputStream = this.context.getContentResolver().openInputStream(documentFile.getUri());
            FileOutputStream fs = new FileOutputStream(targetPath);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1)
            {
                fs.write(buffer, 0, len);
            }
            inputStream.close();
            fs.close();
            return true;
        } catch (Exception var8) {
            var8.printStackTrace();
            return false;
        }
    }
    /**
     * 删除data目录中的指定路径的文件
     * @dir  #删除文件的目录目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #目标文件名
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean delete(String dir,String fileName) {
        try {

            String[] list = dir.split("/");
            if (list.length<2)return false;

            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1]);
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);

            int i = 0;
            while (i < list.length) {
                if (!list[i].equals("")) {
                    DocumentFile a = getDocumentFile1(documentFile, list[i]);
                    if (a == null) {
                        documentFile = documentFile.createDirectory(list[i]);
                    } else {
                        documentFile = a;
                    }
                }
                i++;
            }
            documentFile=documentFile.findFile(fileName);
            return documentFile.delete();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 重命名文件
     * @dir  #重命名文件目录 目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #目标文件名
     * @targetName #重命名后的文件名
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean renameTo(String dir,String fileName,String targetName) {
        try {
            String[] list = dir.split("/");
            if (list.length<2)return false;
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1]);
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            int i = 0;
            while (i < list.length) {
                if (!list[i].equals("")) {
                    DocumentFile a = getDocumentFile1(documentFile, list[i]);
                    if (a == null) {
                        documentFile = documentFile.createDirectory(list[i]);
                    } else {
                        documentFile = a;
                    }
                }
                i++;
            }
            documentFile=documentFile.findFile(fileName);
            return documentFile.renameTo(targetName);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }



    /**
     * 获取Android/Data下的软件包名
     *
     * @param context
     * @return
     */
    public  List<String> getAndroidDataPackageNames(Context context) {
        // 得到PackageManager对象
        PackageManager pm = context.getPackageManager();

        List<String> packageNameList = new ArrayList<>();

        Intent intent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

        String packageName;
        for (ResolveInfo resolveInfo : resolveInfos) {
            packageName = resolveInfo.activityInfo.packageName;
            if (isFileExists(getSdPath()+"/Android/data" + File.separator + packageName)) {
                packageNameList.add(packageName);
            }
        }

        return packageNameList;
    }


    /**
     * Return the file by path.
     *
     * @param filePath The path of file.
     * @return the file
     */
    public  File getFileByPath(final String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    public  boolean isFileExists(final String filePath) {
        File file = getFileByPath(filePath);
        if (file == null) return false;
        if (file.exists()) {
            return true;
        }
        return isFileExistsApi29(filePath);
    }

    public  boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private  boolean isFileExistsApi29(String filePath) {
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                Uri uri = Uri.parse(filePath);
                ContentResolver cr = context.getContentResolver();
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(uri, "r");
                if (afd == null) return false;
                try {
                    afd.close();
                } catch (IOException ignore) {
                }
            } catch (FileNotFoundException e) {
                return false;
            }
            return true;
        }
        return false;
    }


    public static String getSdPath() {
        String state = Environment.getExternalStorageState();
        return "mounted".equals(state) && Environment.getExternalStorageDirectory().canWrite() ? Environment.getExternalStorageDirectory().getPath() : "";
    }


    private static List<String> sExtSdCardPaths = new ArrayList<>();

    public  String[] getExtSdCardPaths(Context context) {
        if (sExtSdCardPaths.size() > 0) {
            return sExtSdCardPaths.toArray(new String[0]);
        }
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    sExtSdCardPaths.add(path);
                }
            }
        }
        if (sExtSdCardPaths.isEmpty()) sExtSdCardPaths.add("/storage/sdcard1");
        return sExtSdCardPaths.toArray(new String[0]);
    }

    /**
     * 获取data目录下所有文件返回文本型数组
     * @return #返回一个文本数组为该目录下所有的文件名
     */
    public String [] getList2() {
        List<String> packageNames = getAndroidDataPackageNames(context);
        List<File> subFiles = new ArrayList<>();

        File tempSubFile;
        for (int i = 0; i < packageNames.size(); i++) {
            tempSubFile = new File(getSdPath()+"/Android/data", packageNames.get(i));
            //不存在就不添加
            if (tempSubFile.exists()) {
                subFiles.add(tempSubFile);
            }
        }


        String[] strings = new String[subFiles.size()];

        for (int i=0;i<subFiles.size();i++){
            strings[i]=subFiles.get(i).getName();
        }

        return strings;


    }

    /**
     * 获取目录下所有文件返回文本型数组
     * @dir  #文件目录 目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @return #返回一个文本数组为该目录下所有的文件名
     */
    public String [] getList(String dir) {
        try {
            String[] list = dir.split("/");
            if (list.length<2)return null;
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1]);
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            int i = 2;
            while (i < list.length) {
                if (!list[i].equals("")) {
                    DocumentFile a = getDocumentFile1(documentFile, list[i]);
                    if (a == null) {
                        documentFile = documentFile.createDirectory(list[i]);
                    } else {
                        documentFile = a;
                    }
                }
                i++;
            }
            DocumentFile[] documentFile1 = documentFile.listFiles();
            String[] res = new String[documentFile1.length];
            int i1 =0;
            while (i1<documentFile1.length){
                res[i1]=documentFile1[i1].getName();
                i1++;
            }
            return res;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 判断目录是否存在
     * @dir  #判断文件目录 目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @return #返回一个boolean true存在 false 不存在
     */
    public boolean dirIsExist(String dir) {
        try {
            String[] list = dir.split("/");
            if (list.length<2)return false;
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1]);
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            int i = 0;
            while (i < list.length) {
                if (!list[i].equals("")) {
                    DocumentFile a = getDocumentFile1(documentFile, list[i]);
                    if (a == null) {
                        return false;
                    } else {
                        documentFile = a;
                    }
                }
                i++;
            }
            return documentFile.exists();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 重命名目录
     * @dir  #重命名文件目录 目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @targetName #重命名后的文件夹名
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean reNameDir(String dir,String targetName) {
        try {
            String[] list = dir.split("/");
            if (list.length<2)return false;
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1]);
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);

            int i = 0;
            while (i < list.length) {
                if (!list[i].equals("")) {
                    DocumentFile a = getDocumentFile1(documentFile, list[i]);
                    if (a == null) {
                        return false;
                    } else {
                        documentFile = a;
                    }
                }
                i++;
            }
            return documentFile.renameTo(targetName);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 将byte[] 写出到data目录的文件中如果没有这个文件会自动创建目录及文件
     * @Dir  #写出的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #写出的文件名
     * @fileType 目录文件类型 如txt文件 application/txt
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean write(String dir,String fileName, String fileType,byte[] bytes) {
        try {
            String[] list = dir.split("/");
            if (list.length<2)return false;
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1] );
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            int i=2;
            while (i<list.length) {
                if (!list[i].equals("")) {
                    DocumentFile a = getDocumentFile1(documentFile,list[i]);
                    if(a==null){
                        documentFile=documentFile.createDirectory(list[i]);
                    }else{
                        documentFile=a;
                    }
                }
                i++;
            }
            DocumentFile newFile = null;
            if (exists(documentFile,fileName)) {
                newFile = documentFile.findFile(fileName);
            } else {
                newFile = documentFile.createFile(fileType, fileName);
            }
            OutputStream excelOutputStream = this.context.getContentResolver().openOutputStream(newFile.getUri());
            return doDataOutput2(bytes, excelOutputStream);
        } catch (Exception var5) {
            var5.printStackTrace();
            return false;
        }
    }
    /**
     * 读取data下指定路径的文件为byte[]
     * @Dir  #读取的文件目录以data开始 如读取data/test/目录 那就是 /test
     * @fileName #写出的文件名
     * @fileType 目录文件类型 如txt文件 application/txt
     * @return #返回一个byte[] 如文件为空或者不存在此返回可能为null请判断后使用
     */
    public byte[] read(String dir ,String fileName) {
        byte[] buffer = null;
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try {
            String[] list = dir.split("/");
            if (list.length<2)return null;
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1]);
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context,uri1);

            int i=2;
            while (i<list.length) {
                if (!list[i].equals("")) {
                    documentFile = getDocumentFile1(documentFile,list[i]);
                }
                i++;
            }
            documentFile=documentFile.findFile(fileName);
            inputStream = this.context.getContentResolver().openInputStream(documentFile.getUri());
            buffer=new byte[inputStream.available()];
            while (true)
            {
                int readLength = inputStream.read(buffer);
                if (readLength == -1) break;
                arrayOutputStream.write(buffer, 0, readLength);
            }
            inputStream.close();
            arrayOutputStream.close();
        } catch (Exception var5) {
            var5.printStackTrace();
            if(inputStream!=null){
                try {
                    inputStream.close();
                    arrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffer;
    }
    /**
     * 异步读取data下指定路径的文件为byte[]
     * @Dir  #读取的文件目录以data开始 如读取data/test/目录 那就是 /test
     * @fileName #写出的文件名
     * @fileType 目录文件类型 如txt文件 application/txt
     * @return #将在asyncRead接口中的onRead中返回数据和传入时的taskId
     */
    public void asyncRead(String dir ,String fileName,int taskId,AsyncRead asyncRead) {
        new Thread(new Runnable() {//保留java1.7的写法方便工程移值
            @Override
            public void run() {
                byte[] buffer = null;
                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                InputStream inputStream = null;
                try {
                    String[] list = dir.split("/");
                    if (list.length<2)return ;
                    Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"+"%2F"+list[1]);
                    DocumentFile documentFile = DocumentFile.fromTreeUri(context,uri1);

                    int i=0;
                    while (i<list.length) {
                        if (!list[i].equals("")) {
                            documentFile = getDocumentFile1(documentFile,list[i]);
                        }
                        i++;
                    }
                    documentFile=documentFile.findFile(fileName);
                    inputStream = context.getContentResolver().openInputStream(documentFile.getUri());
                    buffer=new byte[inputStream.available()];
                    while (true)
                    {
                        int readLength = inputStream.read(buffer);
                        if (readLength == -1) break;
                        arrayOutputStream.write(buffer, 0, readLength);
                    }
                    inputStream.close();
                    arrayOutputStream.close();
                } catch (Exception var5) {
                    var5.printStackTrace();
                    if(inputStream!=null){
                        try {
                            inputStream.close();
                            arrayOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                byte[] finalBuffer = buffer;
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        asyncRead.onRead(finalBuffer,taskId);
                    }
                });

            }
        }).start();
    }
    /**
     * 异步读取接口
     * @data #返回的数据可能为空需要判断
     * @taskId #调用时传入的任务id
     */
    public interface AsyncRead{
        void onRead(byte[] data,int taskId);
    }
    /**
     * 判断是否获得全部文件访问权限
     * @return  #获取权限返回true没有获得返回false
     */
    public boolean isAllFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }else {
            return false;
        }
    }
    /**
     * 申请全部文件访问权限
     * @requestCode 请求权限请求码
     * @return  #onActivityResult 中回调请判断请求码并使用isAllFilePermission检查权限
     */
    public void requestAllFilePermission(int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivityForResult(intent, requestCode);
        }
    }
    private boolean doDataOutput2(byte[] bytes ,OutputStream outputStream){
        try {
            outputStream.write( bytes,0,bytes.length);
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                outputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return false;
        }
    }
    private boolean exists(DocumentFile documentFile ,String name){
        try {
            return documentFile.findFile(name).exists();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    private DocumentFile getDocumentFile(DocumentFile documentFile,String dir){
        if (documentFile==null)return null;
        DocumentFile [] documentFiles =documentFile.listFiles();
        DocumentFile res = null;
        int i = 0 ;
        while (i<documentFile.length()){
            if(documentFiles[i].getName().equals(dir)&&documentFiles[i].isDirectory()){
                res=documentFiles[i];
                return  res;
            }
            i++;
        }
        return res;
    }
    private DocumentFile getDocumentFile1(DocumentFile documentFile,String dir){
        if (documentFile==null)return null;
        try {
            DocumentFile[] documentFiles = documentFile.listFiles();
            DocumentFile res = null;
            int i = 0;
            while (i < documentFile.length()) {
                if (documentFiles[i].getName().equals(dir) && documentFiles[i].isDirectory()) {
                    res = documentFiles[i];
                    return res;
                }
                i++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private static String textual (String str, String find, String replace) {
        if (!"".equals(find) && !"".equals(str)) {
            find = "\\Q" + find + "\\E";
            return str.replaceAll(find, replace);
        } else {
            return "";
        }
    }
}

