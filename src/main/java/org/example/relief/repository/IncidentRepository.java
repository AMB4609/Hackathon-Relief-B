package org.example.relief.repository;

import com.google.common.io.Files;
import org.example.relief.model.Incident;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    @Query("SELECT i FROM Incident i WHERE ST_DWithin(i.location, :point, :distance)")
    List<Incident> findIncidentsWithinDistance(@Param("point") Point point, @Param("distance") Double distance);
}
