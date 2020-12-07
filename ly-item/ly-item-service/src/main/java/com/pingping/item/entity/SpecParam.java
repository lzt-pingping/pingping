package com.pingping.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("tb_spec_param")
@Data
public class SpecParam {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long cid;
    private Long groupId;
    private String name;
    private Boolean isNumeric;
    private String unit;
    private Boolean generic;
    private Boolean searching;
    private String segments;
    private Date createTime;
    private Date updateTime;
}