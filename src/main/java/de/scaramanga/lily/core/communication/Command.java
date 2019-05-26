package de.scaramanga.lily.core.communication;

public interface Command {

  String getName();

  int getArgumentCount();

  String getArgument(int i);

  MessageInfo getMessageInfo();

  static Command ofName(String command) {

    return new Command() {
      @Override
      public String getName() {

        return command;
      }

      @Override
      public int getArgumentCount() {

        return 0;
      }

      @Override
      public String getArgument(int i) {

        throw new IndexOutOfBoundsException(String.format("Index %d bigger than maximum 0.", i));
      }

      @Override
      public MessageInfo getMessageInfo() {

        return MessageInfo.empty();
      }
    };
  }

  static Command withMessageInfo(String command, MessageInfo info) {

    return new Command() {
      @Override
      public String getName() {

        return command;
      }

      @Override
      public int getArgumentCount() {

        return 0;
      }

      @Override
      public String getArgument(int i) {

        throw new IndexOutOfBoundsException(String.format("Index %d bigger than maximum 0.", i));
      }

      @Override
      public MessageInfo getMessageInfo() {

        return info;
      }
    };
  }
}
