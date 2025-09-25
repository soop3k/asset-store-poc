Asset Store PoC
================

## Overview

This proof of concept demonstrates how to store, validate, and publish assets such as CRE or SHIP deals. It is a Spring Boot service that relies on JSON Schemas and JSLT templates, so new asset types and event payloads can be introduced by adding files instead of Java code. Attribute definitions can be bootstrapped from JSON schemas or loaded directly from the database, allowing teams to evolve the model without redeploying the service.

## Architecture

The application follows a hexagonal architecture:

* **Domain services** live at the centre and encapsulate the business logic.
* **Inbound adapters** consist of REST controllers and scheduled jobs.
* **Outbound adapters** include database repositories, schema registries, and message builders.

This separation keeps the domain independent from framework concerns and simplifies adding new adapters.

## Key components

* **AssetController** – Exposes the `/assets` endpoints for create, bulk create, search, read, update, patch, bulk patch, and delete requests.
* **EventController** – Loads an asset, selects the right JSLT template, and returns the generated event payload.
* **CommandServiceImpl** – Routes asset commands to **AssetService** and link commands to **AssetLinkService** while recording the audit log of executed commands.
* **AttributeDefinitionsBootstrapService** – Stores missing attribute definitions at startup so attributes can be introduced through schema files or database seeding.
* **AssetQueryServiceImpl** – Reads assets by id or by search criteria that translate into JPA specifications.
* **TypeSchemaRegistry** – Scans classpath schemas, compiles them, and records which asset types provide schema files.
* **AttributeDefinitionRegistry** – Parses schemas and builds the catalogue of attribute definitions for each asset type.
* **StartupInfoService** – Triggers the bootstrap step after startup and reports available asset types together with the discovered JSLT templates.
* **JsonTransformer** – Loads and caches event templates, applies them to payloads, and validates the output when a schema is present.
* **JsonSchemaValidator** – Wraps the JSON Schema validator and turns failures into readable error messages.
* **Asset domain model** – Represents an asset with core fields and a collection of typed attributes, exposing helper methods for safe updates.
* **AssetMapper** and **AttributeMapper** – Convert between domain objects and JPA entities while handling the soft delete flag and attribute typing.

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
