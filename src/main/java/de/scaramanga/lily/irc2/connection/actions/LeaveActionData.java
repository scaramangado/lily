package de.scaramanga.lily.irc2.connection.actions;

public class LeaveActionData extends ConnectionActionData {

    private static final String CHANNEL_NAME = "channelName";

    public String getChannelName() {
        return data.getProperty(CHANNEL_NAME);
    }

    public void setChannelName(String channelName) {
        data.setProperty(CHANNEL_NAME, channelName);
    }

    public static LeaveActionData withChannelName(String channelName) {

        LeaveActionData data = new LeaveActionData();
        data.setChannelName(channelName);
        return data;
    }
}
