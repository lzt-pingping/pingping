package com.pingping.item.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pingping.common.enums.ExceptionEnum;
import com.pingping.common.exceptions.LyException;
import com.pingping.common.utils.BeanHelper;
import com.pingping.item.dto.SpecGroupDTO;
import com.pingping.item.dto.SpecParamDTO;
import com.pingping.item.entity.SpecGroup;
import com.pingping.item.entity.SpecParam;
import com.pingping.item.mapper.SpecGroupMapper;
import com.pingping.item.mapper.SpecParamMapper;
import com.pingping.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class SpecServiceImpl implements SpecService {

    @Autowired
    private SpecGroupMapper groupMapper;

    @Autowired
    private SpecParamMapper paramMapper;

    @Override
    public List<SpecGroupDTO> queryGroupByCategory(Long id) {
        // 查询规格组
        SpecGroup group = new SpecGroup();
        group.setCid(id);

        List<SpecGroup> groups = groupMapper.selectList(new QueryWrapper<SpecGroup>(group));

        //列表为空的判断
        if (CollectionUtils.isEmpty(groups)) {
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        // 对象转换
        return BeanHelper.copyWithCollection(groups, SpecGroupDTO.class);
    }

    @Override
    public List<SpecParamDTO> querySpecParams(Long gid, Long cid, Boolean searching) {
        //根据分类id查，根据规格组id查，是否是用于搜索项
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);

        //封装查询条件
        QueryWrapper<SpecParam> wrapper = new QueryWrapper<>(specParam);

        List<SpecParam> specParams = paramMapper.selectList(wrapper);

        //如果集合为空抛自定义异常
        if(CollectionUtils.isEmpty(specParams)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(specParams, SpecParamDTO.class);
    }
}