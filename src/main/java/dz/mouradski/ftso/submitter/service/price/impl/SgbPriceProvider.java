package dz.mouradski.ftso.submitter.service.price.impl;

import dz.mouradski.ftso.submitter.service.price.PriceProvider;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SgbPriceProvider implements PriceProvider {
    @Override
    public String getSymbol() {
        return "SGB";
    }

    @Override
    public Integer getIndex() {
        return 11;
    }

    @Override
    public Double getPrice() {
        return new Random().nextDouble();
    }
}
