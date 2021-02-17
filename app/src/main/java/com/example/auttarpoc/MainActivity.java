package com.example.auttarpoc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.BasicSerializerFactory;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.module.paranamer.ParanamerModule;
import com.fasterxml.jackson.module.paranamer.ParanamerOnJacksonAnnotationIntrospector;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import br.com.auttar.libctfclient.Constantes;
import br.com.auttar.libctfclient.ui.CTFClientActivity;
import br.com.auttar.mobile.libctfclient.sdk.AuttarConfiguration;
import br.com.auttar.mobile.libctfclient.sdk.AuttarHost;
import br.com.auttar.mobile.libctfclient.sdk.AuttarPermissionType;
import br.com.auttar.mobile.libctfclient.sdk.AuttarSDK;
import br.com.auttar.mobile.libctfclient.sdk.AuttarTerminal;
import br.com.auttar.mobile.libctfclient.sdk.LibCTFClient;
import br.com.auttar.mobile.libctfclient.sdk.TefResult;

import static br.com.auttar.mobile.libctfclient.sdk.LibCTFClient.*;

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
            TefResult tefResult = createTefResult(data);

            if (tefResult != null) {
                int returnCode = tefResult.getReturnCode();


                Bundle bundle = tefResult.getIntent().getExtras();


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

                        //Tentativa de serialização (MAL SUCEDIDA)
                        ObjectMapper mapper = new ObjectMapper();

                        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
                        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
                        mapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);

//                        mapper.registerModule(new ParanamerModule());
//
//                        mapper.setAnnotationIntrospector(new ParanamerOnJacksonAnnotationIntrospector());


                        try {
//
                            String ba = mapper.writeValueAsString(bundle);
                            Bundle newbundle = mapper.readValue(ba, Bundle.class);

                            Set keyset = bundle.keySet();

                            Intent i = new Intent();
                            i.putExtras(newbundle);

                            TefResult tefResultRestored = createTefResult(i);

                            HashMap transaction = new HashMap<Integer, TefResult>();
                            transaction.put(requestCode, tefResult);
                            transactions.put(NSUCTF, transaction);

                            HashMap transaction2 = new HashMap<Integer, TefResult>();
                            transaction2.put(requestCode, tefResultRestored);
                            transactions.put(NSUCTF+100, transaction2);

                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }


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
                    try {
                        alertDialog.setMessage(tefResult.getDisplay()[0]);
                        alertDialog.show();
                    }catch (Exception e){}

                }
            }


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        final AuttarSDK auttarSDK = new AuttarSDK(getApplicationContext());
        final AuttarConfiguration configuration = auttarSDK.getConfiguration();

        configuration.setCancellationPermissionType(AuttarPermissionType.permited);

        final Intent loginIntent = auttarSDK.createDefaultLoginIntent();

//        DESCOMENTAR ABAIXO PARA USAR CTF STANDALONE
//
//        AuttarTerminal auttarTerminal = new AuttarTerminal("01011", "0302", "005"); // PDV -> 300 (tem que ser 3 dígitos)
//        AuttarHost auttarHost = new AuttarHost("10.8.4.218", 1996);
//        List<AuttarHost> hostList = new ArrayList<>();
//        hostList.add(auttarHost);
//
//        configuration.configureTerminal(auttarTerminal);
//        configuration.configureHostCTF(hostList);


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

//                libCTFClient.executeTransaction(IntentBuilder.from(6).setTransactionID(tefResult.getTransactionID()).setTransactionNumber(tefResult.getTransactionNumber()).setIdentifierMultiEC(tefResult.getIdentifierMultiEC()), requestCode);
            }
        });

//        public void finalizeTransaction (TefResult var1,boolean var2, int var3){
//            byte var4;
//            if (var2) {
//                var4 = 6;  //confirmacao
//            } else {
//                var4 = 7;  //desfazimento
//            }
//
//            lib.executeTransaction(IntentBuilder.from(var4).setTransactionID(var1.getTransactionID()).setTransactionNumber(var1.getTransactionNumber()).setIdentifierMultiEC(var1.getIdentifierMultiEC()), var3);
//        }

        Button iniciarDia = findViewById(R.id.iniciarDia);
        iniciarDia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                IntentBuilder builder = IntentBuilder.from(Constantes.OperacaoCTFClient.INICIO_DIA);
                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.INICIO_DIA);
            }
        });

        Button pagarCCBtn = findViewById(R.id.pagarCC);
        pagarCCBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                IntentBuilder builder = IntentBuilder.from(Constantes.OperacaoCTFClient.CREDITO);
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

                IntentBuilder builder = IntentBuilder.from(Constantes.OperacaoCTFClient.CREDITO_LOJISTA);
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

                IntentBuilder builder = IntentBuilder.from(Constantes.OperacaoCTFClient.DEBITO);
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

                IntentBuilder builder = IntentBuilder.from(Constantes.OperacaoCTFClient.CDC_SEM_PARCELAS_AVISTA);
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

        Button searchTransactionBtn = findViewById(R.id.searchTrans);
        searchTransactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int NSU = Integer.parseInt(transactionIDInput.getText().toString());

                IntentBuilder builder = IntentBuilder.from(Constantes.OperacaoCTFClient.CONFIRMACAO_NSU_CTF);
//                builder.setAmount(new BigDecimal(Double.parseDouble(valueInput.getText().toString())));
//                builder.setInstallments(Integer.parseInt(installmentsInput.getText().toString()));
                builder.setAutomaticConfirmation(autoConf);
                builder.setNsuCTF(NSU);

                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.CONFIRMACAO_NSU_CTF);

            }
        });
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