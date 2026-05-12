package com.digitalid.cli;

import com.digitalid.audit.AuditEvent;
import com.digitalid.audit.AuditLogger;
import com.digitalid.audit.InMemoryAuditLogger;
import com.digitalid.auth.AuthorisationManager;
import com.digitalid.auth.AuthorisationManagerImpl;
import com.digitalid.consumption.IdentityConsumptionService;
import com.digitalid.consumption.IdentityConsumptionServiceImpl;
import com.digitalid.consumption.VerificationResponse;
import com.digitalid.management.IdentityManager;
import com.digitalid.management.IdentityManagerImpl;
import com.digitalid.management.InMemoryDigitalIdRepository;
import com.digitalid.model.DigitalId;
import com.digitalid.model.OrganisationType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Cli {

    private enum MenuResult { CONTINUE, SWITCH, EXIT }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Scanner scanner = new Scanner(System.in);
    private final AuditLogger auditLogger = new InMemoryAuditLogger();
    private final AuthorisationManager authManager = new AuthorisationManagerImpl();

    private final IdentityManager identityManager;
    private final IdentityConsumptionService taxService;
    private final IdentityConsumptionService licenceService;
    private final IdentityConsumptionService employerService;
    private final IdentityConsumptionService bankService;

    // Tracks identities created in this session for the ID-selection menu
    private final List<DigitalId> knownIdentities = new ArrayList<>();

    public Cli() {
        identityManager = new IdentityManagerImpl(OrganisationType.CENTRAL_AUTHORITY, new InMemoryDigitalIdRepository(), authManager, auditLogger);
        taxService     = new IdentityConsumptionServiceImpl(identityManager, OrganisationType.TAX_AUTHORITY,              authManager, auditLogger);
        licenceService = new IdentityConsumptionServiceImpl(identityManager, OrganisationType.DRIVING_LICENCE_AUTHORITY,  authManager, auditLogger);
        employerService = new IdentityConsumptionServiceImpl(identityManager, OrganisationType.EMPLOYER,                  authManager, auditLogger);
        bankService    = new IdentityConsumptionServiceImpl(identityManager, OrganisationType.BANK,                       authManager, auditLogger);
    }

    public void start() {
        System.out.println("Digital ID Management System:");
        outer:
        while (true) {
            OrganisationType org = selectOrganisation();
            MenuResult result = MenuResult.CONTINUE;
            while (result == MenuResult.CONTINUE) {
                result = runMenuStep(org);
            }
            if (result == MenuResult.EXIT) break outer;
        }
        System.out.println("Goodbye.");
        scanner.close();
    }

    // Organisation selection
    private OrganisationType selectOrganisation() {
        OrganisationType[] types = OrganisationType.values();
        while (true) {
            System.out.println("\nSelect organisation:");
            for (int i = 0; i < types.length; i++) {
                System.out.printf("  %d. %s%n", i + 1, types[i]);
            }
            System.out.print("Choice: ");
            int choice = readInt();
            if (choice >= 1 && choice <= types.length) {
                return types[choice - 1];
            }
            System.out.println("Invalid choice, try again.");
        }
    }

    // Menu display and dispatch
    private MenuResult runMenuStep(OrganisationType org) {
        printMenu(org);
        int choice = readInt();
        return handleChoice(org, choice);
    }

    private void printMenu(OrganisationType org) {
        System.out.println("\n " + org);
        switch (org) {
            case CENTRAL_AUTHORITY -> {
                System.out.println("  1. Create identity");
                System.out.println("  2. Update address");
                System.out.println("  3. Update email");
                System.out.println("  4. Update temporary restriction");
                System.out.println("  5. Suspend identity");
                System.out.println("  6. Reactivate identity");
                System.out.println("  7. Revoke identity");
                System.out.println("  8. Print audit log");
                System.out.println("  9. Switch organisation");
                System.out.println("  0. Exit");
            }
            case TAX_AUTHORITY -> {
                System.out.println("  1. Check tax eligibility");
                System.out.println("  2. Print audit log");
                System.out.println("  3. Switch organisation");
                System.out.println("  0. Exit");
            }
            case DRIVING_LICENCE_AUTHORITY -> {
                System.out.println("  1. Check licence eligibility");
                System.out.println("  2. Print audit log");
                System.out.println("  3. Switch organisation");
                System.out.println("  0. Exit");
            }
            case EMPLOYER, BANK -> {
                System.out.println("  1. Check validity");
                System.out.println("  2. Print audit log");
                System.out.println("  3. Switch organisation");
                System.out.println("  0. Exit");
            }
        }
        System.out.print("Choice: ");
    }

    private MenuResult handleChoice(OrganisationType org, int choice) {
        return switch (org) {
            case CENTRAL_AUTHORITY -> handleManagementChoice(choice);
            case TAX_AUTHORITY, DRIVING_LICENCE_AUTHORITY, EMPLOYER, BANK -> handleConsumptionChoice(org, choice);
        };
    }

    private MenuResult handleManagementChoice(int choice) {
        switch (choice) {
            case 1 -> createIdentity();
            case 2 -> updateAddress();
            case 3 -> updateEmail();
            case 4 -> updateTemporaryRestriction();
            case 5 -> suspendIdentity();
            case 6 -> reactivateIdentity();
            case 7 -> revokeIdentity();
            case 8 -> printAuditLog();
            case 9 -> { return MenuResult.SWITCH; }
            case 0 -> { return MenuResult.EXIT; }
            default -> System.out.println("Invalid choice.");
        }
        return MenuResult.CONTINUE;
    }

    private MenuResult handleConsumptionChoice(OrganisationType org, int choice) {
        switch (choice) {
            case 1 -> runConsumptionOperation(org);
            case 2 -> printAuditLog();
            case 3 -> { return MenuResult.SWITCH; }
            case 0 -> { return MenuResult.EXIT; }
            default -> System.out.println("Invalid choice.");
        }
        return MenuResult.CONTINUE;
    }

    private void runConsumptionOperation(OrganisationType org) {
        IdentityConsumptionService service = consumptionServiceFor(org);
        switch (org) {
            case TAX_AUTHORITY            -> checkTaxEligibility(service);
            case DRIVING_LICENCE_AUTHORITY -> checkLicenceEligibility(service);
            case EMPLOYER, BANK           -> checkValidity(service);
            default                        -> System.out.println("No consumption operation defined for " + org);
        }
    }

    // Management operations (CENTRAL AUTHORITY)
    private void createIdentity() {
        String nationalIdNumber = readLine("National ID number: ");
        LocalDate dateOfBirth   = readDate("Date of birth");
        String fullName         = readLine("Full name: ");
        String address          = readLine("Address (blank for none): ");
        String email            = readLine("Email (blank for none): ");
        try {
            DigitalId id = identityManager.createIdentity(
                    nationalIdNumber, dateOfBirth, fullName,
                    address.isEmpty() ? null : address,
                    email.isEmpty()   ? null : email);
            knownIdentities.add(id);
            System.out.println("Created: " + id.getId() + "  " + id.getFullName());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateAddress() {
        UUID id = selectId();
        if (id == null) return;
        String address = readLine("New address: ");
        try {
            identityManager.updateAddress(id, address);
            System.out.println("Address updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateEmail() {
        UUID id = selectId();
        if (id == null) return;
        String email = readLine("New email: ");
        try {
            identityManager.updateEmail(id, email);
            System.out.println("Email updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateTemporaryRestriction() {
        UUID id = selectId();
        if (id == null) return;
        System.out.print("Temporary restriction (true/false): ");
        String input = scanner.nextLine().trim().toLowerCase();
        if (!input.equals("true") && !input.equals("false")) {
            System.out.println("Invalid input. Enter 'true' or 'false'.");
            return;
        }
        boolean restriction = Boolean.parseBoolean(input);
        try {
            identityManager.updateTemporaryRestriction(id, restriction);
            System.out.println("Temporary restriction set to " + restriction + ".");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void suspendIdentity() {
        UUID id = selectId();
        if (id == null) return;
        String reason = readLine("Reason: ");
        try {
            identityManager.suspend(id, reason);
            System.out.println("Identity suspended.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void reactivateIdentity() {
        UUID id = selectId();
        if (id == null) return;
        String reason = readLine("Reason: ");
        try {
            identityManager.reactivate(id, reason);
            System.out.println("Identity reactivated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void revokeIdentity() {
        UUID id = selectId();
        if (id == null) return;
        String reason = readLine("Reason: ");
        try {
            identityManager.revoke(id, reason);
            System.out.println("Identity revoked.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Consumption operations
    private void checkTaxEligibility(IdentityConsumptionService service) {
        UUID id = selectId();
        if (id == null) return;
        LocalDate start = readDate("Period start date");
        LocalDate end   = readDate("Period end date");
        try {
            VerificationResponse response = service.checkTaxEligibility(id, start, end);
            printVerificationResult(response);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void checkLicenceEligibility(IdentityConsumptionService service) {
        UUID id = selectId();
        if (id == null) return;
        try {
            VerificationResponse response = service.checkLicenceEligibility(id);
            printVerificationResult(response);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void checkValidity(IdentityConsumptionService service) {
        UUID id = selectId();
        if (id == null) return;
        try {
            VerificationResponse response = service.checkValidity(id);
            printVerificationResult(response);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void printVerificationResult(VerificationResponse response) {
        System.out.println((response.isValid() ? "RESULT: valid   — " : "RESULT: invalid — ") + response.getReason());
    }

    // Audit log
    private void printAuditLog() {
        List<AuditEvent> log = auditLogger.getLog();
        if (log.isEmpty()) {
            System.out.println("Audit log is empty.");
            return;
        }
        System.out.println("\n--- Audit Log (" + log.size() + " events) ---");
        for (AuditEvent event : log) {
            String shortId = event.getDigitalIdId() != null
                    ? event.getDigitalIdId().toString().substring(0, 8) + "..."
                    : "none";
            System.out.printf("[%s] %-28s %-30s id=%-12s %s  %s%n",
                    event.getTimestamp().format(TIMESTAMP_FMT),
                    event.getOrganisationType(),
                    event.getOperationName(),
                    shortId,
                    event.isSuccess() ? "SUCCESS" : "FAILURE",
                    event.getDetails());
        }
    }

    // ID selection helper
    private UUID selectId() {
        if (knownIdentities.isEmpty()) {
            System.out.println("No identities exist yet. Create one as CENTRAL_AUTHORITY first.");
            return null;
        }
        System.out.println("Select a Digital ID:");
        for (int i = 0; i < knownIdentities.size(); i++) {
            DigitalId d = knownIdentities.get(i);
            System.out.printf("  %d. [%s] %s (%s)%n", i + 1,
                    d.getNationalIdNumber(), d.getFullName(), d.getStatus());
        }
        System.out.println("  0. Enter UUID directly");
        System.out.print("Choice: ");
        int choice = readInt();
        if (choice == 0) {
            String raw = readLine("UUID: ");
            try {
                return UUID.fromString(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid UUID format.");
                return null;
            }
        }
        if (choice >= 1 && choice <= knownIdentities.size()) {
            return knownIdentities.get(choice - 1).getId();
        }
        System.out.println("Invalid choice.");
        return null;
    }

    // Input helpers
    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (yyyy-MM-dd): ");
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input, DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date, use yyyy-MM-dd.");
            }
        }
    }

    private IdentityConsumptionService consumptionServiceFor(OrganisationType org) {
        return switch (org) {
            case TAX_AUTHORITY             -> taxService;
            case DRIVING_LICENCE_AUTHORITY -> licenceService;
            case EMPLOYER                  -> employerService;
            case BANK                      -> bankService;
            default -> throw new IllegalStateException("No consumption service for " + org);
        };
    }
}