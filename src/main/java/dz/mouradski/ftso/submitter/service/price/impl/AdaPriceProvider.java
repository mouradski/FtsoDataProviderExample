package dz.mouradski.ftso.submitter.service.price.impl;

import dz.mouradski.ftso.submitter.service.price.PriceProvider;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AdaPriceProvider implements PriceProvider {
    @Override
    public String getSymbol() {
        return "ADA";
    }

    @Override
    public Integer getIndex() {
        return 4;
    }

    @Override
    public Double getPrice() {
        return new Random().nextDouble();
    }
}
