package dz.mouradski.ftso.submitter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class Config {

    @Value("${web3.provider}")
    private String wssProvider;

    @Bean
    public Web3j web3() throws ConnectException, URISyntaxException {
        WebSocketClient webSocketClient = new WebSocketClient(new URI(wssProvider));

        WebSocketService webSocketService = new WebSocketService(webSocketClient, false);

        webSocketService.connect();

        return Web3j.build(webSocketService);
    }


    @Bean
    public TransactionManager transactionManager(@Autowired Web3j web3,
                                                 @Value("${private.key}") String pk,
                                                 @Value("${chain.id}") Integer chainId) {
        return new RawTransactionManager(web3, Credentials.create(pk), chainId);
    }

}
