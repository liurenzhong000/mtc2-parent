package io.mtc.common.jpa.util;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.List;

/**
 * 检索工具类
 *
 * @author Chinhin
 * 2018/6/13
 */
public class CriteriaUtil {

    public static <T> Predicate result(List<T> list, CriteriaBuilder criteriaBuilder) {
        Predicate[] p = new Predicate[list.size()];
        return criteriaBuilder.and(list.toArray(p));
    }

}
