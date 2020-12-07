package com.pingping.item.service;

import com.pingping.item.dto.SpecGroupDTO;
import com.pingping.item.dto.SpecParamDTO;

import java.util.List;

public interface SpecService {
    List<SpecGroupDTO> queryGroupByCategory(Long cid);

    List<SpecParamDTO> querySpecParams(Long gid, Long cid, Boolean searching);
}
