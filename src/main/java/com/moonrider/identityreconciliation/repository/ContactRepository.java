package com.moonrider.identityreconciliation.repository;

import com.moonrider.identityreconciliation.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Contact entity operations
 * Provides custom queries for identity reconciliation logic
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {

    /**
     * Find contacts by email address (excluding deleted contacts)
     */
    List<Contact> findByEmailAndDeletedAtIsNull(String email);

    /**
     * Find contacts by phone number (excluding deleted contacts)
     */
    List<Contact> findByPhoneNumberAndDeletedAtIsNull(String phoneNumber);

    /**
     * Find contacts by email or phone number (excluding deleted contacts)
     */
    @Query("SELECT c FROM Contact c WHERE (c.email = :email OR c.phoneNumber = :phoneNumber) AND c.deletedAt IS NULL")
    List<Contact> findByEmailOrPhoneNumber(@Param("email") String email, @Param("phoneNumber") String phoneNumber);

    /**
     * Find all contacts in a link chain by primary contact ID
     */
    @Query("SELECT c FROM Contact c WHERE (c.id = :primaryId OR c.linkedId = :primaryId) AND c.deletedAt IS NULL ORDER BY c.linkPrecedence, c.createdAt")
    List<Contact> findAllInLinkChain(@Param("primaryId") Integer primaryId);

    /**
     * Find primary contact by linked ID
     */
    Optional<Contact> findByIdAndLinkPrecedenceAndDeletedAtIsNull(Integer id, Contact.LinkPrecedence linkPrecedence);

    /**
     * Find all secondary contacts linked to a primary contact
     */
    List<Contact> findByLinkedIdAndDeletedAtIsNullOrderByCreatedAt(Integer linkedId);
}