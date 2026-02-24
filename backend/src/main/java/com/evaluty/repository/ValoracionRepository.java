package com.evaluty.repository;

import com.evaluty.model.Valoracion;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ValoracionRepository extends MongoRepository<Valoracion, String> {
    List<Valoracion> findByUserId(String userId);
    List<Valoracion> findByNumeroCatastro(String numeroCatastro);
}
