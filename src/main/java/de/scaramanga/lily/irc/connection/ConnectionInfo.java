package de.scaramanga.lily.irc.connection;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
class ConnectionInfo {

    private String host;

    private Integer port;

    private String username;

    private String password;

    private String channel;
}
