package com.syswin.temail.ps.server.handler;

import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.exception.PacketException;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PacketHandlerTest {

  private final ChannelHandlerContext context = Mockito.mock(ChannelHandlerContext.class);
  private final Channel channel = Mockito.mock(Channel.class);

  private final SessionService sessionService = Mockito.mock(SessionService.class);
  private final RequestService requestService = Mockito.mock(RequestService.class);

  private final CDTPPacket packet = new CDTPPacket();
  private final CDTPHeader header = new CDTPHeader();
  private final PacketHandler packetHandler = new PacketHandler(sessionService, requestService);

  @Before
  public void setUp() {
    when(context.channel()).thenReturn(channel);
    packet.setHeader(header);
  }

  @Test
  public void blowsUpIfNoSenderProvider() {
    try {
      header.setDeviceId("iPhoneX");
      packetHandler.channelRead0(context, packet);
      expectFailing(PacketException.class);
    } catch (PacketException e) {
      assertThat(e.getPacket()).isEqualTo(packet);
    }
  }

  @Test
  public void blowsUpIfNoDeviceIdProvider() {
    try {
      header.setSender("sean@t.email");
      packetHandler.channelRead0(context, packet);
      expectFailing(PacketException.class);
    } catch (PacketException e) {
      assertThat(e.getPacket()).isEqualTo(packet);
    }
  }

  @Test
  public void blowsUpIfNoHeader() {
    try {
      packet.setHeader(null);
      packetHandler.channelRead0(context, packet);
      expectFailing(PacketException.class);
    } catch (PacketException e) {
      assertThat(e.getPacket()).isEqualTo(packet);
    }
  }
}