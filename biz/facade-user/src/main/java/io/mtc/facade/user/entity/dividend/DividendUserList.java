package io.mtc.facade.user.entity.dividend;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DividendUserList implements Dividend {

    private List<DividendUser> users = new ArrayList<>();

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
    public BigDecimal getPrice() {
        BigDecimal price = BigDecimal.ZERO;
        for (DividendUser userItem : this.users) {
            price = price.add(userItem.getPrice());
        }
        return price;
    }

    public Integer length() {
        return this.users.size();
    }
}
