package dev.cyberjar.embabeldemo.civilian.repository;

import dev.cyberjar.embabeldemo.civilian.domain.Civilian;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CivilianRepository extends MongoRepository<Civilian, String>, CivilianRepositoryCustom {

    Optional<Civilian> findById(String id);

    Optional<Civilian> findByNationalId(String nationalId);

    boolean existsById(String id);

    boolean existsByNationalId(String nationalId);

    List<CivilianSummary> findAllByUnderSurveillance(boolean underSurveillance);

    /*
    Alternative to MongoTemplate:

    @Query("{ 'implants': { $elemMatch: { 'lotNumber': { $gte: ?0 } } } }")
    List<Civilian> findAllByImplantLotNumberGreaterThanEqual(int lotNumber);

     */


}