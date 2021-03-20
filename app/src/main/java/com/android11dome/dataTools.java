package com.android11dome;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
        if (requestCode!=resultCode)return;
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
     * 将sdcard中的文件拷贝至data目录中
     * @sourcePath #sdcard中的完整文件路径
     * @targetDir  #拷贝至的文件目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @targetName #目标文件名
     * @fileType 目录文件类型 如txt文件 application/txt
     */
    public  boolean copyToData(String sourcePath, String targetDir ,String targetName , String fileType) {
        targetDir=textual(targetDir,targetName,"");
        if ((new File(sourcePath)).exists()) {
            try {
                int bytesum = 0;
                InputStream inStream = new FileInputStream(sourcePath);
                byte[] buffer = new byte[inStream.available()];
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
                int byteread;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
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
     * 删除data目录中的指定路径的文件
     * @dir  #删除文件的目录目录以data开始 如拷贝至data/test/目录 那就是 /test
     * @fileName #目标文件名
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
    private DocumentFile getDocumentFile1(DocumentFile documentFile,String 目录){
        if (documentFile==null)return null;
        try {
            DocumentFile[] documentFiles = documentFile.listFiles();
            DocumentFile res = null;
            int i = 0;
            while (i < documentFile.length()) {
                if (documentFiles[i].getName().equals(目录) && documentFiles[i].isDirectory()) {
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
