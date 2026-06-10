# Replacement test directory

This test directory is intentionally small and aligned to the current operational-metrics-frontend shape:

- `viewmodels/ServiceLeadTimesViewModelSpec.scala` tests the pure table/filtering logic.
- `models/ServiceLeadTimesSpec.scala` tests backend JSON formats.
- `connector/OperationalMetricsConnectorSpec.scala` tests the downstream HTTP path and decoding with WireMock.
- `controllers/IndexControllerSpec.scala` tests the page route using a mocked connector.
- `views/IndexViewSpec.scala` and `views/LayoutSpec.scala` test rendered HTML and Catalogue navbar links.
- auth/signed-out/unauthorised/action specs provide minimal smoke coverage for remaining auth scaffolding.

Replace your current `test/` directory with this one, then run:

```bash
sbt clean compile test
```

The old `it/` directory only tested `SessionRepository`. If the frontend is not keeping generated journey state in Mongo, delete `it/` and remove the `lazy val it = ...` block from `build.sbt`.
