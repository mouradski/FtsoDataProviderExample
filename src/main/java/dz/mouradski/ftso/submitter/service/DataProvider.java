package dz.mouradski.ftso.submitter.service;

import dz.mouradski.ftso.submitter.model.PriceEpochData;
import dz.mouradski.ftso.submitter.service.price.PriceProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static dz.mouradski.ftso.submitter.utils.DataHelper.getRandomBigIntegerAsString;
import static dz.mouradski.ftso.submitter.utils.DataHelper.priceHash;

@Component
@Slf4j
public class DataProvider {


    private TransactionService transactionService;
    private DataEpochService dataEpochService;
    private List<PriceProvider> priceProviders;
    private String submissionContract;

    private Map<Integer, BigInteger> prices = new HashMap<>();
    private Map<Integer, String> randoms = new HashMap<>();
    private Map<Integer, String> hashes = new HashMap<>();

    private Long submissionEndOffset;
    private Long revealStartOffset;
    private String dataProviderAddress;

    public DataProvider(@Autowired List<PriceProvider> priceProviders,
                        @Autowired TransactionService transactionService,
                        @Autowired DataEpochService dataEpochService,
                        @Value("${price.submitter.contract}") String submissionContract,
                        @Value("${submission.end.offset}") Long submissionEndOffset,
                        @Value("${reveal.start.offset}") Long revealStartOffset,
                        @Value("${data.provider.address}") String dataProviderAddress) throws ExecutionException, InterruptedException {

        this.priceProviders = priceProviders;
        this.dataEpochService = dataEpochService;
        this.transactionService = transactionService;
        this.submissionContract = submissionContract;
        this.submissionEndOffset = submissionEndOffset;
        this.revealStartOffset = revealStartOffset;

        this.dataProviderAddress = dataProviderAddress;

        startSubmissionSession();
    }

    public void startSubmissionSession() {
        PriceEpochData priceEpochData = dataEpochService.getCurrentPriceEpochData();

        Long submissionDelay = (priceEpochData.getPriceEpochEndTimestamp() * 1000 - submissionEndOffset) - new Date().getTime();


        if (submissionDelay < 0) {
            Timer timer = new Timer("Delayed " + new Date().getTime());
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startSubmissionSession();
                }
            }, 15 * 1000);
        } else {
            log.info("Prices submission in {} seconds", submissionDelay / 1000);
            Timer timer = new Timer("Submit " + priceEpochData.getPriceEpochId().toString());
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        submitPrices(priceEpochData.getPriceEpochId());
                    } catch (Exception e) {
                        log.error("Error when submitting prices, epochId : {}", priceEpochData.getPriceEpochId(), e);
                    }
                }
            }, submissionDelay);
        }
    }

    public void submitPrices(Long epochId) throws IOException, ExecutionException, InterruptedException {

        PriceEpochData priceEpochData = dataEpochService.getCurrentPriceEpochData();

        Long revealDelay = (priceEpochData.getPriceEpochEndTimestamp() * 1000 + revealStartOffset) - new Date().getTime();

        log.info("Reveal in {} seconds", revealDelay / 1000);

        Timer timer = new Timer("Submit " + priceEpochData.getPriceEpochId().toString());
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    reveals(priceEpochData.getPriceEpochId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, revealDelay);


        Timer newEpochTimer = new Timer("Next Epoch Timer, previous " + priceEpochData.getPriceEpochId());

        Long nextEpochDelay = (priceEpochData.getPriceEpochRevealEndTimestamp() * 1000 + 3000) - new Date().getTime();

        newEpochTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    startSubmissionSession();
                } catch (Exception e) {
                    log.error("Error in epoch : {}", epochId, e);
                }
            }
        }, nextEpochDelay);

        clearPreviousEpochMaps();

        priceProviders.forEach(priceProvider -> {
            String symbol = priceProvider.getSymbol();
            Integer index = priceProvider.getIndex();
            Long price = BigDecimal.valueOf(priceProvider.getPrice()* 100000).longValue();
            Pair<String, String> HashRandomPair = calculateHash(index, price, dataProviderAddress);

            log.info("Index : {},  Symbol : {}, price : {}, hash : {}, random : {}", index, symbol, price, HashRandomPair.getKey(), HashRandomPair.getValue());
        });


        transactionService.submmitHashes(epochId, getIndices(), getHashes(), submissionContract);
    }

    public void reveals(Long epochId) throws IOException {
        log.info("Revelation des prix pour l'epoch {}", epochId);
        transactionService.reveal(epochId, getIndices(),
                getPrices(), getRandoms(), submissionContract);
    }


    public List<String> getHashes() {
        return hashes.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public List<Integer> getIndices() {
        return hashes.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public List<BigInteger> getPrices() {
        return prices.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public List<String> getRandoms() {
        return randoms.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    private Pair<String, String> calculateHash(Integer index, Long price, String address) {

        Pair<String, BigInteger> random = getRandomBigIntegerAsString();

        Pair<String, Byte[]> hash = priceHash(BigInteger.valueOf(price), random.getKey(), address);

        randoms.put(index, random.getKey());
        hashes.put(index, hash.getKey());
        prices.put(index, BigInteger.valueOf(price));

        return Pair.of(hash.getKey(), random.getKey());
    }

    private void clearPreviousEpochMaps() {
        prices.clear();
        hashes.clear();
        randoms.clear();
    }
}
