package dz.mouradski.ftso.submitter.service.price.impl;

import dz.mouradski.ftso.submitter.service.price.PriceProvider;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class EthPriceProvider implements PriceProvider {
    @Override
    public String getSymbol() {
        return "ETH";
    }

    @Override
    public Integer getIndex() {
        return 9;
    }

    @Override
    public Double getPrice() {
        return new Random().nextDouble();
    }
}
