package io.mtc.common.data.model;

import io.mtc.common.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

/**
 * 分页参数Model
 *
 * @author Chinhin
 * 2018/6/13
 */
@Setter @Getter
public class PagingModel implements Serializable {

    // 当前页，从0开始
    private Integer pageNumber = 0;
    // 每页条数
    private Integer pageSize = 10;
    // ASC || DESC
    private String order;
    // 要排序的参数
    private String sort;

    public Pageable make() {
        if (pageNumber == null) {
            pageNumber = 0;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        Pageable pageable;
        Sort.Direction direction = Sort.Direction.ASC;
        if ("DESC".equals(order)) {
            direction = Sort.Direction.DESC;
        }
        if (StringUtil.isBlank(sort)) {
            pageable = PageRequest.of(pageNumber, pageSize);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize, direction, sort);
        }
        return pageable;
    }

}
