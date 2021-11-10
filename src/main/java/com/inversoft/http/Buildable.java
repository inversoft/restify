/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
 */
package com.inversoft.http;

import java.util.function.Consumer;

/**
 * @author Brian Pontarelli
 */
public interface Buildable<T> {
  @SuppressWarnings("unchecked")
  default T with(Consumer<T> consumer) {
    consumer.accept((T) this);
    return (T) this;
  }
}
