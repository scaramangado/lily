package de.scaramangado.lily.core.communication;

public interface MessageInfo {

  static MessageInfo empty() {

    return new MessageInfo() { };
  }
}
