package com.example.auttarpoc;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Base64;

import br.com.auttar.mobile.libctfclient.sdk.TefResult;

import static br.com.auttar.mobile.libctfclient.sdk.LibCTFClient.createTefResult;

public class Transaction {
    private int NSU, requestCode;
    private TefResult tefResult;

    public Transaction(int NSU, int requestCode, TefResult tefResult) {
        this.NSU = NSU;
        this.requestCode = requestCode;
        this.tefResult = tefResult;
    }

    public Transaction(int requestCode, String confirmationKey) {
        Parcel parcel = Parcel.obtain();
        byte[] data = Base64.decode(confirmationKey, 0);
        parcel.unmarshall(data, 0, data.length);
        parcel.setDataPosition(0);
        Bundle bundle = parcel.readBundle();

        Intent i = new Intent();
        i.putExtras(bundle);

        this.tefResult = createTefResult(i);
        this.requestCode = requestCode;
        this.NSU = this.tefResult.getNsuCTF();
    }

    public int getNSU() {
        return NSU;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public TefResult getTefResult() {
        return tefResult;
    }
}
