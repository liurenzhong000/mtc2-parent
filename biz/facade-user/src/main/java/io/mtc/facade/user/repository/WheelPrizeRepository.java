package io.mtc.facade.user.repository;

import io.mtc.facade.user.entity.wheel.WheelPrize;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author Chinhin
 * 2018/12/13
 */
public interface WheelPrizeRepository extends PagingAndSortingRepository<WheelPrize, Long> {

}
