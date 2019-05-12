package de.scaramanga.lily.irc.connection.actions;

public class JoinActionData extends ConnectionActionData {

    private static final String CHANNEL_NAME = "channelName";

    public String getChannelName() {
        return data.getProperty(CHANNEL_NAME);
    }

    public void setChannelName(String channelName) {
        data.setProperty(CHANNEL_NAME, channelName);
    }

    public static JoinActionData withChannelName(String channelName) {

        JoinActionData data = new JoinActionData();
        data.setChannelName(channelName);
        return data;
    }
}
