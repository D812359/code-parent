package com.atu.redpacket.domain.service;

import com.atu.redpacket.domain.model.RedPacketAccount;

/**
 * @author: Tom
 * @date: 2020-07-22 14:27
 * @description:
 */
public interface RedPacketService {
    public RedPacketAccount findByUserId(long userId);
    public void save(RedPacketAccount redPacketAccount);
}