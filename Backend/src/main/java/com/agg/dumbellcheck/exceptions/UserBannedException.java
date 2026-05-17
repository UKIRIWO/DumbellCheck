package com.agg.dumbellcheck.exceptions;

import java.time.Instant;

public class UserBannedException extends RuntimeException {

    private final BanInfo banInfo;

    public UserBannedException(BanInfo banInfo) {
        super("Tu cuenta está suspendida");
        this.banInfo = banInfo;
    }

    public BanInfo getBanInfo() { return banInfo; }

    public record BanInfo(String motivoBaneo, Instant baneadoHasta, boolean baneadoPermanentemente) {}
}
