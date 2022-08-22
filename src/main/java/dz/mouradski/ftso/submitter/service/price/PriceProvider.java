package dz.mouradski.ftso.submitter.service.price;

import java.util.Random;

public interface PriceProvider {

    String getSymbol();
    Integer getIndex();
    Double getPrice();
}
