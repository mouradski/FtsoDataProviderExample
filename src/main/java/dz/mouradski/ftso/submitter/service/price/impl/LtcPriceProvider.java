package dz.mouradski.ftso.submitter.service.price.impl;

import dz.mouradski.ftso.submitter.service.price.PriceProvider;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class LtcPriceProvider implements PriceProvider {
    @Override
    public String getSymbol() {
        return "LTC";
    }

    @Override
    public Integer getIndex() {
        return 1;
    }

    @Override
    public Double getPrice() {
        return new Random().nextDouble();
    }
}
