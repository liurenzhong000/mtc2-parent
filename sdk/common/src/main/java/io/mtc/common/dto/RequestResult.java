package io.mtc.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 服务间相互调用的结果
 *
 * @author Chinhin
 * 2019-01-17
 */
@AllArgsConstructor
@Getter @Setter
public class RequestResult implements Serializable {

    // 调用是否成功
    private Boolean isSuccess;

    // 异常的消息
    private String errorInfo;

    // 附加信息
    private Object additionInfo;

}
