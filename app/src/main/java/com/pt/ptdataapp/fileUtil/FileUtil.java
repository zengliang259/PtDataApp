package com.pt.ptdataapp.fileUtil;

import android.os.Environment;
import android.util.Log;

import com.pt.ptdataapp.MainActivity;
import com.pt.ptdataapp.Model.FileEntity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class FileUtil {
    private static final String TAG = "FileUtil";
    public static final int FLAG_SUCCESS = 1;//创建成功
    public static final int FLAG_EXISTS = 2;//已存在
    public static final int FLAG_FAILED = 3;//创建失败
    /**
     * 字符串保存到手机内存设备中
     *
     * @param str
     */
    public static void saveFile(String str, String fileName) {
        // 创建String对象保存文件名路径
        FileOutputStream outStream = null;
        try {
            // 创建指定路径的文件
            File file = new File(fileName);
            // 如果文件不存在
            if (file.exists()) {
                // 创建新的空文件
                file.delete();
            }
            file.createNewFile();
            // 获取文件的输出流对象
            outStream = new FileOutputStream(file);
            // 获取字符串对象的byte数组并写入文件流
            outStream.write(str.getBytes());
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e(TAG, "save file error");
        }
        finally {
            try {
                if (outStream != null)
                {
                    outStream.close();
                }
            }
          catch (Exception e)
          {
              Log.e(TAG, "save file close stream error");
          }
        }

    }
    /**
     * 删除已存储的文件
     */
    public static boolean deletefile(String fileName) {
        // 找到文件所在的路径并删除该文件
        File file = new File(fileName);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;

    }
    /**
     * 读取文件里面的内容
     *
     * @return
     */
    public static String getFile(String fileName) {
        byte[] data = null;
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            // 创建文件
            File file = new File(fileName);
            // 创建FileInputStream对象
            fis = new FileInputStream(file);
            // 创建字节数组 每次缓冲1M
            byte[] b = new byte[1024];
            int len = 0;// 一次读取1024字节大小，没有数据后返回-1.
            // 创建ByteArrayOutputStream对象
            baos = new ByteArrayOutputStream();
            // 一次读取1024个字节，然后往字符输出流中写读取的字节数
            while ((len = fis.read(b)) != -1) {
                baos.write(b, 0, len);
            }
            // 将读取的字节总数生成字节数组
            data = baos.toByteArray();
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e(TAG, "get file error");
            return null;
        }
        finally {
            try {
                if (fis != null)
                {
                    fis.close();
                }
                if (baos != null)
                {
                    baos.close();
                }
                if (data != null)
                {
                    // 返回字符串对象
                    return new String(data);
                }
                else
                {
                    return null;
                }
            }
            catch (Exception e)
            {
                Log.e(TAG, "get file close stream error");
                return null;
            }
        }

    }

    /**
     * 读取文件里面的内容
     *
     * @return
     */
    public static String getEncryptFile(String fileName) {
        byte[] data = null;
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            // 创建文件
            File file = new File(fileName);
            // 创建FileInputStream对象
            fis = new FileInputStream(file);
            // 创建字节数组 每次缓冲1M
            byte[] b = new byte[1024];
            int len = 0;// 一次读取1024字节大小，没有数据后返回-1.
            // 创建ByteArrayOutputStream对象
            baos = new ByteArrayOutputStream();
            // 一次读取1024个字节，然后往字符输出流中写读取的字节数
            while ((len = fis.read(b)) != -1) {
                baos.write(b, 0, len);
            }
            // 将读取的字节总数生成字节数组
            data = baos.toByteArray();
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e(TAG, "get encrypt file close stream error");
            return null;
        }
        finally {
            try {
                if (fis != null)
                {
                    fis.close();
                }
                if (baos != null)
                {
                    baos.close();
                }
                if (data != null)
                {
                    // 破解加密
                    byte offset = (byte)0x80;
                    for(int i = 0, dataLen = data.length;i < dataLen ;i ++)
                    {
                        data[i] = (byte)(data[i] - offset);
                    }

                    // 返回字符串对象
                    return new String(data);
                }
                else
                {
                    return null;
                }

            }
            catch (Exception e)
            {
                Log.e(TAG, "get file close stream error");
                return null;
            }
        }

    }

    public static int createDir (String dirPath) {

        File dir = new File(dirPath);
        //文件夹是否已经存在
        if (dir.exists()) {
            Log.w(TAG,"The directory [ " + dirPath + " ] has already exists");
            return FLAG_EXISTS;
        }
        if (!dirPath.endsWith(File.separator)) {//不是以 路径分隔符 "/" 结束，则添加路径分隔符 "/"
            dirPath = dirPath + File.separator;
        }
        //创建文件夹
        if (dir.mkdirs()) {
            Log.d(TAG,"create directory [ "+ dirPath + " ] success");
            return FLAG_SUCCESS;
        }

        Log.e(TAG,"create directory [ "+ dirPath + " ] failed");
        return FLAG_FAILED;
    }

    public static ArrayList<FileEntity> FindAllFile(String path, boolean includeChildDir)
    {
        ArrayList<FileEntity> fileList = new ArrayList<FileEntity>();
        File fatherFile = new File(path);
        File[] files = fatherFile.listFiles();
        if (files != null && files.length > 0)
        {
            for (int i = 0; i < files.length; i++) {
                FileEntity entity = new FileEntity();
                boolean isDirectory = files[i].isDirectory();
                if (isDirectory)
                {
                    entity.setFileType(FileEntity.Type.FLODER);
                } else {
                    entity.setFileType(FileEntity.Type.FILE);
                }
                entity.setFileName(files[i].getName().toString());
                entity.setFilePath(files[i].getAbsolutePath());
                entity.setFileSize(files[i].length() + "");
                fileList.add(entity);
                if (isDirectory && includeChildDir)
                {
                    fileList.addAll(FindAllFile(files[i].getAbsolutePath(), includeChildDir));
                }
            }
        }
        return fileList;
    }
    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                inStream = new FileInputStream(oldPath); //读入原文件
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            Log.e(TAG,"复制单个文件操作出错");
//            e.printStackTrace();

        }
        finally {
            try
            {
                if (inStream != null)
                {
                    inStream.close();
                }
                if (fs != null)
                {
                    fs.close();
                }
            }
            catch (Exception e)
            {
                Log.e(TAG,"复制单个文件操作流出错");
            }
        }

    }

    /**
     * 复制整个文件夹内容
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static void copyFolder(String oldPath, String newPath, MainActivity context) {
        if (context != null && !context.IsPtMounted)
        {
            return ;
        }
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a=new File(oldPath);
            String[] file=a.list();
            File temp=null;
            for (int i = 0; i < file.length; i++) {
                if(oldPath.endsWith(File.separator)){
                    temp=new File(oldPath+file[i]);
                }
                else{
                    temp=new File(oldPath+File.separator+file[i]);
                }
                if(temp.exists())
                {
                    if(temp.isFile()){
                        input = new FileInputStream(temp);
                        output = new FileOutputStream(newPath + "/" +
                                (temp.getName()).toString());
                        byte[] b = new byte[1024 * 5];
                        int len;
                        while ( context.IsPtMounted && (len = input.read(b)) != -1) {
                            output.write(b, 0, len);
                        }
                        output.flush();

                        input.close();
                        output.close();
                    }
                    if(temp.isDirectory()){//如果是子文件夹
                        copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i], context);
                    }
                }

            }
        }
        catch (Exception e)
        {
            Log.e(TAG,"复制整个文件夹内容操作出错");
        }
        finally {
            try {
                if (input != null)
                {
                    input.close();
                }
                if (output != null)
                {
                    output.close();
                }
            }
            catch (Exception e)
            {
                Log.e(TAG,"复制整个文件夹内容关闭流出错");
//            e.printStackTrace();
            }
        }
    }

    /**
     * 删除文件夹以及目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deletefile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }
}