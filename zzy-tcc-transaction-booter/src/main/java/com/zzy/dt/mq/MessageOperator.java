package com.zzy.dt.mq;

public interface MessageOperator<T extends TransactionMessage> extends MessageProvider<T>, MessageConsumer<T> {


}
