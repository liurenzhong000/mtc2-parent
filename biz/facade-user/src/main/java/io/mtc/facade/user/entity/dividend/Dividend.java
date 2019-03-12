package io.mtc.facade.user.entity.dividend;

import java.util.List;

public interface Dividend {

    List<DividendData> getPrice(List<DividendData> dividendDataList);

}
