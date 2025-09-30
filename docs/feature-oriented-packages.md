# Feature-oriented package structure proposal

## Current state summary
The current code base follows a horizontal layering pattern that splits the
code under `com.db.assetstore` into `domain`, `infra`, and a thin selection of
shared service packages. While this offers a clear separation between core
logic and infrastructure, it makes it difficult to reason about a single
feature end-to-end. Code for the same capability (e.g. asset linking, search,
or type management) is scattered across multiple top-level packages (`domain`
versus `infra`), which encourages:

- **Artificial boundaries** – developers add new sub-layers to fit the
  structure instead of the problem they are solving.
- **High navigation cost** – understanding a single feature requires jumping
  between distant packages to collect controllers, services, repositories, and
  models.
- **Weaker ownership** – it is unclear who owns a feature when its classes are
  distributed across infrastructure and domain folders.
- **Leaky abstractions** – cross-feature packages (for example
  `domain.service` or `infra.service`) mix unrelated classes and make it easy
  to introduce dependencies that ignore feature boundaries.

## Guiding principles
To emphasise features instead of horizontal layers, reorganise packages with
these principles in mind:

1. **Slice by capability.** Group everything related to a feature under a
   single top-level package (`asset`, `link`, `search`, `type`, `ingestion`,
   etc.). Include API adapters, application services, domain logic, and data
   access layers inside the slice.
2. **Expose shared building blocks explicitly.** Common components such as
   configuration, shared utilities, cross-cutting concerns, and canonical
   models should live in dedicated `common` packages to avoid accidental
   coupling between features.
3. **Keep vertical flow inside a feature.** Within a feature package, mirror
the natural request flow: API → application/service layer → domain model →
repository or integration adapter.
4. **Optimise for autonomy.** Each feature slice should be independently
deployable/testable and have a clear maintainer. Dependencies across features
should go through explicit interfaces defined in the `common` area.

## Proposed package map
The table below outlines how the existing functionality can be regrouped.
It focuses on the major features already present in the code base.

| Feature slice | Current packages | Proposed package | Notes |
| ------------- | ---------------- | ---------------- | ----- |
| Asset lifecycle | `domain.model`, `domain.service.asset`, `infra.service`, `infra.repository`, `infra.jpa` | `com.db.assetstore.asset` with subpackages `api`, `application`, `domain`, `persistence` | Gather all asset CRUD logic, serializers, persistence adapters, and canonical models into a single slice. |
| Asset types | `domain.service.type`, `domain.model.type`, `infra.service.type`, `infra.api.dto`, `infra.jpa` | `com.db.assetstore.assettype` (or `type`) with `api`, `application`, `domain`, `persistence` | Keep DTOs alongside the REST/controller layer within the feature. |
| Linking | `domain.model.link`, `domain.service.link`, `infra.service.link`, `infra.repository` | `com.db.assetstore.link` with `api`, `application`, `domain`, `persistence` | Collect link definition/model handling and orchestration for link management. |
| Search | `domain.search`, `infra.service.search`, `infra.jpa.search` | `com.db.assetstore.search` with `api`, `application`, `domain`, `persistence` | Group search indexing, query services, and adapters. |
| Command ingestion | `domain.service.cmd`, `infra.service.cmd` | `com.db.assetstore.ingestion` with `api` (message listeners), `application` (command handlers), `domain` (command model) | Supports asynchronous command/event flows. |
| Transformation & events | `domain.service.transform`, `domain.service.EventService`, `infra.json` | `com.db.assetstore.events` with `application` (event orchestration), `domain` (event definitions), `integration` (transformers/serialisers) | Keeps event emission logic cohesive. |
| Validation | `domain.service.validation` | `com.db.assetstore.common.validation` | Provide reusable validators shared across features while keeping the intent explicit. |
| Startup/bootstrap | `domain.service.startup` | `com.db.assetstore.common.startup` | Lifecycle hooks and bootstrap logic that are not feature-specific. |
| Shared infrastructure | `infra.config`, shared mappers/utilities | `com.db.assetstore.common.config` / `common.mapping` / `common.support` | Offer clearly named homes for Spring configuration, mappers, and helpers. |

## Folder layout example
Under `src/main/java/com/db/assetstore`, each feature folder would follow a
consistent internal structure. For example:

```
com/db/assetstore/
  asset/
    api/
      AssetController.java
      AssetDto.java
    application/
      AssetService.java
    domain/
      Asset.java
      AssetFactory.java
    persistence/
      AssetRepository.java
      AssetJpaEntity.java
  link/
    ...
  search/
    ...
  common/
    config/
    validation/
    support/
```

This layout keeps feature-specific DTOs, controllers, domain objects, and
repositories close together while maintaining a small, well-defined `common`
area for shared concerns.

## Migration approach
1. **Define feature boundaries.** Document the responsibilities and owning
   team for each slice to avoid ambiguity during refactors.
2. **Move tests alongside features.** Mirror the production package
   restructuring within `src/test/java/com/db/assetstore/...` so that each
   feature's tests live with the code they cover.
3. **Refactor iteratively.** Move one feature at a time, updating package
   declarations and imports. Use IDE support or automated refactors to reduce
   risk.
4. **Enforce the structure.** Introduce architectural rules (ArchUnit, Error
   Prone, etc.) to prevent cross-feature dependencies that bypass the slice's
   public API.
5. **Communicate conventions.** Update contribution guidelines to explain the
   new package strategy, naming conventions, and expectations around feature
   ownership.

Adopting a feature-oriented package structure will make the code base more
navigable, give teams clear ownership, and reduce the accidental complexity
introduced by artificial layering.
