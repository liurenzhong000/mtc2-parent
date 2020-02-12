package io.mtc.facade.backend.feignController;

import io.mtc.common.data.model.PagingModel;
import io.mtc.common.util.CommonUtil;
import io.mtc.facade.backend.feign.ServiceNotification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 推送模板控制器
 *
 * @author Chinhin
 * 2018/7/16
 */
@RestController
@RequestMapping("/notificationTemplate")
public class NotificationTemplateController {

    @Resource
    private ServiceNotification serviceNotification;

    @PreAuthorize("hasAuthority('notifyTemplate:select')")
    @GetMapping
    public String select(String title, @ModelAttribute PagingModel pageModel) {
        return serviceNotification.selectTemplate(title, CommonUtil.toJson(pageModel));
    }

    @PreAuthorize("hasAuthority('notifyTemplate:insert')")
    @PutMapping
    public String insert(String templateJson) {
        return serviceNotification.insertTemplate(templateJson);
    }

    @PreAuthorize("hasAuthority('notifyTemplate:update')")
    @PostMapping
    public String update(String templateJson) {
        return serviceNotification.updateTemplate(templateJson);
    }

    @PreAuthorize("hasAuthority('notifyTemplate:delete')")
    @DeleteMapping("/{id}")
    public String del(@PathVariable Long id) {
        return serviceNotification.delTemplate(id);
    }

    @PreAuthorize("hasAuthority('notifyTemplate:update')")
    @PostMapping("/push/{id}")
    public String push(@PathVariable Long id) {
        return serviceNotification.pushTemplate(id);
    }

}
