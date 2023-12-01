package com.naberink.crypto.cryptofiattracker.blockstream.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Prevout {
    private String scriptpubkey;
    private String scriptpubkeyAsm;
    private String scriptpubkeyType;
    private String scriptpubkeyAddress;
    private long value;
}
