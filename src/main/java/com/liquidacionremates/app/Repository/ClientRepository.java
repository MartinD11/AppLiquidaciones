package com.liquidacionremates.app.Repository;

import com.liquidacionremates.app.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client,Long> {

    Optional<Client> findByNameAndLastName(String name, String lastName);

    List<Client> findByActiveTrue();

    @Query("SELECT c FROM Client c WHERE LOWER(CONCAT(c.name, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) AND c.active = true")
    List<Client> searchByFullName(@Param("query") String query);

    @Query("SELECT c FROM Client c WHERE LOWER(CONCAT(c.name, ' ', c.lastName)) LIKE LOWER(CONCAT('%' , :query, '%'))")
    List<Client> searchHistoricalByFullName(@Param("query") String query);

    Optional<Client> findByNameIgnoreCaseAndLastNameIgnoreCase(String name, String lastName);

    Collection<Client> findByNameContainingIgnoreCase(String sellerName);
}
