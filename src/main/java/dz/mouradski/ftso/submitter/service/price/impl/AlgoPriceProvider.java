package dz.mouradski.ftso.submitter.service.price.impl;

import dz.mouradski.ftso.submitter.service.price.PriceProvider;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AlgoPriceProvider implements PriceProvider {
    @Override
    public String getSymbol() {
        return "ALGO";
    }

    @Override
    public Integer getIndex() {
        return 5;
    }

    @Override
    public Double getPrice() {
        return new Random().nextDouble();
    }
}
