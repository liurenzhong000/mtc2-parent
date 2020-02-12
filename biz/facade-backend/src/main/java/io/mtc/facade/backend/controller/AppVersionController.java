package io.mtc.facade.backend.controller;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.data.util.PagingResultUtil;
import io.mtc.common.jpa.util.CriteriaUtil;
import io.mtc.common.util.DateUtil;
import io.mtc.common.util.ResultUtil;
import io.mtc.facade.backend.entity.AppVersion;
import io.mtc.facade.backend.repository.AppVersionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * app版本管理控制器
 *
 * @author Chinhin
 * 2018/7/5
 */
@RequestMapping("/appVersion")
@RestController
public class AppVersionController {

    @Resource
    private AppVersionRepository appVersionRepository;

    @PreAuthorize("hasAuthority('appVersion:select')")
    @GetMapping
    public String select(Boolean isActive, Boolean isAndroid, @ModelAttribute PagingModel pageModel) {
        Specification<AppVersion> specification = (Specification<AppVersion>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            if (isActive != null) {
                list.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }
            if (isAndroid != null) {
                list.add(criteriaBuilder.equal(root.get("isAndroid"), isAndroid));
            }
            return CriteriaUtil.result(list, criteriaBuilder);
        };
        Page<AppVersion> all = appVersionRepository.findAll(specification, pageModel.make());
        return PagingResultUtil.list(all);
    }

    @PreAuthorize("hasAuthority('appVersion:insert')")
    @PutMapping
    @Transactional
    public String insert(String versionNumber, String versionName, String description, String url,
                         String descriptionEn, Boolean isAndroid) {
        AppVersion appVersion = new AppVersion();
        appVersion.setVersionName(versionName);
        appVersion.setVersionNumber(versionNumber);
        appVersion.setDescription(description);
        appVersion.setDescriptionEn(descriptionEn);
        appVersion.setUrl(url);
        appVersion.setIsAndroid(isAndroid);
        appVersionRepository.save(appVersion);
        return ResultUtil.success(appVersion);
    }

    @PreAuthorize("hasAuthority('appVersion:delete')")
    @Transactional
    @DeleteMapping("/{id}")
    public String del(@PathVariable Long id) {
        appVersionRepository.deleteById(id);
        return ResultUtil.success("删除成功");
    }

    @PreAuthorize("hasAuthority('appVersion:update')")
    @Transactional
    @PostMapping
    public String update(Long id, String versionNumber, String versionName, String description,
                         String descriptionEn, String url, Boolean isAndroid) {
        if (isAndroid == null) {
            isAndroid = false;
        }
        AppVersion appVersion = appVersionRepository.findById(id).orElse(null);
        // 更换了平台
        if (isAndroid != appVersion.getIsAndroid()) {
            appVersion.setIsActive(false);
        }
        appVersion.setIsAndroid(isAndroid);
        appVersion.setUrl(url);
        appVersion.setDescription(description);
        appVersion.setVersionNumber(versionNumber);
        appVersion.setVersionName(versionName);
        appVersion.setDescriptionEn(descriptionEn);

        appVersionRepository.save(appVersion);
        return ResultUtil.success(appVersion);
    }

    @PreAuthorize("hasAuthority('appVersion:update')")
    @Transactional
    @PostMapping("/changeActive/{id}")
    public String changeActive(@PathVariable Long id) {
        AppVersion appVersion = appVersionRepository.findById(id).orElse(null);
        appVersionRepository.setAllNotActiveWithPlatform(appVersion.getIsAndroid());
        appVersion.setIsActive(!appVersion.getIsActive());
        appVersionRepository.save(appVersion);
        return ResultUtil.success(appVersion);
    }

    @GetMapping("/info")
    public Object versionInfo() {
        Map<String, Object> result = new HashMap<>();

        AppVersion androidActiveVersion = appVersionRepository.findByIsActiveAndIsAndroid(true, true);
        Map<String, Object> androidResult = makeInfo(androidActiveVersion);

        AppVersion iosActiveVersion = appVersionRepository.findByIsActiveAndIsAndroid(true, false);
        Map<String, Object> iosResult = makeInfo(iosActiveVersion);

        result.put("androidInfo", androidResult);
        result.put("iosInfo", iosResult);
        return ResultUtil.successObj(result);
    }

    /**
     * 根据数据库实例生成info信息
     * @param info 数据库实例
     * @return 结果
     */
    private Map<String, Object> makeInfo(AppVersion info) {
        Map<String, Object> result = new HashMap<>();
        if (info != null) {
            result.put("createTime", DateUtil.formatDate(info.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            result.put("updateTime", DateUtil.formatDate(info.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            result.put("versionNumber", info.getVersionNumber());
            result.put("versionName", info.getVersionName());
            result.put("description", info.getDescription());
            result.put("descriptionEn", info.getDescriptionEn());
            result.put("url", info.getUrl());
        }
        return result;
    }

}
