package com.android11dome;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;

import androidx.documentfile.provider.DocumentFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *dataTools 提供一个对android11 Android/data目录下非自身应用文件的一个操作方案
 * by 若忧愁
 * qq 2557594045
 *
 */
class dataTools {
    Activity context ;//内部操作Activity对象
    int requestCode=11;//请求标识
    /**
     * 构造方法
     * @context # Activity对象
     * @requestCode  #请求码
     */
    public  dataTools(Activity context,int requestCode) {
       this.context=context;
       this.requestCode=requestCode;
    }
    /**
     *申请data访问权限请在onActivityResult事件中调用savePermissions方法保存权限
     */
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
     * @return #返回一个boolean true有权限 false 无权限
     */
    public boolean isPermissions() {
        Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
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
               // byte[] buffer = new byte[inStream.available()];
                Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata" );
                DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
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
                InputStream inStream = new FileInputStream(sourcePath);
                // byte[] buffer = new byte[inStream.available()];
                Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata" );
                DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
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
                Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata" );
                DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
                String[] list=dir.split("/");
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
               return documentFile;
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
     * 将Android/data中的文件拷贝至sdcard
     * @sourceDir #文件原目录以data开始 如拷贝data/test/目录中的文件 那就是 /test
     * @sourceFilename #拷贝的文件名 如拷贝 data/test/1.txt 那就是1.txt
     * @targetPath #目标文件路径需提供完整的路径目录+文件名
     * @return #返回一个boolean true成功 false 失败
     */
    public boolean copyToSdcard(String sourceDir,String sourceFilename, String targetPath) {
        try {
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            String[] list = sourceDir.split("/");
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
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            String[] list = dir.split("/");
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
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            String[] list = dir.split("/");
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
     * 获取目录下所有文件返回文本型数组
     * @dir  #文件目录 目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @return #返回一个文本数组为该目录下所有的文件名
     */
    public String [] getList(String dir) {
        try {
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            String[] list = dir.split("/");
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
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            String[] list = dir.split("/");
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
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            String[] list = dir.split("/");
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
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata" );
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context, uri1);
            String[] list = dir.split("/");
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
            Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
            DocumentFile documentFile = DocumentFile.fromTreeUri(this.context,uri1);
            String[] list = dir.split("/");
            int i=0;
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
                    Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
                    DocumentFile documentFile = DocumentFile.fromTreeUri(context,uri1);
                    String[] list = dir.split("/");
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
