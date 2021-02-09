package com.example.auttarpoc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.auttar.libctfclient.Constantes;
import br.com.auttar.libctfclient.ui.CTFClientActivity;
import br.com.auttar.mobile.libctfclient.sdk.AuttarConfiguration;
import br.com.auttar.mobile.libctfclient.sdk.AuttarHost;
import br.com.auttar.mobile.libctfclient.sdk.AuttarPermissionType;
import br.com.auttar.mobile.libctfclient.sdk.AuttarSDK;
import br.com.auttar.mobile.libctfclient.sdk.AuttarTerminal;
import br.com.auttar.mobile.libctfclient.sdk.LibCTFClient;
import br.com.auttar.mobile.libctfclient.sdk.TefResult;

public class MainActivity extends AppCompatActivity {

    Boolean autoConf = false;
    HashMap transactions = new HashMap<Integer, HashMap<Integer, TefResult>>();


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            TefResult tefResult = LibCTFClient.createTefResult(data);

            if (tefResult != null) {
                int returnCode = tefResult.getReturnCode();

                //Aprovado
                if (returnCode == 0) {
                    int NSUCTF = 0;
                    int installments = 1;

                    try {
                        NSUCTF = tefResult.getNsuCTF();
                        installments = tefResult.getInstallments();
                    } catch (Exception e) {
                    }

                    if (NSUCTF > 0) {
                        HashMap transaction = new HashMap<Integer, TefResult>();
                        transaction.put(requestCode, tefResult);
                        transactions.put(NSUCTF, transaction);

                        String Adquirente = tefResult.getAcquirer();
                        String AditionalData = tefResult.getAdditionalData();
                        BigDecimal amount = tefResult.getAmount();
                        String approvalCode = tefResult.getApprovalCode();
                        String authorizedCode = tefResult.getAuthorizerCode();
                        String authorizer = tefResult.getAuthorizerName();
                        String authorizationCode = tefResult.getAuttarAuthorizationCode();
                        Long authorizerNSU = tefResult.getAuthorizerNsu();
                        String brand = tefResult.getBrand();
                        String cardNumber = tefResult.getCardNumber();
                        String errorCode = tefResult.getErrorCode();
                        String responseCode = tefResult.getResponseCode();
                        String terminal = tefResult.getTerminal();
                        String[] abbreviateCupom = tefResult.getAbbreviatedReceipt();
                        String[] customerCupom = tefResult.getCustomerSalesReceipt();
                        String[] storeCupom = tefResult.getStoreSalesReceipt();

                        alertDialog.setMessage(tefResult.getDisplay()[0] +
                                "\nNSU: " + NSUCTF);
                        alertDialog.show();
                    }
                }
                // Negado no CTF
                else if (returnCode == 11) {
                    alertDialog.setMessage(tefResult.getDisplay()[0]);
                    alertDialog.show();
                }
                //Tente Novamente
                else if (returnCode == 20) {
                    alertDialog.setMessage(tefResult.getDisplay()[0]);
                    alertDialog.show();
                }
                //OUTROS??
                else {
                    alertDialog.setMessage(tefResult.getDisplay()[0]);
                    alertDialog.show();
                }
            }


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        AuttarSDK auttarSDK = new AuttarSDK(getApplicationContext());
        final AuttarConfiguration configuration = auttarSDK.getConfiguration();

        configuration.setCancellationPermissionType(AuttarPermissionType.permited);

        final Intent loginIntent = auttarSDK.createDefaultLoginIntent();

//        DESCOMENTAR ABAIXO PARA USAR CTF STANDALONE

        AuttarTerminal auttarTerminal = new AuttarTerminal("01011", "0710", "001"); // PDV -> 300 (tem que ser 3 dígitos)
        AuttarHost auttarHost = new AuttarHost("10.8.4.218", 1996);
        List<AuttarHost> hostList = new ArrayList<>();
        hostList.add(auttarHost);

        configuration.configureTerminal(auttarTerminal);
        configuration.configureHostCTF(hostList);


        final Intent configIntent = configuration.createDefaultIntent();
        final Intent pinpadIntent = configuration.createPinpadBluetoothIntent();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText valueInput = findViewById(R.id.valueInput);
        valueInput.setText("170");

        final EditText installmentsInput = findViewById(R.id.parcelasInput);
        installmentsInput.setText("6");

        final EditText transactionIDInput = findViewById(R.id.transactionIDInput);
        transactionIDInput.setText("Trans. ID");

        Button configBtn = findViewById(R.id.configBtn);
        configBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startActivity(configIntent);
            }
        });

        Button loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startActivity(loginIntent);
            }
        });

        Button pinpadBtn = findViewById(R.id.configPinpad);
        pinpadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startActivity(pinpadIntent);
            }
        });

        ToggleButton autoConfToggle = findViewById(R.id.autoConfToggle);
        autoConfToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoConf = !autoConf;
            }
        });

        Button confTransBtn = findViewById(R.id.confTransBtn);
        confTransBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Confirma a transação
                int NSU = Integer.parseInt(transactionIDInput.getText().toString());

                HashMap transaction = (HashMap) transactions.get(NSU);
                int requestCode = (Integer) transaction.keySet().toArray()[0];
                TefResult tefResult = (TefResult) transaction.get(transaction.keySet().toArray()[0]);

                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.finalizeTransaction(tefResult, true, requestCode);
            }
        });

        Button iniciarDia = findViewById(R.id.iniciarDia);
        iniciarDia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LibCTFClient.IntentBuilder builder = LibCTFClient.IntentBuilder.from(Constantes.OperacaoCTFClient.INICIO_DIA);
                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.INICIO_DIA);
            }
        });

        Button pagarCCBtn = findViewById(R.id.pagarCC);
        pagarCCBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LibCTFClient.IntentBuilder builder = LibCTFClient.IntentBuilder.from(Constantes.OperacaoCTFClient.CREDITO);
                builder.setAmount(new BigDecimal(Double.parseDouble(valueInput.getText().toString())));
                builder.setAutomaticConfirmation(autoConf);
                builder.setInstallments(1);

                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.CREDITO);
            }
        });

        Button pagarCCPrazo = findViewById(R.id.pagarCCPrazo);
        pagarCCPrazo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LibCTFClient.IntentBuilder builder = LibCTFClient.IntentBuilder.from(Constantes.OperacaoCTFClient.CREDITO_LOJISTA);
                builder.setAmount(new BigDecimal(Double.parseDouble(valueInput.getText().toString())));
                builder.setInstallments(Integer.parseInt(installmentsInput.getText().toString()));
                builder.setAutomaticConfirmation(autoConf);

                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.CREDITO_LOJISTA);
            }
        });

        Button pagarCDBtn = findViewById(R.id.pagarCD);
        pagarCDBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LibCTFClient.IntentBuilder builder = LibCTFClient.IntentBuilder.from(Constantes.OperacaoCTFClient.DEBITO);
                builder.setAmount(new BigDecimal(Double.parseDouble(valueInput.getText().toString())));
                builder.setInstallments(1);
                builder.setAutomaticConfirmation(autoConf);

                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.DEBITO);
            }
        });

        // Banricompras em + de 1X Crédito
        Button pagarCDPrazoBtn = findViewById(R.id.pagarCDPrazo);
        pagarCDPrazoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LibCTFClient.IntentBuilder builder = LibCTFClient.IntentBuilder.from(Constantes.OperacaoCTFClient.CDC_SEM_PARCELAS_AVISTA);
                builder.setAmount(new BigDecimal(Double.parseDouble(valueInput.getText().toString())));
//                builder.setInstallments(Integer.parseInt(installmentsInput.getText().toString()));
                builder.setAutomaticConfirmation(autoConf);

                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.DEBITO_PREDATADO);

            }
        });

        // Banricompras em 1X
        // libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.DEBITO_PREDATADO);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}