package dz.mouradski.ftso.submitter.service.price;

public interface PriceProvider {

    String getSymbol();
    Integer getIndex();
    Double getPrice();
}
