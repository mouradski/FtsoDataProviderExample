package dz.mouradski.ftso.submitter.service.price.impl;

import dz.mouradski.ftso.submitter.service.price.PriceProvider;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class DogePriceProvider implements PriceProvider {
    @Override
    public String getSymbol() {
        return "DOGE";
    }

    @Override
    public Integer getIndex() {
        return 3;
    }

    @Override
    public Double getPrice() {
        return new Random().nextDouble();
    }
}
