package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.service.dto.ProdutoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Produto} and its DTO {@link ProdutoDTO}.
 */
@Mapper(componentModel = "spring", uses = { EmpresaMapper.class })
public interface ProdutoMapper extends EntityMapper<ProdutoDTO, Produto> {
    @Mapping(target = "empresa", source = "empresa", qualifiedByName = "id")
    ProdutoDTO toDto(Produto s);
}
