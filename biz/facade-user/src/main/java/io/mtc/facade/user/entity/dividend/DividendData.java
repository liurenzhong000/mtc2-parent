package io.mtc.facade.user.entity.dividend;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: hyp
 * @Date: 2019/3/12 09:18
 * @Description: 分红用户统计
 */
@Setter @Getter
public class DividendData {

    /**第几级*/
    private Integer level;

    /**快照持有平均量(用于计算分红)*/
    private BigDecimal balance;

    /**当前级别用户的平均持有量（用于记录）*/
    private BigDecimal levelBalance;

    /**当前级别的下级所获得的收益*/
    private BigDecimal price;

    /**用户id*/
    private Long userId;

    public DividendData(Integer level, BigDecimal balance, BigDecimal levelBalance, BigDecimal price, Long userId){
        this.level = level;
        this.balance = balance;
        this.levelBalance = levelBalance;
        this.price = price;
        this.userId = userId;
    }

    public static List<DividendData> listByLevel(List<DividendData> list, Integer level){
        if (list == null || list.size() <= 0) {
            return Lists.newArrayList();
        }
        return list.stream().filter(item -> item.getLevel() == level).collect(Collectors.toList());
    }

    public static String joinUserIdsFromList(List<DividendData> list){
        return list.stream().map(item -> item.getUserId().toString()).reduce("", (a,b) -> {
            if (a.equals("")){
                return "" + b;
            }
            return a +"," +b;
        });
    }

    public static BigDecimal countPriceFromList(List<DividendData> list){
        return list.stream().map(item -> item.getPrice()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
