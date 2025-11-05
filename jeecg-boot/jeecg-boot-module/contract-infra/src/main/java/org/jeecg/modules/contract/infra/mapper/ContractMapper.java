package org.jeecg.modules.contract.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.contract.domain.Contract;

/**
 * 合同主档Mapper接口
 * @author: jeecg-boot
 * @date: 2024-05-20
 */
@Mapper
public interface ContractMapper extends BaseMapper<Contract> {

}
