package com.naberink.crypto.cryptofiattracker.blockstream.response;

import lombok.Data;

@Data
public class Vout {
    private String scriptpubkey;
    private String scriptpubkeyAsm;
    private String scriptpubkeyType;
    private String scriptpubkeyAddress;
    private long value;
}