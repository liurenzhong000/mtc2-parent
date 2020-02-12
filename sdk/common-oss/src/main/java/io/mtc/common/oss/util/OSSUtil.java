package io.mtc.common.oss.util;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import io.mtc.common.util.CommonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 阿里云OSS工具类
 *
 * @author Chinhin
 * 2018/7/3
 */
public class OSSUtil {

    private static final String endpoint = "oss-ap-southeast-1.aliyuncs.com";
    private static final String accessKeyId = "LTAIQDLWQ1yGUcrS";
    private static final String accessKeySecret = "mqUFcmwTpuhchzrsiUlpHK1dXu2SBY";
    private static final String bucketName = "zcd-wallet";

    public static String upload(String name, File file) {
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, name, file);
        ossClient.shutdown();
        return null;
    }

    public static List<String> list() {
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        ObjectListing objectListing = ossClient.listObjects(bucketName);
        List<OSSObjectSummary> objectSummary = objectListing.getObjectSummaries();
        ossClient.shutdown();

        List<String> files = new ArrayList<>();
        for (OSSObjectSummary temp : objectSummary) {
            // 不是文件夹, 只是图片文件夹下的
            if (temp.getSize() != 0 && Pattern.matches("image.*", temp.getKey())) {
                files.add(temp.getKey());
            }
        }
        return files;
    }

    public static void main(String[] args) {
        System.out.println(CommonUtil.toJson(list()));
    }

    /**
     * 删除文件
     * @param name 文件名（包含oss里面的文件夹路径）
     */
    public static void delete(String name) {
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        ossClient.deleteObject(bucketName, name);
        ossClient.shutdown();
    }

}
