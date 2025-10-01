Asset Store PoC
================

## Overview

This proof of concept demonstrates how to store, validate, and publish assets such as CRE or SHIP deals. It is a Spring Boot service that relies on JSON Schemas and JSLT templates, so new asset types and event payloads can be introduced by adding files instead of Java code. Attribute definitions can be bootstrapped from JSON schemas or loaded directly from the database, allowing teams to evolve the model without redeploying the service.

At a high level, the application consists of a REST API backed by domain services that coordinate persistence, validation, and event generation. Assets are persisted in a relational database through JPA, schemas and templates are loaded from the classpath to guarantee deterministic bootstrapping, and each adapter is isolated so the domain can be exercised consistently in tests and production.

## Architecture

The application follows a hexagonal architecture in which the domain layer owns the core asset lifecycle logic and infrastructure concerns are pushed to adapters. Controllers submit commands into the orchestration layer, where transactions, validation, and audit logging are coordinated before touching persistence. Outbound adapters supply the domain with repositories, schema registries, and transformation utilities, each isolated behind interfaces so they can be swapped or tested independently.

At runtime a typical request flows through the REST controller into the command service, which resolves validators, invokes the appropriate asset or link service, persists state through JPA repositories, and records history snapshots. Event generation reuses the same domain model by reading the persisted asset, applying a JSLT template, and validating the output with the JSON Schema validator before returning it to the caller. Bootstrapping ties these paths together by loading schema resources and attribute definitions into in-memory registries when the application starts, allowing subsequent commands and queries to rely on consistent metadata.

## Key components

### API layer

* **AssetController** – Hosts the `/assets` endpoints that front every lifecycle operation. Besides basic CRUD, it dispatches bulk create and bulk patch requests to the command pipeline and maps validation issues into HTTP responses that clients can understand.
* **EventController** – Delegates event generation to the domain layer. It resolves an asset, identifies the correct template based on `{eventName}`, and streams back the transformed payload so downstream systems can publish it.

### Command and orchestration services

* **CommandServiceImpl** – Implements both `AssetCommandService` and the visitor that handles individual commands. It determines which specialised service should execute each command, wraps the call in a transaction, and persists an audit trail through **CommandLogService** when the command succeeds.
* **AssetService** – Persists assets and their dynamic attributes. It resolves identifiers, maps incoming commands into domain models, records historical snapshots after every mutation, and delegates JSON attribute conversions to **AttributeMapper**. Soft deletes are handled by toggling the `deleted` flag while keeping history entries in sync.
* **AssetLinkService** – Manages creation and deletion of relationships between assets. It enforces link definitions through `AssetLinkCommandValidator`, prevents duplicates, honours cardinality rules, and can reactivate previously deactivated links instead of creating duplicates.
* **AssetHistoryServiceImpl** – Provides read access to the change log accumulated by **AssetService**, giving consumers a chronological trail of state transitions for auditing.

### Query services

* **AssetQueryServiceImpl** – Serves read scenarios by translating `SearchCriteria` objects into JPA specifications. It supports direct lookups by identifier as well as filtered searches, returning domain objects that controllers can serialise.
* **AssetLinkQueryServiceImpl** – Resolves inbound and outbound links for a given asset id, optionally including inactive links, so the controller layer does not need to interact with repositories directly.

### Bootstrap and registry services

* **BootstrapService** – Listens for `ApplicationReadyEvent`, triggers the registry rebuild via **AttributeBootstrapService**, and logs the asset types that were discovered so operators can verify configuration at startup.
* **AttributeBootstrapService** – Rebuilds type schemas and attribute definitions by invoking **TypeSchemaRegistry** and **AttributeDefinitionRegistry**, ensuring the in-memory caches reflect the latest resources bundled with the application.
* **TypeSchemaRegistry** – Scans the classpath for JSON Schema files, compiles them up front, and tracks which asset types expose which schemas to prevent runtime lookups from failing.
* **AttributeDefinitionRegistry** – Parses the compiled schemas (through its loaders) to build an in-memory catalogue of attribute definitions and constraints. The registry is reused during validation and when materialising new attributes from payloads.

### Transformation and validation services

* **JsonTransformer** – Loads and caches JSLT templates, applies them to the canonical JSON produced for each asset, and optionally validates the result against the schema advertised for the event.
* **EventService** – Orchestrates event generation by wrapping the canonical asset payload with metadata (event name and timestamp) before invoking **JsonTransformer**. It centralises error handling so template or schema issues surface as meaningful exceptions.
* **JsonSchemaValidator** – Wraps the underlying JSON Schema library and converts its diagnostics into human friendly error messages, ensuring both API responses and command logs contain actionable feedback.

### Mapping layer

* **Asset domain model** – Represents an asset with core fields and a collection of typed attributes, exposing helper methods for safe updates.
* **AssetMapper**, **AssetCommandMapper**, and **AttributeMapper** – Convert between domain objects, command DTOs, and JPA entities while handling soft delete flags, attribute typing, and history snapshots so persistence concerns stay out of the domain services.


## Functional capabilities

The system currently supports:

* Creating, updating, patching, and deleting assets through the REST API, including bulk operations.
* Searching and reading assets by translating criteria objects into JPA specifications.
* Managing dynamic attributes by loading definitions from JSON schemas or the database.
* Maintaining links between assets so relationships can be stored with metadata and revisited later.
* Generating events via `/events/{assetId}/{eventName}`, which applies the configured JSLT template and optional schema validation.
* Validating asset payloads with schemas that are discovered and persisted at startup.
* Handling typed attributes inside the domain model while the mapper layer translates values to and from the database.

## Roadmap

Outstanding work includes:

* Adding pagination and richer filters for `GET /assets` because the current implementation returns the full list when no query parameters are provided.
* Exposing HTTP endpoints for the link management that is already supported by the command service.
* Improving event generation error handling so clients receive structured responses instead of generic runtime exceptions.
