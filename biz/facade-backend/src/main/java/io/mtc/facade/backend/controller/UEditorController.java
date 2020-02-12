package io.mtc.facade.backend.controller;

import io.mtc.common.constants.Constants;
import io.mtc.common.oss.util.FileUtil;
import io.mtc.common.oss.util.OSSUtil;
import io.mtc.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 富文本插件ueditor控制器
 *
 * @author Chinhin
 * 2018/7/3
 */
@Slf4j
@RequestMapping("/ueditor")
@RestController
public class UEditorController {

    // 文件上传路径
    @Value("${fileUploadPath}")
    private String fileUploadPath = "";

    @GetMapping(value="/config")
    public String config(HttpServletResponse response) {
        response.setContentType("application/json");
        return  "{\n" +
                "    /* 上传图片配置项 */\n" +
                "    \"imageActionName\": \"uploadimage\", /* 执行上传图片的action名称 */\n" +
                "    \"imageFieldName\": \"upfile\", /* 提交的图片表单名称 */\n" +
                "    \"imageMaxSize\": 2048000, /* 上传大小限制，单位B */\n" +
                "    \"imageAllowFiles\": [\".png\", \".jpg\", \".jpeg\", \".gif\", \".bmp\"], /* 上传图片格式显示 */\n" +
                "    \"imageCompressEnable\": true, /* 是否压缩图片,默认是true */\n" +
                "    \"imageCompressBorder\": 1600, /* 图片压缩最长边限制 */\n" +
                "    \"imageInsertAlign\": \"none\", /* 插入的图片浮动方式 */\n" +
                "    \"imageUrlPrefix\": \"\", /* 图片访问路径前缀 */\n" +
                "    \"imagePathFormat\": \"{filename}\", /* 上传保存路径,可以自定义保存路径和文件名格式 */\n" +
                "/* 列出指定目录下的图片 */\n" +
                "    \"imageManagerActionName\": \"listimage\", /* 执行图片管理的action名称 */\n" +
                "    \"imageManagerListPath\": \"/ueditor/jsp/upload/image/\", /* 指定要列出图片的目录 */\n" +
                "    \"imageManagerListSize\": 20, /* 每次列出文件数量 */\n" +
                "    \"imageManagerUrlPrefix\": \"\", /* 图片访问路径前缀 */\n" +
                "    \"imageManagerInsertAlign\": \"none\", /* 插入的图片浮动方式 */\n" +
                "    \"imageManagerAllowFiles\": [\".png\", \".jpg\", \".jpeg\", \".gif\", \".bmp\"], /* 列出的文件类型 */" +
                "}";
    }

    /**
     * 文件上传Action
     * @param req APPLICATION_JSON_VALUE
     * @return UEDITOR 需要的json格式数据
     */
    @RequestMapping("/upload")
    public Map<String,Object> upload(HttpServletRequest req){
        Map<String,Object> result = new HashMap<>();

        MultipartHttpServletRequest mReq;
        MultipartFile file;
        InputStream is;
        String fileName = "";
        // 原始文件名 UEDITOR创建页面元素时的alt和title属性
        String originalFileName;
        String filePath;

        try {
            mReq = (MultipartHttpServletRequest)req;
            // 从config.json中取得上传文件的ID
            file = mReq.getFile("upfile");
            if (file == null) {
                throw new IOException("文件未获取到");
            }
            // 取得文件的原始文件名称
            fileName = file.getOriginalFilename();
            originalFileName = fileName;

            if(StringUtil.isNotBlank(fileName)){
                is = file.getInputStream();
                fileName = FileUtil.reName(fileName);
                filePath = FileUtil.saveFile(fileName, is, "custom", fileUploadPath);
            } else {
                throw new IOException("文件名为空!");
            }

            result.put("state", "SUCCESS");// UEDITOR的规则:不为SUCCESS则显示state的内容
            result.put("url", Constants.ALI_OSS_URI + filePath);
            log.info(Constants.ALI_OSS_URI + filePath);
            result.put("title", originalFileName);
            result.put("original", originalFileName);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("state", "文件上传失败!");
            result.put("url","");
            result.put("title", "");
            result.put("original", "");
            log.error("文件 {} 上传失败!", fileName);
        }
        return result;
    }

    @RequestMapping("/list")
    public Map<String,Object> list(HttpServletRequest req){
        Map<String,Object> result = new HashMap<>();
        List<String> fileList = OSSUtil.list();
        List<Map<String, String>> list = new ArrayList<>();
        for (String temp : fileList) {
            Map<String, String> tempMap = new HashMap<>();
            tempMap.put("url", Constants.ALI_OSS_URI + temp);
            list.add(tempMap);
        }
        result.put("state", "SUCCESS");
        result.put("list", list.toArray());
        result.put( "start", 0);
        result.put( "total", list.size());
        return result;
    }

}
