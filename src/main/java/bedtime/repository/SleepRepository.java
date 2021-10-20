package bedtime.repository;

import bedtime.models.Sleep;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SleepRepository extends MongoRepository<Sleep,String> {

    List<Sleep>  findByDateAndUsername(LocalDate date, String username);

    @Query(value = "{'date':{ $gte: ?1, $lte: ?2},  'username' : ?0}")
    List<Sleep> findByUsernameAndDateBetween(String username, LocalDate startDate, LocalDate endDate);

    @Query(value = "{'date':{$lte: ?1}, 'username' : ?0}")
    List<Sleep> findAllByUsernameAndDateLessThan(String username, LocalDate date);

    List<Sleep> findAllByLocation(String location);

    List<Sleep> findByFellAsleep(LocalDateTime timeSlept);
}
