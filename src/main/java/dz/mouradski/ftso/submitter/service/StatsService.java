package dz.mouradski.ftso.submitter.service;

import dz.mouradski.ftso.submitter.model.PriceEpochData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class StatsService {

    private Web3j web3;

    private DataEpochService dataEpochService;

    private DataProvider dataProvider;

    public StatsService(@Autowired Web3j web3,
                        @Autowired DataEpochService dataEpochService,
                        @Autowired DataProvider dataProvider) {
        this.web3 = web3;
        this.dataEpochService = dataEpochService;
        this.dataProvider = dataProvider;
    }


    @PostConstruct
    public void init() throws ExecutionException, InterruptedException {
        loadFtsoContracts();
    }

    public void loadFtsoContracts() throws ExecutionException, InterruptedException {

        TypeReference<DynamicArray<Address>> oa = new TypeReference<DynamicArray<Address>>() {
        };

        Function function = new Function(
                "getFtsos",
                Arrays.asList(),
                Arrays.asList(oa));

        String encodedFunction = FunctionEncoder.encode(function);

        org.web3j.protocol.core.methods.response.EthCall response =
                web3.ethCall(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(null, "0xbfA12e4E1411B62EdA8B035d71735667422A6A9e", encodedFunction), DefaultBlockParameterName.LATEST)
                        .sendAsync().get();

        List<Type> someTypes = FunctionReturnDecoder.decode(
                response.getValue(), function.getOutputParameters());


        ArrayList contracts =  ((ArrayList)someTypes.get(0).getValue());

        Integer index = 0;

        for (Object contractObj : contracts) {

            String contract = contractObj.toString();

            int i = index.intValue();


            List<TypeReference<?>> params = Arrays.asList(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Bool>(true) {},
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Uint8>(true) {},
                    new TypeReference<Uint256>(true) {});


            Event event = new Event("PriceFinalized", params);


            EthFilter filter = new EthFilter(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST, contract);
            filter.addSingleTopic(EventEncoder.encode(event));

            web3.ethLogFlowable(filter).subscribe(log -> {
                try {

                    String symbol = dataProvider.getSymbol(i);

                    List<Type> eventParams = FunctionReturnDecoder.decode(
                            log.getData(), event.getIndexedParameters().subList(0,6));


                    BigInteger min = (BigInteger) eventParams.get(0).getValue();
                    BigInteger max = (BigInteger) eventParams.get(3).getValue();

                    PriceEpochData priceEpochData = dataEpochService.getCurrentPriceEpochData();

                    BigInteger previousEpochPrice = dataProvider.getPreviousEpochPrice(i);

                    if (previousEpochPrice != null) {
                        this.log.info("PriceFinalized epochId : {}, Symbol : {}, minPrice : {}, maxPrice : {}, ourPrice : {}", priceEpochData.getPriceEpochId() - 1, dataProvider.getSymbol(i), min, max, previousEpochPrice);

                        if (previousEpochPrice.doubleValue() >= min.doubleValue() && previousEpochPrice.doubleValue() <= max.doubleValue()) {
                            this.log.info("Bingo !! {} = {}", dataProvider.getSymbol(i), previousEpochPrice);
                        }
                    } else {
                        this.log.info("PriceFinalized epochId : {}, Symbol : {}, minPrice : {}, maxPrice : {}", priceEpochData.getPriceEpochId() - 1, dataProvider.getSymbol(i), min, max);

                    }
                } catch (Exception e) {
                    this.log.error("Erreur when parsing PriceFinalized event", e);
                }
            });

            index++;
        }
    }
}
