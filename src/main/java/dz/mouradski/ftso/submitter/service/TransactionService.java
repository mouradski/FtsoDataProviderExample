package dz.mouradski.ftso.submitter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class TransactionService {

    private TransactionManager transactionManager;

    public TransactionService(@Autowired TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public boolean submmitHashes(Long epochId, List<Integer> indices, List<String> hashes, String contract) throws IOException {

        org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                "submitPriceHashes",
                Arrays.asList(new Uint256(epochId),
                        new DynamicArray(indices.stream().map(v -> new Uint256(v)).collect(Collectors.toList())),
                        new DynamicArray(hashes.stream().map(v -> new Bytes32(Numeric.hexStringToByteArray(v))).collect(Collectors.toList()))),
                Arrays.asList());

        String encodedFunction = FunctionEncoder.encode(function);


        EthSendTransaction ethSendTransaction = transactionManager.sendTransaction(BigInteger.valueOf(725000000000l), BigInteger.valueOf(25000000l), contract, encodedFunction, BigInteger.valueOf(0));
        return !ethSendTransaction.hasError();

    }


    public boolean reveal(Long epochId, List<Integer> indices, List<BigInteger> prices, List<String> randoms, String contract) throws IOException {


        org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                "revealPrices",
                Arrays.asList(new Uint256(epochId),
                        new DynamicArray(indices.stream().map(v -> new Uint256(v)).collect(Collectors.toList())),
                        new DynamicArray(prices.stream().map(v -> new Uint256(v)).collect(Collectors.toList())),
                        new DynamicArray(randoms.stream().map(v -> new Uint256(new BigInteger(v))).collect(Collectors.toList()))),
                Arrays.asList());

        String encodedFunction = FunctionEncoder.encode(function);

        EthSendTransaction ethSendTransaction = transactionManager.sendTransaction(BigInteger.valueOf(725000000000l), BigInteger.valueOf(25000000l), contract, encodedFunction, BigInteger.valueOf(0));

        return !ethSendTransaction.hasError();
    }
}
