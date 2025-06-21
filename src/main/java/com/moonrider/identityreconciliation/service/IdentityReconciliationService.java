package com.moonrider.identityreconciliation.service;

import com.moonrider.identityreconciliation.dto.IdentifyRequest;
import com.moonrider.identityreconciliation.dto.IdentifyResponse;
import com.moonrider.identityreconciliation.model.Contact;
import com.moonrider.identityreconciliation.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core service for identity reconciliation logic
 * Handles contact linking and consolidation across multiple purchases
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityReconciliationService {

    private final ContactRepository contactRepository;

    /**
     * Main identity reconciliation method
     * Processes incoming contact information and returns consolidated data
     */
    @Transactional
    public IdentifyResponse identifyContact(IdentifyRequest request) {
        log.info("Processing identity reconciliation for email: {}, phone: {}",
                request.getEmail(), request.getPhoneNumber());

        // Find existing contacts matching email or phone
        List<Contact> existingContacts = findExistingContacts(request);

        if (existingContacts.isEmpty()) {
            // No existing contacts - create new primary contact
            return handleNewContact(request);
        }

        // Existing contacts found - determine linking strategy
        return handleExistingContacts(request, existingContacts);
    }

    /**
     * Find existing contacts matching the request criteria
     */
    private List<Contact> findExistingContacts(IdentifyRequest request) {
        Set<Contact> contacts = new HashSet<>();

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            contacts.addAll(contactRepository.findByEmailAndDeletedAtIsNull(request.getEmail()));
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            contacts.addAll(contactRepository.findByPhoneNumberAndDeletedAtIsNull(request.getPhoneNumber()));
        }

        return new ArrayList<>(contacts);
    }

    /**
     * Handle case where no existing contacts are found
     */
    private IdentifyResponse handleNewContact(IdentifyRequest request) {
        log.info("Creating new primary contact");

        Contact newContact = Contact.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .linkPrecedence(Contact.LinkPrecedence.PRIMARY)
                .build();

        Contact savedContact = contactRepository.save(newContact);

        return buildResponse(List.of(savedContact));
    }

    /**
     * Handle case where existing contacts are found
     */
    private IdentifyResponse handleExistingContacts(IdentifyRequest request, List<Contact> existingContacts) {
        // Check if request introduces new information
        boolean hasNewEmail = request.getEmail() != null &&
                existingContacts.stream().noneMatch(c -> request.getEmail().equals(c.getEmail()));
        boolean hasNewPhone = request.getPhoneNumber() != null &&
                existingContacts.stream().noneMatch(c -> request.getPhoneNumber().equals(c.getPhoneNumber()));

        if (hasNewEmail || hasNewPhone) {
            // New information detected - create secondary contact
            return handleNewInformation(request, existingContacts);
        }

        // No new information - return consolidated existing data
        return handleExistingInformation(existingContacts);
    }

    /**
     * Handle case where new information is provided
     */
    private IdentifyResponse handleNewInformation(IdentifyRequest request, List<Contact> existingContacts) {
        log.info("New information detected, creating secondary contact");

        // Find or determine primary contact
        Contact primaryContact = findOrCreatePrimaryContact(existingContacts);

        // Check if we need to merge multiple primary contacts
        List<Contact> primaryContacts = existingContacts.stream()
                .filter(c -> c.getLinkPrecedence() == Contact.LinkPrecedence.PRIMARY)
                .collect(Collectors.toList());

        if (primaryContacts.size() > 1) {
            // Multiple primaries detected - merge them
            primaryContact = mergePrimaryContacts(primaryContacts);
        }

        // Create secondary contact with new information
        Contact secondaryContact = Contact.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .linkedId(primaryContact.getId())
                .linkPrecedence(Contact.LinkPrecedence.SECONDARY)
                .build();

        contactRepository.save(secondaryContact);

        // Return consolidated data
        return buildConsolidatedResponse(primaryContact.getId());
    }

    /**
     * Handle case where no new information is provided
     */
    private IdentifyResponse handleExistingInformation(List<Contact> existingContacts) {
        log.info("No new information, returning existing consolidated data");

        // Find primary contact
        Contact primaryContact = findOrCreatePrimaryContact(existingContacts);

        return buildConsolidatedResponse(primaryContact.getId());
    }

    /**
     * Find primary contact or convert oldest to primary
     */
    private Contact findOrCreatePrimaryContact(List<Contact> contacts) {
        // Look for existing primary
        Optional<Contact> primary = contacts.stream()
                .filter(c -> c.getLinkPrecedence() == Contact.LinkPrecedence.PRIMARY)
                .findFirst();

        if (primary.isPresent()) {
            return primary.get();
        }

        // No primary found - convert oldest to primary
        Contact oldest = contacts.stream()
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow(() -> new IllegalStateException("No contacts available"));

        oldest.setLinkPrecedence(Contact.LinkPrecedence.PRIMARY);
        oldest.setLinkedId(null);

        return contactRepository.save(oldest);
    }

    /**
     * Merge multiple primary contacts into one
     */
    private Contact mergePrimaryContacts(List<Contact> primaryContacts) {
        log.info("Merging {} primary contacts", primaryContacts.size());

        // Keep the oldest as primary
        Contact keepPrimary = primaryContacts.stream()
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow();

        // Convert others to secondary
        primaryContacts.stream()
                .filter(c -> !c.getId().equals(keepPrimary.getId()))
                .forEach(c -> {
                    c.setLinkPrecedence(Contact.LinkPrecedence.SECONDARY);
                    c.setLinkedId(keepPrimary.getId());
                    contactRepository.save(c);
                });

        return keepPrimary;
    }

    /**
     * Build consolidated response for a primary contact
     */
    private IdentifyResponse buildConsolidatedResponse(Integer primaryContactId) {
        List<Contact> allContacts = contactRepository.findAllInLinkChain(primaryContactId);
        return buildResponse(allContacts);
    }

    /**
     * Build response DTO from contact list
     */
    private IdentifyResponse buildResponse(List<Contact> contacts) {
        if (contacts.isEmpty()) {
            throw new IllegalStateException("No contacts to build response from");
        }

        // Find primary contact
        Contact primary = contacts.stream()
                .filter(c -> c.getLinkPrecedence() == Contact.LinkPrecedence.PRIMARY)
                .findFirst()
                .orElse(contacts.get(0)); // Fallback to first if no primary found

        // Collect all unique emails and phone numbers
        Set<String> emails = contacts.stream()
                .map(Contact::getEmail)
                .filter(Objects::nonNull)
                .filter(email -> !email.trim().isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> phoneNumbers = contacts.stream()
                .map(Contact::getPhoneNumber)
                .filter(Objects::nonNull)
                .filter(phone -> !phone.trim().isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Collect secondary contact IDs
        List<Integer> secondaryIds = contacts.stream()
                .filter(c -> c.getLinkPrecedence() == Contact.LinkPrecedence.SECONDARY)
                .map(Contact::getId)
                .sorted()
                .collect(Collectors.toList());

        IdentifyResponse.ContactInfo contactInfo = IdentifyResponse.ContactInfo.builder()
                .primaryContactId(primary.getId())
                .emails(new ArrayList<>(emails))
                .phoneNumbers(new ArrayList<>(phoneNumbers))
                .secondaryContactIds(secondaryIds)
                .build();

        return IdentifyResponse.builder()
                .contact(contactInfo)
                .build();
    }
}