package com.wwadge.planetsforwarder.service.impl;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.util.Encoder;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.AssetHolding;
import com.algorand.algosdk.v2.client.model.PostTransactionsResponse;
import com.algorand.algosdk.v2.client.model.TransactionParametersResponse;
import com.wwadge.planetsforwarder.config.TransferProperties;
import com.wwadge.planetsforwarder.config.TransferProperties.AccountDetail;
import com.wwadge.planetsforwarder.service.TransferService;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component @Slf4j
public class TransferServiceImpl implements TransferService {

  final TransferProperties transferProperties;

  public TransferServiceImpl(TransferProperties transferProperties) {
    this.transferProperties = transferProperties;
  }


  // Utility function for sending a raw signed transaction to the network
  public String submitTransaction(SignedTransaction signedTx, AlgodClient client) throws Exception {
    // Msgpack encode the signed transaction
    byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTx);
    String[] headers = {"Content-Type"};
    String[] values = {"application/x-binary"};
    Response<PostTransactionsResponse> rawtxresponse = client.RawTransaction().rawtxn(encodedTxBytes).execute(headers, values);

    if (!rawtxresponse.isSuccessful()) {
      throw new Exception(rawtxresponse.message());
    }

    return rawtxresponse.body().txId;
  }

  @Override
  @Scheduled(cron = "${planets.schedule:-}")
  public void start() throws Exception {

    log.info("Starting execution...");
    AlgodClient client = new AlgodClient("http://mainnet-api.algonode.network", 80, "");

    Map<AccountDetail, Account> accounts = new LinkedHashMap<>();

    transferProperties.getFrom().forEach(p -> {
      try {
        log.info("Configuring {} to transfer {}% of funds...", p.getName(), p.getPercentageToTransfer().multiply(new BigDecimal(100)));
        accounts.put(p, new Account(p.getSeed()));
      } catch (GeneralSecurityException e) {
        throw new RuntimeException(e);
      }
    });

    Response<TransactionParametersResponse> resp = client.TransactionParams().execute();
    if (!resp.isSuccessful()) {
      throw new IllegalStateException(resp.message());
    }
    TransactionParametersResponse params = resp.body();

    if (params == null) {
      throw new IllegalStateException("Params retrieval error");
    }


    accounts.forEach((key, value) -> {
      BigDecimal bd;
      try {
        Optional<AssetHolding> holding = client.AccountInformation(value.getAddress())
            .execute().body()
            .assets.stream().filter(planet -> planet.assetId.equals(27165954L))
            .findFirst();

        if (holding.isEmpty()){
          log.warn("Could not get planets from {}", value.getAddress());
        } else {

          bd = BigDecimal.valueOf(holding.get().amount.doubleValue());

          Long algos = client.AccountInformation(value.getAddress()).execute().body().amount;

          if (algos > 400000) { // min balance required
            long amount = bd.multiply(key.getPercentageToTransfer()).longValue();

            Transaction tx = Transaction.AssetTransferTransactionBuilder()
                .sender(value.getAddress())
                .assetReceiver(transferProperties.getTo())
                .assetAmount(amount)
                .assetIndex(27165954L) // planets
                .suggestedParams(params)
                .build();

            SignedTransaction signedTx = value.signTransaction(tx);
            if (bd.longValue() > 0) {
              String id = submitTransaction(signedTx, client);
              log.info("{} transferred {} via transaction ID: {}", key.getName(), amount / 1000000,
                  id);
            } else {
              log.info("No PLANETS to transfer from {}", key.getName());
            }
          } else {
            log.info("Not enough ALGOs for {}, you need at least 0.4 ALGO", key.getName());
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    });
  }
}
