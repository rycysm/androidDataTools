package com.android11dome;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *dataTools 提供一个对android11 Android/data目录下非自身应用文件的一个操作方案
 * by 若忧愁
 * qq 2557594045
 *
 */
public class dataTools {
    Activity context ;//内部操作Activity对象
    int requestCode=11;//请求标识
    /**
     * 构造方法
     * @context # Activity对象
     * @requestCode  #请求码
     */
    public dataTools(Activity context, int requestCode) {
        this.context=context;
        this.requestCode=requestCode;
    }

    /**
     * 判断是否已经获取到data权限
     * @return #true为有权限,false为无权限
     */
    public  boolean isPermissions() {
        List<UriPermission> persistedUriPermissions = context.getContentResolver().getPersistedUriPermissions();
        for(UriPermission persistedUriPermission:persistedUriPermissions){
            if (persistedUriPermission.isReadPermission() && persistedUriPermission.getUri().toString().equals("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata") ) {
                return true;
            }
        }
        return false;
    }


    /**
     *申请data访问权限请在onActivityResult事件中调用savePermissions方法保存权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void requestPermission() {
        Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
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
     * 将byte[] 写出到data目录的文件中如果没有这个文件会自动创建目录及文件
     * @Dir  #写出的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #写出的文件名
     * @return #将在asyncRead接口中的onRead中返回数据和传入时的taskId
     */
    public void asyncRead(final String dir , final String fileName, final int taskId, final AsyncRead asyncRead) {
        new Thread(new Runnable() {//保留java1.7的写法方便工程移值
            @Override
            public void run() {
                byte[] buffer = null;
                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                InputStream inputStream = null;
                try {
                    String s= getPathUri2(dir.replaceAll("/","%2F"), context);
                    DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s+"%2F"+fileName));
                    if(documentFile==null||!documentFile.exists()){
                        asyncRead.onRead(null,taskId);
                        return;
                    }
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
                final byte[] finalBuffer = buffer;
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
        void onRead(byte[] data, int taskId);
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

    /**
     * 保存权限onActivityResult返回的参数全部传入即可
     * @requestCode #onActivityResult
     * @resultCode  #onActivityResult
     * @data #onActivityResult
     */
    @SuppressLint("WrongConstant")
    public int savePermissions(int requestCode, int resultCode, Intent data) {
        if (this.requestCode!=requestCode)return 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Uri uri = data.getData();
                if (uri==null)return 1;
                this.context.getContentResolver().takePersistableUriPermission(uri,data.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                return 2;
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
        }
        return -1;
    }



    /**
     * 将sdcard中的文件拷贝至data目录中
     * @sourcePath #sdcard中的完整文件路径
     * @targetDir  #拷贝至的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @targetName #目标文件名
     * @fileType 文件类型可以留空
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean copyToData(String sourcePath, String targetDir ,String targetName,String fileType) {
        targetDir=textual(targetDir,targetName,"");
        if ((new File(sourcePath)).exists()) {
            try {
                InputStream inStream = new FileInputStream(sourcePath);
                byte[] buffer = new byte[inStream.available()];
                String s= getPathUri(targetDir.replaceAll("/","%2F"), this.context);
                DocumentFile destDocumentFile =DocumentFile.fromTreeUri(this.context, Uri.parse(s+"%2F"+targetName));
                if (destDocumentFile != null && destDocumentFile.exists()) {
                    destDocumentFile.delete();
                }
                DocumentFile documentFile2=DocumentFile.fromTreeUri(this.context, Uri.parse(s));
                if(documentFile2!=null){
                    documentFile2.createFile(fileType, targetName);
                }else{
                    return false;
                }
                OutputStream excelOutputStream = this.context.getContentResolver().openOutputStream(Uri.parse(s+"%2F"+targetName));
                int byteread;
                while ((byteread = inStream.read(buffer)) != -1) {
                    excelOutputStream.write(buffer, 0, byteread);
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
     * 将Android/data中的文件拷贝至sdcard
     * @sourceDir #文件原目录以data开始 如拷贝data/test/目录中的文件 那就是 /test
     * @sourceFilename #拷贝的文件名 如拷贝 data/test/1.txt 那就是1.txt
     * @targetPath #目标文件路径需提供完整的路径目录+文件名
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean copyToSdcard(String sourceDir,String sourceFilename, String targetPath) {
        try {
            String s= getPathUri2(sourceDir.replaceAll("/","%2F"), context);
            DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s+"%2F"+sourceFilename));
            if(documentFile==null||!documentFile.exists()){
                return false;
            }
            InputStream   inputStream = this.context.getContentResolver().openInputStream(documentFile.getUri());
            byte[]  buffer=new byte[inputStream.available()];
            FileOutputStream fs = new FileOutputStream(targetPath);
            int byteread;
            while ((byteread = inputStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
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
     * 删除data目录中的指定路径的文件或目录
     * @dir  #删除文件的目录目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #目标文件名
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean delete(String dir,String fileName) {
        try {
            String s= getPathUri2(dir.replaceAll("/","%2F"), context);
            DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s+"%2F"+fileName));
            if(documentFile==null||!documentFile.exists()){
                return false;
            }
            return documentFile.delete();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 文件是否存在
     * @dir  #data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #目标文件名
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean isFileExists(String dir,String fileName) {
        try {
            String s= getPathUri2(dir.replaceAll("/","%2F"), context);
            DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s+"%2F"+fileName));
            if(documentFile==null||!documentFile.exists()||!documentFile.isFile()){
                return false;
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 目录是否存在
     * @dir  #删除文件的目录目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #目标目录名
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean isDirExists(String dir,String fileName) {
        try {
            String s= getPathUri2(dir.replaceAll("/","%2F"), context);
            DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s+"%2F"+fileName));
            if(documentFile==null||!documentFile.exists()||!documentFile.isDirectory()){
                return false;
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建目录
     * @dir  #创建目录的根目录目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @dirname #目录名
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean createDirectory(String dir,String dirname) {
        try {
            String s= getPathUri(dir.replaceAll("/","%2F"), context);
            DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s));
            documentFile.createDirectory(dirname);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 重命名文件或目录
     * @dir  #重命名文件目录 目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #目标文件名
     * @targetName #重命名后的文件名
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean renameTo(String dir,String fileName,String targetName) {
        try {
            String s= getPathUri2(dir.replaceAll("/","%2F"), context);
            DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s+"%2F"+fileName));
            if(documentFile==null||!documentFile.exists()){
                return false;
            }
            return documentFile.renameTo(targetName);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 获取目录下所有文件返回文本型二维数组
     * @dir  #文件目录 目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @return #返回一个文本数组为该目录下所有的文件名
     */
    public String[][] getList2(String dir) {
        try {
            String s= getPathUri2(dir.replaceAll("/","%2F"), context);
            DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s));
            if(documentFile==null||!documentFile.exists()){
                return new String[0][0];
            }
            DocumentFile[] documentFile1 = documentFile.listFiles();
            String[] res = new String[documentFile1.length];
            String[] res1 = new String[documentFile1.length];
            int i1 =0;
            while (i1<documentFile1.length){
                res[i1]=documentFile1[i1].getName();
                if (documentFile1[i1].isDirectory()){
                    res1[i1]="1";
                }else {
                    res1[i1]="0";
                }
                i1++;
            }
            return new String[][]{res,res1};
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 搜索后缀名
     * @dir  #文件目录 目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @tr #后缀名
     * @return #返回一个文本数组
     */
    public String[] search(String dir,String tr) {
        try {
            String s= getPathUri2(dir.replaceAll("/","%2F"), context);
            DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s));
            if(documentFile==null||!documentFile.exists()){
                return new String[0];
            }
            DocumentFile[] documentFile1 = documentFile.listFiles();
            List<String> stringList= new ArrayList<>();
            int i1 =0;
            while (i1<documentFile1.length){
                if (documentFile1[i1].isFile()){
                    String name = documentFile1[i1].getName();
                    if(getStrRight(name,tr.length()).equals(tr)){
                        stringList.add(name);
                    }
                }
                i1++;
            }
            String[] strings= new String[stringList.size()];
            for(int i=0;i<stringList.size();i++){
                strings[i]=stringList.get(i);
            }
            return strings;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 搜索文件名关键词
     * @dir  #文件目录 目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @tr #文件关键词
     * @return #返回一个文本数组
     */
    public String[] search2(String dir,String tr) {
        try {
            String s= getPathUri2(dir.replaceAll("/","%2F"), context);
            DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s));
            if(documentFile==null||!documentFile.exists()){
                return new String[0];
            }
            DocumentFile[] documentFile1 = documentFile.listFiles();
            List<String> stringList= new ArrayList<>();
            int i1 =0;
            while (i1<documentFile1.length){
                if (documentFile1[i1].isFile()){
                    String name = documentFile1[i1].getName();
                    if(name.contains(tr)){
                        stringList.add(name);
                    }
                }
                i1++;
            }
            String[] strings= new String[stringList.size()];
            for(int i=0;i<stringList.size();i++){
                strings[i]=stringList.get(i);
            }
            return strings;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取目录下所有文件返回文本型数组
     * @dir  #文件目录 目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @return #返回一个文本数组为该目录下所有的文件名
     */
    public String [] getList(String dir) {
        try {
            String s= getPathUri2(dir.replaceAll("/","%2F"), context);
            DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s));
            if(documentFile==null||!documentFile.exists()){
                return new String[0];
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
     * 将byte[] 写出到data目录的文件中如果没有这个文件会自动创建目录及文件
     * @Dir  #写出的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #写出的文件名
     * @fileType 文件类型,可以留空
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean write(String dir,String fileName,String fileType,byte[] bytes) {
        try {
            String s= getPathUri(dir.replaceAll("/","%2F"), context);
            DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s+"%2F"+fileName));
            if (documentFile != null && documentFile.exists()) {
                documentFile.delete();
            }
            DocumentFile documentFile2=DocumentFile.fromTreeUri(this.context, Uri.parse(s));
            if(documentFile2!=null){
                documentFile2.createFile(fileType, fileName);
            }else{
                return false;
            }
            OutputStream excelOutputStream = this.context.getContentResolver().openOutputStream(Uri.parse(s+"%2F"+fileName));
            return doDataOutput2(bytes, excelOutputStream);
        } catch (Exception var5) {
            var5.printStackTrace();
            return false;
        }
    }

    /**
     * 读取data中的文件为byte[]
     * @Dir  #写出的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #写出的文件名
     * @return #返回一个byte[] 如文件为空或者不存在此返回可能为null请判断后使用
     */
    public byte[] read(String dir ,String fileName) {
        byte[] buffer = null;
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try {
            String s= getPathUri2(dir.replaceAll("/","%2F"), context);
            DocumentFile documentFile =DocumentFile.fromTreeUri(context, Uri.parse(s+"%2F"+fileName));
            if (documentFile == null || !documentFile.exists()) {
                return null;
            }
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
        return buffer;
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

    /**
     * 拼接URI
     * @param packageName 包名
     * @param context 上下文对象
     * @return 返回拼接好的路径
     */
    private static String getPathUri(String packageName , Context context )  {
        String dataPath = packageName;
        String path = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata%2F"+dataPath;
        Uri uri = Uri.parse(path);
        DocumentFile destDocumentFile = DocumentFile.fromTreeUri(context, uri);
        if(destDocumentFile == null || !destDocumentFile.exists()){
            createDirs(dataPath, context);
        }
        return path;
    }

    /**
     * 拼接URI2
     * @param packageName 包名
     * @param context 上下文对象
     * @return 返回拼接好的路径
     */
    private static String getPathUri2(String packageName , Context context )  {
        String dataPath = packageName;
        String path = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata%2F"+dataPath;
        return path;
    }

    private  static void createDirs(String dataPath , Context context ) {
        String mainPath = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata";
        String[] stringList = dataPath.split("%2F");
        String tempPath = "";
        String lastPath = mainPath;
        for (String s : stringList) {
            tempPath = "%2F" + s;
            DocumentFile destDocumentFile = DocumentFile.fromTreeUri(context, Uri.parse(mainPath + tempPath));
            if (destDocumentFile == null || !destDocumentFile.exists()) {
                DocumentFile documentFile = DocumentFile.fromTreeUri(context, Uri.parse(lastPath));
                if (documentFile != null) {
                    documentFile.createDirectory(s);
                }
            }
            lastPath = lastPath + "%2F" + s;
        }
    }

    private static String getStrRight(String str, int len) {
        if (!"".equals(str) && len > 0) {
            if (len > str.length()) {
                return str;
            } else {
                int start = str.length() - len;
                return str.substring(start, str.length());
            }
        } else {
            return "";
        }
    }

}
