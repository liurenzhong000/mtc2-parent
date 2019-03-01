package io.mtc.service.currency.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.mtc.common.jpa.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

/**
 * 代币分类
 *
 * @author Chinhin
 * 2018/8/15
 */
@Getter @Setter
@Entity
public class Category extends BaseEntity {

    @Column(columnDefinition = "varchar(100) COMMENT '分类名称'", nullable = false, unique = true)
    private String name;

    @JSONField(serialize = false)
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CreateCurrency> currencies;

}