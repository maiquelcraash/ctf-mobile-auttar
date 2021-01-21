package com.example.auttarpoc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import br.com.auttar.mobile.libctfclient.sdk.Constants;

public class MainActivity extends AppCompatActivity {


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            TefResult tefResult = LibCTFClient.createTefResult(data);
            System.out.println(tefResult.getReturnCode());
            System.out.println(data);

            // Faz alguma coisa antes de confirmar a transação

            // Confirma a transação
//            LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
//            libCTFClient.finalizeTransaction(tefResult, true, Constantes.OperacaoCTFClient.CONFIRMACAO);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AuttarSDK auttarSDK = new AuttarSDK(getApplicationContext());
        final AuttarConfiguration configuration = auttarSDK.getConfiguration();

        configuration.setCancellationPermissionType(AuttarPermissionType.permited);

        final Intent loginIntent = auttarSDK.createDefaultLoginIntent();

//        DESCOMENTAR ABAIXO PARA USAR CTF STANDALONE
//        AuttarTerminal auttarTerminal = new AuttarTerminal("01011","0710","001"); // PDV -> 300 (tem que ser 3 dígitos)
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

        Button pagar100Btn = findViewById(R.id.pagar100Btn);
        pagar100Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LibCTFClient.IntentBuilder builder = LibCTFClient.IntentBuilder.from(Constantes.OperacaoCTFClient.CREDITO);
                builder.setAmount(new BigDecimal(165));
                builder.setInstallments(2);
//                builder.setAutomaticConfirmation(false);

                LibCTFClient libCTFClient = new LibCTFClient(MainActivity.this);
                libCTFClient.setCustomViewCTFClient(CTFClientActivity.class);
                libCTFClient.executeTransaction(builder, Constantes.OperacaoCTFClient.CREDITO);
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