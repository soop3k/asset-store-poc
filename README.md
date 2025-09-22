# Asset Store PoC

This project now uses JSLT (Schibsted) exclusively for JSON->JSON transforms. JSONata templates and any custom transformer code have been removed. At runtime the app logs supported asset types (based on present JSON Schemas) and all discovered JSLT templates, so you can immediately see what is active.

What changed visibly:
- Only `.jslt` templates are used under `src/main/resources/transforms/**`. JSONata files were removed (emptied) to avoid confusion.
- On startup we log:
  - Supported asset types discovered from `src/main/resources/schemas/*.schema.json`
  - Discovered JSLT templates from `src/main/resources/transforms/**/*.jslt`

How to run
- Windows: `mvnw.cmd clean test` (or `mvnw.cmd spring-boot:run` to start the app)
- Unix: `./mvnw clean test` (or `./mvnw spring-boot:run`)

Where to look for the changes
- Application logs at startup will include lines like:
  - `Supported asset types (schemas found): [CRE, SHIP]`
  - `Discovered JSLT transforms (2): [transforms/asset-to-external.jslt, transforms/events/AssetUpserted.jslt]`
- Resource layout:
  - Schemas: `src/main/resources/schemas/{TYPE}.schema.json`
  - Transforms: `src/main/resources/transforms/*.jslt` and `src/main/resources/transforms/events/*.jslt`

Testing
- Unit tests cover:
  - Transformations via JSLT
  - Event generation via JSLT templates
  - Schema-driven asset type validation and discovery

Notes
- Unknown (additional) properties are ignored during schema validation; other errors are reported clearly.
- Adding a new asset type is as simple as adding `schemas/{TYPE}.schema.json`.
- Adding a new event is as simple as adding a new `.jslt` template under `transforms/events/`.


Architektura i lista klas/serwisów (PL)
- Cel: poniżej znajduje się lista wszystkich kluczowych klas w projekcie wraz z opisem działania i powiązań z innymi komponentami. Ma to ułatwić szybkie zrozumienie przepływów oraz miejsc, w których wprowadzamy zmiany.

1) Warstwa uruchomieniowa i typy bazowe
- com.db.assetstore.AssetStorePocApplication
  - Główna klasa startowa Spring Boot (main). Uruchamia aplikację i skan komponentów.
- com.db.assetstore.AssetType
  - Enum typów aktywów (np. CRE, SHIP, AV, SPV). Używany przez model, repozytorium, walidację i transformacje.

2) Konfiguracja
- com.db.assetstore.config.AssetConfig
  - Rejestruje beany: AssetRepository (JpaAssetRepository) oraz AssetService (DefaultAssetService).
- com.db.assetstore.service.StartupInfoService
  - Loguje dostępne typy (z wykrytych schematów) i szablony JSLT przy starcie (ApplicationReadyEvent).
- com.db.assetstore.service.AttributeDefinitionsBootstrapService
  - Na starcie wypełnia tabelę definicji atrybutów na podstawie schematów JSON (AttributeDefinitionRegistry -> AttributeDefEntity w DB). Chroni integralność danych na poziomie repozytorium.

3) Warstwa web (HTTP)
- com.db.assetstore.infra.api.AssetController
  - Endpointy REST:
    - POST /assets – tworzenie aktywa w formacie koperty {type, id?, attributes{...}}.
    - POST /assets/cre – przykład endpointu typu-specyficznego, gdzie JSON zawiera atrybuty bez koperty.
    - GET /assets – lista aktywów (proste wykorzystanie SearchCriteria).
  - Deleguje do AssetService, nie zawiera logiki biznesowej.
- com.db.assetstore.infra.api.GlobalExceptionHandler
  - Globalny handler wyjątków (400 dla IllegalArgumentException, 500 dla innych błędów). Zwraca zunifikowany ErrorResponse.
- com.db.assetstore.infra.api.ErrorResponse
  - Model odpowiedzi błędu: timestamp, status, error, message, path.

4) Warstwa serwisowa (logika domenowa i orkiestracja)
- com.db.assetstore.service.AssetService (interfejs)
  - Operacje: addAsset(Asset), addAssetFromJson(json), addAssetFromJson(type,json), removeAsset, getAsset, search, history.
- com.db.assetstore.service.DefaultAssetService
  - Implementacja AssetService. Orkiestruje:
    - Walidację wejścia (AssetAttributeValidationService),
    - Parsowanie JSON -> Asset (AssetJsonFactory),
    - Zapis przez repozytorium (AssetRepository).
- com.db.assetstore.service.AssetAttributeValidationService
  - Logika walidacji atrybutów:
    - validateEnvelope(json) – odczyt typu z koperty, walidacja atrybutów względem schematu typu (jeśli schemat istnieje).
    - validateForType(type,json) – dla JSON bez koperty: wyciąga atrybuty i waliduje opcjonalnie względem schematu.
  - Używa AssetJsonFactory (wydobycie typu/atrybutów) oraz AssetTypeValidator.
- com.db.assetstore.service.AssetTypeValidator
  - Nie odrzuca typów tylko dlatego, że brak pliku schematu (enum wyznacza dopuszczalne typy).
  - validateAttributes: jeśli dla typu istnieje schemat na classpath, to waliduje (JsonSchemaValidator). W przeciwnym razie pomija walidację.
- com.db.assetstore.service.TypeSchemaRegistry
  - Odkrywa, które typy mają schematy JSON (schemas/{TYPE}.schema.json). Pozwala sprawdzić ścieżkę do schematu i zbiór „obsługiwanych” typów (z perspektywy schematów).
- com.db.assetstore.service.JsonSchemaValidator
  - Walidacja JSON wg JSON Schema (networknt). Ignoruje naruszenia additionalProperties (nieznane pola). Inne błędy rzucają IllegalArgumentException.
- com.db.assetstore.service.AssetJsonFactory
  - Tworzy model domenowy Asset z JSON:
    - fromJson(envelope) – we/wy w kopercie {type, id?, attributes{...}}.
    - fromJsonForType(type, json) – dla JSON bez koperty; wszystkie pola poza polami modelu Asset traktowane jako atrybuty.
  - Automatycznie rozpoznaje pola modelowe Asset (odbijanie/reflection) i ustawia je, a pozostałe trafiają do mapy atrybutów (AttributeValue).
- com.db.assetstore.service.AssetAttributeConverter
  - Konwersja obiektów JSON atrybutów do listy AttributeValue<?> (String/Long/Double/Boolean; inne typy serializowane jako String).
- com.db.assetstore.json.AssetCanonicalizer
  - Buduje kanoniczne JSON aktywa (płaski model + attributes{}) pod transformacje / eventy.
- com.db.assetstore.service.JsonTransformer
  - Silnik transformacji JSON->JSON oparty o JSLT (Schibsted). Szablony pod transforms/{nazwa}.jslt.
  - Opcjonalnie waliduje wynik transformacji względem schematu:
    - schemas/transforms/{nazwa}.schema.json lub
    - dla nazw przestrzennych (np. events/AssetUpserted): schemas/events/{basename}.schema.json.
- com.db.assetstore.service.EventService
  - Generuje JSON zdarzenia na podstawie Asset i szablonu JSLT (transforms/events/{eventName}.jslt). Używa AssetCanonicalizer i JsonTransformer.
- com.db.assetstore.service.type.AttributeDefinitionRegistry
  - Buduje katalog definicji atrybutów per typ na podstawie schematów JSON (properties/required). Przechowuje ValueType (STRING/LONG/DOUBLE/BOOLEAN) i flagę required. Wykorzystywany przez bootstrap do zapisania definicji w DB.

5) Model domenowy
- com.db.assetstore.model.Asset
  - Reprezentacja aktywa: metadane (id, type, createdAt, itp.) + mapa atrybutów (Map<String, AttributeValue<?>>). Metody ułatwiające odczyt/ustawianie pojedynczych i wielu atrybutów.
- com.db.assetstore.model.AttributeValue<T>
  - Pojedynczy atrybut (nazwa, wartość, typ Javy). Używany przez repo i serwisy.
- com.db.assetstore.model.AttributeHistory
  - POJO reprezentujący wpis historii zmian atrybutów (assetId, name, value, valueType, changedAt). Zasilany z encji historii.

6) Warstwa repozytorium (dostęp do danych)
- com.db.assetstore.repository.AssetRepository (interfejs)
  - saveAsset, softDelete, findById, setAttributes (placeholder), search, history.
- com.db.assetstore.repository.JpaAssetRepository
  - Implementacja repozytorium oparta o JPA/EntityManager.
  - Podczas zapisu wymusza spójność względem definicji atrybutów z DB (AttributeDefEntity):
    - każdy atrybut musi być zdefiniowany,
    - wszystkie „required” muszą być dostarczone,
    - zapisuje bieżące wartości atrybutów i historię zmian.
  - Wyszukiwanie: buduje predykaty Criteria API w oparciu o SearchCriteria i dołączenie do tabeli atrybutów.

7) Warstwa JPA (encje bazodanowe)
- com.db.assetstore.jpa.AssetEntity
  - Encja „assets” z polami modelu (typ, status, itp.), flagą deleted (soft delete) oraz relacją OneToMany do AttributeEntity.
- com.db.assetstore.jpa.AttributeEntity
  - Encja „asset_attribute” (nazwa, value jako String, valueType jako nazwa typu, updatedAt). Utrzymuje relację do historii zmian.
- com.db.assetstore.jpa.AttributeHistoryEntity
  - Encja „asset_attribute_history” – utrwala zmiany wartości atrybutu w czasie (redundantne kolumny dla prostszego zapytań).
- com.db.assetstore.jpa.AttributeDefEntity
  - Encja „asset_attribute_def” – definicje dozwolonych atrybutów per typ (name, valueType, required). Zasilana na starcie z JSON Schema.

8) Mapowania (mappery)
- com.db.assetstore.mapper.AssetMapper (MapStruct)
  - Mapuje Asset <-> AssetEntity (z wyjątkiem attributes, które mapujemy ręcznie). Obsługuje konwersję softDelete <-> deleted (0/1).
- com.db.assetstore.mapper.AttributeMapper
  - Mapowanie pojedynczych atrybutów: serializacja wartości do String + nazwa typu (String/Long/Double/Boolean/Instant). Odpowiada też za deserializację z wierszy DB.

9) Wyszukiwanie (model zapytań)
- com.db.assetstore.search.SearchCriteria
  - Budowniczy kryteriów (opcjonalny type, lista Condition). Używany przez repozytorium.
- com.db.assetstore.search.Condition
  - Pojedynczy warunek: attribute, operator, value.
- com.db.assetstore.search.Operator
  - Dostępne operatory: EQ, GT, LT, LIKE.

10) Zasoby (resources)
- Schematy typów aktywów: src/main/resources/schemas/{TYPE}.schema.json (np. CRE.schema.json, SHIP.schema.json).
- Schematy wyników transformacji: src/main/resources/schemas/transforms/*.schema.json i/lub schemas/events/*.schema.json.
- Szablony JSLT: src/main/resources/transforms/*.jslt oraz transforms/events/*.jslt.

Relacje i przepływy
- Tworzenie aktywa (koperta):
  1. HTTP POST /assets (AssetController)
  2. DefaultAssetService.validateEnvelope -> AssetTypeValidator (opcjonalna walidacja schematu przez JsonSchemaValidator) + AssetJsonFactory.attributesFromEnvelopeJson
  3. DefaultAssetService.fromJson -> AssetJsonFactory.fromJson (Asset + lista AttributeValue)
  4. Zapis: AssetRepository.saveAsset -> JpaAssetRepository (sprawdza definicje atrybutów w DB, utrwala AttributeEntity + historię)
- Tworzenie aktywa (JSON typu):
  1. HTTP POST /assets/cre
  2. DefaultAssetService.validateForType -> AssetJsonFactory.attributesFromTypeJson (odfiltrowuje pola modelowe), walidacja opcjonalna względem schematu CRE
  3. DefaultAssetService.fromJsonForType -> AssetJsonFactory.fromJsonForType
  4. Zapis jak wyżej w repozytorium
- Generowanie zdarzeń:
  1. EventService.generate(eventName, asset) -> AssetCanonicalizer.toCanonicalJson
  2. JsonTransformer.transform("events/"+eventName, json) -> JSLT -> wynikowy JSON
  3. Opcjonalna walidacja wyniku względem schemas/transforms/... i/lub schemas/events/...
- Inicjalizacja na starcie:
  - TypeSchemaRegistry wykrywa schematy typów.
  - AttributeDefinitionRegistry buduje definicje atrybutów per typ na podstawie JSON Schema.
  - AttributeDefinitionsBootstrapService zapisuje brakujące definicje do DB.

Uwagi implementacyjne
- Walidacja schematów jest „best-effort”: brak schematu nie blokuje działania (typy z enum są akceptowane), a nieznane pola są ignorowane przez walidator.
- AssetJsonFactory dynamicznie wykrywa pola modelowe Asset – nie trzeba uaktualniać listy ręcznie przy zmianach modelu.
- Repozytorium stanowi ostatnią linię obrony spójności danych, nawet gdy walidacja wyżej została pominięta.



---
Chore (2025-09-21): housekeeping commit to confirm VCS/pipeline. No functional changes; documentation note added.


## Diagramy przepływów (flows)
Dla wizualnego przedstawienia etapów działania oraz użytych komponentów przygotowaliśmy zestaw diagramów (Mermaid):
- Zobacz: docs/flows.md

