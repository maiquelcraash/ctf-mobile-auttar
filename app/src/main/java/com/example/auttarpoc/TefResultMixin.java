package com.example.auttarpoc;

import android.content.Intent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import br.com.auttar.mobile.libctfclient.sdk.AuttarMerchantMultiEC;
import br.com.auttar.mobile.libctfclient.sdk.AuttarPhone;
import br.com.auttar.mobile.libctfclient.sdk.AuttarReceipt;
import br.com.auttar.mobile.libctfclient.sdk.LibCTFClient;
import br.com.auttar.mobile.libctfclient.sdk.TefResult;

public abstract class TefResultMixin{

    public TefResultMixin(
            @JsonProperty("intent") Intent intent,
            @JsonProperty("status") String status,
            @JsonProperty("transactionID") long transactionID,
            @JsonProperty("sdkVersion") int sdkVersion,
            @JsonProperty("searchTransaction") boolean searchTransaction,
            @JsonProperty("searchTransactionID") Long searchTransactionID,
            @JsonProperty("token") String token,
            @JsonProperty("terminal") String terminal,
            @JsonProperty("transactionNumber") int transactionNumber,
            @JsonProperty("returnCode") int returnCode,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("display") String[] display,
            @JsonProperty("authorizerNsu") Long authorizerNsu,
            @JsonProperty("responseCode") String responseCode,
            @JsonProperty("errorCode") String errorCode,
            @JsonProperty("cardNumber") String cardNumber,
            @JsonProperty("nsuCTF") Integer nsuCTF,
            @JsonProperty("approvalCode") String approvalCode,
            @JsonProperty("installments") Integer installments,
            @JsonProperty("brand") String brand,
            @JsonProperty("abbreviatedReceipt") String[] abbreviatedReceipt,
            @JsonProperty("customerSalesReceipt") String[] customerSalesReceipt,
            @JsonProperty("storeSalesReceipt") String[] storeSalesReceipt,
            @JsonProperty("transactionDateTime") String transactionDateTime,
            @JsonProperty("acquirer") String acquirer,
            @JsonProperty("authorizerCode") String authorizerCode,
            @JsonProperty("authorizerName") String authorizerName,
            @JsonProperty("additionalData") String additionalData,
            @JsonProperty("paymentResult") TefResult paymentResult,
            @JsonProperty("automaticConfirmation") boolean automaticConfirmation,
            @JsonProperty("remoteConfirmation") boolean remoteConfirmation,
            @JsonProperty("auttarAuthorizationCode") String auttarAuthorizationCode,
            @JsonProperty("identifierMultiEC") String identifierMultiEC,
            @JsonProperty("scheduledDate") Date scheduledDate,
            @JsonProperty("auttarPhone") AuttarPhone auttarPhone,
            @JsonProperty("reprints") List<AuttarReceipt> reprints,
            @JsonProperty("auttarMerchantMultiECS") List<AuttarMerchantMultiEC> auttarMerchantMultiECS

    ) {
    }
}
