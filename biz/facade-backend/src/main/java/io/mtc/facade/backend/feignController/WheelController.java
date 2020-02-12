package io.mtc.facade.backend.feignController;

import io.mtc.common.constants.Constants;
import io.mtc.common.constants.MTCError;
import io.mtc.common.data.model.PagingModel;
import io.mtc.common.oss.util.FileUtil;
import io.mtc.common.util.CommonUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.common.util.StringUtil;
import io.mtc.facade.backend.feign.FacadeUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

/**
 * 转盘控制器
 *
 * @author Chinhin
 * 2018/10/11
 */
@RestController
@RequestMapping("/wheel")
public class WheelController {

    @Resource
    private FacadeUser facadeUser;

    /**
     * 转盘设置
     */
    @PreAuthorize("hasAuthority('wheel:update')")
    @GetMapping("/prize")
    public String getLoanConfig() {
        return facadeUser.wheelPrizes();
    }

    @PreAuthorize("hasAuthority('wheel:update')")
    @PostMapping("/prize")
    public String wheelUpdatePrize(String json) {
        return facadeUser.wheelUpdatePrize(json);
    }

    @GetMapping("/record")
    public String record(String prizeName, String startTime, String endTime, Integer status,
                         Integer selectUserType, String userInfo, @ModelAttribute PagingModel pageModel) {
        return facadeUser.wheelRecord(startTime, endTime, status, prizeName, selectUserType, userInfo, CommonUtil.toJson(pageModel));
    }

    @PostMapping("/record")
    public String updateStatus(Long recordId, Integer status) {
        return facadeUser.updateStatus(recordId, status);
    }

    @PostMapping("/upload")
    public Object upload(@RequestParam("file") MultipartFile file) {
        InputStream is;
        String fileName;
        String filePath;
        try {
            if (file == null) {
                throw new IOException("文件未获取到");
            }
            // 取得文件的原始文件名称
            fileName = file.getOriginalFilename();

            if(StringUtil.isNotBlank(fileName)){
                is = file.getInputStream();
                fileName = FileUtil.reName(fileName);
                filePath = FileUtil.saveFile(fileName, is, "wheel_prize", Constants.EMPTY);
            } else {
                throw new IOException("文件名为空");
            }
            return ResultUtil.successObj(Constants.ALI_OSS_URI + filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.errorObj(MTCError.UPLOAD_ERROR);
        }
    }
}
