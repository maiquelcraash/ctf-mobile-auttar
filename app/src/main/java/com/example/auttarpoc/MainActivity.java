package com.example.auttarpoc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Parcel;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    AuttarSDK auttarSDK;
    AuttarConfiguration configuration;
    Boolean splitTransaction = false;
    Double splitValue = 0.0;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        String pinpad = configuration.getPinpadBluetooth();
        String prompt = configuration.getPromptPinpad();

        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            TefResult tefResult = createTefResult(data);

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
                        try {
                            Transaction transaction = new Transaction(NSUCTF, requestCode, tefResult);
                            saveTransaction(transaction);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        alertDialog.setMessage(tefResult.getDisplay()[0] +
                                "\nNSU: " + NSUCTF);
                        alertDialog.show();

//                         Pagamento com 2 cartões
                        if(splitTransaction) {
                            splitTransaction = false;

                            LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                            LibCTFClient.IntentBuilder builder = libCTFClient.nextTransaction(requestCode,  tefResult);

                            builder.setAmount(new BigDecimal(splitValue));
                            builder.setAutomaticConfirmation(autoConf);
                            builder.setInstallments(1);

                            libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                            libCTFClient.executeTransaction(builder, requestCode);
                        }
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
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auttarSDK = new AuttarSDK(getApplicationContext());
        configuration = auttarSDK.getConfiguration();

        configuration.setCancellationPermissionType(AuttarPermissionType.permited);

        final Intent loginIntent = auttarSDK.createDefaultLoginIntent();

        AuttarTerminal auttarTerminal = new AuttarTerminal("01011", "0302", "401"); // PDV -> 300 (tem que ser 3 dígitos) "01011", "0302", "005"
//        AuttarTerminal auttarTerminal = new AuttarTerminal("01011", "0846", "001"); // banricompras
        AuttarHost auttarHost = new AuttarHost("10.8.4.217", 1996); //"10.8.4.218", 1996
        List<AuttarHost> hostList = new ArrayList<>();
        hostList.add(auttarHost);

        // DESCOMENTAR ABAIXO PARA USAR CTF STANDALONE
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

                Transaction transaction = loadTransaction(NSU);

                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.finalizeTransaction(transaction.getTefResult(), true, transaction.getRequestCode());
            }
        });
        Button iniciarDia = findViewById(R.id.iniciarDia);
        iniciarDia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                IntentBuilder builder = IntentBuilder.from(Constantes.OperacaoCTFClient.OP_TESTE_COMUNICACAO_5N);
                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.OP_TESTE_COMUNICACAO_5N);
            }
        });

        Button pagarCCBtn = findViewById(R.id.pagarCC);
        pagarCCBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                splitTransaction = false;

                IntentBuilder builder = IntentBuilder.from(Constantes.OperacaoCTFClient.CREDITO);
                builder.setAmount(new BigDecimal(Double.parseDouble(valueInput.getText().toString())));
                builder.setAutomaticConfirmation(autoConf);
                builder.setInstallments(1);

                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.CREDITO);
            }
        });
        Button pagarCC2Btn = findViewById(R.id.pagarCC2);
        pagarCC2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                splitTransaction = true;

                Double value = Double.parseDouble(valueInput.getText().toString());
                splitValue = value / 2;
                value = value - splitValue;

                IntentBuilder builder = IntentBuilder.from(Constantes.OperacaoCTFClient.CREDITO);
                builder.setAmount(new BigDecimal(value));
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
                splitTransaction = false;

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
                splitTransaction = false;

                IntentBuilder builder = IntentBuilder.from(Constantes.OperacaoCTFClient.DEBITO);
                builder.setAmount(new BigDecimal(Double.parseDouble(valueInput.getText().toString())));
                builder.setInstallments(1);
                builder.setAutomaticConfirmation(autoConf);

                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.DEBITO);
            }
        });

        // Banricompras pré datado
        Button pagarCDPrazoBtn = findViewById(R.id.pagarCDPrazo);
        pagarCDPrazoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                splitTransaction = false;

                IntentBuilder builder = IntentBuilder.from(Constantes.OperacaoCTFClient.DEBITO_PREDATADO);
                builder.setAmount(new BigDecimal(Double.parseDouble(valueInput.getText().toString())));
//                builder.setInstallments(Integer.parseInt(installmentsInput.getText().toString()));
                builder.setAutomaticConfirmation(autoConf);

                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.DEBITO_PREDATADO);

            }
        });

        // Banricompras cédito
        Button pagarCCBanriBtn = findViewById(R.id.pagarCCBanri);
        pagarCCBanriBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                splitTransaction = false;

                IntentBuilder builder = IntentBuilder.from(Constantes.OperacaoCTFClient.CDC_SEM_PARCELAS_AVISTA);
                builder.setAmount(new BigDecimal(Double.parseDouble(valueInput.getText().toString())));
                builder.setInstallments(Integer.parseInt(installmentsInput.getText().toString()));
                builder.setAutomaticConfirmation(autoConf);

                builder.setCardTypedEnabled(true);
//                builder.setCardNumber("6396649900071989");


                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.CDC_SEM_PARCELAS_AVISTA);

            }
        });

        // Banricompras em 1X
        // libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.DEBITO_PREDATADO);

        Button searchTransactionBtn = findViewById(R.id.searchTrans);
        searchTransactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                splitTransaction = false;

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

    private void saveTransaction(Transaction transaction) {
        Bundle bundle = transaction.getTefResult().getIntent().getExtras();

        Parcel parcel = Parcel.obtain();
        String serialized = null;
        try {
            bundle.writeToParcel(parcel, 0);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.write(parcel.marshall(), bos);

            serialized = Base64.encodeToString(bos.toByteArray(), 0);
            System.out.println(serialized);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), e.toString(), e);
        } finally {
            parcel.recycle();
        }
        if (serialized != null) {
            SharedPreferences settings = getSharedPreferences("TRANSACTIONS", 0);
            SharedPreferences.Editor editor = settings.edit();

            Set<String> transactionSet = new HashSet<String>();
            transactionSet.add(transaction.getRequestCode() + "");
            transactionSet.add(serialized);

//            editor.putString(transaction.getNSU() + "", serialized);
            editor.putStringSet(transaction.getNSU() + "", transactionSet);
            editor.commit();
        }
    }

    //    /data/user/0/com.example.auttarpoc/shared_prefs/TRANSACTIONS.xml
    private Transaction loadTransaction(int NSU) {
        Transaction transaction = null;
        SharedPreferences settings = getSharedPreferences("TRANSACTIONS", 0);

        Set<String> transactionSet = settings.getStringSet(NSU + "", null);

        String requestCodeStr = (String) transactionSet.toArray()[0];
        String serialized = (String) transactionSet.toArray()[1];
        int requestCode = Integer.parseInt(requestCodeStr);

        //loga todas as transactions salvas
        Map<String, ?> allEntries = settings.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey());  // + ": " + entry.getValue().toString());
        }

        if (serialized != null) {
            Parcel parcel = Parcel.obtain();
            try {
                byte[] data = Base64.decode(serialized, 0);
                parcel.unmarshall(data, 0, data.length);
                parcel.setDataPosition(0);
                Bundle bundle = parcel.readBundle();

                Intent i = new Intent();
                i.putExtras(bundle);

                TefResult tefResult = createTefResult(i);

                transaction = new Transaction(NSU, requestCode, tefResult);

            } finally {
                parcel.recycle();
            }
        }
        return transaction;
    }

    private void removeFromPreferences(int NSU) {
        SharedPreferences settings = getSharedPreferences("TRANSACTIONS", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(NSU + "");
        editor.apply();
    }
}