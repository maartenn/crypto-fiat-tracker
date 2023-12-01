package com.naberink.crypto.cryptofiattracker.blockstream.response;

import lombok.Data;

@Data
public class Status {
    private boolean confirmed;
    private long blockHeight;
    private String blockHash;
    private long blockTime;
}
