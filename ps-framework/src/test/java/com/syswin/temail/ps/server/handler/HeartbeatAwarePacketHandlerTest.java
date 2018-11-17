package com.syswin.temail.ps.server.handler;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.LOGIN_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.LOGOUT_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.PING_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.server.service.HeartBeatService;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class HeartbeatAwarePacketHandlerTest {

  private final ChannelHandlerContext context = Mockito.mock(ChannelHandlerContext.class);
  private final Channel channel = Mockito.mock(Channel.class);

  private final HeartBeatService heartBeatService = Mockito.mock(HeartBeatService.class);
  private final SessionService sessionService = Mockito.mock(SessionService.class);
  private final RequestService requestService = Mockito.mock(RequestService.class);

  private final CDTPPacket packet = new CDTPPacket();
  private final CDTPHeader header = new CDTPHeader();
  private final HeartbeatAwarePacketHandler packetHandler = new HeartbeatAwarePacketHandler(sessionService, requestService, heartBeatService);

  @Before
  public void setUp() {
    when(context.channel()).thenReturn(channel);
    header.setDeviceId("iPhoneX");
    header.setSender("sean@t.email");
    packet.setHeader(header);
  }

  @Test
  public void handlesHeartbeat() {
    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(PING_CODE);

    packetHandler.channelRead0(context, packet);

    verify(heartBeatService).pong(channel, packet);
    verify(sessionService, never()).login(channel, packet);
    verify(sessionService, never()).logout(channel, packet);
    verify(requestService, never()).handleRequest(same(packet), any());
  }

  @Test
  public void handlesLogin() {
    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(LOGIN_CODE);

    packetHandler.channelRead0(context, packet);

    verify(heartBeatService, never()).pong(channel, packet);
    verify(sessionService).login(channel, packet);
    verify(sessionService, never()).logout(channel, packet);
    verify(requestService, never()).handleRequest(same(packet), any());
  }

  @Test
  public void handlesLogout() {
    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(LOGOUT_CODE);

    packetHandler.channelRead0(context, packet);

    verify(heartBeatService, never()).pong(channel, packet);
    verify(sessionService, never()).login(channel, packet);
    verify(sessionService).logout(channel, packet);
    verify(requestService, never()).handleRequest(same(packet), any());
  }

  @Test
  public void ignoreUnknownCommand() {
    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand((short) 10);

    packetHandler.channelRead0(context, packet);

    verify(heartBeatService, never()).pong(channel, packet);
    verify(sessionService, never()).login(channel, packet);
    verify(sessionService, never()).logout(channel, packet);
    verify(requestService, never()).handleRequest(same(packet), any());
  }

  @Test
  public void handlesRequest() {
    packet.setCommandSpace(SINGLE_MESSAGE_CODE);
    packet.setCommand(SINGLE_MESSAGE_CODE);

    packetHandler.channelRead0(context, packet);

    verify(heartBeatService, never()).pong(channel, packet);
    verify(sessionService, never()).login(channel, packet);
    verify(sessionService, never()).logout(channel, packet);
    verify(requestService).handleRequest(same(packet), any());
  }
}