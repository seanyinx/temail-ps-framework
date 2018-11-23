package com.syswin.temail.ps.server.service.channels.strategy.one2one;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import com.syswin.temail.ps.server.entity.Session;
import io.netty.channel.Channel;
import java.util.Collection;
import org.junit.Test;
import org.mockito.Mockito;

public class ChannelManagerOne2OneTest {

  private final Channel channel1 = Mockito.mock(Channel.class, "channel1");
  private final Channel channel2 = Mockito.mock(Channel.class, "channel2");

  private final ChannelManagerOne2One manager = new ChannelManagerOne2One();
  private final String temail1 = "a@email.com";
  private final String device1 = "device1";
  private final String temail2 = "b@email.com";
  private final String device2 = "device2";
  private final String temail3 = "c@email.com";

  @Test
  public void addSession() throws Exception {
    Collection<Session> sessionsToDelete = manager.addSession(temail1, device1, channel1);
    assertThat(sessionsToDelete).isEmpty();

    Channel channel = manager.getChannel(temail1, device1);
    assertThat(channel).isEqualTo(channel1);
    assertThat(manager.getChannels(temail1)).containsOnly(channel1);
  }

  @Test
  public void addSessionsFromDifferentDevices() throws Exception {
    Collection<Session> sessionsToDelete = manager.addSession(temail1, device1, channel1);
    assertThat(sessionsToDelete).isEmpty();

    sessionsToDelete = manager.addSession(temail1, device2, channel2);
    assertThat(sessionsToDelete).isEmpty();

    assertThat(manager.getChannels(temail1)).containsOnly(channel1, channel2);
  }

  @Test
  public void replaceChannelWhenAddSessionOnNewChannel() throws Exception {
    manager.addSession(temail1, device1, channel1);

    Collection<Session> sessionsToDelete = manager.addSession(temail1, device1, channel2);
    assertThat(sessionsToDelete).isEmpty();

    Channel channel = manager.getChannel(temail1, device1);
    assertThat(channel).isEqualTo(channel2);
  }

  @Test
  public void returnSessionsToDeleteWhenAddDifferentSessionOnNewChannel() throws Exception {
    manager.addSession(temail1, device1, channel1);

    Collection<Session> sessionsToDelete = manager.addSession(temail2, device1, channel2);
    assertThat(sessionsToDelete).isNotEmpty();

    Session session = sessionsToDelete.iterator().next();
    assertThat(session.getTemail()).isEqualTo(temail1);
    assertThat(session.getDeviceId()).isEqualTo(device1);

    Channel channel = manager.getChannel(temail2, device1);
    assertThat(channel).isEqualTo(channel2);

    assertThat(manager.getChannel(temail1, device1)).isNull();
  }

  @Test
  public void retainChannelWhenAddSessionWithDifferentDevice() throws Exception {
    manager.addSession(temail1, device1, channel1);

    Collection<Session> sessionsToDelete = manager.addSession(temail1, device2, channel2);
    assertThat(sessionsToDelete).isEmpty();

    Channel channel = manager.getChannel(temail1, device1);
    assertThat(channel).isEqualTo(channel1);

    channel = manager.getChannel(temail1, device2);
    assertThat(channel).isEqualTo(channel2);
  }

  @Test
  public void existedSessionWillNotBeDeletedWhenAddSessionAgain() throws Exception {
    manager.addSession(temail1, device1, channel1);
    manager.addSession(temail2, device1, channel1);
    manager.addSession(temail3, device1, channel1);

    assertThat(manager.getChannel(temail1, device1)).isEqualTo(channel1);
    assertThat(manager.getChannel(temail2, device1)).isEqualTo(channel1);
    assertThat(manager.getChannel(temail3, device1)).isEqualTo(channel1);

    Collection<Session> sessionToDelete = manager.addSession(temail1, device1, channel2);
    assertThat(sessionToDelete).isNotEmpty();
    assertThat(sessionToDelete).containsOnly(new Session(temail2, device1), new Session(temail3, device1));

    assertThat(manager.getChannel(temail1, device1)).isEqualTo(channel2);
    assertThat(manager.getChannel(temail2, device1)).isNull();
    assertThat(manager.getChannel(temail3, device1)).isNull();
  }

  @Test
  public void removeSession() throws Exception {
    manager.addSession(temail1, device1, channel1);
    Collection<Session> sessions = manager.removeSession(temail1, device1, channel1);

    assertThat(sessions).containsOnly(new Session(temail1, device1));

    assertThat(manager.getChannel(temail1, device1)).isNull();
    assertThat(manager.getChannels(temail1)).isEmpty();
    verify(channel1, never()).close();
  }

  @Test
  public void allSessionsRemovedWhenRemoveChannel() throws Exception {
    manager.addSession(temail1, device1, channel1);
    manager.addSession(temail2, device1, channel1);

    Collection<Session> sessionsToDelete = manager.removeChannel(channel1);
    assertThat(sessionsToDelete).isNotEmpty();
    assertThat(sessionsToDelete).containsOnly(new Session(temail1, device1), new Session(temail2, device1));

    assertThat(manager.getChannel(temail1, device1)).isNull();
    assertThat(manager.getChannel(temail2, device1)).isNull();
  }
}
