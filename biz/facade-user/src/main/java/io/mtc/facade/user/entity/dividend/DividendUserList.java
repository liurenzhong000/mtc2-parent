package io.mtc.facade.user.entity.dividend;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DividendUserList implements Dividend {

    private List<DividendUser> users = new ArrayList<>();

    //能否从二级获得到分红 true = 不能从二级获取
    protected boolean notGetFromTwoChildes = false;

    //  0级持有量的balance，用来判断是分红计算的基数（和下级比，去二者中最小）
    protected BigDecimal masterBalance;

    public DividendUser addUser(DividendUser user) {
        this.users.add(user);
        return user;
    }

    public DividendUser findUser(Long id) {
        for (DividendUser item : users) {
            if (item.getUser().getId().longValue() == id) return item;
        }
        return null;
    }

    public void deleteUser(Long id) {
        for (DividendUser item : users) {
            if (item.getUser().getId().longValue() == id) users.remove(item);
        }
    }

    @Override
    public List<DividendData> getPrice(List<DividendData> dividendDataList) {
//        BigDecimal price = BigDecimal.ZERO;
        for (DividendUser userItem : this.users) {
            //传递到下级
            userItem.getChildren().notGetFromTwoChildes = notGetFromTwoChildes;
            userItem.getChildren().masterBalance = masterBalance;
            if (userItem.getLevel() == 2 && notGetFromTwoChildes) {
//                price = BigDecimal.ZERO;
            } else {
//                price = price.add(userItem.getPrice());
                userItem.getPrice(dividendDataList);
            }
        }
        return dividendDataList;
    }

    public Integer length() {
        return this.users.size();
    }
}
