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
  private final Channel channel3 = Mockito.mock(Channel.class, "channel3");

  private final ChannelManagerOne2One manager = new ChannelManagerOne2One();
  private final String temail1 = "a@email.com";
  private final String device1 = "device1";
  private final String temail2 = "b@email.com";
  private final String device2 = "device2";
  private final String temail3 = "c@email.com";
  private final String device3 = "device3";

  @Test
  public void addSession() {
    Collection<Session> sessionsToDelete = manager.addSession(temail1, device1, channel1);
    assertThat(sessionsToDelete).isEmpty();

    boolean channel = manager.hasSession(temail1, device1, channel1);
    assertThat(channel).isEqualTo(true);
    assertThat(manager.getChannels(temail1)).containsOnly(channel1);
    verify(channel1, never()).close();
  }

  @Test
  public void addSessionsFromDifferentDevices() {
    Collection<Session> sessionsToDelete = manager.addSession(temail1, device1, channel1);
    assertThat(sessionsToDelete).isEmpty();

    sessionsToDelete = manager.addSession(temail1, device2, channel2);
    assertThat(sessionsToDelete).isEmpty();

    assertThat(manager.getChannels(temail1)).containsOnly(channel1, channel2);
    verify(channel1, never()).close();
    verify(channel2, never()).close();
  }

  @Test
  public void replaceChannelWhenAddSessionOnNewChannel() {
    manager.addSession(temail1, device1, channel1);

    Collection<Session> sessionsToDelete = manager.addSession(temail1, device1, channel2);
    assertThat(sessionsToDelete).isEmpty();

    boolean channel = manager.hasSession(temail1, device1, channel2);
    assertThat(channel).isEqualTo(true);
    verify(channel1).close();
    verify(channel2, never()).close();
  }

  @Test
  public void returnSessionsToDeleteWhenAddDifferentSessionOnNewChannel() {
    manager.addSession(temail1, device1, channel1);

    Collection<Session> sessionsToDelete = manager.addSession(temail2, device1, channel2);
    assertThat(sessionsToDelete).isNotEmpty();

    Session session = sessionsToDelete.iterator().next();
    assertThat(session.getTemail()).isEqualTo(temail1);
    assertThat(session.getDeviceId()).isEqualTo(device1);

    boolean channel = manager.hasSession(temail2, device1, channel2);
    assertThat(channel).isEqualTo(true);

    assertThat(manager.hasSession(temail1, device1, channel1)).isFalse();
    assertThat(manager.hasSession(temail1, device1, channel2)).isFalse();
    verify(channel1).close();
    verify(channel2, never()).close();
  }

  @Test
  public void existedSessionWillNotBeDeletedWhenAddSessionAgain() {
    manager.addSession(temail1, device1, channel1);
    manager.addSession(temail2, device1, channel1);
    manager.addSession(temail3, device1, channel1);

    assertThat(manager.hasSession(temail1, device1, channel1)).isTrue();
    assertThat(manager.hasSession(temail2, device1, channel1)).isTrue();
    assertThat(manager.hasSession(temail3, device1, channel1)).isTrue();

    Collection<Session> sessionToDelete = manager.addSession(temail1, device1, channel2);
    assertThat(sessionToDelete).isNotEmpty();
    assertThat(sessionToDelete).containsOnly(new Session(temail2, device1), new Session(temail3, device1));

    assertThat(manager.hasSession(temail1, device1, channel2)).isTrue();
    assertThat(manager.hasSession(temail2, device1, channel2)).isFalse();
    assertThat(manager.hasSession(temail3, device1, channel2)).isFalse();
    verify(channel1).close();
    verify(channel2, never()).close();
  }

  @Test
  public void removeSession() {
    manager.addSession(temail1, device1, channel1);
    manager.removeSession(temail1, device1, channel1);

    assertThat(manager.hasSession(temail1, device1, channel1)).isFalse();
    assertThat(manager.getChannels(temail1)).isEmpty();
    verify(channel1, never()).close();
  }

  @Test
  public void allSessionsRemovedWhenRemoveChannel() {
    manager.addSession(temail1, device1, channel1);
    manager.addSession(temail2, device1, channel1);

    Collection<Session> sessionsToDelete = manager.removeChannel(channel1);
    assertThat(sessionsToDelete).isNotEmpty();
    assertThat(sessionsToDelete).containsOnly(new Session(temail1, device1), new Session(temail2, device1));

    assertThat(manager.hasSession(temail1, device1, channel1)).isFalse();
    assertThat(manager.hasSession(temail2, device1, channel1)).isFalse();
    verify(channel1, never()).close();
  }

  @Test
  public void emptySessionIfRemovingInactiveChannel() {
    manager.addSession(temail1, device1, channel1);
    manager.addSession(temail2, device1, channel1);

    manager.removeSession(temail1, device1, channel1);
    manager.removeSession(temail2, device1, channel1);

    Collection<Session> sessionsToDelete = manager.removeChannel(channel1);
    assertThat(sessionsToDelete).isEmpty();
    verify(channel1, never()).close();
  }

  @Test
  public void noSuchSession() {
    assertThat(manager.hasSession(temail1, device1, channel1)).isFalse();
    assertThat(manager.getChannels(temail1)).isEmpty();
  }

  @Test
  public void exceptChannelByDesignedDeviceId(){
    manager.addSession(temail1,device1,channel1);
    manager.addSession(temail1,device2,channel2);
    manager.addSession(temail1,device3,channel3);
    Iterable<Channel> channels = manager.getChannelsExceptSenderN(temail1, temail1, device2);
    assertThat(channels).doesNotContain(channel2);
    assertThat(channels).containsOnly(channel1, channel3);
  }

}
