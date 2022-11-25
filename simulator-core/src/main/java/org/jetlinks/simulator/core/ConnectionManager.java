package org.jetlinks.simulator.core;

import lombok.Getter;
import lombok.Setter;
import org.jetlinks.reactor.ql.utils.CastUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ConnectionManager extends Disposable {

    static ConnectionManager global() {
        return DefaultConnectionManager.global;
    }

    long getConnectionSize();

    Flux<Connection> getConnections();

    Mono<Connection> getConnection(String id);

    Optional<Connection> getConnectionNow(String id);

    /**
     * <pre>
     *      type = 'mqtt' and clientId like 'test%' limit 0,10
     *  </pre>
     *
     * @param ql 查询表达式
     * @return 查询结果
     */
    Flux<Connection> findConnection(String ql);

    Flux<Connection> randomConnection(int size);

    ConnectionManager addConnection(Connection connection);

    default Mono<Summary> summary() {
        return getConnections()
                .reduce(new Summary(), Summary::add);
    }

    @Getter
    @Setter
    class Summary {
        private long size;
        private long connected;
        private long closed;
        private long sent;
        private long received;
        private long sentBytes;
        private long receivedBytes;

        public Summary add(Connection connection) {
            size++;

            if (connection.state()== Connection.State.connected) {
                connected++;
            } else {
                closed++;
            }

            sent += connection
                    .attribute(Connection.ATTR_SENT)
                    .map(CastUtils::castNumber)
                    .orElse(0)
                    .longValue();
            sentBytes += connection
                    .attribute(Connection.ATTR_SENT_BYTES)
                    .map(CastUtils::castNumber)
                    .orElse(0)
                    .longValue();

            received += connection
                    .attribute(Connection.ATTR_RECEIVE)
                    .map(CastUtils::castNumber)
                    .orElse(0)
                    .longValue();

            receivedBytes += connection
                    .attribute(Connection.ATTR_RECEIVE_BYTES)
                    .map(CastUtils::castNumber)
                    .orElse(0)
                    .longValue();
            return this;
        }

        public Summary merge(Summary summary) {
            Summary sum = new Summary();
            sum.size += summary.size;
            sum.connected += summary.connected;
            sum.closed += summary.closed;
            sum.sent += summary.sent;
            sum.received += summary.received;
            sum.sentBytes += summary.sentBytes;
            sum.receivedBytes += summary.receivedBytes;
            return sum;

        }

    }

}
