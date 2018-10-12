package com.syswin.temail.ps.server.service;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.syswin.temail.ps.server.entity.Session;
import io.netty.channel.Channel;
import java.util.Iterator;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

public class ChannelHolderTest {

  private final Channel channel = Mockito.mock(Channel.class, "channel");
  private final Channel newChannel = Mockito.mock(Channel.class, "newChannel");

  private final ChannelHolder channelHolder = new ChannelHolder();
  private final String temail = uniquify("temail");
  private final String temail1 = uniquify("temail1");
  private final String deviceId1 = uniquify("deviceId1");
  private final String deviceId2 = uniquify("deviceId2");

  @Test
  public void hasNoSessionIfNoSuchChannel() {
    boolean hasNoSession = channelHolder.hasNoSession(channel);

    assertThat(hasNoSession).isTrue();
  }

  @Test
  public void hasNoSessionIfEmptySessions() {
    channelHolder.addSession(temail, deviceId1, channel);
    boolean hasNoSession = channelHolder.hasNoSession(channel);
    assertThat(hasNoSession).isFalse();

    channelHolder.removeSession(temail, deviceId1, channel);
    hasNoSession = channelHolder.hasNoSession(channel);

    assertThat(hasNoSession).isTrue();
  }

  @Test
  public void shouldAddSession() {
    channelHolder.addSession(temail, deviceId1, channel);

    Channel channel = channelHolder.getChannel(temail, deviceId1);
    assertThat(channel).isEqualTo(this.channel);

    Iterable<Channel> channels = channelHolder.getChannels(temail);
    assertThat(channels).containsExactly(this.channel);
  }

  @Test
  public void shouldNotAddDuplicateChannel() {
    int threads = 10;
    CyclicBarrier barrier = new CyclicBarrier(threads);
    Runnable runnable = () -> {
      try {
        barrier.await();
        channelHolder.addSession(temail, deviceId1, channel);
      } catch (InterruptedException | BrokenBarrierException e) {
        fail(e.getMessage());
      }
    };

    CompletableFuture[] futures = new CompletableFuture[threads];
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    for (int i = 0; i < threads; i++) {
      futures[i] = (CompletableFuture.runAsync(runnable, executorService));
    }

    CompletableFuture.allOf(futures).join();

    Iterable<Channel> channels = channelHolder.getChannels(temail);
    assertThat(channels).containsExactly(this.channel);

    Iterable<Session> sessions = channelHolder.removeChannel(channel);
    assertThat(sessions).hasSize(1);
  }

  // 会话管理与通道管理分享，通道不会被会话自动关闭
  @Ignore
  @Test
  public void shouldReplaceSession() {
    channelHolder.addSession(temail, deviceId1, channel);
    channelHolder.addSession(temail, deviceId1, newChannel);

    Channel channel = channelHolder.getChannel(temail, deviceId1);
    assertThat(channel).isEqualTo(this.newChannel);

    Iterable<Channel> channels = channelHolder.getChannels(temail);
    assertThat(channels).containsOnly(this.newChannel);

    assertThat(channelHolder.hasNoSession(this.channel)).isTrue();
    verify(this.channel).close();
    verify(this.newChannel, never()).close();
  }

  @Test
  public void shouldRetainChannelIfItHasOtherSessions() {
    channelHolder.addSession(temail, deviceId1, channel);
    channelHolder.addSession(temail1, deviceId1, channel);

    channelHolder.addSession(temail, deviceId1, newChannel);

    Channel channel = channelHolder.getChannel(temail, deviceId1);
    assertThat(channel).isEqualTo(this.newChannel);

    Iterable<Channel> channels = channelHolder.getChannels(temail);
    assertThat(channels).containsExactlyInAnyOrder(this.newChannel);

    channels = channelHolder.getChannels(temail1);
    assertThat(channels).containsExactlyInAnyOrder(this.channel);

    assertThat(channelHolder.hasNoSession(this.channel)).isFalse();
    verify(this.channel, never()).close();
    verify(this.newChannel, never()).close();
  }

  // 会话管理与通道管理分享，通道不会被会话自动关闭
  @Ignore
  @Test
  public void shouldRemoveSession() {
    channelHolder.addSession(temail, deviceId1, channel);
    channelHolder.addSession(temail1, deviceId1, channel);

    Iterable<Channel> channels = channelHolder.getChannels(temail);
    assertThat(channels).containsExactlyInAnyOrder(this.channel);

    channelHolder.removeSession(temail, deviceId1, channel);

    Channel channel = channelHolder.getChannel(temail, deviceId1);
    assertThat(channel).isNull();

    channels = channelHolder.getChannels(temail1);
    assertThat(channels).containsExactlyInAnyOrder(this.channel);

    channelHolder.removeSession(temail1, deviceId1, this.channel);

    channels = channelHolder.getChannels(temail);
    assertThat(channels).isEmpty();

    verify(this.channel).close();
  }

  // 会话管理与通道管理分享，通道不会被会话自动关闭
  @Ignore
  @Test
  public void shouldRemoveChannel() {
    channelHolder.addSession(temail, deviceId1, channel);
    channelHolder.addSession(temail1, deviceId1, channel);

    Iterable<Channel> channels = channelHolder.getChannels(temail);
    assertThat(channels).containsExactlyInAnyOrder(this.channel);

    Iterable<Session> sessions = channelHolder.removeChannel(channel);
    assertThat(sessions).hasSize(2);

    Iterator<Session> iterator = sessions.iterator();
    assertThat(iterator.next()).isEqualToComparingFieldByField(new Session(temail, deviceId1));
    assertThat(iterator.next()).isEqualToComparingFieldByField(new Session(temail1, deviceId1));

    channels = channelHolder.getChannels(temail);
    assertThat(channels).isEmpty();

    verify(this.channel).close();
  }

  // 会话管理与通道管理分享，通道不会被会话自动关闭
  @Ignore
  @Test
  public void shouldRetainSessionIfItHasOtherChannel() {
    channelHolder.addSession(temail, deviceId1, channel);
    channelHolder.addSession(temail1, deviceId1, channel);
    channelHolder.addSession(temail, deviceId2, newChannel);

    Iterable<Channel> channels = channelHolder.getChannels(temail);
    assertThat(channels).containsExactlyInAnyOrder(this.channel, this.newChannel);

    Iterable<Session> sessions = channelHolder.removeChannel(channel);
    assertThat(sessions).hasSize(2);
    Iterator<Session> iterator = sessions.iterator();
    assertThat(iterator.next()).isEqualToComparingFieldByField(new Session(temail, deviceId1));
    assertThat(iterator.next()).isEqualToComparingFieldByField(new Session(temail1, deviceId1));

    channels = channelHolder.getChannels(temail);
    assertThat(channels).containsExactly(this.newChannel);

    verify(this.channel).close();
    verify(this.newChannel, never()).close();
  }

  @Test
  public void shouldReturnEmptyCollectionIfNoSuchChannel() {
    Iterable<Session> sessions = channelHolder.removeChannel(channel);

    assertThat(sessions).isEmpty();
  }
}