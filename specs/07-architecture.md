# Architecture Specification

## 1. Architectural style

The system uses a layered architecture with MVC at the boundary. A request enters through a presentation controller, is translated to an application command, and is coordinated by an application service. Domain entities, value objects, policies and domain services enforce business rules. Repositories expose persistence abstractions from the domain/application boundary; infrastructure implements those abstractions. Tests are kept as a separate layer and may exercise each lower layer through its public contract.

The dependency rule is one-way: presentation depends on application and DTO contracts; application depends on domain; infrastructure depends on domain repository interfaces; domain depends on no outer layer. A controller never calls a repository directly.

## 2. Layers

### Presentation / Controller layer (MVC controller)

- Exposes HTTP or equivalent user-facing endpoints for reader, librarian and system-administrator actions.
- Authenticates the request context and performs coarse input shape validation.
- Maps requests to DTOs and delegates one use case to an application service.
- Maps application results and typed errors to response DTOs/status codes.
- Does not calculate fines, decide borrowing eligibility, access persistence, or contain transaction orchestration.

### Application service layer (MVC controller coordination)

- Implements use-case workflows such as register reader, issue card, borrow, return, reserve, search and administration maintenance.
- Loads aggregate roots through repository interfaces, invokes domain behavior/policies, and persists changed aggregates.
- Defines transaction boundaries and publishes domain events after a successful commit where needed.
- Converts between DTOs and domain objects without leaking persistence models to presentation.

### Domain layer (MVC model)

- Contains Reader, StudentReader, TeacherReader, BorrowCard, BookTitle, LibraryItem, Book, Magazine, Thesis, Loan, Reservation and FineRecord.
- Contains BorrowPolicy and FineRule strategy objects plus domain services for operations spanning more than one aggregate.
- Owns invariants: card validity, active-loan limits, overdue blocking, local-collection validation, reservation ordering, item state transitions and fine calculation.
- Defines repository interfaces and domain error types. It has no HTTP, SQL, framework or DTO dependency.

### Infrastructure / Persistence layer

- Implements repository interfaces with the selected database and transaction manager.
- Maps persistence records to domain objects and handles indexes, locking, clock and ID providers.
- Provides adapters for authentication, notification and search where those integrations are introduced.
- Infrastructure classes may depend on domain interfaces; domain code must not depend on infrastructure classes.

### Test layer

- Domain unit tests cover policies, state transitions, fine rules and invariants without a database.
- Application integration tests cover transaction workflows with repository adapters or test doubles.
- Controller/API tests cover DTO validation, authorization mapping and response contracts.
- Acceptance tests trace the core use cases: borrow, return, reserve, card issuance and fine assessment.

## 3. Module boundaries

| Module | Responsibility | Main domain concepts |
| --- | --- | --- |
| `reader` | Register and maintain reader profiles and reader status | `Reader`, `StudentReader`, `TeacherReader` |
| `card` | Issue, validate and deactivate borrowing cards | `BorrowCard` |
| `catalog` | Maintain titles and physical collection copies; search availability | `BookTitle`, `LibraryItem`, `Book`, `Magazine`, `Thesis` |
| `circulation` | Borrow, return and inspect active loans | `Loan`, `BorrowPolicy` |
| `reservation` | Queue, cancel and promote reservations | `Reservation` |
| `fine` | Assess and maintain overdue fines | `FineRule`, `FineRecord` |
| `admin` | Maintain librarians, administrators, catalog data and configurable rules | administration services and policies |

Modules communicate through application services and domain contracts. For example, circulation may read card, reader, catalog and rule repositories through its service boundary; it must not reach into another module's database tables.

## 4. Authorization strategy

The request context carries the authenticated actor and role (`READER`, `LIBRARIAN`, or `SYSTEM_ADMIN`). Authorization is checked at the application-service entry point using a role/policy component, then domain invariants are checked again where the decision affects state. Readers may register, search, reserve and view their own loans. Librarians may perform borrow and return on behalf of a reader and view circulation information. System administrators may issue/deactivate cards, maintain librarians, catalog records and borrowing/fine rules. Object ownership checks prevent a reader from viewing or changing another reader's records. Unauthorized requests fail before a transaction starts and are recorded by the audit adapter.

## 5. Error handling

Domain violations use typed errors such as `InvalidBorrowCard`, `BorrowLimitExceeded`, `OverdueLoanExists`, `ItemNotInCollection`, `ItemUnavailable`, `LoanNotFound` and `UnauthorizedOperation`. The application layer translates them into stable error codes; the presentation layer maps those codes to client responses. Validation errors are returned without persistence. Unexpected infrastructure errors are logged with a correlation ID, rolled back, and exposed as a generic failure response without database details. Error handling must not silently convert a failed borrow or return into a successful state.

## 6. Transaction boundaries and consistency

- `borrowItem`: one transaction validates card/reader/policy/overdue status and local item availability, creates a `Loan`, marks the item on loan, and commits all changes atomically.
- `returnItem`: one transaction locates the active loan, validates the local item, closes the loan, marks the item available, and records any `FineRecord` atomically.
- `reserveItem`: one transaction checks eligibility, assigns the next queue position and persists the reservation.
- Card, catalog and rule maintenance operations each use a transaction for one command and enforce uniqueness/optimistic locking.

Repository writes occur only inside an application-service transaction. Concurrent borrow/return requests lock the relevant item and active-loan/card rows or use optimistic version checks; a conflict is reported for retry. Notifications or search-index updates run after commit and cannot change the committed circulation result.

## 7. Design patterns

- **MVC and Service Layer:** controllers handle protocol concerns; application services coordinate use cases.
- **Strategy:** `BorrowPolicy` varies limits/periods by reader type; `FineRule` varies calculation by item type.
- **Factory:** creates the correct reader or library-item subtype from a validated type.
- **Repository:** domain interfaces isolate aggregate persistence from use cases.
- **DTO / Mapper:** isolates external request/response shapes from domain entities.
- **Unit of Work / transaction manager:** commits all state changes in a use-case boundary.
- **Specification (optional):** composes search and eligibility predicates without moving rules into controllers.

## 8. Architectural constraints

1. Business rules remain in entities, policies, or domain services; controllers contain no business decisions.
2. No presentation class imports a repository implementation or persistence model.
3. Domain class diagrams contain domain concepts only, never Controller, Repository or DTO design classes.
4. Every state-changing core use case has an automated test for its normal path and important rejection paths.
5. Configuration of borrowing/fine rules is versioned and validated before activation.
