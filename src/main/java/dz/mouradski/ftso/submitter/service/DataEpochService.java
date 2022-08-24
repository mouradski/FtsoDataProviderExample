package dz.mouradski.ftso.submitter.service;

import dz.mouradski.ftso.submitter.model.PriceEpochData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class DataEpochService {

    private Web3j web3;
    private String priceSubmitterContractAddress;


    public DataEpochService(@Autowired Web3j web3, @Value("${price.submitter.contract}") String priceSubmitterContractAddress) {
        this.web3 = web3;
        this.priceSubmitterContractAddress = priceSubmitterContractAddress;
    }


    public String getFtsoManager() throws ExecutionException, InterruptedException {
        org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                "getFtsoManager",
                Arrays.asList(),
                Arrays.asList(new TypeReference<Address>() {
                }));

        String encodedFunction = FunctionEncoder.encode(function);

        org.web3j.protocol.core.methods.response.EthCall response =
                web3.ethCall(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(null,
                                priceSubmitterContractAddress, encodedFunction), DefaultBlockParameterName.LATEST)
                        .sendAsync().get();

        List<Type> someTypes = FunctionReturnDecoder.decode(
                response.getValue(), function.getOutputParameters());


        return someTypes.get(0).toString();
    }

    public PriceEpochData getCurrentPriceEpochData() {

        org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                "getCurrentPriceEpochData",
                Arrays.asList(),
                Arrays.asList(new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }, new TypeReference<Uint256>() {
                }));

        String encodedFunction = FunctionEncoder.encode(function);

        try {
            org.web3j.protocol.core.methods.response.EthCall response =
                    web3.ethCall(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(null, getFtsoManager(), encodedFunction), DefaultBlockParameterName.LATEST)
                            .sendAsync().get();

            List<Type> someTypes = FunctionReturnDecoder.decode(
                    response.getValue(), function.getOutputParameters());


            return PriceEpochData.builder()
                    .priceEpochId(Long.parseLong(someTypes.get(0).getValue().toString()))
                    .priceEpochStartTimestamp(Long.parseLong(someTypes.get(1).getValue().toString()))
                    .priceEpochEndTimestamp(Long.parseLong(someTypes.get(2).getValue().toString()))
                    .priceEpochRevealEndTimestamp(Long.parseLong(someTypes.get(3).getValue().toString()))
                    .currentTimestamp(Long.parseLong(someTypes.get(4).getValue().toString()))
                    .build();
        } catch (Exception e) {
            log.error("Error when retreiving price epoch data", e);
            return null;
        }

    }
}
