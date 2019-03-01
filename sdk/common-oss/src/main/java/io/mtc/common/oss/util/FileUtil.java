package io.mtc.common.oss.util;

import io.mtc.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Date;

/**
 * 文件工具类
 *
 * @author Chinhin
 * 2018/7/3
 */
@Slf4j
public class FileUtil {

    /**
     * 文件copy方法
     */
    public static void copy(InputStream src, OutputStream dest) {
        try {
            byte[] tmp = new byte[1024];
            int len;
            while ((len = src.read(tmp)) != -1)
                dest.write(tmp, 0, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 给文件重命名 防止覆盖
     */
    public static String reName(String fileName){
        StringBuffer sb = new StringBuffer();
        sb.append(new Date().getTime());
        sb.append(StringUtil.getRandomString(4));
        sb.append(fileName.substring(fileName.indexOf(".")));
        return sb.toString();
    }

    /**
     * 文件保存
     * @param fileName reName之后的文件名称
     * @param content InputStream
     * @param folder 文件夹
     * @param systemPath 系统保存路径
     * @return 文件路径
     */
    public static String saveFile(String fileName, InputStream content, String folder, String systemPath) throws IOException {
        FileOutputStream fos = null;
        StringBuffer contentPath =  new StringBuffer();
        try {
            contentPath.append("image/");
            contentPath.append(folder);
            contentPath.append("/");
            contentPath.append(fileName);

            File pictureFile = new File(systemPath + contentPath.toString());
            File pf = pictureFile.getParentFile();
            if(!pf.exists()){
                pf.mkdirs();
            }
            pictureFile.createNewFile();    // 创建文件
            fos = new FileOutputStream(pictureFile);
            copy(content, fos);

            OSSUtil.upload(contentPath.toString(), pictureFile);

            boolean delete = pictureFile.delete();
            if (!delete) {
                log.error("删除暂存文件失败");
            }
        } catch (Exception e) {
            throw new IOException("文件保存失败!");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    throw new IOException("文件保存失败!");
                }
            }
        }
        return contentPath.toString();
    }

}
