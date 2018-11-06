package com.syswin.temail.ps.common.packet;

import com.syswin.temail.ps.common.entity.CDTPPacket;

/**
 * @author 姚华成
 * @date 2018-11-06
 */
public interface PacketDecryptor {

  void decrypt(CDTPPacket packet);
}
