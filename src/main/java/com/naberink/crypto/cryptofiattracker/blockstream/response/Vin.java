package com.naberink.crypto.cryptofiattracker.blockstream.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Vin {
    private String txid;
    private long vout;
    private Vout prevout;
    private String scriptsig;
    private String scriptsigAsm;
    private List<String> witness;
    @JsonProperty("is_coinbase")
    private boolean coinbase;
    private long sequence;
}
