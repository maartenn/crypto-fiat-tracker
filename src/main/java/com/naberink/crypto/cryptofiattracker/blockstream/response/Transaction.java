package com.naberink.crypto.cryptofiattracker.blockstream.response;

import lombok.Data;
import java.util.List;

@Data
public class Transaction {
    private String txid;
    private int version;
    private long locktime;
    private List<Vin> vin;
    private List<Vout> vout;
    private int size;
    private int weight;
    private int fee;
    private Status status;
}