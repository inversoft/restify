/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.rest;

import java.util.function.Supplier;

/**
 * Supplies a {@link ProxyInfo}
 *
 * @author Daniel DeGroff
 */
@FunctionalInterface
public interface ProxyInfoSupplier extends Supplier<ProxyInfo> {
}
