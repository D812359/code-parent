package com.atu.service.impl;

import com.atu.dao.LearnDao;
import com.atu.domain.LearnResouce;
import com.atu.service.LearnService;
import com.atu.tools.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LearnServiceImpl implements LearnService {

    @Autowired
    LearnDao learnDao;

    @Override
    public int add(LearnResouce learnResouce) {
        return this.learnDao.add(learnResouce);
    }

    @Override
    public int update(LearnResouce learnResouce) {
        return this.learnDao.update(learnResouce);
    }

    @Override
    public int deleteByIds(String ids) {
        return this.learnDao.deleteByIds(ids);
    }

    @Override
    public LearnResouce queryLearnResouceById(Long id) {
        return this.learnDao.queryLearnResouceById(id);
    }

    @Override
    public Page queryLearnResouceList(Map<String, Object> params) {
        return this.learnDao.queryLearnResouceList(params);
    }
}
